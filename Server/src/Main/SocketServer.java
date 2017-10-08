package Main;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// tiến trình riêng để làm việc với từng Client
class ServerThread extends Thread {

    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1;
    public String username = "";
    public ObjectInputStream streamIn = null;
    public ObjectOutputStream streamOut = null;
    public ServerForm ui;

    // khởi tạo tiến trình riêng khi có Client kết nối đến
    public ServerThread(SocketServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
        ui = _server.ui;
    }

    // send thông điệp từ tiến trình con lên tiến trình chính của server
    public void send(Message msg) {
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }

    public int getID() {
        return ID;
    }

    // tiến trình con làm việc
    @Override
    public void run() {
        ui.jTextArea1.append("\nServer Thread " + ID + " running.");
        while (true) {
            try {
                Message msg = (Message) streamIn.readObject();
                server.handle(ID, msg);
            } catch (IOException | ClassNotFoundException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            } catch (SQLException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void open() throws IOException {
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (streamIn != null) {
            streamIn.close();
        }
        if (streamOut != null) {
            streamOut.close();
        }
    }
}

// tiến trình server lắng nghe kết nối và làm việc với tất 
// cả các Client (tiến trình chính của server)
public class SocketServer implements Runnable {

    ServerThread clients[];
    public ServerSocket server = null;
    public Thread thread = null;
    public int clientCount = 0, port = 10000;
    public ServerForm ui;
    public DatabaseUser dbUser;

    // khởi tạo server và lắng nghe kết nối
    public SocketServer(ServerForm frame) {

        clients = new ServerThread[50];
        ui = frame;
        dbUser = new DatabaseUser();

        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            ui.jTextArea1.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
        } catch (IOException ioe) {
            ui.jTextArea1.append("Can not bind to port : " + port + "\nRetrying");
            ui.RetryStart(0);
        }
    }

    public SocketServer(ServerForm frame, int Port) {

        clients = new ServerThread[50];
        ui = frame;
        port = Port;
        dbUser = new DatabaseUser();

        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            ui.jTextArea1.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
        } catch (IOException ioe) {
            ui.jTextArea1.append("\nCan not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    // chấp nhận kết nối và đẩy Client ra luồng riêng(luồng con)
    @Override
    public void run() {
        while (thread != null) {
            try {
                ui.jTextArea1.append("\nWaiting for a client ...");
                addThread(server.accept());
            } catch (Exception ioe) {
                ui.jTextArea1.append("\nServer accept error: \n");
                ui.RetryStart(0);
            }
        }
    }

    // hàm bắt đầu khởi động luồng
    public final void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    // hàm dừng luồng
    public void stop() {
        if (thread != null) {
            thread = null;
        }
    }

    // hàm tìm số thứ tự của luồng con
    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    // hàm nhận ,xử lý và gửi thông điệp giữa các Client( giữa các luồng con)
    public synchronized void handle(int ID, Message msg) throws SQLException {
        //Xử lý video
        if (msg.type.equals("videocall")) {
            if (msg.recipient.equals("All")) {
                clients[findClient(ID)].send(new Message("message", "SERVER", "Video calling to 'All' forbidden", msg.sender));
            } else {
                // lấy địa chỉ người nhận video gửi cho người gọi
                String IP_client = findUserThread(msg.recipient).socket.getInetAddress().getHostAddress();
                findUserThread(msg.sender).send(new Message("clientAddress", IP_client, "đã lấy được địa chỉ của người nhận video call", msg.sender));
                // lấy địa chỉ người gọi video gửi cho người nhận
                String IP_server = findUserThread(msg.sender).socket.getInetAddress().getHostAddress();
                findUserThread(msg.recipient).send(new Message(msg.type, IP_server, msg.content, msg.recipient));
            }
        }
        // xử lý chat Voice
//        if (msg.type.equals("call")) {
//            if (msg.recipient.equals("All")) {
//                clients[findClient(ID)].send(new Message("message", "SERVER", "Voice calling to 'All' forbidden", msg.sender));
//            } else {
//                String IP = findUserThread(msg.sender).socket.getInetAddress().getHostAddress();
//                findUserThread(msg.recipient).send(new Message(msg.type, IP, msg.content, msg.recipient));
//            }
//        }
        // server phân tích message.type để có hành động thích hợp gửi cho Client
        if (msg.content.equals(".bye")) {
            Announce("signout", "SERVER", msg.sender);
            remove(ID);
        } else if (msg.type.equals("login")) {
            if (findUserThread(msg.sender) == null) {
                if (dbUser.check(msg.sender, msg.content) == 1) {
                    clients[findClient(ID)].username = msg.sender;
                    clients[findClient(ID)].send(new Message("login", "SERVER", "TRUE", msg.sender));
                    Announce("newuser", "SERVER", msg.sender);
                    SendUserList(msg.sender);
                } else {
                    clients[findClient(ID)].send(new Message("login", "SERVER", "FALSE", msg.sender));
                }
            } else {
                clients[findClient(ID)].send(new Message("login", "SERVER", "FALSE", msg.sender));
            }
        } else if (msg.type.equals("message")) {
            if (msg.recipient.equals("All")) {
                Announce("message", msg.sender, msg.content);
            } else {
                findUserThread(msg.recipient).send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
                clients[findClient(ID)].send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
            }
        } else if (msg.type.equals("test")) {
            clients[findClient(ID)].send(new Message("test", "SERVER", "OK", msg.sender));
        } else if (msg.type.equals("upload_req")) {
            if (msg.recipient.equals("All")) {
                clients[findClient(ID)].send(new Message("message", "SERVER", "Uploading to 'All' forbidden", msg.sender));
            } else {
                findUserThread(msg.recipient).send(new Message("upload_req", msg.sender, msg.content, msg.recipient));
            }
        } else if (msg.type.equals("upload_res")) {
            if (!msg.content.equals("NO")) {
                String IP = findUserThread(msg.sender).socket.getInetAddress().getHostAddress();
                findUserThread(msg.recipient).send(new Message("upload_res", IP, msg.content, msg.recipient));
            } else {
                findUserThread(msg.recipient).send(new Message("upload_res", msg.sender, msg.content, msg.recipient));
            }
        }
    }

    // hàm gửi thông điệp tới tất cả các luồng con(Client)
    public void Announce(String type, String sender, String content) {
        Message msg = new Message(type, sender, content, "All");
        for (int i = 0; i < clientCount; i++) {
            clients[i].send(msg);
        }
    }

    // gửi danh sách thành viên cho tất cả các luồng con(Client)
    public void SendUserList(String toWhom) {
        for (int i = 0; i < clientCount; i++) {
            findUserThread(toWhom).send(new Message("newuser", "SERVER", clients[i].username, toWhom));
        }
    }

    // hàm tìm luồng con
    private ServerThread findUserThread(String usr) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].username.equals(usr)) {
                return clients[i];
            }
        }
        return null;
    }

    // hàm xóa luồng con
    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ServerThread toTerminate = clients[pos];
            ui.jTextArea1.append("\nRemoving client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                ui.jTextArea1.append("\nError closing thread: " + ioe);
            }
            toTerminate.stop();
        }
    }

    // hàm đẩy kết nối mới ra luồng con
    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            ui.jTextArea1.append("\nClient accepted: " + socket);
            clients[clientCount] = new ServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException ioe) {
                ui.jTextArea1.append("\nError opening thread: " + ioe);
            }
        } else {
            ui.jTextArea1.append("\nClient refused: maximum " + clients.length + " reached.");
        }
    }
}

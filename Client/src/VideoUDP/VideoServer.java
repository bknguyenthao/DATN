package VideoUDP;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class VideoServer extends javax.swing.JFrame {

    DatagramSocket serverSocket;
    DatagramPacket receiverPacket, sendPacket;
    Webcam webcam;
    VoiceServer voiceServer;

    public VideoServer() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Stop");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public Dimension[] nonStandardResolutions = new Dimension[]{
        WebcamResolution.PAL.getSize(),
        WebcamResolution.HD720.getSize(),
        new Dimension(2000, 1000),
        new Dimension(1000, 500),};

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            // khởi tạo server gọi video
            serverSocket = new DatagramSocket(1999);
            // khởi tạo server gọi âm thanh
            voiceServer = new VoiceServer(2000);
        } catch (SocketException ex) {
            Logger.getLogger(VideoServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(VideoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //server nhận video
        Timer timer2 = new Timer();
        timer2.scheduleAtFixedRate(new ServerDown(), 1, 1);
        //server gửi video
        webcam = Webcam.getDefault();
        webcam.setCustomViewSizes(nonStandardResolutions);
        webcam.setViewSize(WebcamResolution.PAL.getSize());
        webcam.open();
        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new ServerUp(), 1, 1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        serverSocket.close();
        webcam.close();
        voiceServer.targetDataLine.close();
        voiceServer.sourceDataLine.close();
        this.dispose();

    }//GEN-LAST:event_jButton2ActionPerformed

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        }
        java.awt.EventQueue.invokeLater(() -> {
            new VideoServer().setVisible(true);
        });
    }

    public class ServerUp extends TimerTask {

        @Override
        public void run() {

            try {
                byte[] data = getVideo();
                sendPacket = new DatagramPacket(data, data.length, InetAddress.getByName(Statics.CLIENT_IP), 1999);
                serverSocket.send(sendPacket);
            } catch (Exception ex) {
                Logger.getLogger(VideoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public byte[] getVideo() throws Exception {
            BufferedImage image = webcam.getImage();
            byte[] imageByte = Statics.getBytesFromBufferedImage(image);
            return imageByte;
        }
    }

    public class ServerDown extends TimerTask {

        @Override
        public void run() {
            try {
                byte[] data = new byte[40000];
                receiverPacket = new DatagramPacket(data, 40000);
                serverSocket.receive(receiverPacket);
                ShowVideo(receiverPacket.getData());
            } catch (IOException ex) {
                Logger.getLogger(VideoServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(VideoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void ShowVideo(byte[] data) throws Exception {
            BufferedImage bImage;
            bImage = Statics.getBufferedImageFromByte(data);
            Graphics g = jPanel1.getGraphics();
            g.drawImage(bImage, 0, 0, 640, 480, null);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

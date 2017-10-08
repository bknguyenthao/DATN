package Main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUser {

    public Connection connection = null;

    public DatabaseUser() {
        try {
            String userName = "sa";
            String password = "20112213";
            String url = "jdbc:sqlserver://localhost:1433;databaseName=DB;";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = java.sql.DriverManager.getConnection(url, userName, password);
        } catch (ClassNotFoundException | SQLException e) {
        }
    }

    // hàm kiểm tra đăng nhập, kết quả trả về là 1 nếu đăng nhập đúng, 0 nếu sai
    public int check(String _user, String _pass) throws SQLException {
        String sql = "Select * from tbUser";
        // Tạo đối tượng Statement.
        Statement statement = connection.createStatement();
        Statement statement1 = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
        ResultSet rs1 = statement1.executeQuery(sql);

        // Duyệt trên kết quả trả về.
        int dem = 0;
        int length = 0;
        int kq = 0;
        //vòng lặp này để đếm số hàng trong 1 bảng
        while (rs1.next()) {
            length++;
        }
        // Di chuyển con trỏ xuống bản ghi kế tiếp.
        while (rs.next()) {
            String user = rs.getString(1);
            String pass = rs.getString(2);
            if (user.equals(_user) && pass.equals(_pass)) {
                kq = 1;
                break;
            } else {
                dem++;
            }
        }
        if (dem == length) {
            kq = 0;
        }
        return kq;
    }
}

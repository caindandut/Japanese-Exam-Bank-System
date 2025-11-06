package com.exammanager.nganhangdethi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // --- CẤU HÌNH THÔNG SỐ KẾT NỐI MYSQL CỦA BẠN TẠI ĐÂY ---
    // Host của MySQL Server (thường là "localhost" hoặc "127.0.0.1" nếu chạy trên cùng máy)
    private static final String MYSQL_SERVER_HOST = "localhost";

    // Cổng mặc định của MySQL là 3306
    private static final String MYSQL_PORT = "3306";

    // Tên cơ sở dữ liệu bạn đã tạo trong MySQL
    private static final String DATABASE_NAME = "nganhangdethi";

    // Tên người dùng để kết nối MySQL (thường là "root" cho môi trường phát triển)
    private static final String MYSQL_USERNAME = "root";

    // Mật khẩu của người dùng MySQL (mật khẩu bạn đã đặt cho 'root' khi cài đặt MySQL)
    private static final String MYSQL_PASSWORD = "YOUR_MYSQL_ROOT_PASSWORD"; // <<<--- THAY THẾ BẰNG MẬT KHẨU CỦA BẠN
    private static final String CONNECTION_PARAMETERS = "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useUnicode=true";
    // --- KẾT THÚC PHẦN CẤU HÌNH ---


    // Chuỗi kết nối JDBC cho MySQL được xây dựng tự động
    private static final String JDBC_URL = "jdbc:mysql://" + MYSQL_SERVER_HOST + ":" + MYSQL_PORT + "/" + DATABASE_NAME + CONNECTION_PARAMETERS;

    static {
        
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, MYSQL_USERNAME, MYSQL_PASSWORD);
    }

   
    public static void main(String[] args) {
        Connection connection = null;
        try {
            System.out.println("Đang kiểm tra kết nối tới MySQL Server...");
            System.out.println("Host: " + MYSQL_SERVER_HOST);
            System.out.println("Port: " + MYSQL_PORT);
            System.out.println("Database: " + DATABASE_NAME);
            System.out.println("Username: " + MYSQL_USERNAME);
            if ("YOUR_MYSQL_ROOT_PASSWORD".equals(MYSQL_PASSWORD) || MYSQL_PASSWORD.isEmpty()) {
                System.err.println("CẢNH BÁO: Mật khẩu MySQL (MYSQL_PASSWORD) chưa được cấu hình trong DatabaseConnector.java!");
            }

            connection = DatabaseConnector.getConnection();

            if (connection != null && !connection.isClosed()) {
                System.out.println("THÀNH CÔNG! Đã kết nối tới cơ sở dữ liệu MySQL '" + DATABASE_NAME + "'.");
                try (java.sql.Statement stmt = connection.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT DATABASE();")) {
                    if (rs.next()) {
                        System.out.println("Cơ sở dữ liệu hiện tại đang kết nối: " + rs.getString(1));
                    }
                }
            } else {
                System.err.println("THẤT BẠI! Không thể kết nối tới MySQL. (Đối tượng Connection là null hoặc đã bị đóng)");
            }
        } catch (SQLException e) {
            System.err.println("LỖI KẾT NỐI MYSQL: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            e.printStackTrace(); 
            if (e.getMessage().toLowerCase().contains("access denied for user")) {
                System.err.println("GỢI Ý: Kiểm tra lại MYSQL_USERNAME ('" + MYSQL_USERNAME + "') và MYSQL_PASSWORD. Đảm bảo người dùng có quyền truy cập database '" + DATABASE_NAME + "' từ 'localhost' (hoặc host bạn cấu hình).");
            } else if (e.getMessage().toLowerCase().contains("communications link failure") || e.getMessage().toLowerCase().contains("connect timed out") || e.getMessage().toLowerCase().contains("connection refused")) {
                 System.err.println("GỢI Ý: MySQL Server có thể chưa chạy, hoặc bị chặn bởi tường lửa trên cổng " + MYSQL_PORT + ", hoặc MYSQL_SERVER_HOST ('" + MYSQL_SERVER_HOST + "') không đúng.");
            } else if (e.getMessage().toLowerCase().contains("unknown database")) {
                 System.err.println("GỢI Ý: Cơ sở dữ liệu '" + DATABASE_NAME + "' không tồn tại trên server. Hãy tạo nó trước.");
            } else if (e.getMessage().toLowerCase().contains("no suitable driver found")) {
                System.err.println("GỢI Ý: Lỗi 'No suitable driver found'. Hãy kiểm tra xem MySQL Connector/J JAR (ví dụ: mysql-connector-j-X.Y.Z.jar) đã được thêm vào Build Path (Classpath) của dự án đúng cách chưa.");
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Đã đóng kết nối MySQL.");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng kết nối MySQL: " + e.getMessage());
                }
            }
        }
    }
}

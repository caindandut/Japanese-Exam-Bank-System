package com.exammanager.nganhangdethi.dao;

import com.exammanager.nganhangdethi.model.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO (Data Access Object) cho việc quản lý Cấp độ (Levels).
 * Cung cấp các phương thức để tương tác với bảng 'Levels' trong cơ sở dữ liệu.
 */
public class LevelDAO {

    
    public List<Level> getAllLevels() {
        List<Level> levels = new ArrayList<>();
        String sql = "SELECT LevelID, LevelName FROM Levels ORDER BY LevelID"; 

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Level level = new Level();
                level.setLevelID(rs.getLong("LevelID"));
                level.setLevelName(rs.getString("LevelName"));
                levels.add(level);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách Levels: " + e.getMessage());
            e.printStackTrace();
            
        }
        return levels;
    }

    
    public Level getLevelById(long levelId) {
        String sql = "SELECT LevelID, LevelName FROM Levels WHERE LevelID = ?";
        Level level = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, levelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    level = new Level();
                    level.setLevelID(rs.getLong("LevelID"));
                    level.setLevelName(rs.getString("LevelName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Level theo ID " + levelId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return level;
    }

    
    public boolean addLevel(Level level) {
        String sql = "INSERT INTO Levels (LevelName) VALUES (?)";
        boolean rowInserted = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, level.getLevelName());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                rowInserted = true;
                // Lấy ID được tạo tự động
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        level.setLevelID(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Level mới: " + e.getMessage());
            e.printStackTrace();
        }
        return rowInserted;
    }

    /**
     * Cập nhật thông tin của một cấp độ đã có.
     * @param level Đối tượng Level chứa thông tin cập nhật (LevelID phải có).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateLevel(Level level) {
        String sql = "UPDATE Levels SET LevelName = ? WHERE LevelID = ?";
        boolean rowUpdated = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, level.getLevelName());
            pstmt.setLong(2, level.getLevelID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rowUpdated = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Level ID " + level.getLevelID() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    
    public boolean deleteLevel(long levelId) {
        String sql = "DELETE FROM Levels WHERE LevelID = ?";
        boolean rowDeleted = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, levelId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rowDeleted = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Level ID " + levelId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowDeleted;
    }

    public static void main(String[] args) {
        LevelDAO levelDAO = new LevelDAO();

        // Kiểm tra thêm mới
        System.out.println("Đang thêm Level mới...");
        Level newLevel = new Level("N0 (Test)");
        if (levelDAO.addLevel(newLevel)) {
            System.out.println("Thêm thành công Level mới với ID: " + newLevel.getLevelID());
        } else {
            System.out.println("Thêm Level mới thất bại.");
        }
        System.out.println("-----");

        // Kiểm tra lấy tất cả
        System.out.println("Danh sách tất cả Levels:");
        List<Level> levels = levelDAO.getAllLevels();
        for (Level l : levels) {
            System.out.println("ID: " + l.getLevelID() + ", Name: " + l.getLevelName());
        }
        System.out.println("-----");

        // Kiểm tra lấy theo ID (giả sử ID 1 tồn tại)
        long testId = newLevel.getLevelID() > 0 ? newLevel.getLevelID() : 1; // Lấy ID vừa thêm hoặc ID=1
        if (newLevel.getLevelID() == 0 && !levels.isEmpty()) { // Nếu thêm thất bại, lấy ID đầu tiên trong list
             testId = levels.get(0).getLevelID();
        }

        System.out.println("Lấy Level có ID = " + testId);
        Level foundLevel = levelDAO.getLevelById(testId);
        if (foundLevel != null) {
            System.out.println("Tìm thấy: " + foundLevel.getLevelName());

            // Kiểm tra cập nhật
            System.out.println("Đang cập nhật Level ID " + foundLevel.getLevelID() + "...");
            foundLevel.setLevelName(foundLevel.getLevelName() + " - Updated");
            if (levelDAO.updateLevel(foundLevel)) {
                System.out.println("Cập nhật thành công. Tên mới: " + foundLevel.getLevelName());
            } else {
                System.out.println("Cập nhật thất bại.");
            }
            System.out.println("-----");

            // Kiểm tra xóa (chỉ xóa level vừa tạo để test)
            if (newLevel.getLevelID() > 0 && newLevel.getLevelID() == testId) { // Chỉ xóa nếu đó là level vừa thêm
                System.out.println("Đang xóa Level ID " + foundLevel.getLevelID() + "...");
                if (levelDAO.deleteLevel(foundLevel.getLevelID())) {
                    System.out.println("Xóa thành công.");
                } else {
                    System.out.println("Xóa thất bại.");
                }
            }


        } else {
            System.out.println("Không tìm thấy Level với ID = " + testId);
        }
         System.out.println("-----");
         System.out.println("Danh sách Levels sau khi test:");
         levels = levelDAO.getAllLevels();
         for (Level l : levels) {
             System.out.println("ID: " + l.getLevelID() + ", Name: " + l.getLevelName());
         }
    }
}

package com.exammanager.nganhangdethi.dao;

import com.exammanager.nganhangdethi.model.QuestionType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO (Data Access Object) cho việc quản lý Loại Câu hỏi (QuestionTypes).
 * Cung cấp các phương thức để tương tác với bảng 'QuestionTypes' trong cơ sở dữ liệu.
 */
public class QuestionTypeDAO {

    /**
     * Lấy tất cả các loại câu hỏi từ cơ sở dữ liệu.
     * @return Danh sách các đối tượng QuestionType.
     */
    public List<QuestionType> getAllQuestionTypes() {
        List<QuestionType> questionTypes = new ArrayList<>();
        String sql = "SELECT QuestionTypeID, TypeName FROM QuestionTypes ORDER BY QuestionTypeID";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                QuestionType qt = new QuestionType();
                qt.setQuestionTypeID(rs.getLong("QuestionTypeID"));
                qt.setTypeName(rs.getString("TypeName"));
                questionTypes.add(qt);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách QuestionTypes: " + e.getMessage());
            e.printStackTrace();
        }
        return questionTypes;
    }

    /**
     * Lấy một loại câu hỏi theo ID.
     * @param questionTypeId ID của loại câu hỏi cần lấy.
     * @return Đối tượng QuestionType nếu tìm thấy, ngược lại là null.
     */
    public QuestionType getQuestionTypeById(long questionTypeId) {
        String sql = "SELECT QuestionTypeID, TypeName FROM QuestionTypes WHERE QuestionTypeID = ?";
        QuestionType qt = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, questionTypeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    qt = new QuestionType();
                    qt.setQuestionTypeID(rs.getLong("QuestionTypeID"));
                    qt.setTypeName(rs.getString("TypeName"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy QuestionType theo ID " + questionTypeId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return qt;
    }

    /**
     * Thêm một loại câu hỏi mới vào cơ sở dữ liệu.
     * @param questionType Đối tượng QuestionType chứa thông tin loại câu hỏi mới (TypeName).
     * @return true nếu thêm thành công, false nếu thất bại.
     * ID của loại câu hỏi mới sẽ được gán vào đối tượng questionType sau khi thêm.
     */
    public boolean addQuestionType(QuestionType questionType) {
        String sql = "INSERT INTO QuestionTypes (TypeName) VALUES (?)";
        boolean rowInserted = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, questionType.getTypeName());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                rowInserted = true;
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        questionType.setQuestionTypeID(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm QuestionType mới: " + e.getMessage());
            e.printStackTrace();
        }
        return rowInserted;
    }

    /**
     * Cập nhật thông tin của một loại câu hỏi đã có.
     * @param questionType Đối tượng QuestionType chứa thông tin cập nhật (QuestionTypeID phải có).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateQuestionType(QuestionType questionType) {
        String sql = "UPDATE QuestionTypes SET TypeName = ? WHERE QuestionTypeID = ?";
        boolean rowUpdated = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, questionType.getTypeName());
            pstmt.setLong(2, questionType.getQuestionTypeID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rowUpdated = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật QuestionType ID " + questionType.getQuestionTypeID() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    /**
     * Xóa một loại câu hỏi khỏi cơ sở dữ liệu.
     * @param questionTypeId ID của loại câu hỏi cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean deleteQuestionType(long questionTypeId) {
        String sql = "DELETE FROM QuestionTypes WHERE QuestionTypeID = ?";
        boolean rowDeleted = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, questionTypeId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rowDeleted = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa QuestionType ID " + questionTypeId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowDeleted;
    }

     // Phương thức main để kiểm tra nhanh các chức năng DAO
    public static void main(String[] args) {
        QuestionTypeDAO qtDAO = new QuestionTypeDAO();

        // Kiểm tra thêm mới
        System.out.println("Đang thêm QuestionType mới...");
        QuestionType newQt = new QuestionType("Kiểm tra đọc (Test)");
        if (qtDAO.addQuestionType(newQt)) {
            System.out.println("Thêm thành công QuestionType mới với ID: " + newQt.getQuestionTypeID());
        } else {
            System.out.println("Thêm QuestionType mới thất bại.");
        }
        System.out.println("-----");

        // Kiểm tra lấy tất cả
        System.out.println("Danh sách tất cả QuestionTypes:");
        List<QuestionType> qts = qtDAO.getAllQuestionTypes();
        for (QuestionType qt : qts) {
            System.out.println("ID: " + qt.getQuestionTypeID() + ", Name: " + qt.getTypeName());
        }
        System.out.println("-----");

        // Kiểm tra lấy theo ID
        long testIdQt = newQt.getQuestionTypeID() > 0 ? newQt.getQuestionTypeID() : 1;
        if (newQt.getQuestionTypeID() == 0 && !qts.isEmpty()) {
             testIdQt = qts.get(0).getQuestionTypeID();
        }

        System.out.println("Lấy QuestionType có ID = " + testIdQt);
        QuestionType foundQt = qtDAO.getQuestionTypeById(testIdQt);
        if (foundQt != null) {
            System.out.println("Tìm thấy: " + foundQt.getTypeName());

            // Kiểm tra cập nhật
            System.out.println("Đang cập nhật QuestionType ID " + foundQt.getQuestionTypeID() + "...");
            foundQt.setTypeName(foundQt.getTypeName() + " - Updated");
            if (qtDAO.updateQuestionType(foundQt)) {
                System.out.println("Cập nhật thành công. Tên mới: " + foundQt.getTypeName());
            } else {
                System.out.println("Cập nhật thất bại.");
            }
            System.out.println("-----");

             // Kiểm tra xóa (chỉ xóa type vừa tạo để test)
            if (newQt.getQuestionTypeID() > 0 && newQt.getQuestionTypeID() == testIdQt) {
                System.out.println("Đang xóa QuestionType ID " + foundQt.getQuestionTypeID() + "...");
                if (qtDAO.deleteQuestionType(foundQt.getQuestionTypeID())) {
                    System.out.println("Xóa thành công.");
                } else {
                    System.out.println("Xóa thất bại.");
                }
            }

        } else {
            System.out.println("Không tìm thấy QuestionType với ID = " + testIdQt);
        }
        System.out.println("-----");
        System.out.println("Danh sách QuestionTypes sau khi test:");
        qts = qtDAO.getAllQuestionTypes();
        for (QuestionType qt : qts) {
            System.out.println("ID: " + qt.getQuestionTypeID() + ", Name: " + qt.getTypeName());
        }
    }
}

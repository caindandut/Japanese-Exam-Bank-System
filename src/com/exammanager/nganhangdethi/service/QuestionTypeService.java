package com.exammanager.nganhangdethi.service;

import com.exammanager.nganhangdethi.dao.QuestionTypeDAO;
import com.exammanager.nganhangdethi.model.QuestionType;

import java.util.List;

/**
 * Lớp Service cho việc quản lý Loại Câu hỏi (QuestionTypes).
 */
public class QuestionTypeService {

    private QuestionTypeDAO questionTypeDAO;

    public QuestionTypeService() {
        this.questionTypeDAO = new QuestionTypeDAO();
    }

    /**
     * Lấy tất cả các loại câu hỏi.
     * @return Danh sách các QuestionType.
     */
    public List<QuestionType> getAllQuestionTypes() {
        return questionTypeDAO.getAllQuestionTypes();
    }

    /**
     * Lấy một loại câu hỏi theo ID.
     * @param questionTypeId ID của loại câu hỏi.
     * @return Đối tượng QuestionType hoặc null nếu không tìm thấy.
     */
    public QuestionType getQuestionTypeById(long questionTypeId) {
        return questionTypeDAO.getQuestionTypeById(questionTypeId);
    }

    /**
     * Thêm một loại câu hỏi mới.
     * @param questionType Đối tượng QuestionType cần thêm (chỉ cần TypeName).
     * @return true nếu thêm thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu TypeName trống hoặc null.
     */
    public boolean addQuestionType(QuestionType questionType) throws IllegalArgumentException {
        if (questionType == null || questionType.getTypeName() == null || questionType.getTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại câu hỏi không được để trống.");
        }
        return questionTypeDAO.addQuestionType(questionType);
    }

    /**
     * Cập nhật một loại câu hỏi.
     * @param questionType Đối tượng QuestionType cần cập nhật (phải có QuestionTypeID và TypeName mới).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu TypeName trống hoặc null, hoặc QuestionTypeID không hợp lệ.
     */
    public boolean updateQuestionType(QuestionType questionType) throws IllegalArgumentException {
        if (questionType == null || questionType.getQuestionTypeID() <= 0) {
            throw new IllegalArgumentException("ID loại câu hỏi không hợp lệ để cập nhật.");
        }
        if (questionType.getTypeName() == null || questionType.getTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại câu hỏi không được để trống khi cập nhật.");
        }
        return questionTypeDAO.updateQuestionType(questionType);
    }

    /**
     * Xóa một loại câu hỏi.
     * @param questionTypeId ID của loại câu hỏi cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     * @throws Exception nếu loại câu hỏi này đang được sử dụng.
     */
    public boolean deleteQuestionType(long questionTypeId) throws Exception {
        boolean deleted = questionTypeDAO.deleteQuestionType(questionTypeId);
        if (!deleted) {
            // Có thể ném một Exception cụ thể hơn
            // throw new Exception("Không thể xóa loại câu hỏi. Có thể loại này đang được sử dụng.");
        }
        return deleted;
    }

    // Phương thức main để kiểm tra nhanh (tùy chọn)
    public static void main(String[] args) {
        QuestionTypeService qtService = new QuestionTypeService();

        System.out.println("Lấy tất cả QuestionTypes từ Service:");
        List<QuestionType> qts = qtService.getAllQuestionTypes();
         if (qts.isEmpty()) {
            System.out.println("Chưa có QuestionType nào. Thêm một vài type mẫu để test.");
            try {
                qtService.addQuestionType(new QuestionType("Ngữ pháp Service Test"));
                qtService.addQuestionType(new QuestionType("Từ vựng Service Test"));
                qts = qtService.getAllQuestionTypes();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        for (QuestionType qt : qts) {
            System.out.println("ID: " + qt.getQuestionTypeID() + ", Name: " + qt.getTypeName());
        }
        System.out.println("-----");

        try {
            QuestionType newQt = new QuestionType("Đọc hiểu Service Test");
            if (qtService.addQuestionType(newQt)) {
                System.out.println("Thêm thành công QuestionType từ Service: " + newQt.getTypeName() + " (ID: " + newQt.getQuestionTypeID() + ")");

                newQt.setTypeName("Đọc hiểu Service Test - Updated");
                if (qtService.updateQuestionType(newQt)) {
                    System.out.println("Cập nhật thành công. Tên mới: " + newQt.getTypeName());
                } else {
                    System.out.println("Cập nhật thất bại.");
                }

                System.out.println("Đang xóa QuestionType ID: " + newQt.getQuestionTypeID());
                if (qtService.deleteQuestionType(newQt.getQuestionTypeID())) {
                    System.out.println("Xóa thành công.");
                } else {
                    System.out.println("Xóa thất bại.");
                }

            } else {
                System.out.println("Thêm QuestionType từ Service thất bại.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình test QuestionTypeService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.exammanager.nganhangdethi.model;

/**
 * Lớp đại diện cho một Loại Câu hỏi trong ngân hàng đề thi.
 * Ví dụ: Ngữ pháp, Từ vựng, Đọc hiểu, Nghe hiểu.
 */
public class QuestionType {
    private long questionTypeID;
    private String typeName;

    // Constructors
    public QuestionType() {
    }

    public QuestionType(long questionTypeID, String typeName) {
        this.questionTypeID = questionTypeID;
        this.typeName = typeName;
    }

    public QuestionType(String typeName) {
        this.typeName = typeName;
    }

    // Getters and Setters
    public long getQuestionTypeID() {
        return questionTypeID;
    }

    public void setQuestionTypeID(long questionTypeID) {
        this.questionTypeID = questionTypeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        // Quan trọng cho việc hiển thị trong ComboBox hoặc JList
        return typeName;
    }
}

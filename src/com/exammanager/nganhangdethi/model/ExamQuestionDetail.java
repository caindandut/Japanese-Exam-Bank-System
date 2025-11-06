package com.exammanager.nganhangdethi.model;

/**
 * Lớp đại diện cho chi tiết một câu hỏi trong một đề thi, bao gồm cả thứ tự của nó.
 * Đây không phải là một bảng trực tiếp trong DB mà là một đối tượng tiện ích.
 * Bảng ExamQuestions sẽ lưu ExamID, QuestionID, QuestionOrder.
 */
public class ExamQuestionDetail {
    private Question question; // Đối tượng câu hỏi đầy đủ
    private int questionOrder; // Thứ tự của câu hỏi trong đề thi

    // Constructors
    public ExamQuestionDetail() {
    }

    public ExamQuestionDetail(Question question, int questionOrder) {
        this.question = question;
        this.questionOrder = questionOrder;
    }

    // Getters and Setters
    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    @Override
    public String toString() {
        return "ExamQuestionDetail{" +
               "questionID=" + (question != null ? question.getQuestionID() : "null") +
               ", questionOrder=" + questionOrder +
               ", questionText='" + (question != null ? question.getQuestionText() : "null") + '\'' +
               '}';
    }
}

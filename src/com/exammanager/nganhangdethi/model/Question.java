package com.exammanager.nganhangdethi.model;

import java.util.ArrayList;
import java.util.Date; // Sử dụng java.util.Date cho CreatedAt và UpdatedAt
import java.util.List;

/**
 * Lớp đại diện cho một Câu hỏi trong ngân hàng đề thi.
 */
public class Question {
    private long questionID;
    private Level level; // Đối tượng Level thay vì chỉ LevelID
    private QuestionType questionType; // Đối tượng QuestionType thay vì chỉ QuestionTypeID
    private String questionText;
    private String audioPath;
    private Date createdAt;
    private Date updatedAt;
    private List<Choice> choices; // Danh sách các lựa chọn cho câu hỏi này

    // Constructors
    public Question() {
        this.choices = new ArrayList<>(); // Khởi tạo danh sách choices
    }

    public Question(long questionID, Level level, QuestionType questionType, String questionText, String audioPath, Date createdAt, Date updatedAt) {
        this.questionID = questionID;
        this.level = level;
        this.questionType = questionType;
        this.questionText = questionText;
        this.audioPath = audioPath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.choices = new ArrayList<>();
    }

    // Getters and Setters
    public long getQuestionID() {
        return questionID;
    }

    public void setQuestionID(long questionID) {
        this.questionID = questionID;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    // Tiện ích để thêm một lựa chọn
    public void addChoice(Choice choice) {
        if (this.choices == null) {
            this.choices = new ArrayList<>();
        }
        this.choices.add(choice);
        choice.setQuestionID(this.questionID); // Đảm bảo choice này thuộc về question hiện tại
    }

    @Override
    public String toString() {
        return "Question{" +
               "questionID=" + questionID +
               ", level=" + (level != null ? level.getLevelName() : "null") +
               ", questionType=" + (questionType != null ? questionType.getTypeName() : "null") +
               ", questionText='" + questionText + '\'' +
               ", audioPath='" + audioPath + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", choicesCount=" + (choices != null ? choices.size() : 0) +
               '}';
    }
}

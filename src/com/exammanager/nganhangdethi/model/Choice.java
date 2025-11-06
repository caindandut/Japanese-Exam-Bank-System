package com.exammanager.nganhangdethi.model;

/**
 * Lớp đại diện cho một Lựa chọn (đáp án) cho một Câu hỏi.
 */
public class Choice {
    private long choiceID;
    private long questionID; // Khóa ngoại tham chiếu đến Question
    private String choiceText;
    private boolean isCorrect;

    // Constructors
    public Choice() {
    }

    public Choice(long choiceID, long questionID, String choiceText, boolean isCorrect) {
        this.choiceID = choiceID;
        this.questionID = questionID;
        this.choiceText = choiceText;
        this.isCorrect = isCorrect;
    }

    public Choice(long questionID, String choiceText, boolean isCorrect) {
        this.questionID = questionID;
        this.choiceText = choiceText;
        this.isCorrect = isCorrect;
    }

    public Choice(String choiceText, boolean isCorrect) {
        this.choiceText = choiceText;
        this.isCorrect = isCorrect;
    }


    // Getters and Setters
    public long getChoiceID() {
        return choiceID;
    }

    public void setChoiceID(long choiceID) {
        this.choiceID = choiceID;
    }

    public long getQuestionID() {
        return questionID;
    }

    public void setQuestionID(long questionID) {
        this.questionID = questionID;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public String toString() {
        return "Choice{" +
               "choiceID=" + choiceID +
               ", questionID=" + questionID +
               ", choiceText='" + choiceText + '\'' +
               ", isCorrect=" + isCorrect +
               '}';
    }
}

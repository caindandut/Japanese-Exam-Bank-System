package com.exammanager.nganhangdethi.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exam {
    private long examID;
    private String examName;
    private Level level;
    private Integer durationMinutes; // Thời gian làm bài tính bằng phút
    private Date generatedAt;
    private List<ExamQuestionDetail> examQuestions;

    public Exam() {
        this.examQuestions = new ArrayList<>();
    }

    public Exam(long examID, String examName, Level level, Integer durationMinutes, Date generatedAt) {
        this.examID = examID;
        this.examName = examName;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.generatedAt = generatedAt;
        this.examQuestions = new ArrayList<>();
    }

    // Getters and Setters
    public long getExamID() {
        return examID;
    }

    public void setExamID(long examID) {
        this.examID = examID;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<ExamQuestionDetail> getExamQuestions() {
        return examQuestions;
    }

    public void setExamQuestions(List<ExamQuestionDetail> examQuestions) {
        this.examQuestions = examQuestions;
    }

    public void addExamQuestionDetail(ExamQuestionDetail examQuestionDetail) {
        if (this.examQuestions == null) {
            this.examQuestions = new ArrayList<>();
        }
        this.examQuestions.add(examQuestionDetail);
    }

    @Override
    public String toString() {
        return "Exam{" +
               "examID=" + examID +
               ", examName='" + examName + '\'' +
               ", level=" + (level != null ? level.getLevelName() : "null") +
               ", durationMinutes=" + durationMinutes +
               ", generatedAt=" + generatedAt +
               ", numberOfQuestions=" + (examQuestions != null ? examQuestions.size() : 0) +
               '}';
    }
}

package com.exammanager.nganhangdethi.model;

/**
 * Lớp đại diện cho một Cấp độ trong ngân hàng đề thi.
 * Ví dụ: N1, N2, N3, N4, N5.
 */
public class Level {
    private long levelID;
    private String levelName;

    // Constructors
    public Level() {
    }

    public Level(long levelID, String levelName) {
        this.levelID = levelID;
        this.levelName = levelName;
    }

    public Level(String levelName) {
        this.levelName = levelName;
    }

    // Getters and Setters
    public long getLevelID() {
        return levelID;
    }

    public void setLevelID(long levelID) {
        this.levelID = levelID;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    @Override
    public String toString() {
        // Quan trọng cho việc hiển thị trong ComboBox hoặc JList
        return levelName;
    }
}

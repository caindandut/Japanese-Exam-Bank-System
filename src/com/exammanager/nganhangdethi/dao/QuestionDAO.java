package com.exammanager.nganhangdethi.dao;

import com.exammanager.nganhangdethi.model.Choice;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.QuestionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner; // Import để tạo chuỗi IN clause

public class QuestionDAO {

    // ... (các phương thức addQuestionWithChoices, getQuestionById, getAllQuestions, updateQuestionWithChoices, deleteQuestion giữ nguyên như trước) ...
    public boolean addQuestionWithChoices(Question question) {
        String sqlInsertQuestion = "INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlInsertChoice = "INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (?, ?, ?)";
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtQuestion = conn.prepareStatement(sqlInsertQuestion, Statement.RETURN_GENERATED_KEYS)) {
                pstmtQuestion.setLong(1, question.getLevel().getLevelID());
                pstmtQuestion.setLong(2, question.getQuestionType().getQuestionTypeID());
                pstmtQuestion.setString(3, question.getQuestionText());
                pstmtQuestion.setString(4, question.getAudioPath());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                pstmtQuestion.setTimestamp(5, now); 
                pstmtQuestion.setTimestamp(6, now); 

                int affectedRows = pstmtQuestion.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Thêm Question thất bại, không có dòng nào được thêm.");
                }

                try (ResultSet generatedKeys = pstmtQuestion.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        question.setQuestionID(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Thêm Question thất bại, không lấy được ID.");
                    }
                }
            }

            if (question.getChoices() != null && !question.getChoices().isEmpty()) {
                try (PreparedStatement pstmtChoice = conn.prepareStatement(sqlInsertChoice)) {
                    for (Choice choice : question.getChoices()) {
                        pstmtChoice.setLong(1, question.getQuestionID()); 
                        pstmtChoice.setString(2, choice.getChoiceText());
                        pstmtChoice.setBoolean(3, choice.isCorrect());
                        pstmtChoice.addBatch(); 
                    }
                    pstmtChoice.executeBatch(); 
                }
            }

            conn.commit(); 
            success = true;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Question với Choices (transaction): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public Question getQuestionById(long questionId) {
        String sqlQuestion = "SELECT q.QuestionID, q.QuestionText, q.AudioPath, q.CreatedAt, q.UpdatedAt, " +
                             "l.LevelID AS L_ID, l.LevelName, " +
                             "qt.QuestionTypeID AS QT_ID, qt.TypeName " +
                             "FROM Questions q " +
                             "JOIN Levels l ON q.LevelID = l.LevelID " +
                             "JOIN QuestionTypes qt ON q.QuestionTypeID = qt.QuestionTypeID " +
                             "WHERE q.QuestionID = ?";
        String sqlChoices = "SELECT ChoiceID, ChoiceText, IsCorrect FROM Choices WHERE QuestionID = ?";
        Question question = null;

        try (Connection conn = DatabaseConnector.getConnection()) {
            try (PreparedStatement pstmtQuestion = conn.prepareStatement(sqlQuestion)) {
                pstmtQuestion.setLong(1, questionId);
                try (ResultSet rsQuestion = pstmtQuestion.executeQuery()) {
                    if (rsQuestion.next()) {
                        Level level = new Level(rsQuestion.getLong("L_ID"), rsQuestion.getString("LevelName"));
                        QuestionType questionType = new QuestionType(rsQuestion.getLong("QT_ID"), rsQuestion.getString("TypeName"));

                        question = new Question();
                        question.setQuestionID(rsQuestion.getLong("QuestionID"));
                        question.setLevel(level);
                        question.setQuestionType(questionType);
                        question.setQuestionText(rsQuestion.getString("QuestionText"));
                        question.setAudioPath(rsQuestion.getString("AudioPath"));
                        question.setCreatedAt(rsQuestion.getTimestamp("CreatedAt"));
                        question.setUpdatedAt(rsQuestion.getTimestamp("UpdatedAt"));
                    }
                }
            }

            if (question != null) {
                List<Choice> choices = new ArrayList<>();
                try (PreparedStatement pstmtChoices = conn.prepareStatement(sqlChoices)) {
                    pstmtChoices.setLong(1, questionId);
                    try (ResultSet rsChoices = pstmtChoices.executeQuery()) {
                        while (rsChoices.next()) {
                            Choice choice = new Choice();
                            choice.setChoiceID(rsChoices.getLong("ChoiceID"));
                            choice.setQuestionID(questionId); 
                            choice.setChoiceText(rsChoices.getString("ChoiceText"));
                            choice.setCorrect(rsChoices.getBoolean("IsCorrect"));
                            choices.add(choice);
                        }
                    }
                }
                question.setChoices(choices);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Question theo ID " + questionId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return question;
    }
    
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.QuestionID, q.QuestionText, q.AudioPath, q.CreatedAt, q.UpdatedAt, " +
                     "l.LevelID AS L_ID, l.LevelName, " +
                     "qt.QuestionTypeID AS QT_ID, qt.TypeName " +
                     "FROM Questions q " +
                     "JOIN Levels l ON q.LevelID = l.LevelID " +
                     "JOIN QuestionTypes qt ON q.QuestionTypeID = qt.QuestionTypeID " +
                     "ORDER BY q.QuestionID DESC"; 

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Level level = new Level(rs.getLong("L_ID"), rs.getString("LevelName"));
                QuestionType questionType = new QuestionType(rs.getLong("QT_ID"), rs.getString("TypeName"));

                Question question = new Question();
                question.setQuestionID(rs.getLong("QuestionID"));
                question.setLevel(level);
                question.setQuestionType(questionType);
                question.setQuestionText(rs.getString("QuestionText"));
                question.setAudioPath(rs.getString("AudioPath"));
                question.setCreatedAt(rs.getTimestamp("CreatedAt"));
                question.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                questions.add(question);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách Questions: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Lấy danh sách câu hỏi dựa trên LevelID và một danh sách các QuestionTypeID.
     * @param levelId ID của Level (0 hoặc số âm nếu không lọc theo Level).
     * @param questionTypeIds Danh sách các ID của QuestionType. Nếu null hoặc rỗng, không lọc theo QuestionType.
     * @return Danh sách các Question phù hợp.
     */
    public List<Question> getQuestionsByLevelAndTypes(long levelId, List<Long> questionTypeIds) {
        List<Question> questions = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT q.QuestionID, q.QuestionText, q.AudioPath, q.CreatedAt, q.UpdatedAt, " +
            "l.LevelID AS L_ID, l.LevelName, " +
            "qt.QuestionTypeID AS QT_ID, qt.TypeName " +
            "FROM Questions q " +
            "JOIN Levels l ON q.LevelID = l.LevelID " +
            "JOIN QuestionTypes qt ON q.QuestionTypeID = qt.QuestionTypeID WHERE 1=1"
        );

        if (levelId > 0) {
            sqlBuilder.append(" AND q.LevelID = ?");
        }

        if (questionTypeIds != null && !questionTypeIds.isEmpty()) {
            sqlBuilder.append(" AND q.QuestionTypeID IN (");
            StringJoiner sj = new StringJoiner(",");
            for (int i = 0; i < questionTypeIds.size(); i++) {
                sj.add("?");
            }
            sqlBuilder.append(sj.toString());
            sqlBuilder.append(")");
        }
        sqlBuilder.append(" ORDER BY q.QuestionID DESC");

        System.out.println("Executing SQL in DAO: " + sqlBuilder.toString()); // DEBUG

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            if (levelId > 0) {
                pstmt.setLong(paramIndex++, levelId);
            }
            if (questionTypeIds != null && !questionTypeIds.isEmpty()) {
                for (Long typeId : questionTypeIds) {
                    pstmt.setLong(paramIndex++, typeId);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Level level = new Level(rs.getLong("L_ID"), rs.getString("LevelName"));
                    QuestionType questionType = new QuestionType(rs.getLong("QT_ID"), rs.getString("TypeName"));
                    Question question = new Question();
                    question.setQuestionID(rs.getLong("QuestionID"));
                    question.setLevel(level);
                    question.setQuestionType(questionType);
                    question.setQuestionText(rs.getString("QuestionText"));
                    question.setAudioPath(rs.getString("AudioPath"));
                    question.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    question.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Questions theo Level và Types: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }


    public boolean updateQuestionWithChoices(Question question) {
        // ... (giữ nguyên như trước) ...
        String sqlUpdateQuestion = "UPDATE Questions SET LevelID = ?, QuestionTypeID = ?, QuestionText = ?, AudioPath = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE QuestionID = ?";
        String sqlDeleteChoices = "DELETE FROM Choices WHERE QuestionID = ?";
        String sqlInsertChoice = "INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (?, ?, ?)";
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtUpdateQuestion = conn.prepareStatement(sqlUpdateQuestion)) {
                pstmtUpdateQuestion.setLong(1, question.getLevel().getLevelID());
                pstmtUpdateQuestion.setLong(2, question.getQuestionType().getQuestionTypeID());
                pstmtUpdateQuestion.setString(3, question.getQuestionText());
                pstmtUpdateQuestion.setString(4, question.getAudioPath());
                pstmtUpdateQuestion.setLong(5, question.getQuestionID());
                pstmtUpdateQuestion.executeUpdate();
            }

            try (PreparedStatement pstmtDeleteChoices = conn.prepareStatement(sqlDeleteChoices)) {
                pstmtDeleteChoices.setLong(1, question.getQuestionID());
                pstmtDeleteChoices.executeUpdate();
            }

            if (question.getChoices() != null && !question.getChoices().isEmpty()) {
                try (PreparedStatement pstmtInsertChoice = conn.prepareStatement(sqlInsertChoice)) {
                    for (Choice choice : question.getChoices()) {
                        pstmtInsertChoice.setLong(1, question.getQuestionID());
                        pstmtInsertChoice.setString(2, choice.getChoiceText());
                        pstmtInsertChoice.setBoolean(3, choice.isCorrect());
                        pstmtInsertChoice.addBatch();
                    }
                    pstmtInsertChoice.executeBatch();
                }
            }

            conn.commit(); 
            success = true;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Question với Choices (transaction) ID " + question.getQuestionID() + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public boolean deleteQuestion(long questionId) {
        // ... (giữ nguyên như trước) ...
        String sql = "DELETE FROM Questions WHERE QuestionID = ?";
        boolean rowDeleted = false;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, questionId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rowDeleted = true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Question ID " + questionId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowDeleted;
    }
    // Phương thức main để test (đã có)
}

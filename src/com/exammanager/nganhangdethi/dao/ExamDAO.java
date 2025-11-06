package com.exammanager.nganhangdethi.dao;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.ExamQuestionDetail;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.Question;
// import com.exammanager.nganhangdethi.model.QuestionType; // Không còn cần trực tiếp cho Exam

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types; // For setting NULL for date and Integer
import java.util.ArrayList;
import java.util.List;
import java.util.Date; // For handling search by date

public class ExamDAO {

    private QuestionDAO questionDAO;
    private LevelDAO levelDAO;

    public ExamDAO() {
        this.questionDAO = new QuestionDAO();
        this.levelDAO = new LevelDAO();
    }

    public boolean addExamWithQuestions(Exam exam) {
        // Câu SQL INSERT vào Exams giờ có DurationMinutes
        String sqlInsertExam = "INSERT INTO Exams (ExamName, LevelID, DurationMinutes, GeneratedAt) VALUES (?, ?, ?, ?)";
        String sqlInsertExamQuestion = "INSERT INTO ExamQuestions (ExamID, QuestionID, QuestionOrder) VALUES (?, ?, ?)";
        Connection conn = null;
        boolean success = false;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtExam = conn.prepareStatement(sqlInsertExam, Statement.RETURN_GENERATED_KEYS)) {
                pstmtExam.setString(1, exam.getExamName());
                pstmtExam.setLong(2, exam.getLevel().getLevelID());
                if (exam.getDurationMinutes() != null) {
                    pstmtExam.setInt(3, exam.getDurationMinutes());
                } else {
                    pstmtExam.setNull(3, Types.INTEGER);
                }
                pstmtExam.setTimestamp(4, new Timestamp(exam.getGeneratedAt() != null ? exam.getGeneratedAt().getTime() : System.currentTimeMillis()));

                int affectedRows = pstmtExam.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Thêm Exam thất bại, không có dòng nào được thêm.");
                try (ResultSet generatedKeys = pstmtExam.getGeneratedKeys()) {
                    if (generatedKeys.next()) exam.setExamID(generatedKeys.getLong(1));
                    else throw new SQLException("Thêm Exam thất bại, không lấy được ID.");
                }
            }
            if (exam.getExamQuestions() != null && !exam.getExamQuestions().isEmpty()) {
                try (PreparedStatement pstmtExamQuestion = conn.prepareStatement(sqlInsertExamQuestion)) {
                    for (ExamQuestionDetail eqDetail : exam.getExamQuestions()) {
                        if (eqDetail.getQuestion() == null || eqDetail.getQuestion().getQuestionID() == 0)
                            throw new SQLException("Question trong ExamQuestionDetail không hợp lệ (null hoặc ID=0).");
                        pstmtExamQuestion.setLong(1, exam.getExamID());
                        pstmtExamQuestion.setLong(2, eqDetail.getQuestion().getQuestionID());
                        pstmtExamQuestion.setInt(3, eqDetail.getQuestionOrder());
                        pstmtExamQuestion.addBatch();
                    }
                    pstmtExamQuestion.executeBatch();
                }
            }
            conn.commit(); success = true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Exam với Questions (transaction): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

   
    public Exam getExamById(long examId) {
        // Câu SQL SELECT giờ có DurationMinutes
        String sqlExam = "SELECT e.ExamID, e.ExamName, e.LevelID AS L_ID, e.DurationMinutes, e.GeneratedAt " +
                         "FROM Exams e WHERE e.ExamID = ?";
        String sqlExamQuestions = "SELECT QuestionID, QuestionOrder FROM ExamQuestions WHERE ExamID = ? ORDER BY QuestionOrder ASC";
        Exam exam = null;
        try (Connection conn = DatabaseConnector.getConnection()) {
            try (PreparedStatement pstmtExam = conn.prepareStatement(sqlExam)) {
                pstmtExam.setLong(1, examId);
                try (ResultSet rsExam = pstmtExam.executeQuery()) {
                    if (rsExam.next()) {
                        Level level = levelDAO.getLevelById(rsExam.getLong("L_ID"));
                        if (level == null) System.err.println("CẢNH BÁO: Không tìm thấy Level với ID: " + rsExam.getLong("L_ID") + " cho ExamID: " + examId);
                        
                        exam = new Exam();
                        exam.setExamID(rsExam.getLong("ExamID"));
                        exam.setExamName(rsExam.getString("ExamName"));
                        exam.setLevel(level);
                        int duration = rsExam.getInt("DurationMinutes");
                        if (!rsExam.wasNull()) { // Kiểm tra xem giá trị có phải là SQL NULL không
                            exam.setDurationMinutes(duration);
                        } else {
                            exam.setDurationMinutes(null);
                        }
                        exam.setGeneratedAt(rsExam.getTimestamp("GeneratedAt"));
                    }
                }
            }
            if (exam != null && exam.getExamQuestions() != null) { 
                try (PreparedStatement pstmtExamQuestions = conn.prepareStatement(sqlExamQuestions)) {
                    pstmtExamQuestions.setLong(1, examId);
                    try (ResultSet rsExamQuestions = pstmtExamQuestions.executeQuery()) {
                        while (rsExamQuestions.next()) {
                            long questionId = rsExamQuestions.getLong("QuestionID");
                            int questionOrder = rsExamQuestions.getInt("QuestionOrder");
                            Question question = questionDAO.getQuestionById(questionId);
                            if (question != null) exam.addExamQuestionDetail(new ExamQuestionDetail(question, questionOrder));
                            else System.err.println("CẢNH BÁO: Không tìm thấy Question với ID: " + questionId + " cho ExamID: " + examId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Exam theo ID " + examId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return exam;
    }

    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        // Câu SQL SELECT giờ có DurationMinutes
        String sql = "SELECT e.ExamID, e.ExamName, e.LevelID AS L_ID, e.DurationMinutes, e.GeneratedAt " +
                     "FROM Exams e ORDER BY e.GeneratedAt DESC, e.ExamID DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Level level = levelDAO.getLevelById(rs.getLong("L_ID"));
                if (level == null) {
                     System.err.println("CẢNH BÁO: Không tìm thấy Level với ID: " + rs.getLong("L_ID") + " cho ExamID: " + rs.getLong("ExamID") + ". Bỏ qua đề thi này.");
                     continue; 
                }
                Exam exam = new Exam();
                exam.setExamID(rs.getLong("ExamID"));
                exam.setExamName(rs.getString("ExamName"));
                exam.setLevel(level);
                int duration = rs.getInt("DurationMinutes");
                if (!rs.wasNull()) {
                    exam.setDurationMinutes(duration);
                } else {
                    exam.setDurationMinutes(null);
                }
                exam.setGeneratedAt(rs.getTimestamp("GeneratedAt"));
                exams.add(exam);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách Exams: " + e.getMessage());
            e.printStackTrace();
        }
        return exams;
    }

    
    public boolean updateExamBasicInfo(Exam exam) {
        // Câu SQL UPDATE giờ có DurationMinutes
        String sql = "UPDATE Exams SET ExamName = ?, LevelID = ?, DurationMinutes = ? WHERE ExamID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exam.getExamName());
            pstmt.setLong(2, exam.getLevel().getLevelID());
            if (exam.getDurationMinutes() != null) {
                pstmt.setInt(3, exam.getDurationMinutes());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setLong(4, exam.getExamID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thông tin cơ bản cho Exam ID " + exam.getExamID() + ": " + e.getMessage());
            e.printStackTrace(); return false;
        }
    }

    private void deleteExamQuestionsByExamId(long examId, Connection conn) throws SQLException {
        String sql = "DELETE FROM ExamQuestions WHERE ExamID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, examId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Phương thức helper để cập nhật thông tin cơ bản của Exam bên trong một transaction đã có.
     * Bao gồm DurationMinutes.
     */
    private boolean updateExamBasicInfoWithinTransaction(Exam exam, Connection conn) throws SQLException {
        String sql = "UPDATE Exams SET ExamName = ?, LevelID = ?, DurationMinutes = ? WHERE ExamID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, exam.getExamName());
            pstmt.setLong(2, exam.getLevel().getLevelID());
            if (exam.getDurationMinutes() != null) {
                pstmt.setInt(3, exam.getDurationMinutes());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setLong(4, exam.getExamID());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateExamWithQuestions(Exam exam) {
        // ... (logic transaction giữ nguyên, chỉ có updateExamBasicInfoWithinTransaction đã được sửa) ...
        Connection conn = null;
        boolean success = false;
        String sqlInsertExamQuestion = "INSERT INTO ExamQuestions (ExamID, QuestionID, QuestionOrder) VALUES (?, ?, ?)";
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);
            if (!updateExamBasicInfoWithinTransaction(exam, conn)) throw new SQLException("Cập nhật thông tin cơ bản của Exam thất bại.");
            deleteExamQuestionsByExamId(exam.getExamID(), conn);
            if (exam.getExamQuestions() != null && !exam.getExamQuestions().isEmpty()) {
                try (PreparedStatement pstmtExamQuestion = conn.prepareStatement(sqlInsertExamQuestion)) {
                    for (ExamQuestionDetail eqDetail : exam.getExamQuestions()) {
                        if (eqDetail.getQuestion() == null || eqDetail.getQuestion().getQuestionID() == 0)
                            throw new SQLException("Question trong ExamQuestionDetail không hợp lệ khi cập nhật (null hoặc ID=0).");
                        pstmtExamQuestion.setLong(1, exam.getExamID());
                        pstmtExamQuestion.setLong(2, eqDetail.getQuestion().getQuestionID());
                        pstmtExamQuestion.setInt(3, eqDetail.getQuestionOrder());
                        pstmtExamQuestion.addBatch();
                    }
                    pstmtExamQuestion.executeBatch();
                }
            }
            conn.commit(); success = true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Exam với Questions (transaction) cho ExamID " + exam.getExamID() + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

    public boolean deleteExam(long examId) {
        String sql = "DELETE FROM Exams WHERE ExamID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, examId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Exam ID " + examId + ": " + e.getMessage());
            e.printStackTrace(); return false;
        }
    }

    public List<Exam> searchExams(long examId, String examName, long levelId, Date generatedDate) {
        List<Exam> exams = new ArrayList<>();
        // Câu SQL SELECT giờ có DurationMinutes
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT e.ExamID, e.ExamName, e.LevelID AS L_ID, e.DurationMinutes, e.GeneratedAt " +
            "FROM Exams e WHERE 1=1" // Không join với Levels ở đây nữa để đơn giản, sẽ lấy Level sau
        );
        List<Object> params = new ArrayList<>();

        if (examId > 0) {
            sqlBuilder.append(" AND e.ExamID = ?");
            params.add(examId);
        }
        if (examName != null && !examName.trim().isEmpty()) {
            sqlBuilder.append(" AND e.ExamName LIKE ?");
            params.add("%" + examName.trim() + "%");
        }
        if (levelId > 0) {
            sqlBuilder.append(" AND e.LevelID = ?");
            params.add(levelId);
        }
        if (generatedDate != null) {
            sqlBuilder.append(" AND DATE(e.GeneratedAt) = DATE(?)");
            params.add(new java.sql.Date(generatedDate.getTime()));
        }

        sqlBuilder.append(" ORDER BY e.GeneratedAt DESC, e.ExamID DESC");

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Level level = levelDAO.getLevelById(rs.getLong("L_ID"));
                    if (level == null) {
                        System.err.println("CẢNH BÁO: Không tìm thấy Level với ID: " + rs.getLong("L_ID") + " cho ExamID: " + rs.getLong("ExamID") + ". Bỏ qua đề thi này.");
                        continue;
                    }
                    Exam exam = new Exam();
                    exam.setExamID(rs.getLong("ExamID"));
                    exam.setExamName(rs.getString("ExamName"));
                    exam.setLevel(level);
                    int duration = rs.getInt("DurationMinutes");
                    if (!rs.wasNull()) {
                        exam.setDurationMinutes(duration);
                    } else {
                        exam.setDurationMinutes(null);
                    }
                    exam.setGeneratedAt(rs.getTimestamp("GeneratedAt"));
                    exams.add(exam);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm Exams: " + e.getMessage());
            e.printStackTrace();
        }
        return exams;
    }
}

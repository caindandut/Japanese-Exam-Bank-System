package com.exammanager.nganhangdethi.service;

import com.exammanager.nganhangdethi.dao.ExamDAO;
import com.exammanager.nganhangdethi.dao.LevelDAO;
import com.exammanager.nganhangdethi.dao.QuestionTypeDAO;
import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.ExamQuestionDetail;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.QuestionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Lớp Service cho việc quản lý Đề thi (Exams).
 * Đã cập nhật để Exam xử lý DurationMinutes.
 */
public class ExamService {

    private ExamDAO examDAO;
    private QuestionService questionService;
    private LevelDAO levelDAO;
    private QuestionTypeDAO questionTypeDAO;


    public ExamService() {
        this.examDAO = new ExamDAO();
        this.questionService = new QuestionService();
        this.levelDAO = new LevelDAO();
        this.questionTypeDAO = new QuestionTypeDAO();
    }

    /**
     * Thêm một đề thi mới cùng với danh sách các câu hỏi của nó.
     * @param exam Đối tượng Exam chứa thông tin đề thi, bao gồm durationMinutes.
     * @return true nếu thêm thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu thông tin đầu vào không hợp lệ.
     */
    public boolean addExamWithQuestions(Exam exam) throws IllegalArgumentException {
        if (exam == null) {
            throw new IllegalArgumentException("Đối tượng Exam không được null.");
        }
        if (exam.getLevel() == null || exam.getLevel().getLevelID() <= 0) {
            throw new IllegalArgumentException("Level của Exam không hợp lệ.");
        }
        if (exam.getExamName() == null || exam.getExamName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đề thi không được để trống.");
        }
        if (exam.getDurationMinutes() != null && exam.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Thời gian làm bài (DurationMinutes) phải là số dương nếu được cung cấp.");
        }
        if (exam.getExamQuestions() == null) {
             exam.setExamQuestions(new ArrayList<>());
        }
        if (exam.getExamQuestions() != null) {
            for (ExamQuestionDetail eqd : exam.getExamQuestions()) {
                if (eqd.getQuestion() == null || eqd.getQuestion().getQuestionID() <= 0) {
                    throw new IllegalArgumentException("Câu hỏi trong đề thi không hợp lệ (ID câu hỏi không đúng).");
                }
                if (eqd.getQuestionOrder() <= 0) {
                    throw new IllegalArgumentException("Thứ tự câu hỏi trong đề thi phải là số dương.");
                }
            }
        }
        
        Level fullLevel = levelDAO.getLevelById(exam.getLevel().getLevelID());
        if (fullLevel == null) throw new IllegalArgumentException("Không tìm thấy Level ID: " + exam.getLevel().getLevelID());
        exam.setLevel(fullLevel);

        if(exam.getGeneratedAt() == null) exam.setGeneratedAt(new Date());

        return examDAO.addExamWithQuestions(exam); // ExamDAO đã được cập nhật để xử lý DurationMinutes
    }

    /**
     * Tạo một đề thi mới dựa trên các tiêu chí.
     * @param examName Tên của đề thi.
     * @param levelId ID của Level chính cho đề thi.
     * @param selectedQuestionTypeIds Danh sách các ID của QuestionType để lọc câu hỏi.
     * @param durationMinutes Thời gian làm bài (phút).
     * @param numberOfQuestions Số lượng câu hỏi mong muốn trong đề thi.
     * @param shuffleQuestions true nếu muốn xáo trộn thứ tự câu hỏi, false nếu không.
     * @return Đối tượng Exam đã được tạo và lưu vào DB, hoặc null nếu thất bại/không đủ câu hỏi.
     * @throws IllegalArgumentException nếu thông tin đầu vào không hợp lệ.
     */
    public Exam createExamFromBank(String examName, long levelId, List<Long> selectedQuestionTypeIds, Integer durationMinutes, int numberOfQuestions, boolean shuffleQuestions) throws IllegalArgumentException {
        if (examName == null || examName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đề thi không được để trống.");
        }
        if (levelId <= 0) {
            throw new IllegalArgumentException("Level ID không hợp lệ.");
        }
        if (selectedQuestionTypeIds == null || selectedQuestionTypeIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một Loại Câu hỏi.");
        }
        if (durationMinutes != null && durationMinutes <= 0) { // durationMinutes có thể null
            throw new IllegalArgumentException("Thời gian làm bài (DurationMinutes) phải là số dương nếu được cung cấp.");
        }
        if (numberOfQuestions <= 0) {
            throw new IllegalArgumentException("Số lượng câu hỏi phải lớn hơn 0.");
        }

        Level level = levelDAO.getLevelById(levelId);
        if (level == null) {
            throw new IllegalArgumentException("Không tìm thấy Level với ID: " + levelId);
        }
        
        for (Long qtId : selectedQuestionTypeIds) {
            if (qtId == null || qtId <= 0 || questionTypeDAO.getQuestionTypeById(qtId) == null) {
                throw new IllegalArgumentException("Danh sách Loại Câu hỏi chứa ID không hợp lệ: " + qtId);
            }
        }

        List<Question> availableQuestions = questionService.getQuestionsByCriteria(levelId, selectedQuestionTypeIds);

        if (availableQuestions.size() < numberOfQuestions) {
            System.err.println("Không đủ câu hỏi trong ngân hàng để tạo đề thi. Có: " + availableQuestions.size() + ", Cần: " + numberOfQuestions + " cho LevelID=" + levelId + " và các loại đã chọn.");
            return null;
        }

        if (shuffleQuestions) {
            Collections.shuffle(availableQuestions);
        }

        List<Question> questionsForExam = availableQuestions.subList(0, numberOfQuestions);

        Exam newExam = new Exam();
        newExam.setExamName(examName);
        newExam.setLevel(level);
        newExam.setDurationMinutes(durationMinutes); // Gán thời gian làm bài
        newExam.setGeneratedAt(new Date());

        for (int i = 0; i < questionsForExam.size(); i++) {
            Question q = questionsForExam.get(i);
            Question detailedQuestion = questionService.getQuestionById(q.getQuestionID());
            if(detailedQuestion == null) {
                 System.err.println("Không thể lấy chi tiết cho câu hỏi ID: " + q.getQuestionID() + " khi tạo đề thi.");
                 continue;
            }
            newExam.addExamQuestionDetail(new ExamQuestionDetail(detailedQuestion, i + 1));
        }
        
        if(newExam.getExamQuestions().isEmpty() && numberOfQuestions > 0){
            System.err.println("Không có câu hỏi nào được thêm vào đề thi sau khi lọc và lấy chi tiết.");
            return null;
        }

        if (examDAO.addExamWithQuestions(newExam)) { // ExamDAO đã được cập nhật
            return newExam;
        } else {
            return null;
        }
    }

    public Exam getExamById(long examId) {
        if (examId <= 0) return null;
        return examDAO.getExamById(examId); // ExamDAO đã được cập nhật
    }

    public List<Exam> getAllExams() {
        return examDAO.getAllExams(); // ExamDAO đã được cập nhật
    }

    public boolean updateExamBasicInfo(Exam exam) throws IllegalArgumentException {
        if (exam == null || exam.getExamID() <= 0) throw new IllegalArgumentException("Thông tin đề thi không hợp lệ để cập nhật (ID).");
        if (exam.getExamName() == null || exam.getExamName().trim().isEmpty()) throw new IllegalArgumentException("Tên đề thi không được để trống khi cập nhật.");
        if (exam.getLevel() == null || exam.getLevel().getLevelID() <= 0) throw new IllegalArgumentException("Cấp độ không hợp lệ khi cập nhật.");
        if (exam.getDurationMinutes() != null && exam.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Thời gian làm bài (DurationMinutes) phải là số dương nếu được cung cấp.");
        }
        
        Level fullLevel = levelDAO.getLevelById(exam.getLevel().getLevelID());
        if (fullLevel == null) throw new IllegalArgumentException("Không tìm thấy Cấp độ với ID: " + exam.getLevel().getLevelID() + " trong cơ sở dữ liệu.");
        exam.setLevel(fullLevel);

        return examDAO.updateExamBasicInfo(exam); // ExamDAO đã được cập nhật
    }

    public boolean updateExamWithQuestions(Exam exam) throws IllegalArgumentException {
        if (exam == null || exam.getExamID() <= 0) throw new IllegalArgumentException("ID Đề thi không hợp lệ để cập nhật.");
        if (exam.getLevel() == null || exam.getLevel().getLevelID() <= 0) throw new IllegalArgumentException("Level của Exam không hợp lệ khi cập nhật.");
        if (exam.getExamName() == null || exam.getExamName().trim().isEmpty()) throw new IllegalArgumentException("Tên đề thi không được để trống khi cập nhật.");
        if (exam.getDurationMinutes() != null && exam.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Thời gian làm bài (DurationMinutes) phải là số dương nếu được cung cấp.");
        }
        
        if (exam.getExamQuestions() == null) exam.setExamQuestions(new ArrayList<>());
        for (int i = 0; i < exam.getExamQuestions().size(); i++) {
            ExamQuestionDetail eqd = exam.getExamQuestions().get(i);
            if (eqd.getQuestion() == null || eqd.getQuestion().getQuestionID() <= 0) throw new IllegalArgumentException("Câu hỏi trong danh sách cập nhật không hợp lệ (ID câu hỏi không đúng).");
            eqd.setQuestionOrder(i + 1);
        }

        Level fullLevel = levelDAO.getLevelById(exam.getLevel().getLevelID());
        if (fullLevel == null) throw new IllegalArgumentException("Không tìm thấy Level ID: " + exam.getLevel().getLevelID());
        exam.setLevel(fullLevel);

        return examDAO.updateExamWithQuestions(exam); // ExamDAO đã được cập nhật
    }

    public boolean deleteExam(long examId) throws Exception {
        if (examId <= 0) throw new IllegalArgumentException("ID đề thi không hợp lệ để xóa.");
        return examDAO.deleteExam(examId);
    }

    public List<Exam> searchExams(long examId, String examName, long levelId, Date generatedDate) {
        // Phương thức này trong ExamDAO đã được cập nhật để lấy DurationMinutes
        return examDAO.searchExams(examId, examName, levelId, generatedDate);
    }
}

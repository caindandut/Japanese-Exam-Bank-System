package com.exammanager.nganhangdethi.service;

import com.exammanager.nganhangdethi.dao.QuestionDAO;
import com.exammanager.nganhangdethi.dao.LevelDAO;
import com.exammanager.nganhangdethi.dao.QuestionTypeDAO;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.Choice;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.QuestionType;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays; // Import để test

/**
 * Lớp Service cho việc quản lý Câu hỏi (Questions) và các Lựa chọn (Choices).
 */
public class QuestionService {

    private QuestionDAO questionDAO;
    private LevelDAO levelDAO;
    private QuestionTypeDAO questionTypeDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
        this.levelDAO = new LevelDAO();
        this.questionTypeDAO = new QuestionTypeDAO();
    }

    /**
     * Thêm một câu hỏi mới cùng với các lựa chọn của nó.
     * @param question Đối tượng Question chứa thông tin câu hỏi.
     * Level và QuestionType trong question phải có ID hợp lệ.
     * Danh sách Choices trong question phải được cung cấp.
     * @return true nếu thêm thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu thông tin đầu vào không hợp lệ.
     */
    public boolean addQuestionWithChoices(Question question) throws IllegalArgumentException {
        if (question == null) {
            throw new IllegalArgumentException("Đối tượng Question không được null.");
        }
        if (question.getLevel() == null || question.getLevel().getLevelID() <= 0) {
            throw new IllegalArgumentException("Level của Question không hợp lệ.");
        }
        if (question.getQuestionType() == null || question.getQuestionType().getQuestionTypeID() <= 0) {
            throw new IllegalArgumentException("QuestionType của Question không hợp lệ.");
        }
        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống.");
        }
        if (question.getChoices() == null || question.getChoices().isEmpty()) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất một lựa chọn.");
        }
        boolean correctAnswerExists = false;
        for (Choice choice : question.getChoices()) {
            if (choice.getChoiceText() == null || choice.getChoiceText().trim().isEmpty()) {
                throw new IllegalArgumentException("Nội dung lựa chọn không được để trống.");
            }
            if (choice.isCorrect()) {
                correctAnswerExists = true;
            }
        }
        if (!correctAnswerExists) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất một đáp án đúng.");
        }

        Level level = levelDAO.getLevelById(question.getLevel().getLevelID());
        QuestionType qt = questionTypeDAO.getQuestionTypeById(question.getQuestionType().getQuestionTypeID());

        if (level == null) {
            throw new IllegalArgumentException("Không tìm thấy Level với ID: " + question.getLevel().getLevelID());
        }
        if (qt == null) {
            throw new IllegalArgumentException("Không tìm thấy QuestionType với ID: " + question.getQuestionType().getQuestionTypeID());
        }
        question.setLevel(level); 
        question.setQuestionType(qt); 

        return questionDAO.addQuestionWithChoices(question);
    }

    /**
     * Lấy một câu hỏi theo ID, bao gồm cả Level, QuestionType và Choices.
     * @param questionId ID của câu hỏi.
     * @return Đối tượng Question hoặc null nếu không tìm thấy.
     */
    public Question getQuestionById(long questionId) {
        if (questionId <= 0) {
            return null;
        }
        return questionDAO.getQuestionById(questionId);
    }

    /**
     * Lấy tất cả các câu hỏi.
     * (Phiên bản này lấy thông tin cơ bản, không bao gồm choices cho mỗi question để tối ưu)
     * @return Danh sách các Question.
     */
    public List<Question> getAllQuestions() {
        return questionDAO.getAllQuestions();
    }

    /**
     * Lấy danh sách câu hỏi dựa trên LevelID và một danh sách các QuestionTypeID.
     * @param levelId ID của Level (0 hoặc số âm nếu không lọc theo Level).
     * @param questionTypeIds Danh sách các ID của QuestionType. Nếu null hoặc rỗng, sẽ không lọc theo QuestionType.
     * @return Danh sách các Question phù hợp.
     */
    public List<Question> getQuestionsByCriteria(long levelId, List<Long> questionTypeIds) {
        // Gọi phương thức DAO mới đã được cập nhật trong question_dao_java_multi_type
        return questionDAO.getQuestionsByLevelAndTypes(levelId, questionTypeIds);
    }

    /**
     * Phương thức getQuestionsByCriteria cũ (nhận một questionTypeId).
     * Được giữ lại để tương thích ngược hoặc có thể được thay thế hoàn toàn.
     * Hiện tại, nó gọi phương thức mới với một danh sách chứa một phần tử.
     * @param levelId ID của Level (0 hoặc số âm nếu không lọc theo Level).
     * @param singleQuestionTypeId ID của một QuestionType.
     * @return Danh sách các Question phù hợp.
     */
    public List<Question> getQuestionsByCriteria(long levelId, long singleQuestionTypeId) {
        List<Long> typeIds = new ArrayList<>();
        if (singleQuestionTypeId > 0) {
            typeIds.add(singleQuestionTypeId);
        }
        // Nếu singleQuestionTypeId <= 0, typeIds sẽ rỗng, 
        // và getQuestionsByLevelAndTypes trong DAO sẽ không lọc theo type nếu typeIds là null hoặc rỗng.
        return questionDAO.getQuestionsByLevelAndTypes(levelId, typeIds.isEmpty() ? null : typeIds);
    }


    /**
     * Cập nhật một câu hỏi cùng với các lựa chọn của nó.
     * @param question Đối tượng Question chứa thông tin cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu thông tin đầu vào không hợp lệ.
     */
    public boolean updateQuestionWithChoices(Question question) throws IllegalArgumentException {
        if (question == null || question.getQuestionID() <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ để cập nhật.");
        }
        if (question.getLevel() == null || question.getLevel().getLevelID() <= 0) {
            throw new IllegalArgumentException("Level của Question không hợp lệ khi cập nhật.");
        }
        if (question.getQuestionType() == null || question.getQuestionType().getQuestionTypeID() <= 0) {
            throw new IllegalArgumentException("QuestionType của Question không hợp lệ khi cập nhật.");
        }
        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống khi cập nhật.");
        }
        if (question.getChoices() == null || question.getChoices().isEmpty()) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất một lựa chọn khi cập nhật.");
        }
        boolean correctAnswerExists = false;
        for (Choice choice : question.getChoices()) {
             if (choice.getChoiceText() == null || choice.getChoiceText().trim().isEmpty()) {
                throw new IllegalArgumentException("Nội dung lựa chọn không được để trống khi cập nhật.");
            }
            if (choice.isCorrect()) {
                correctAnswerExists = true;
            }
        }
        if (!correctAnswerExists) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất một đáp án đúng khi cập nhật.");
        }

        Level level = levelDAO.getLevelById(question.getLevel().getLevelID());
        QuestionType qt = questionTypeDAO.getQuestionTypeById(question.getQuestionType().getQuestionTypeID());
        if (level == null) throw new IllegalArgumentException("Không tìm thấy Level ID: " + question.getLevel().getLevelID());
        if (qt == null) throw new IllegalArgumentException("Không tìm thấy QuestionType ID: " + question.getQuestionType().getQuestionTypeID());
        question.setLevel(level);
        question.setQuestionType(qt);

        return questionDAO.updateQuestionWithChoices(question);
    }

    /**
     * Xóa một câu hỏi.
     * @param questionId ID của câu hỏi cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     * @throws Exception nếu có lỗi xảy ra (ví dụ: câu hỏi đang được sử dụng trong đề thi).
     */
    public boolean deleteQuestion(long questionId) throws Exception {
        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ để xóa.");
        }
        boolean deleted = questionDAO.deleteQuestion(questionId);
        if (!deleted) {
            // throw new Exception("Không thể xóa câu hỏi. Có thể câu hỏi này đang được sử dụng trong một đề thi.");
        }
        return deleted;
    }

    // Phương thức main để kiểm tra nhanh (cần cập nhật để test getQuestionsByCriteria mới)
    public static void main(String[] args) {
        QuestionService questionService = new QuestionService();
        LevelService levelService = new LevelService(); 
        QuestionTypeService qtService = new QuestionTypeService(); 

        // Chuẩn bị Level và QuestionType mẫu (giả sử đã có trong DB hoặc tạo mới)
        Level levelN5 = levelService.getAllLevels().stream().filter(l -> l.getLevelName().contains("N5")).findFirst().orElse(null);
        if (levelN5 == null) {
            levelN5 = new Level("N5 Service Main");
            levelService.addLevel(levelN5);
            levelN5 = levelService.getLevelById(levelN5.getLevelID());
        }

        QuestionType qtGrammar = qtService.getAllQuestionTypes().stream().filter(qt -> qt.getTypeName().contains("Ngữ pháp")).findFirst().orElse(null);
        if (qtGrammar == null) {
            qtGrammar = new QuestionType("Ngữ pháp Service Main");
            qtService.addQuestionType(qtGrammar);
            qtGrammar = qtService.getQuestionTypeById(qtGrammar.getQuestionTypeID());
        }
        
        QuestionType qtKanji = qtService.getAllQuestionTypes().stream().filter(qt -> qt.getTypeName().contains("Kanji")).findFirst().orElse(null);
        if (qtKanji == null) {
            qtKanji = new QuestionType("Kanji Service Main");
            qtService.addQuestionType(qtKanji);
            qtKanji = qtService.getQuestionTypeById(qtKanji.getQuestionTypeID());
        }

        if (levelN5 == null || qtGrammar == null || qtKanji == null) {
            System.err.println("Không thể lấy/tạo đủ Level/QuestionType mẫu. Dừng test.");
            return;
        }
        System.out.println("--- Testing QuestionService ---");
        // ... (các test add, getById, update, delete giữ nguyên) ...

        System.out.println("\nTesting getQuestionsByCriteria (Level N5, Types: Ngữ pháp, Kanji)...");
        List<Long> typeIdsToFilter = new ArrayList<>();
        typeIdsToFilter.add(qtGrammar.getQuestionTypeID());
        typeIdsToFilter.add(qtKanji.getQuestionTypeID());
        
        // Gọi phương thức mới
        List<Question> filteredQuestions = questionService.getQuestionsByCriteria(levelN5.getLevelID(), typeIdsToFilter);
        System.out.println("Số câu hỏi N5 - Ngữ pháp hoặc Kanji: " + filteredQuestions.size());
         for (Question q : filteredQuestions) {
            System.out.println("ID: " + q.getQuestionID() + ", Text: " + q.getQuestionText().substring(0, Math.min(q.getQuestionText().length(), 30)) + "..., Type: " + q.getQuestionType().getTypeName());
        }

        System.out.println("\nTesting getQuestionsByCriteria (Level N5, chỉ Type Ngữ pháp - dùng phương thức cũ)...");
        // Gọi phương thức cũ (overloaded)
        List<Question> filteredGrammarOnly = questionService.getQuestionsByCriteria(levelN5.getLevelID(), qtGrammar.getQuestionTypeID());
        System.out.println("Số câu hỏi N5 - Chỉ Ngữ pháp: " + filteredGrammarOnly.size());
         for (Question q : filteredGrammarOnly) {
            System.out.println("ID: " + q.getQuestionID() + ", Text: " + q.getQuestionText().substring(0, Math.min(q.getQuestionText().length(), 30)) + "..., Type: " + q.getQuestionType().getTypeName());
        }
        
        System.out.println("\nTesting getQuestionsByCriteria (Không lọc Level, chỉ Type Kanji - dùng phương thức mới)...");
        List<Question> filteredKanjiOnlyNoLevel = questionService.getQuestionsByCriteria(0, Arrays.asList(qtKanji.getQuestionTypeID()));
        System.out.println("Số câu hỏi - Chỉ Kanji (mọi level): " + filteredKanjiOnlyNoLevel.size());
         for (Question q : filteredKanjiOnlyNoLevel) {
            System.out.println("ID: " + q.getQuestionID() + ", Text: " + q.getQuestionText().substring(0, Math.min(q.getQuestionText().length(), 30)) + "..., Level: " + q.getLevel().getLevelName());
        }

        System.out.println("\n--- Kết thúc Test QuestionService ---");
    }
}

package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.QuestionType;
import com.exammanager.nganhangdethi.model.ExamQuestionDetail;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.service.ExamService;
import com.exammanager.nganhangdethi.service.LevelService;
import com.exammanager.nganhangdethi.service.QuestionTypeService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
// import java.text.SimpleDateFormat; // Không dùng trực tiếp ở đây nữa

public class ExamEditFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Exam currentExam;
    private ExamService examService;
    private LevelService levelService;
    // QuestionTypeService có thể không cần trực tiếp nếu chỉ sửa Level, Name, Duration
    private QuestionTypeService questionTypeService;
    private ExamManagementPanel parentPanel;

    private JTextField examNameField;
    private JComboBox<Level> levelComboBox;
    private JSpinner durationSpinner; // <<< THÊM JSpinner CHO THỜI GIAN LÀM BÀI
    
    private JList<ExamQuestionWrapper> questionsListJList;
    private DefaultListModel<ExamQuestionWrapper> questionsListModel;
    private JButton removeQuestionButton;
    private JButton addQuestionFromBankButton;

    private JButton saveButton;
    private JButton cancelButton;

    private List<ExamQuestionDetail> editableExamQuestionDetails;
    private Font unicodeGuiFont; // Giả sử font này được truyền từ panel cha hoặc load ở đây


    public ExamEditFormPanel(ExamManagementPanel parentPanel, ExamService examService, LevelService levelService, QuestionTypeService questionTypeService) {
        this.parentPanel = parentPanel;
        this.examService = examService;
        this.levelService = levelService;
        this.questionTypeService = questionTypeService; // Cần cho việc load QuestionType ComboBox (nếu có)
        this.editableExamQuestionDetails = new ArrayList<>();
        
        // Load font (nếu chưa được truyền vào)
        // Tạm thời giả định unicodeGuiFont sẽ được set từ panel cha hoặc một lớp tiện ích
        // Hoặc bạn có thể thêm logic load font tương tự như các panel khác ở đây
        // Ví dụ:
        // loadCustomFont(); 

        initComponents();
        loadDropdownData();
    }
    
    // Ví dụ cách load font nếu cần (tương tự các panel khác)
    /*
    private void loadCustomFont() {
        try (InputStream fontStream = ExamEditFormPanel.class.getResourceAsStream("/font/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("Không tìm thấy file font: /font/NotoSansJP-Regular.ttf trong ExamEditFormPanel. Sử dụng font mặc định.");
                unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
                return;
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(13f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            unicodeGuiFont = customFont;
        } catch (IOException | FontFormatException e) {
            System.err.println("Lỗi khi tải font NotoSansJP-Regular: " + e.getMessage());
            unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
        }
    }
    */
    
    private void initComponents() {
        // Sử dụng font đã được load (nếu có)
        if (parentPanel != null && parentPanel	 != null) { // Lấy font từ parent nếu có
            this.unicodeGuiFont = parentPanel.unicodeGuiFont;
        } else { // Fallback nếu không lấy được từ parent
            this.unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
        }


        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Chỉnh sửa Thông tin Đề thi"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên Đề thi
        gbc.gridx = 0; gbc.gridy = 0;
        formFieldsPanel.add(new JLabel("Tên Đề thi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 2;
        examNameField = new JTextField(30);
        if (unicodeGuiFont != null) examNameField.setFont(unicodeGuiFont);
        formFieldsPanel.add(examNameField, gbc);
        gbc.gridwidth = 1; gbc.weightx = 0;

        // Cấp độ
        gbc.gridx = 0; gbc.gridy = 1;
        formFieldsPanel.add(new JLabel("Cấp độ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        levelComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) levelComboBox.setFont(unicodeGuiFont);
        formFieldsPanel.add(levelComboBox, gbc);
        gbc.gridwidth = 1; gbc.weightx = 0;

        // Thời gian làm bài (phút) <<< THÊM MỚI
        gbc.gridx = 0; gbc.gridy = 2;
        formFieldsPanel.add(new JLabel("Thời gian làm bài (phút):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 2;
        SpinnerModel durationModel = new SpinnerNumberModel(60, 5, 300, 5); // Từ 5 đến 300 phút, bước nhảy 5
        durationSpinner = new JSpinner(durationModel);
        if (unicodeGuiFont != null) {
            JComponent editor = durationSpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().setFont(unicodeGuiFont);
            }
        }
        formFieldsPanel.add(durationSpinner, gbc);
        gbc.gridwidth = 1; gbc.weightx = 0;


        // Khu vực quản lý danh sách câu hỏi (bắt đầu từ gridy = 3)
        gbc.gridx = 0; gbc.gridy = 3; 
        gbc.gridwidth = 3; 
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; 
        
        JPanel questionsManagementSectionPanel = new JPanel(new BorderLayout(5,5));
        questionsManagementSectionPanel.setBorder(BorderFactory.createTitledBorder("Câu hỏi trong Đề thi"));

        questionsListModel = new DefaultListModel<>();
        questionsListJList = new JList<>(questionsListModel);
        questionsListJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (unicodeGuiFont != null) questionsListJList.setFont(unicodeGuiFont); // Đặt font cho JList
        JScrollPane questionsScrollPane = new JScrollPane(questionsListJList);
        questionsManagementSectionPanel.add(questionsScrollPane, BorderLayout.CENTER);

        JPanel questionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removeQuestionButton = new JButton("Xóa Câu hỏi Khỏi Đề");
        addQuestionFromBankButton = new JButton("Thêm Câu hỏi từ Ngân hàng...");
        
        questionButtonsPanel.add(removeQuestionButton);
        questionButtonsPanel.add(addQuestionFromBankButton);
        questionsManagementSectionPanel.add(questionButtonsPanel, BorderLayout.SOUTH);
        
        formFieldsPanel.add(questionsManagementSectionPanel, gbc);
        gbc.gridwidth = 1; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        add(formFieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu thay đổi");
        cancelButton = new JButton("Hủy bỏ");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        removeQuestionButton.addActionListener(e -> removeSelectedQuestionFromList());
        addQuestionFromBankButton.addActionListener(e -> openSelectQuestionsDialog());
        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> cancelEdit());
    }

    private void loadDropdownData() {
        List<Level> levels = levelService.getAllLevels();
        levelComboBox.removeAllItems();
        if (levels != null) {
            for (Level level : levels) {
                levelComboBox.addItem(level);
            }
        }
        // Không còn load QuestionType cho ComboBox của Exam nữa
    }

    public void loadExamData(Exam exam) {
        this.currentExam = exam; 
        this.editableExamQuestionDetails.clear(); 
        questionsListModel.clear();

        if (exam != null) {
            examNameField.setText(exam.getExamName());

            if (exam.getLevel() != null) {
                levelComboBox.setSelectedItem(exam.getLevel());
            } else {
                levelComboBox.setSelectedIndex(-1);
            }

            // Load DurationMinutes
            if (exam.getDurationMinutes() != null) {
                durationSpinner.setValue(exam.getDurationMinutes());
            } else {
                durationSpinner.setValue(60); // Giá trị mặc định nếu null
            }

            if (exam.getExamQuestions() != null) {
                for (ExamQuestionDetail eqd : exam.getExamQuestions()) {
                    Question originalQuestion = eqd.getQuestion();
                    if (originalQuestion != null) {
                        Question questionCopy = new Question();
                        questionCopy.setQuestionID(originalQuestion.getQuestionID());
                        questionCopy.setQuestionText(originalQuestion.getQuestionText());
                        questionCopy.setLevel(originalQuestion.getLevel());
                        questionCopy.setQuestionType(originalQuestion.getQuestionType());
                        ExamQuestionDetail newEqd = new ExamQuestionDetail(questionCopy, eqd.getQuestionOrder());
                        this.editableExamQuestionDetails.add(newEqd);
                    }
                }
                this.editableExamQuestionDetails.sort((eqd1, eqd2) -> Integer.compare(eqd1.getQuestionOrder(), eqd2.getQuestionOrder()));
                refreshQuestionListJList();
            }
        } else {
            examNameField.setText("");
            levelComboBox.setSelectedIndex(-1);
            durationSpinner.setValue(60); // Reset về mặc định
            // questionsListModel.clear(); // Đã clear ở trên
        }
    }
    
    private void removeSelectedQuestionFromList() {
        int selectedIndex = questionsListJList.getSelectedIndex();
        if (selectedIndex != -1) {
            ExamQuestionWrapper selectedWrapper = questionsListModel.getElementAt(selectedIndex);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa câu hỏi:\n'" + selectedWrapper.toString() + "'\nkhỏi đề thi không?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                editableExamQuestionDetails.remove(selectedWrapper.getExamQuestionDetail()); 
                for (int i = 0; i < editableExamQuestionDetails.size(); i++) {
                    editableExamQuestionDetails.get(i).setQuestionOrder(i + 1);
                }
                refreshQuestionListJList(); 
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshQuestionListJList() {
        questionsListModel.clear();
        editableExamQuestionDetails.sort((eqd1, eqd2) -> Integer.compare(eqd1.getQuestionOrder(), eqd2.getQuestionOrder()));
        for (ExamQuestionDetail eqd : editableExamQuestionDetails) {
            questionsListModel.addElement(new ExamQuestionWrapper(eqd));
        }
    }
    
    private void openSelectQuestionsDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SelectQuestionsDialog dialog = new SelectQuestionsDialog(owner); 
        dialog.setVisible(true); 

        List<Question> newlySelectedQuestions = dialog.getSelectedQuestions();
        if (newlySelectedQuestions != null && !newlySelectedQuestions.isEmpty()) {
            int currentOrder = editableExamQuestionDetails.size();
            for (Question q : newlySelectedQuestions) {
                boolean alreadyExists = editableExamQuestionDetails.stream()
                                        .anyMatch(eqd -> eqd.getQuestion().getQuestionID() == q.getQuestionID());
                if (!alreadyExists) {
                    currentOrder++;
                    ExamQuestionDetail newEqd = new ExamQuestionDetail(q, currentOrder);
                    editableExamQuestionDetails.add(newEqd);
                } else {
                    System.out.println("Câu hỏi ID " + q.getQuestionID() + " đã có trong danh sách chỉnh sửa của đề thi.");
                }
            }
            refreshQuestionListJList(); 
            JOptionPane.showMessageDialog(this, "Đã thêm " + newlySelectedQuestions.size() + " câu hỏi vào danh sách chỉnh sửa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("Không có câu hỏi nào được chọn từ dialog.");
        }
    }

    private void saveChanges() {
        if (currentExam == null) {
             JOptionPane.showMessageDialog(this, "Không có đề thi nào được tải để lưu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newName = examNameField.getText().trim();
        Level selectedLevel = (Level) levelComboBox.getSelectedItem();
        Integer duration = (Integer) durationSpinner.getValue(); // << LẤY GIÁ TRỊ THỜI GIAN

        if (newName.isEmpty() || selectedLevel == null) {
            JOptionPane.showMessageDialog(this, "Tên đề thi và Cấp độ không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (duration != null && duration <= 0) { // Cho phép duration là null, nhưng nếu có giá trị thì phải > 0
            JOptionPane.showMessageDialog(this, "Thời gian làm bài phải là số dương (hoặc để trống nếu không xác định).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Exam examToUpdate = new Exam();
        examToUpdate.setExamID(currentExam.getExamID()); 
        examToUpdate.setExamName(newName);
        examToUpdate.setLevel(selectedLevel);
        examToUpdate.setDurationMinutes(duration); // << GÁN THỜI GIAN
        examToUpdate.setGeneratedAt(currentExam.getGeneratedAt()); 

        for(int i=0; i < editableExamQuestionDetails.size(); i++){
            editableExamQuestionDetails.get(i).setQuestionOrder(i + 1);
        }
        examToUpdate.setExamQuestions(new ArrayList<>(this.editableExamQuestionDetails)); 

        try {
            boolean success = examService.updateExamWithQuestions(examToUpdate); 

            if (success) { 
                 JOptionPane.showMessageDialog(this, "Lưu thay đổi đề thi thành công!", 
                                                     "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                 parentPanel.loadExams(); 
                 parentPanel.switchToBottomPanel("VIEW_DETAILS_CARD"); 
                 Exam updatedExamFromDB = examService.getExamById(currentExam.getExamID());
                 if (updatedExamFromDB != null) {
                    parentPanel.displayExamDetails(updatedExamFromDB);
                 } else {
                    parentPanel.displayExamDetails(null); 
                 }
            } else {
                 JOptionPane.showMessageDialog(this, "Lưu thay đổi thất bại. Vui lòng kiểm tra lại thông tin hoặc log lỗi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu đầu vào: " + ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu thay đổi đề thi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void cancelEdit() {
        parentPanel.switchToBottomPanel("VIEW_DETAILS_CARD");
        if (parentPanel.selectedExamForAction != null) {
            parentPanel.displayExamDetails(parentPanel.selectedExamForAction);
        } else if (currentExam != null) { 
             parentPanel.displayExamDetails(examService.getExamById(currentExam.getExamID()));
        } else {
            parentPanel.displayExamDetails(null);
        }
    }

    private static class ExamQuestionWrapper {
        private ExamQuestionDetail examQuestionDetail;
        public ExamQuestionWrapper(ExamQuestionDetail eqd) { this.examQuestionDetail = eqd; }
        public ExamQuestionDetail getExamQuestionDetail() { return examQuestionDetail; }
        @Override
        public String toString() {
            if (examQuestionDetail == null || examQuestionDetail.getQuestion() == null) {
                return "(Câu hỏi không hợp lệ)";
            }
            Question q = examQuestionDetail.getQuestion();
            String qText = q.getQuestionText() != null ? q.getQuestionText() : "";
            String questionTypeStr = (q.getQuestionType() != null) ? " [" + q.getQuestionType().getTypeName() + "]" : "";
            return examQuestionDetail.getQuestionOrder() + ". (ID:" + q.getQuestionID() + ")" + questionTypeStr + " " +
                   (qText.length() > 50 ? qText.substring(0, 50) + "..." : qText);
        }
    }
}

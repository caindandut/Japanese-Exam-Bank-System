package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.service.ExamService;
import com.exammanager.nganhangdethi.service.LevelService;
import com.exammanager.nganhangdethi.service.QuestionTypeService;
import com.exammanager.nganhangdethi.model.ExamQuestionDetail;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.Choice;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.awt.FontFormatException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExamManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ExamService examService;
    private LevelService levelService;
    private QuestionTypeService questionTypeService;

    private JTable examTable;
    private DefaultTableModel tableModel;

    private JTextField filterExamIdField;
    private JTextField filterExamNameField;
    private JComboBox<Level> filterLevelComboBox;
    private JTextField filterGeneratedDateField;
    private JButton filterButton;
    private JButton clearFilterButton;

    private JPanel bottomPanelContainer;
    private CardLayout bottomCardLayout;

    private JTextArea examDetailArea;
    private ExamEditFormPanel examEditForm;

    private JButton deleteButton;
    private JButton refreshButton;
    private JButton editButton;

    protected Exam selectedExamForAction = null;
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected Font unicodeGuiFont; // << ĐỔI TỪ private THÀNH protected HOẶC THÊM GETTER

    public ExamManagementPanel() {
        this.examService = new ExamService();
        this.levelService = new LevelService();
        this.questionTypeService = new QuestionTypeService();

        loadCustomFont();
        initComponents();
        loadFilterDropdownData();
        loadExams();
    }

    // <<< THÊM PHƯƠNG THỨC GETTER NÀY >>>
    public Font getUnicodeGuiFont() {
        return unicodeGuiFont;
    }

    private void loadCustomFont() {
        try (InputStream fontStream = ExamManagementPanel.class.getResourceAsStream("/font/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("Không tìm thấy file font: /font/NotoSansJP-Regular.ttf trong ExamManagementPanel. Sử dụng font mặc định.");
                unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
                return;
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(13f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            unicodeGuiFont = customFont;
            System.out.println("Đã tải và đăng ký thành công font: NotoSansJP-Regular cho ExamManagementPanel");
        } catch (IOException | FontFormatException e) {
            System.err.println("Lỗi khi tải font NotoSansJP-Regular trong ExamManagementPanel: " + e.getMessage());
            e.printStackTrace();
            unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel Lọc ---
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm / Lọc Đề thi"));
        GridBagConstraints gbcFilter = new GridBagConstraints();
        gbcFilter.insets = new Insets(5, 5, 5, 5);
        gbcFilter.anchor = GridBagConstraints.WEST;

        gbcFilter.gridx = 0; gbcFilter.gridy = 0;
        filterPanel.add(new JLabel("ID Đề thi:"), gbcFilter);
        gbcFilter.gridx = 1; gbcFilter.gridy = 0; gbcFilter.fill = GridBagConstraints.HORIZONTAL; gbcFilter.weightx = 0.25;
        filterExamIdField = new JTextField(10);
        if (unicodeGuiFont != null) filterExamIdField.setFont(unicodeGuiFont);
        filterPanel.add(filterExamIdField, gbcFilter);

        gbcFilter.gridx = 2; gbcFilter.gridy = 0; gbcFilter.fill = GridBagConstraints.NONE; gbcFilter.weightx = 0;  gbcFilter.insets = new Insets(5, 15, 5, 5);
        filterPanel.add(new JLabel("Tên Đề thi:"), gbcFilter);
        gbcFilter.gridx = 3; gbcFilter.gridy = 0; gbcFilter.fill = GridBagConstraints.HORIZONTAL; gbcFilter.weightx = 0.75; gbcFilter.insets = new Insets(5, 5, 5, 5);
        filterExamNameField = new JTextField(20);
        if (unicodeGuiFont != null) filterExamNameField.setFont(unicodeGuiFont);
        filterPanel.add(filterExamNameField, gbcFilter);

        gbcFilter.gridx = 0; gbcFilter.gridy = 1; gbcFilter.fill = GridBagConstraints.NONE; gbcFilter.weightx = 0;
        filterPanel.add(new JLabel("Cấp độ:"), gbcFilter);
        gbcFilter.gridx = 1; gbcFilter.gridy = 1; gbcFilter.fill = GridBagConstraints.HORIZONTAL; gbcFilter.weightx = 0.25;
        filterLevelComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) filterLevelComboBox.setFont(unicodeGuiFont);
        filterPanel.add(filterLevelComboBox, gbcFilter);

        gbcFilter.gridx = 2; gbcFilter.gridy = 1; gbcFilter.fill = GridBagConstraints.NONE; gbcFilter.weightx = 0; gbcFilter.insets = new Insets(5, 15, 5, 5);
        filterPanel.add(new JLabel("Ngày tạo (dd/MM/yyyy):"), gbcFilter);
        gbcFilter.gridx = 3; gbcFilter.gridy = 1; gbcFilter.fill = GridBagConstraints.HORIZONTAL; gbcFilter.weightx = 0.25; gbcFilter.insets = new Insets(5, 5, 5, 5);
        filterGeneratedDateField = new JTextField(10);
        if (unicodeGuiFont != null) filterGeneratedDateField.setFont(unicodeGuiFont);
        filterPanel.add(filterGeneratedDateField, gbcFilter);
        gbcFilter.weightx = 0; 

        gbcFilter.gridx = 0; gbcFilter.gridy = 2; gbcFilter.gridwidth = 4;
        gbcFilter.anchor = GridBagConstraints.CENTER; gbcFilter.fill = GridBagConstraints.NONE;
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filterButton = new JButton("Tìm/Lọc");
        clearFilterButton = new JButton("Bỏ lọc");
        filterButtonPanel.add(filterButton);
        filterButtonPanel.add(clearFilterButton);
        filterPanel.add(filterButtonPanel, gbcFilter);

        JPanel mainContentPanel = new JPanel(new BorderLayout(10,10));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách Đề thi"));
        String[] columnNames = {"ID Đề thi", "Tên Đề thi", "Cấp độ", "Thời gian (phút)", "Ngày tạo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        examTable = new JTable(tableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (unicodeGuiFont != null) {
            examTable.setFont(unicodeGuiFont); 
            examTable.getTableHeader().setFont(unicodeGuiFont.deriveFont(Font.BOLD, 14f));
        } else { 
            examTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        }
        examTable.setRowHeight(25);
        TableColumnModel columnModel = examTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(300);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(150);

        examTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && examTable.getSelectedRow() != -1) {
                int selectedRow = examTable.getSelectedRow();
                long examId = (long) tableModel.getValueAt(selectedRow, 0);
                selectedExamForAction = examService.getExamById(examId); 

                if (selectedExamForAction != null) {
                    deleteButton.setEnabled(true);
                    editButton.setEnabled(true);
                    displayExamDetails(selectedExamForAction); 
                    switchToBottomPanel("VIEW_DETAILS_CARD"); 
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể tải chi tiết đề thi. Đề thi có thể đã bị xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    clearSelectionAndDetails();
                    loadExams(); 
                }
            } else {
                if (examTable.getSelectedRow() == -1) {
                    clearSelectionAndDetails();
                }
            }
        });
        JScrollPane scrollPaneTable = new JScrollPane(examTable);
        tablePanel.add(scrollPaneTable, BorderLayout.CENTER);

        bottomCardLayout = new CardLayout();
        bottomPanelContainer = new JPanel(bottomCardLayout);

        JPanel detailViewPanel = new JPanel(new BorderLayout());
        detailViewPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết Đề thi"));
        examDetailArea = new JTextArea(15, 40);
        examDetailArea.setEditable(false);
        if (unicodeGuiFont != null) examDetailArea.setFont(unicodeGuiFont.deriveFont(14f));
        else examDetailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        examDetailArea.setLineWrap(true);
        examDetailArea.setWrapStyleWord(true);
        JScrollPane detailScrollPane = new JScrollPane(examDetailArea);
        detailViewPanel.add(detailScrollPane, BorderLayout.CENTER);
        bottomPanelContainer.add(detailViewPanel, "VIEW_DETAILS_CARD");

        try {
            examEditForm = new ExamEditFormPanel(this, examService, levelService, questionTypeService);
            bottomPanelContainer.add(examEditForm, "EDIT_EXAM_CARD");
        } catch (Exception e) {
             JPanel errorPanel = new JPanel(new BorderLayout());
             errorPanel.add(new JLabel("Lỗi khi tải form sửa đề thi: " + e.getMessage(), SwingConstants.CENTER));
             bottomPanelContainer.add(errorPanel, "EDIT_EXAM_CARD"); 
             System.err.println("Lỗi khởi tạo ExamEditFormPanel: " + e.getMessage());
             e.printStackTrace();
        }

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        editButton = new JButton("Sửa Đề thi");
        deleteButton = new JButton("Xóa Đề thi");
        refreshButton = new JButton("Làm mới Toàn bộ");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        actionButtonPanel.add(editButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(refreshButton);

        JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, bottomPanelContainer);
        topSplitPane.setResizeWeight(0.5); 

        mainContentPanel.add(topSplitPane, BorderLayout.CENTER);
        mainContentPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        add(filterPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        
        switchToBottomPanel("VIEW_DETAILS_CARD"); 

        filterButton.addActionListener(e -> searchExams());
        clearFilterButton.addActionListener(e -> clearFiltersAndLoadAll());
        editButton.addActionListener(e -> openEditMode()); 
        deleteButton.addActionListener(e -> deleteExam());
        refreshButton.addActionListener(e -> clearFiltersAndLoadAll());
    }
    
    private void loadFilterDropdownData() {
        filterLevelComboBox.addItem(new Level(0, "Tất cả Cấp độ"));
        List<Level> levels = levelService.getAllLevels();
        if (levels != null) {
            for (Level level : levels) {
                filterLevelComboBox.addItem(level);
            }
        }
    }
    
    protected void loadExams() { 
        populateTable(examService.getAllExams());
        clearSelectionAndDetails();
    }

    private void searchExams() {
        long examId = 0;
        String idText = filterExamIdField.getText().trim();
        if (!idText.isEmpty()) {
            try {
                examId = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID Đề thi không hợp lệ. Vui lòng nhập số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                filterExamIdField.requestFocusInWindow();
                return;
            }
        }

        String examName = filterExamNameField.getText().trim();
        Level selectedFilterLevel = (Level) filterLevelComboBox.getSelectedItem();
        long levelIdFilter = (selectedFilterLevel != null && selectedFilterLevel.getLevelID() != 0) ? selectedFilterLevel.getLevelID() : 0;
        
        Date generatedDate = null;
        String dateText = filterGeneratedDateField.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                inputDateFormat.setLenient(false); 
                generatedDate = inputDateFormat.parse(dateText);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Định dạng Ngày tạo không hợp lệ. Vui lòng nhập theo dd/MM/yyyy.", "Lỗi Ngày", JOptionPane.ERROR_MESSAGE);
                filterGeneratedDateField.requestFocusInWindow();
                return;
            }
        }
        
        List<Exam> results = examService.searchExams(examId, examName, levelIdFilter, generatedDate);
        populateTable(results);
        clearSelectionAndDetails(); 
    }

    private void clearFiltersAndLoadAll() {
        filterExamIdField.setText("");
        filterExamNameField.setText("");
        if (filterLevelComboBox.getItemCount() > 0) filterLevelComboBox.setSelectedIndex(0);
        filterGeneratedDateField.setText("");
        loadExams(); 
    }

    private void populateTable(List<Exam> exams) {
        tableModel.setRowCount(0);
        if (exams != null) {
            for (Exam exam : exams) {
                tableModel.addRow(new Object[]{
                        exam.getExamID(),
                        exam.getExamName(),
                        exam.getLevel() != null ? exam.getLevel().getLevelName() : "N/A",
                        exam.getDurationMinutes() != null ? exam.getDurationMinutes() : "N/A", 
                        exam.getGeneratedAt() != null ? displayDateFormat.format(exam.getGeneratedAt()) : "N/A"
                });
            }
        }
    }
    
    protected void switchToBottomPanel(String cardName) { 
        bottomCardLayout.show(bottomPanelContainer, cardName);
    }

    private void openEditMode() {
        if (selectedExamForAction == null || selectedExamForAction.getExamID() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (examEditForm != null) { 
            examEditForm.loadExamData(selectedExamForAction); 
            switchToBottomPanel("EDIT_EXAM_CARD");
        } else {
            System.err.println("ExamEditFormPanel chưa được khởi tạo đúng cách hoặc bị lỗi.");
            JOptionPane.showMessageDialog(this, "Không thể mở form chỉnh sửa do lỗi khởi tạo panel.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    protected void displayExamDetails(Exam examToDisplay) { 
        if (examToDisplay == null) {
            examDetailArea.setText("Vui lòng chọn một đề thi để xem chi tiết.");
            return;
        }
        StringBuilder details = new StringBuilder();
        details.append("ID Đề thi: \t").append(examToDisplay.getExamID()).append("\n");
        details.append("Tên Đề thi: \t").append(examToDisplay.getExamName()).append("\n");
        details.append("Cấp độ: \t").append(examToDisplay.getLevel().getLevelName()).append("\n");
        if (examToDisplay.getDurationMinutes() != null && examToDisplay.getDurationMinutes() > 0) {
            details.append("Thời gian làm bài: \t").append(examToDisplay.getDurationMinutes()).append(" phút\n");
        }
        details.append("Ngày tạo: \t").append(displayDateFormat.format(examToDisplay.getGeneratedAt())).append("\n");
        details.append("----------------------------------------------------------\n");
        details.append("DANH SÁCH CÂU HỎI:\n");
        details.append("----------------------------------------------------------\n");

        if (examToDisplay.getExamQuestions() != null && !examToDisplay.getExamQuestions().isEmpty()) {
            for (ExamQuestionDetail eqd : examToDisplay.getExamQuestions()) {
                Question q = eqd.getQuestion();
                if (q == null) continue;

                details.append("\n").append(eqd.getQuestionOrder()).append(". (ID Câu hỏi: ").append(q.getQuestionID()).append(")\n");
                details.append("   Nội dung: ").append(q.getQuestionText()).append("\n");
                if (q.getAudioPath() != null && !q.getAudioPath().isEmpty()) {
                    details.append("   Audio: ").append(q.getAudioPath()).append("\n");
                }
                if (q.getQuestionType() != null) {
                    details.append("   Loại câu hỏi: ").append(q.getQuestionType().getTypeName()).append("\n");
                }

                details.append("   Lựa chọn:\n");
                if (q.getChoices() != null && !q.getChoices().isEmpty()) {
                    for (Choice choice : q.getChoices()) {
                        details.append("     ").append(choice.getChoiceText());
                        if (choice.isCorrect()) {
                            details.append(" (ĐÚNG)");
                        }
                        details.append("\n");
                    }
                } else {
                    details.append("     (Không có lựa chọn nào)\n");
                }
            }
        } else {
            details.append("  (Không có câu hỏi nào trong đề thi này)\n");
        }
        examDetailArea.setText(details.toString());
        examDetailArea.setCaretPosition(0);
    }
    
    private void deleteExam() {
        if (selectedExamForAction == null || selectedExamForAction.getExamID() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đề thi '" + selectedExamForAction.getExamName() + "' (ID: " + selectedExamForAction.getExamID() + ") không?",
                "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                if (examService.deleteExam(selectedExamForAction.getExamID())) {
                    JOptionPane.showMessageDialog(this, "Xóa đề thi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadExams();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa đề thi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa đề thi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void clearSelectionAndDetails() {
        examTable.clearSelection();
        selectedExamForAction = null;
        deleteButton.setEnabled(false);
        editButton.setEnabled(false); 
        if (examDetailArea != null) {
            examDetailArea.setText("");
        }
        switchToBottomPanel("VIEW_DETAILS_CARD"); 
    }
}

package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.QuestionType;
import com.exammanager.nganhangdethi.model.Choice;
import com.exammanager.nganhangdethi.service.QuestionService;
import com.exammanager.nganhangdethi.service.LevelService;
import com.exammanager.nganhangdethi.service.QuestionTypeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.InputStream; // Để load font
import java.io.IOException;  // Để xử lý exception
import java.awt.FontFormatException; // Để xử lý exception
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectQuestionsDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private QuestionService questionService;
    private LevelService levelService;
    private QuestionTypeService questionTypeService;

    private JTable availableQuestionsTable;
    private DefaultTableModel tableModel;
    private JButton okButton;
    private JButton cancelButton;
    private JComboBox<Level> filterLevelComboBox;
    private JComboBox<QuestionType> filterTypeComboBox;
    private JButton filterButton;
    private JTextArea questionDetailArea;

    private List<Question> allQuestionsCache;
    private List<Question> selectedQuestionsList;
    private Font unicodeGuiFont; // Font sẽ được load

    public SelectQuestionsDialog(Frame owner) {
        super(owner, "Chọn Câu hỏi từ Ngân hàng", true);
        this.questionService = new QuestionService();
        this.levelService = new LevelService();
        this.questionTypeService = new QuestionTypeService();
        this.selectedQuestionsList = new ArrayList<>();

        loadCustomFont(); // Load font trước khi initComponents

        initComponents();
        loadInitialData();
        
        // Kích thước ban đầu lớn hơn và đặt kích thước tối thiểu
        setSize(900, 750); // Tăng kích thước ban đầu
        setMinimumSize(new Dimension(700, 500)); // Đặt kích thước tối thiểu
        setLocationRelativeTo(owner);
    }

    private void loadCustomFont() {
        try (InputStream fontStream = SelectQuestionsDialog.class.getResourceAsStream("/font/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("Không tìm thấy file font: /font/NotoSansJP-Regular.ttf. Sử dụng font mặc định.");
                unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13); // Font dự phòng
                return;
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(13f); // Kích thước font cơ bản
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            unicodeGuiFont = customFont;
            System.out.println("Đã tải và đăng ký thành công font: NotoSansJP-Regular cho SelectQuestionsDialog");
        } catch (IOException | FontFormatException e) {
            System.err.println("Lỗi khi tải font NotoSansJP-Regular: " + e.getMessage());
            e.printStackTrace();
            unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13); // Fallback nếu lỗi
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // --- Panel Lọc ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Lọc theo Cấp độ:"));
        filterLevelComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) filterLevelComboBox.setFont(unicodeGuiFont);
        filterPanel.add(filterLevelComboBox);

        filterPanel.add(new JLabel("Loại Câu hỏi:"));
        filterTypeComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) filterTypeComboBox.setFont(unicodeGuiFont);
        filterPanel.add(filterTypeComboBox);

        filterButton = new JButton("Lọc");
        filterButton.addActionListener(e -> filterQuestions());
        filterPanel.add(filterButton);
        
        JButton clearFilterButton = new JButton("Bỏ lọc");
        clearFilterButton.addActionListener(e -> {
            filterLevelComboBox.setSelectedIndex(0); 
            filterTypeComboBox.setSelectedIndex(0);  
            filterQuestions();
        });
        filterPanel.add(clearFilterButton);

        // --- Panel Bảng Câu hỏi ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnNames = {"ID", "Nội dung Câu hỏi", "Cấp độ", "Loại"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        availableQuestionsTable = new JTable(tableModel);
        availableQuestionsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (unicodeGuiFont != null) {
            availableQuestionsTable.setFont(unicodeGuiFont); // Đặt font cho nội dung bảng
            availableQuestionsTable.getTableHeader().setFont(unicodeGuiFont.deriveFont(Font.BOLD)); // Font cho header
        }
        availableQuestionsTable.setRowHeight(25); // Tăng chiều cao dòng nếu cần cho font

        TableColumnModel columnModel = availableQuestionsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(450); // Có thể tăng thêm nếu cần
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(150);
        JScrollPane scrollPaneTable = new JScrollPane(availableQuestionsTable);
        tablePanel.add(scrollPaneTable, BorderLayout.CENTER);

        // --- Panel Chi tiết Câu hỏi ---
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết Câu hỏi được chọn"));
        questionDetailArea = new JTextArea(10, 40); // Tăng số dòng hiển thị ban đầu
        questionDetailArea.setEditable(false);
        if (unicodeGuiFont != null) questionDetailArea.setFont(unicodeGuiFont.deriveFont(14f)); // Tăng kích thước font chi tiết
        questionDetailArea.setLineWrap(true);
        questionDetailArea.setWrapStyleWord(true);
        JScrollPane detailScrollPane = new JScrollPane(questionDetailArea);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        // detailPanel.setPreferredSize(new Dimension(getWidth(), 250)); // Có thể không cần nếu JSplitPane xử lý tốt

        // --- JSplitPane để chia bảng và chi tiết ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, detailPanel);
        mainSplitPane.setResizeWeight(0.60); // Bảng chiếm 60% không gian ban đầu
        mainSplitPane.setOneTouchExpandable(true); // Cho phép thu gọn/mở rộng nhanh
        mainSplitPane.setContinuousLayout(true); // Cập nhật layout liên tục khi kéo


        // --- Panel Nút Bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("Chọn");
        cancelButton = new JButton("Hủy");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(filterPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        availableQuestionsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && availableQuestionsTable.getSelectedRow() != -1) {
                displaySelectedQuestionDetails();
            } else if (availableQuestionsTable.getSelectedRowCount() == 0) {
                questionDetailArea.setText("");
            }
        });
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
    }

    private void loadInitialData() {
        filterLevelComboBox.addItem(new Level(0, "Tất cả Cấp độ")); 
        List<Level> levels = levelService.getAllLevels();
        if (levels != null) {
            for (Level level : levels) {
                filterLevelComboBox.addItem(level);
            }
        }

        filterTypeComboBox.addItem(new QuestionType(0, "Tất cả Loại")); 
        List<QuestionType> types = questionTypeService.getAllQuestionTypes();
        if (types != null) {
            for (QuestionType type : types) {
                filterTypeComboBox.addItem(type);
            }
        }
        
        allQuestionsCache = questionService.getAllQuestions(); 
        if (allQuestionsCache == null) {
            allQuestionsCache = new ArrayList<>();
        }
        populateTable(allQuestionsCache);
    }

    private void filterQuestions() {
        Level selectedLevel = (Level) filterLevelComboBox.getSelectedItem();
        QuestionType selectedType = (QuestionType) filterTypeComboBox.getSelectedItem();

        long levelIdFilter = (selectedLevel != null && selectedLevel.getLevelID() != 0) ? selectedLevel.getLevelID() : 0;
        long typeIdFilter = (selectedType != null && selectedType.getQuestionTypeID() != 0) ? selectedType.getQuestionTypeID() : 0;

        List<Question> filteredList = allQuestionsCache.stream()
            .filter(q -> (levelIdFilter == 0 || (q.getLevel() != null && q.getLevel().getLevelID() == levelIdFilter)))
            .filter(q -> (typeIdFilter == 0 || (q.getQuestionType() != null && q.getQuestionType().getQuestionTypeID() == typeIdFilter)))
            .collect(Collectors.toList());
        
        populateTable(filteredList);
        questionDetailArea.setText("");
    }

    private void populateTable(List<Question> questions) {
        tableModel.setRowCount(0); 
        if (questions != null) {
            for (Question q : questions) {
                tableModel.addRow(new Object[]{
                        q.getQuestionID(),
                        q.getQuestionText(),
                        q.getLevel() != null ? q.getLevel().getLevelName() : "N/A",
                        q.getQuestionType() != null ? q.getQuestionType().getTypeName() : "N/A"
                });
            }
        }
    }

    private void displaySelectedQuestionDetails() {
        int selectedRowView = availableQuestionsTable.getSelectedRow();
        if (selectedRowView == -1) {
            questionDetailArea.setText("");
            return;
        }
        // Chuyển đổi chỉ số hàng của view sang chỉ số hàng của model (quan trọng nếu bảng có sắp xếp/lọc)
        int selectedRowModel = availableQuestionsTable.convertRowIndexToModel(selectedRowView);


        long questionId = (long) tableModel.getValueAt(selectedRowModel, 0);
        Question detailedQuestion = questionService.getQuestionById(questionId);

        if (detailedQuestion != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("ID Câu hỏi: ").append(detailedQuestion.getQuestionID()).append("\n");
            sb.append("Cấp độ: ").append(detailedQuestion.getLevel().getLevelName()).append("\n");
            sb.append("Loại: ").append(detailedQuestion.getQuestionType().getTypeName()).append("\n");
            sb.append("Nội dung:\n").append(detailedQuestion.getQuestionText()).append("\n\n");
            sb.append("Các lựa chọn:\n");
            if (detailedQuestion.getChoices() != null && !detailedQuestion.getChoices().isEmpty()) {
                char choiceLabel = 'A';
                for (Choice choice : detailedQuestion.getChoices()) {
                    sb.append("  ").append(choiceLabel).append(". ").append(choice.getChoiceText());
                    if (choice.isCorrect()) {
                        sb.append(" (ĐÚNG)");
                    }
                    sb.append("\n");
                    choiceLabel++;
                }
            } else {
                sb.append("  (Không có lựa chọn nào)\n");
            }
            if (detailedQuestion.getAudioPath() != null && !detailedQuestion.getAudioPath().isEmpty()) {
                sb.append("\nAudio: ").append(detailedQuestion.getAudioPath());
            }
            questionDetailArea.setText(sb.toString());
            questionDetailArea.setCaretPosition(0);
        } else {
            questionDetailArea.setText("Không thể tải chi tiết cho câu hỏi ID: " + questionId);
        }
    }


    private void onOK() {
        int[] selectedRowsView = availableQuestionsTable.getSelectedRows();
        selectedQuestionsList.clear();
        if (selectedRowsView.length > 0) {
            for (int rowIndexView : selectedRowsView) {
                int rowIndexModel = availableQuestionsTable.convertRowIndexToModel(rowIndexView);
                long questionId = (long) tableModel.getValueAt(rowIndexModel, 0);
                Question detailedQuestion = questionService.getQuestionById(questionId);
                if (detailedQuestion != null) {
                    selectedQuestionsList.add(detailedQuestion);
                }
            }
        }
        setVisible(false);
    }

    private void onCancel() {
        selectedQuestionsList.clear();
        setVisible(false);
    }

    public List<Question> getSelectedQuestions() {
        return selectedQuestionsList;
    }
}

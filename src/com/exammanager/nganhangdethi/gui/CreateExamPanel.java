package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.QuestionType;
import com.exammanager.nganhangdethi.service.ExamService;
import com.exammanager.nganhangdethi.service.LevelService;
import com.exammanager.nganhangdethi.service.QuestionTypeService;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.awt.FontFormatException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CreateExamPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ExamService examService;
    private LevelService levelService;
    private QuestionTypeService questionTypeService;
    private MainAppFrame parentFrame;

    private JTextField examNameField;
    private JComboBox<Level> levelComboBox;
    private JPanel questionTypesCheckBoxPanel;
    private JScrollPane typeScrollPane;
    private List<JCheckBox> questionTypeCheckBoxes;

    private JSpinner numberOfQuestionsSpinner;
    private JSpinner durationSpinner; // <<< THÊM JSpinner CHO THỜI GIAN LÀM BÀI
    private JCheckBox shuffleCheckBox;
    private JButton createExamButton;
    private JTextArea resultArea;
    private Font unicodeGuiFont;

    public CreateExamPanel(MainAppFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.examService = new ExamService();
        this.levelService = new LevelService();
        this.questionTypeService = new QuestionTypeService();
        this.questionTypeCheckBoxes = new ArrayList<>();

        loadCustomFont();

        initComponents();
        loadLevelsAndTypes();
    }

    private void loadCustomFont() {
        try (InputStream fontStream = CreateExamPanel.class.getResourceAsStream("/font/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("Không tìm thấy file font: /font/NotoSansJP-Regular.ttf trong CreateExamPanel. Sử dụng font mặc định.");
                unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
                return;
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(13f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            unicodeGuiFont = customFont;
            System.out.println("Đã tải và đăng ký thành công font: NotoSansJP-Regular cho CreateExamPanel");
        } catch (IOException | FontFormatException e) {
            System.err.println("Lỗi khi tải font NotoSansJP-Regular trong CreateExamPanel: " + e.getMessage());
            e.printStackTrace();
            unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputCriteriaPanel = new JPanel(new GridBagLayout());
        inputCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Tiêu chí Tạo Đề thi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Tên Đề thi
        gbc.gridx = 0; gbc.gridy = 0;
        inputCriteriaPanel.add(new JLabel("Tên Đề thi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        examNameField = new JTextField(25);
        if (unicodeGuiFont != null) examNameField.setFont(unicodeGuiFont);
        inputCriteriaPanel.add(examNameField, gbc);

        // Cấp độ
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputCriteriaPanel.add(new JLabel("Cấp độ (Chính):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        levelComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) levelComboBox.setFont(unicodeGuiFont);
        inputCriteriaPanel.add(levelComboBox, gbc);

        // Các Loại Câu hỏi
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        inputCriteriaPanel.add(new JLabel("Các Loại Câu hỏi:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3; 
        questionTypesCheckBoxPanel = new JPanel();
        questionTypesCheckBoxPanel.setLayout(new BoxLayout(questionTypesCheckBoxPanel, BoxLayout.Y_AXIS));
        
        typeScrollPane = new JScrollPane(questionTypesCheckBoxPanel);
        typeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        typeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        typeScrollPane.setPreferredSize(new Dimension(250, 100)); // Điều chỉnh chiều cao nếu cần
        typeScrollPane.setMinimumSize(new Dimension(150, 60));   
        typeScrollPane.setBorder(BorderFactory.createEtchedBorder());
        inputCriteriaPanel.add(typeScrollPane, gbc);
        gbc.weighty = 0; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        // Số lượng câu hỏi
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
        inputCriteriaPanel.add(new JLabel("Số lượng câu hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        SpinnerModel numQuestionsModel = new SpinnerNumberModel(10, 1, 200, 1); // Tăng max lên 200
        numberOfQuestionsSpinner = new JSpinner(numQuestionsModel);
        if (unicodeGuiFont != null) { 
            JComponent editor = numberOfQuestionsSpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().setFont(unicodeGuiFont);
            }
        }
        inputCriteriaPanel.add(numberOfQuestionsSpinner, gbc);

        // Thời gian làm bài (phút) <<< THÊM MỚI
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        inputCriteriaPanel.add(new JLabel("Thời gian làm bài (phút):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        SpinnerModel durationModel = new SpinnerNumberModel(60, 5, 300, 5); // Từ 5 đến 300 phút, bước nhảy 5
        durationSpinner = new JSpinner(durationModel);
        if (unicodeGuiFont != null) {
            JComponent editor = durationSpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().setFont(unicodeGuiFont);
            }
        }
        inputCriteriaPanel.add(durationSpinner, gbc);


        // Tùy chọn xáo trộn
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; // Đẩy lên 1 dòng
        shuffleCheckBox = new JCheckBox("Xáo trộn thứ tự câu hỏi", true);
        if (unicodeGuiFont != null) shuffleCheckBox.setFont(unicodeGuiFont);
        inputCriteriaPanel.add(shuffleCheckBox, gbc);
        gbc.gridwidth = 1;

        // Nút Tạo Đề thi
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; // Đẩy lên 1 dòng
        createExamButton = new JButton("Tạo Đề thi");
        createExamButton.setFont(new Font("Arial", Font.BOLD, 14));
        createExamButton.setPreferredSize(new Dimension(150, 40));
        inputCriteriaPanel.add(createExamButton, gbc);

        // Panel Kết quả
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Đề thi Vừa Tạo"));
        resultArea = new JTextArea(8, 40); // Giảm số dòng một chút
        resultArea.setEditable(false);
        if (unicodeGuiFont != null) {
            resultArea.setFont(unicodeGuiFont.deriveFont(14f));
        } else {
            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        }
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputCriteriaPanel, resultPanel);
        splitPane.setResizeWeight(0.70); // Tăng không gian cho panel nhập liệu tiêu chí
        add(splitPane, BorderLayout.CENTER);

        createExamButton.addActionListener(e -> createExam());
    }

    private void loadLevelsAndTypes() {
        List<Level> levels = levelService.getAllLevels();
        levelComboBox.removeAllItems();
        levelComboBox.addItem(null);
        if (levels != null) {
            for (Level level : levels) {
                levelComboBox.addItem(level);
            }
        }

        List<QuestionType> types = questionTypeService.getAllQuestionTypes();
        questionTypesCheckBoxPanel.removeAll();
        questionTypeCheckBoxes.clear();
        if (types != null) {
            for (QuestionType type : types) {
                JCheckBox checkBox = new JCheckBox(type.getTypeName());
                if (unicodeGuiFont != null) checkBox.setFont(unicodeGuiFont);
                checkBox.putClientProperty("questionTypeObject", type);
                questionTypeCheckBoxes.add(checkBox);
                questionTypesCheckBoxPanel.add(checkBox);
            }
        }
        questionTypesCheckBoxPanel.revalidate();
        questionTypesCheckBoxPanel.repaint();
        if (typeScrollPane != null) {
            typeScrollPane.revalidate();
            typeScrollPane.repaint();
        }
    }

    public static String toUrlFriendlyString(String str) {
        // ... (giữ nguyên)
        if (str == null || str.trim().isEmpty()) {
            return "file_khong_ten";
        }
        String nfdNormalizedString = str.toLowerCase();
        nfdNormalizedString = Normalizer.normalize(nfdNormalizedString, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        nfdNormalizedString = pattern.matcher(nfdNormalizedString).replaceAll("");
        nfdNormalizedString = nfdNormalizedString.replaceAll("đ", "d");
        nfdNormalizedString = nfdNormalizedString.replaceAll("\\s+", ""); 
        nfdNormalizedString = nfdNormalizedString.replaceAll("[^a-z0-9]", "");
        return nfdNormalizedString.isEmpty() ? "file_khong_ten_hop_le" : nfdNormalizedString;
    }

    private void resetInputCriteriaForm() {
        examNameField.setText("");
        if (levelComboBox.getItemCount() > 0) {
            levelComboBox.setSelectedIndex(0);
        } else {
            levelComboBox.setSelectedItem(null);
        }
        for (JCheckBox checkBox : questionTypeCheckBoxes) {
            checkBox.setSelected(false);
        }
        numberOfQuestionsSpinner.setValue(10);
        durationSpinner.setValue(60); // << ĐẶT LẠI GIÁ TRỊ MẶC ĐỊNH CHO THỜI GIAN
        shuffleCheckBox.setSelected(true);
        examNameField.requestFocusInWindow();
    }

    private void createExam() {
        String examName = examNameField.getText().trim();
        Level selectedLevel = (Level) levelComboBox.getSelectedItem();
        
        List<Long> selectedQuestionTypeIds = new ArrayList<>();
        for (JCheckBox checkBox : questionTypeCheckBoxes) {
            if (checkBox.isSelected()) {
                QuestionType qt = (QuestionType) checkBox.getClientProperty("questionTypeObject");
                if (qt != null) {
                    selectedQuestionTypeIds.add(qt.getQuestionTypeID());
                }
            }
        }

        int numberOfQuestions = (Integer) numberOfQuestionsSpinner.getValue();
        Integer duration = (Integer) durationSpinner.getValue(); // << LẤY GIÁ TRỊ THỜI GIAN
        boolean shuffle = shuffleCheckBox.isSelected();

        if (examName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên Đề thi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            examNameField.requestFocusInWindow();
            return;
        }
        if (selectedLevel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Cấp độ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            levelComboBox.requestFocusInWindow();
            return;
        }
        if (selectedQuestionTypeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một Loại Câu hỏi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (duration <= 0) { // Kiểm tra thời gian làm bài
            JOptionPane.showMessageDialog(this, "Thời gian làm bài phải là số dương.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            durationSpinner.requestFocusInWindow();
            return;
        }


        try {
            resultArea.setText("Đang tạo đề thi, vui lòng chờ...\n");
            Exam createdExam = examService.createExamFromBank(
                    examName,
                    selectedLevel.getLevelID(),
                    selectedQuestionTypeIds,
                    duration, // << TRUYỀN THỜI GIAN VÀO SERVICE
                    numberOfQuestions,
                    shuffle
            );

            if (createdExam != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("TẠO ĐỀ THI THÀNH CÔNG!\n");
                sb.append("---------------------------------\n");
                sb.append("ID Đề thi: ").append(createdExam.getExamID()).append("\n");
                sb.append("Tên Đề thi: ").append(createdExam.getExamName()).append("\n");
                sb.append("Cấp độ: ").append(createdExam.getLevel().getLevelName()).append("\n");
                if (createdExam.getDurationMinutes() != null) { // Hiển thị thời gian nếu có
                    sb.append("Thời gian làm bài: ").append(createdExam.getDurationMinutes()).append(" phút\n");
                }
                sb.append("Ngày tạo: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(createdExam.getGeneratedAt())).append("\n");
                sb.append("Số lượng câu hỏi: ").append(createdExam.getExamQuestions().size()).append("\n\n");
                sb.append("DANH SÁCH CÂU HỎI TRONG ĐỀ (Loại câu hỏi của từng câu):\n");
                for (com.exammanager.nganhangdethi.model.ExamQuestionDetail eqd : createdExam.getExamQuestions()) {
                    sb.append("  ").append(eqd.getQuestionOrder()).append(". (ID: ").append(eqd.getQuestion().getQuestionID()).append(") ");
                    String qText = eqd.getQuestion().getQuestionText();
                    sb.append(qText.substring(0, Math.min(qText.length(), 50)));
                    if (qText.length() > 50) sb.append("...");
                    sb.append(" [Loại: ").append(eqd.getQuestion().getQuestionType().getTypeName()).append("]\n");
                }
                resultArea.setText(sb.toString());
                JOptionPane.showMessageDialog(this, "Tạo đề thi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                if (parentFrame != null) {
                    parentFrame.refreshAllExamRelatedPanels();
                }
                resetInputCriteriaForm(); 

            } else {
                resultArea.setText("Tạo đề thi thất bại.\nCó thể không đủ câu hỏi trong ngân hàng phù hợp với tiêu chí của bạn.\nVui lòng kiểm tra lại hoặc thêm câu hỏi vào ngân hàng.");
                JOptionPane.showMessageDialog(this, "Tạo đề thi thất bại. Vui lòng xem chi tiết ở khu vực kết quả.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            resultArea.setText("Lỗi dữ liệu đầu vào: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            resultArea.setText("Đã xảy ra lỗi hệ thống khi tạo đề thi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

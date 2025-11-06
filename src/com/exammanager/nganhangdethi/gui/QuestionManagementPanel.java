package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Choice;
import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.model.Question;
import com.exammanager.nganhangdethi.model.QuestionType;
import com.exammanager.nganhangdethi.service.LevelService;
import com.exammanager.nganhangdethi.service.QuestionService;
import com.exammanager.nganhangdethi.service.QuestionTypeService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Imports cho việc phát audio
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class QuestionManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private QuestionService questionService;
    private LevelService levelService;
    private QuestionTypeService questionTypeService;

    private JTable questionTable;
    private DefaultTableModel tableModel;

    // Components cho Form Nhập liệu
    private JComboBox<Level> formLevelComboBox;
    private JComboBox<QuestionType> formQuestionTypeComboBox;
    private JTextArea questionTextArea;
    private JTextField audioPathField;
    private JButton browseAudioButton;
    private JButton clearAudioButton;
    private JButton playStopAudioButton;
    private JPanel choicesPanelContainer;
    private JScrollPane choicesScrollPane;
    private JPanel choicesOuterPanel;
    private JPanel formInputPanel;
    private List<ChoiceInputPanel> choiceInputPanels;

    // Components cho Panel Lọc
    private JTextField filterQuestionIdField;
    private JComboBox<Level> filterLevelComboBox;
    private JComboBox<QuestionType> filterQuestionTypeComboBox;
    private JButton filterButton;
    private JButton clearFilterButton;


    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearFormButton;
    private JButton refreshButton;
    private JButton addChoiceButton;

    private Question selectedQuestion = null;
    private static final int MAX_CHOICES = 5;
    private static final String AUDIO_STORAGE_DIR = "data" + File.separator + "audio";
    private Clip currentClip;
    private boolean isPlayingAudio = false;
    private Font unicodeGuiFont;

    public QuestionManagementPanel() {
        this.questionService = new QuestionService();
        this.levelService = new LevelService();
        this.questionTypeService = new QuestionTypeService();
        this.choiceInputPanels = new ArrayList<>();

        loadCustomFont();

        File audioDir = new File(AUDIO_STORAGE_DIR);
        if (!audioDir.exists()) {
            if (audioDir.mkdirs()) {
                System.out.println("Đã tạo thư mục lưu audio: " + audioDir.getAbsolutePath());
            } else {
                System.err.println("Không thể tạo thư mục lưu audio: " + audioDir.getAbsolutePath());
            }
        }

        initComponents();
        loadFilterDropdownData();
        loadFormDropdownData();
        loadQuestions();
    }

    private void loadCustomFont() {
        try (InputStream fontStream = QuestionManagementPanel.class.getResourceAsStream("/font/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("Không tìm thấy file font: /font/NotoSansJP-Regular.ttf. Sử dụng font mặc định.");
                unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
                return;
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(13f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            unicodeGuiFont = customFont;
            System.out.println("Đã tải và đăng ký thành công font: NotoSans-Regular");
        } catch (IOException | FontFormatException e) {
            System.err.println("Lỗi khi tải font NotoSans-Regular: " + e.getMessage());
            e.printStackTrace();
            unicodeGuiFont = new Font("SansSerif", Font.PLAIN, 13);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm / Lọc Câu hỏi"));
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.insets = new Insets(5, 5, 5, 5);
        filterGbc.anchor = GridBagConstraints.WEST;

        filterGbc.gridx = 0; filterGbc.gridy = 0;
        filterPanel.add(new JLabel("ID Câu hỏi:"), filterGbc);
        filterGbc.gridx = 1; filterGbc.gridy = 0; filterGbc.fill = GridBagConstraints.HORIZONTAL; filterGbc.weightx = 0.3;
        filterQuestionIdField = new JTextField(10);
        if (unicodeGuiFont != null) filterQuestionIdField.setFont(unicodeGuiFont);
        filterPanel.add(filterQuestionIdField, filterGbc);

        filterGbc.gridx = 2; filterGbc.gridy = 0; filterGbc.fill = GridBagConstraints.NONE; filterGbc.weightx = 0;  filterGbc.insets = new Insets(5, 15, 5, 5);
        filterPanel.add(new JLabel("Cấp độ:"), filterGbc);
        filterGbc.gridx = 3; filterGbc.gridy = 0; filterGbc.fill = GridBagConstraints.HORIZONTAL; filterGbc.weightx = 0.3;
        filterLevelComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) filterLevelComboBox.setFont(unicodeGuiFont);
        filterPanel.add(filterLevelComboBox, filterGbc);
        
        filterGbc.gridx = 0; filterGbc.gridy = 1; filterGbc.fill = GridBagConstraints.NONE; filterGbc.weightx = 0;  filterGbc.insets = new Insets(5, 5, 5, 5);
        filterPanel.add(new JLabel("Loại Câu hỏi:"), filterGbc);
        filterGbc.gridx = 1; filterGbc.gridy = 1; filterGbc.fill = GridBagConstraints.HORIZONTAL; filterGbc.weightx = 0.3;
        filterQuestionTypeComboBox = new JComboBox<>();
        if (unicodeGuiFont != null) filterQuestionTypeComboBox.setFont(unicodeGuiFont);
        filterPanel.add(filterQuestionTypeComboBox, filterGbc);

        filterGbc.gridx = 2; filterGbc.gridy = 1; filterGbc.fill = GridBagConstraints.NONE; filterGbc.weightx = 0;
        filterButton = new JButton("Tìm/Lọc");
        filterPanel.add(filterButton, filterGbc);

        filterGbc.gridx = 3; filterGbc.gridy = 1;
        clearFilterButton = new JButton("Bỏ lọc");
        filterPanel.add(clearFilterButton, filterGbc);
        filterGbc.weightx = 0.1;


        JPanel mainContentPanel = new JPanel(new BorderLayout(10,10));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách Câu hỏi"));
        String[] columnNames = {"ID", "Nội dung Câu hỏi", "Cấp độ", "Loại Câu hỏi", "Audio"};
        tableModel = new DefaultTableModel(columnNames, 0) { 
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        questionTable = new JTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); 
        questionTable.setFont(unicodeGuiFont); 
        questionTable.setRowHeight(25);
        
        TableColumnModel columnModelTable = questionTable.getColumnModel();
        columnModelTable.getColumn(0).setPreferredWidth(50);
        columnModelTable.getColumn(1).setPreferredWidth(400);
        columnModelTable.getColumn(2).setPreferredWidth(100);
        columnModelTable.getColumn(3).setPreferredWidth(150);
        columnModelTable.getColumn(4).setPreferredWidth(150);

        questionTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && questionTable.getSelectedRow() != -1) {
                populateFormWithSelectedQuestion();
            } else {
                 if (questionTable.getSelectedRow() == -1) {
                    clearForm();
                }
            }
        });
        JScrollPane scrollPaneTable = new JScrollPane(questionTable);
        tablePanel.add(scrollPaneTable, BorderLayout.CENTER);

        JPanel formAndButtonPanel = new JPanel(new BorderLayout(10,10));
        formInputPanel = new JPanel(new GridBagLayout()); 
        formInputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Câu hỏi"));
        GridBagConstraints gbcForm = new GridBagConstraints(); 
        gbcForm.insets = new Insets(5, 5, 5, 5);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.anchor = GridBagConstraints.WEST;

        gbcForm.gridx = 0; gbcForm.gridy = 0;
        formInputPanel.add(new JLabel("Cấp độ:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.gridy = 0; gbcForm.weightx = 1.0;
        formLevelComboBox = new JComboBox<>(); 
        if (unicodeGuiFont != null) formLevelComboBox.setFont(unicodeGuiFont);
        formInputPanel.add(formLevelComboBox, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 1; gbcForm.weightx = 0;
        formInputPanel.add(new JLabel("Loại câu hỏi:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.gridy = 1; gbcForm.weightx = 1.0;
        formQuestionTypeComboBox = new JComboBox<>(); 
        if (unicodeGuiFont != null) formQuestionTypeComboBox.setFont(unicodeGuiFont);
        formInputPanel.add(formQuestionTypeComboBox, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 2; gbcForm.weightx = 0; gbcForm.anchor = GridBagConstraints.NORTHWEST;
        formInputPanel.add(new JLabel("Nội dung:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.gridy = 2; gbcForm.weightx = 1.0; gbcForm.fill = GridBagConstraints.BOTH; gbcForm.weighty = 0.7;
        questionTextArea = new JTextArea(5, 20);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        if (unicodeGuiFont != null) questionTextArea.setFont(unicodeGuiFont);
        JScrollPane questionTextScrollPane = new JScrollPane(questionTextArea);
        formInputPanel.add(questionTextScrollPane, gbcForm);
        gbcForm.weighty = 0; gbcForm.fill = GridBagConstraints.HORIZONTAL;

        gbcForm.gridx = 0; gbcForm.gridy = 3; gbcForm.weightx = 0; gbcForm.anchor = GridBagConstraints.WEST;
        formInputPanel.add(new JLabel("File âm thanh:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.gridy = 3; gbcForm.weightx = 1.0;
        JPanel audioPanel = new JPanel(new BorderLayout(5,0));
        audioPathField = new JTextField(15);
        audioPathField.setEditable(false);
        if (unicodeGuiFont != null) audioPathField.setFont(unicodeGuiFont);
        
        browseAudioButton = new JButton("Chọn File...");
        clearAudioButton = new JButton("Xóa Audio");
        playStopAudioButton = new JButton("Phát");
        playStopAudioButton.setEnabled(false);
        playStopAudioButton.setMargin(new Insets(2,5,2,5));
        clearAudioButton.setMargin(new Insets(2,5,2,5));

        JPanel audioButtonSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        audioButtonSubPanel.add(browseAudioButton);
        audioButtonSubPanel.add(clearAudioButton);
        audioButtonSubPanel.add(playStopAudioButton);

        audioPanel.add(audioPathField, BorderLayout.CENTER);
        audioPanel.add(audioButtonSubPanel, BorderLayout.EAST);
        formInputPanel.add(audioPanel, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 4; gbcForm.gridwidth = 2; gbcForm.weightx = 1.0;
        gbcForm.fill = GridBagConstraints.BOTH;
        gbcForm.weighty = 0.3;
        choicesOuterPanel = new JPanel(new BorderLayout());
        choicesOuterPanel.setBorder(BorderFactory.createTitledBorder("Các Lựa chọn Trả lời"));
        
        choicesPanelContainer = new JPanel();
        choicesPanelContainer.setLayout(new BoxLayout(choicesPanelContainer, BoxLayout.Y_AXIS));
        
        choicesScrollPane = new JScrollPane(choicesPanelContainer);
        choicesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        choicesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        addChoiceButton = new JButton("Thêm Lựa chọn");
        addChoiceButton.addActionListener(e -> addChoiceInputPanel());

        choicesOuterPanel.add(choicesScrollPane, BorderLayout.CENTER);
        choicesOuterPanel.add(addChoiceButton, BorderLayout.SOUTH);
        formInputPanel.add(choicesOuterPanel, gbcForm);
        gbcForm.gridwidth = 1;
        gbcForm.weighty = 0;

        JPanel mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Thêm Câu hỏi");
        updateButton = new JButton("Sửa Câu hỏi");
        deleteButton = new JButton("Xóa Câu hỏi");
        clearFormButton = new JButton("Xóa Form");
        refreshButton = new JButton("Làm mới Toàn bộ"); 

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        mainButtonPanel.add(addButton);
        mainButtonPanel.add(updateButton);
        mainButtonPanel.add(deleteButton);
        mainButtonPanel.add(clearFormButton);
        mainButtonPanel.add(refreshButton);

        formAndButtonPanel.add(formInputPanel, BorderLayout.CENTER);
        formAndButtonPanel.add(mainButtonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, formAndButtonPanel);
        splitPane.setResizeWeight(0.5);
        mainContentPanel.add(splitPane, BorderLayout.CENTER);

        add(filterPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);

        addChoiceInputPanel();
        addChoiceInputPanel();

        filterButton.addActionListener(e -> searchQuestions());
        clearFilterButton.addActionListener(e -> {
            filterQuestionIdField.setText("");
            filterLevelComboBox.setSelectedIndex(0); 
            filterQuestionTypeComboBox.setSelectedIndex(0); 
            loadQuestions(); 
        });

        browseAudioButton.addActionListener(e -> browseAudioFile());
        clearAudioButton.addActionListener(e -> clearAudioFile());
        playStopAudioButton.addActionListener(e -> togglePlayStopAudio());
        addButton.addActionListener(e -> addQuestion());
        updateButton.addActionListener(e -> updateQuestion());
        deleteButton.addActionListener(e -> deleteQuestion());
        clearFormButton.addActionListener(e -> clearFormAndSelection());
        refreshButton.addActionListener(e -> { 
            filterQuestionIdField.setText("");
            filterLevelComboBox.setSelectedIndex(0);
            filterQuestionTypeComboBox.setSelectedIndex(0);
            loadQuestions();
        });
    }

    private void togglePlayStopAudio() {
        if (isPlayingAudio) {
            stopCurrentAudio();
        } else {
            playSelectedAudio();
        }
    }

    private void stopCurrentAudio() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            // LineListener sẽ xử lý việc close clip và stream khi nhận sự kiện STOP
        }
        // Trạng thái isPlayingAudio và text nút sẽ được cập nhật trong LineListener
        // Hoặc có thể cập nhật ngay ở đây để phản hồi nhanh hơn nếu cần
        // isPlayingAudio = false;
        // playStopAudioButton.setText("Phát");
    }
    
    private void playSelectedAudio() {
        String relativePath = audioPathField.getText();
        if (relativePath == null || relativePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có file âm thanh nào được chọn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        stopCurrentAudio(); 

        AudioInputStream decodedAudioStream = null; 
        AudioInputStream rawSourceStream = null;    

        try {
            File audioFile = new File(relativePath);
            if (!audioFile.exists()) {
                audioFile = new File(System.getProperty("user.dir") + File.separator + relativePath);
            }

            if (!audioFile.exists()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy file âm thanh tại: " + audioFile.getAbsolutePath() +
                                                "\n(Thư mục làm việc hiện tại: " + System.getProperty("user.dir") + ")",
                                                "Lỗi File", JOptionPane.ERROR_MESSAGE);
                playStopAudioButton.setText("Phát");
                isPlayingAudio = false;
                return;
            }

            rawSourceStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat sourceFormat = rawSourceStream.getFormat();

            AudioFormat pcmTargetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16, 
                    sourceFormat.getChannels(),
                    (sourceFormat.getSampleSizeInBits() > 0 ? sourceFormat.getSampleSizeInBits() / 8 : 2) * sourceFormat.getChannels(), 
                    sourceFormat.getSampleRate(),
                    false 
            );
            
            if (AudioSystem.isConversionSupported(pcmTargetFormat, sourceFormat)) {
                decodedAudioStream = AudioSystem.getAudioInputStream(pcmTargetFormat, rawSourceStream);
            } else {
                if ((sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) ||
                     sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) &&
                     sourceFormat.getFrameSize() != AudioSystem.NOT_SPECIFIED && sourceFormat.getFrameSize() > 0) {
                    decodedAudioStream = rawSourceStream; 
                } else {
                    throw new UnsupportedAudioFileException("Không thể chuyển đổi định dạng âm thanh sang PCM mong muốn: " + sourceFormat + ". MP3 SPI có thể chưa hoạt động đúng.");
                }
            }
            
            DataLine.Info info = new DataLine.Info(Clip.class, decodedAudioStream.getFormat());
            currentClip = (Clip) AudioSystem.getLine(info);
            
            final AudioInputStream finalDecodedStreamForListener = decodedAudioStream; 
            final AudioInputStream finalRawStreamForListener = (rawSourceStream == decodedAudioStream) ? null : rawSourceStream; 

            currentClip.addLineListener(event -> { 
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlayingAudio = false;
                    playStopAudioButton.setText("Phát");
                    if (event.getLine() instanceof Clip) {
                        Clip c = (Clip) event.getLine();
                        c.close(); 
                        try {
                            if (finalDecodedStreamForListener != null) finalDecodedStreamForListener.close();
                            if (finalRawStreamForListener != null) finalRawStreamForListener.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Đã đóng clip và stream sau khi phát xong hoặc dừng.");
                    }
                } else if (event.getType() == LineEvent.Type.START) {
                    isPlayingAudio = true;
                    playStopAudioButton.setText("Dừng");
                    System.out.println("Bắt đầu phát audio.");
                }
            });

            currentClip.open(decodedAudioStream); 
            currentClip.start();

        } catch (UnsupportedAudioFileException ex) {
            String message = "Định dạng file âm thanh không được hỗ trợ: " + ex.getMessage();
            if (relativePath.toLowerCase().endsWith(".mp3")) {
                message += "\nLƯU Ý: Phát file MP3 có thể yêu cầu thêm thư viện MP3 SPI (ví dụ: Tritonus MP3SPI, JLayer) vào classpath của dự án.";
            }
            JOptionPane.showMessageDialog(this, message, "Lỗi Định dạng Audio", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            playStopAudioButton.setText("Phát"); isPlayingAudio = false;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi vào/ra khi xử lý file âm thanh: " + ex.getMessage(), "Lỗi I/O Audio", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            playStopAudioButton.setText("Phát"); isPlayingAudio = false;
        } catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở line audio, có thể đang được sử dụng: " + ex.getMessage(), "Lỗi Line Audio", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            playStopAudioButton.setText("Phát"); isPlayingAudio = false;
        } catch (IllegalArgumentException ex) { 
             JOptionPane.showMessageDialog(this, "Lỗi khi mở audio clip: " + ex.getMessage(), "Lỗi Audio Clip", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            playStopAudioButton.setText("Phát"); isPlayingAudio = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stopCurrentAudio();
        super.finalize();
    }

    private void loadFilterDropdownData() {
        filterLevelComboBox.addItem(new Level(0, "Tất cả Cấp độ")); 
        List<Level> levels = levelService.getAllLevels();
        if (levels != null) {
            for (Level level : levels) {
                filterLevelComboBox.addItem(level);
            }
        }
        filterQuestionTypeComboBox.addItem(new QuestionType(0, "Tất cả Loại")); 
        List<QuestionType> types = questionTypeService.getAllQuestionTypes();
        if (types != null) {
            for (QuestionType type : types) {
                filterQuestionTypeComboBox.addItem(type);
            }
        }
    }
    
    private void loadFormDropdownData() { 
        List<Level> levels = levelService.getAllLevels();
        formLevelComboBox.removeAllItems();
        if (levels != null) {
            for (Level level : levels) {
                formLevelComboBox.addItem(level);
            }
        }
        if (formLevelComboBox.getItemCount() > 0) formLevelComboBox.setSelectedIndex(0);

        List<QuestionType> types = questionTypeService.getAllQuestionTypes();
        formQuestionTypeComboBox.removeAllItems();
        if (types != null) {
            for (QuestionType type : types) {
                formQuestionTypeComboBox.addItem(type);
            }
        }
         if (formQuestionTypeComboBox.getItemCount() > 0) formQuestionTypeComboBox.setSelectedIndex(0);
    }
    
    protected void loadQuestions() { 
        populateTable(questionService.getAllQuestions());
        clearFormAndSelection(); 
    }

    private void searchQuestions() {
        long questionId = 0;
        String idText = filterQuestionIdField.getText().trim();
        if (!idText.isEmpty()) {
            try {
                questionId = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID Câu hỏi không hợp lệ. Vui lòng nhập số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Level selectedFilterLevel = (Level) filterLevelComboBox.getSelectedItem();
        QuestionType selectedFilterType = (QuestionType) filterQuestionTypeComboBox.getSelectedItem();

        long levelIdFilter = (selectedFilterLevel != null && selectedFilterLevel.getLevelID() != 0) ? selectedFilterLevel.getLevelID() : 0;
        long typeIdFilter = (selectedFilterType != null && selectedFilterType.getQuestionTypeID() != 0) ? selectedFilterType.getQuestionTypeID() : 0;
        
        System.out.println("Đang tìm kiếm với ID: " + questionId + ", LevelID: " + levelIdFilter + ", TypeID: " + typeIdFilter);

        List<Question> results;
        List<Long> typeIds = new ArrayList<>();
        if (typeIdFilter > 0) {
            typeIds.add(typeIdFilter);
        }
        
        if (questionId > 0) {
            Question q = questionService.getQuestionById(questionId);
            results = new ArrayList<>();
            if (q != null) {
                boolean levelMatch = (levelIdFilter == 0 || (q.getLevel() != null && q.getLevel().getLevelID() == levelIdFilter));
                boolean typeMatch = (typeIdFilter == 0 || (q.getQuestionType() != null && q.getQuestionType().getQuestionTypeID() == typeIdFilter));
                if (levelMatch && typeMatch) {
                    results.add(q);
                }
            }
        } else {
            results = questionService.getQuestionsByCriteria(levelIdFilter, typeIds.isEmpty() ? null : typeIds);
        }
        populateTable(results);
        clearFormAndSelection();
    }

    private void populateTable(List<Question> questions) {
        tableModel.setRowCount(0);
        if (questions != null) {
            for (Question q : questions) {
                tableModel.addRow(new Object[]{
                        q.getQuestionID(),
                        q.getQuestionText(),
                        q.getLevel() != null ? q.getLevel().getLevelName() : "N/A",
                        q.getQuestionType() != null ? q.getQuestionType().getTypeName() : "N/A",
                        q.getAudioPath() != null ? q.getAudioPath() : ""
                });
            }
        }
    }
    
    protected void populateFormWithSelectedQuestion() { 
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) return;

        long questionId = (long) tableModel.getValueAt(selectedRow, 0);
        selectedQuestion = questionService.getQuestionById(questionId);

        if (selectedQuestion != null) {
            stopCurrentAudio(); 
            questionTextArea.setText(selectedQuestion.getQuestionText());
            String audioPath = selectedQuestion.getAudioPath();
            audioPathField.setText(audioPath != null ? audioPath : "");
            playStopAudioButton.setEnabled(audioPath != null && !audioPath.isEmpty()); 
            playStopAudioButton.setText("Phát");
            isPlayingAudio = false;

            if (selectedQuestion.getLevel() != null) {
                for (int i = 0; i < formLevelComboBox.getItemCount(); i++) { 
                    if (formLevelComboBox.getItemAt(i) != null && formLevelComboBox.getItemAt(i).getLevelID() == selectedQuestion.getLevel().getLevelID()) {
                        formLevelComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            } else { formLevelComboBox.setSelectedIndex(-1); }

            if (selectedQuestion.getQuestionType() != null) {
                for (int i = 0; i < formQuestionTypeComboBox.getItemCount(); i++) { 
                    if (formQuestionTypeComboBox.getItemAt(i) != null && formQuestionTypeComboBox.getItemAt(i).getQuestionTypeID() == selectedQuestion.getQuestionType().getQuestionTypeID()) {
                        formQuestionTypeComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            } else { formQuestionTypeComboBox.setSelectedIndex(-1); }

            choicesPanelContainer.removeAll();
            choiceInputPanels.clear();
            if (selectedQuestion.getChoices() != null) {
                for (Choice choice : selectedQuestion.getChoices()) {
                    if (choiceInputPanels.size() < MAX_CHOICES) {
                         ChoiceInputPanel choicePanel = new ChoiceInputPanel(choiceInputPanels.size() + 1, this, unicodeGuiFont); 
                         choicePanel.setChoiceText(choice.getChoiceText());
                         choicePanel.setCorrect(choice.isCorrect());
                         choiceInputPanels.add(choicePanel);
                         choicesPanelContainer.add(choicePanel);
                    }
                }
            }
            while(choiceInputPanels.size() < 2 && choiceInputPanels.size() < MAX_CHOICES){
                addChoiceInputPanel(); 
            }
            
            updateChoicesPanelLayout();

            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            addButton.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết câu hỏi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            clearForm();
        }
    }

    private void addChoiceInputPanel() {
        if (choiceInputPanels.size() < MAX_CHOICES) {
            ChoiceInputPanel choicePanel = new ChoiceInputPanel(choiceInputPanels.size() + 1, this, unicodeGuiFont); 
            choiceInputPanels.add(choicePanel);
            choicesPanelContainer.add(choicePanel);
            updateChoicesPanelLayout();
        } else {
            JOptionPane.showMessageDialog(this, "Đã đạt số lượng lựa chọn tối đa (" + MAX_CHOICES + ").", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void removeChoicePanelFromGui(ChoiceInputPanel choicePanelToRemove) {
        if (choiceInputPanels.size() > 1) { 
            choiceInputPanels.remove(choicePanelToRemove);
            choicesPanelContainer.remove(choicePanelToRemove);
            for (int i = 0; i < choiceInputPanels.size(); i++) {
                choiceInputPanels.get(i).updateChoiceNumber(i + 1);
            }
            updateChoicesPanelLayout();
        } else {
             JOptionPane.showMessageDialog(this, "Phải có ít nhất một lựa chọn.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void browseAudioFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file âm thanh (MP3, WAV)");
        fileChooser.setAcceptAllFileFilterUsed(false); 
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files (.mp3, .wav)", "mp3", "wav");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter); 

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                stopCurrentAudio(); // Dừng audio hiện tại nếu có
                Path sourcePath = selectedFile.toPath();
                String originalFileName = selectedFile.getName();
                String fileExtension = "";
                int lastDot = originalFileName.lastIndexOf('.');
                if (lastDot > 0 && lastDot < originalFileName.length() - 1) {
                    fileExtension = originalFileName.substring(lastDot); 
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                
                File storageDir = new File(AUDIO_STORAGE_DIR);
                if (!storageDir.exists()){
                    storageDir.mkdirs();
                }

                Path destinationPath = Paths.get(AUDIO_STORAGE_DIR, uniqueFileName);
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                String relativePath = Paths.get(AUDIO_STORAGE_DIR, uniqueFileName).toString().replace(File.separatorChar, '/');
                audioPathField.setText(relativePath);
                playStopAudioButton.setEnabled(true); 
                playStopAudioButton.setText("Phát");
                isPlayingAudio = false;
                JOptionPane.showMessageDialog(this, "Đã tải lên và lưu file: " + uniqueFileName + "\nĐường dẫn tương đối: " + relativePath, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu file âm thanh: " + ex.getMessage(), "Lỗi File", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void clearAudioFile() {
        stopCurrentAudio(); 
        audioPathField.setText("");
        playStopAudioButton.setEnabled(false); 
        playStopAudioButton.setText("Phát"); 
        isPlayingAudio = false;
        JOptionPane.showMessageDialog(this, "Đã xóa đường dẫn file âm thanh.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private Question getQuestionFromForm() throws IllegalArgumentException {
        Question question = (selectedQuestion == null) ? new Question() : new Question(); 
        if(selectedQuestion != null) { 
            question.setQuestionID(selectedQuestion.getQuestionID());
            question.setCreatedAt(selectedQuestion.getCreatedAt()); 
        }
        Level selectedFormLevelObj = (Level) formLevelComboBox.getSelectedItem(); 
        if (selectedFormLevelObj == null) {
            throw new IllegalArgumentException("Vui lòng chọn Cấp độ cho câu hỏi.");
        }
        question.setLevel(selectedFormLevelObj);

        QuestionType selectedFormTypeObj = (QuestionType) formQuestionTypeComboBox.getSelectedItem(); 
        if (selectedFormTypeObj == null) {
            throw new IllegalArgumentException("Vui lòng chọn Loại câu hỏi cho câu hỏi.");
        }
        question.setQuestionType(selectedFormTypeObj);
        String qText = questionTextArea.getText().trim();
        if (qText.isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống.");
        }
        question.setQuestionText(qText);
        String relativeAudioPath = audioPathField.getText().trim();
        question.setAudioPath(relativeAudioPath.isEmpty() ? null : relativeAudioPath);
        List<Choice> choices = new ArrayList<>();
        boolean hasCorrectChoice = false;
        for (ChoiceInputPanel choicePanel : choiceInputPanels) {
            String choiceText = choicePanel.getChoiceText().trim();
            if (!choiceText.isEmpty()) { 
                Choice choice = new Choice(choiceText, choicePanel.isCorrect());
                choices.add(choice);
                if (choice.isCorrect()) {
                    hasCorrectChoice = true;
                }
            } else if (choicePanel.isCorrect()){
                 throw new IllegalArgumentException("Lựa chọn đúng không được để trống nội dung.");
            }
        }
        if (choices.isEmpty()) {
            throw new IllegalArgumentException("Phải có ít nhất một lựa chọn có nội dung.");
        }
        if (!hasCorrectChoice) {
            throw new IllegalArgumentException("Phải có ít nhất một lựa chọn được đánh dấu là đúng.");
        }
        question.setChoices(choices);
        return question;
    }
    
    private void addQuestion() {
         try {
            Question newQuestion = getQuestionFromForm();
            selectedQuestion = null; 
            if (questionService.addQuestionWithChoices(newQuestion)) {
                JOptionPane.showMessageDialog(this, "Thêm câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadQuestions(); 
            } else {
                JOptionPane.showMessageDialog(this, "Thêm câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi thêm câu hỏi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateQuestion() {
        if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Question questionToUpdate = getQuestionFromForm();
            if (questionService.updateQuestionWithChoices(questionToUpdate)) {
                JOptionPane.showMessageDialog(this, "Cập nhật câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadQuestions();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi cập nhật câu hỏi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteQuestion() {
         if (selectedQuestion == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa câu hỏi này?\n\"" + selectedQuestion.getQuestionText().substring(0, Math.min(selectedQuestion.getQuestionText().length(), 50)) + "...\"",
                "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                stopCurrentAudio(); 
                if (questionService.deleteQuestion(selectedQuestion.getQuestionID())) {
                    JOptionPane.showMessageDialog(this, "Xóa câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadQuestions();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa câu hỏi thất bại. Có thể câu hỏi đang được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void clearForm() {
        formLevelComboBox.setSelectedIndex(-1); 
        formQuestionTypeComboBox.setSelectedIndex(-1); 
        questionTextArea.setText("");
        audioPathField.setText("");
        stopCurrentAudio(); 
        playStopAudioButton.setEnabled(false); 
        playStopAudioButton.setText("Phát");
        isPlayingAudio = false;
        selectedQuestion = null;

        choicesPanelContainer.removeAll();
        choiceInputPanels.clear();
        addChoiceInputPanel();
        addChoiceInputPanel();
        
        updateChoicesPanelLayout();

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        addButton.setEnabled(true);
        questionTextArea.requestFocusInWindow(); 
    }

    private void clearFormAndSelection() {
        questionTable.clearSelection(); 
        clearForm();
    }

    private void updateChoicesPanelLayout() {
        if (choicesPanelContainer != null) {
            choicesPanelContainer.revalidate();
            choicesPanelContainer.repaint();
        }
        if (choicesScrollPane != null) {
            choicesScrollPane.getViewport().setViewSize(choicesPanelContainer.getPreferredSize()); 
            choicesScrollPane.revalidate();
            choicesScrollPane.repaint();
        }
        if (choicesOuterPanel != null) {
            choicesOuterPanel.revalidate();
            choicesOuterPanel.repaint();
        }
        if (formInputPanel != null) { 
            formInputPanel.revalidate();
            formInputPanel.repaint();
        }
    }

    private static class ChoiceInputPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private JLabel choiceNumberLabel;
        private JTextField choiceTextField;
        private JCheckBox correctCheckBox;
        private JButton removeChoiceButton;
        private QuestionManagementPanel parentPanel; 
        private Font guiFont;

        public ChoiceInputPanel(int number, QuestionManagementPanel parent, Font font) {
            this.parentPanel = parent; 
            this.guiFont = font; 
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            choiceNumberLabel = new JLabel("Lựa chọn " + number + ":");
            choiceTextField = new JTextField(30);
            if (this.guiFont != null) { 
                choiceTextField.setFont(this.guiFont);
                choiceNumberLabel.setFont(this.guiFont);
            }
            correctCheckBox = new JCheckBox("Đúng");
            if (this.guiFont != null) correctCheckBox.setFont(this.guiFont);
            
            removeChoiceButton = new JButton("Xóa LC");
            removeChoiceButton.setMargin(new Insets(2,5,2,5));

            removeChoiceButton.addActionListener(e -> parentPanel.removeChoicePanelFromGui(this));
            add(choiceNumberLabel);
            add(choiceTextField);
            add(correctCheckBox);
            add(removeChoiceButton);
        }
        public String getChoiceText() { return choiceTextField.getText(); }
        public void setChoiceText(String text) { choiceTextField.setText(text); }
        public boolean isCorrect() { return correctCheckBox.isSelected(); }
        public void setCorrect(boolean selected) { correctCheckBox.setSelected(selected); }
        public void updateChoiceNumber(int number) { choiceNumberLabel.setText("Lựa chọn " + number + ":"); }
    }
}

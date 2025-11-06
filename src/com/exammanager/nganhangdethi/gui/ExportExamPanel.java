package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Exam;
import com.exammanager.nganhangdethi.service.ExamService;
import com.exammanager.nganhangdethi.service.ExamExportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.List;

public class ExportExamPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ExamService examService;
    private ExamExportService examExportService;

    private JComboBox<ExamWrapper> examComboBox;
    private JComboBox<String> formatComboBox;
    private JButton exportButton;
    private JProgressBar progressBar;
    private JTextArea logArea;

    public ExportExamPanel() {
        this.examService = new ExamService();
        this.examExportService = new ExamExportService();

        initComponents();
        // Không gọi loadExams() ở đây nữa, sẽ gọi khi panel được hiển thị

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("ExportExamPanel is now visible, reloading exams...");
                loadExams(); // Tải lại danh sách đề thi khi panel được hiển thị
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Tùy chọn Xuất Đề thi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        selectionPanel.add(new JLabel("Chọn Đề thi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        examComboBox = new JComboBox<>();
        selectionPanel.add(examComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        selectionPanel.add(new JLabel("Định dạng Xuất:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formatComboBox = new JComboBox<>(new String[]{"PDF", "DOCX"});
        selectionPanel.add(formatComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 8, 8, 8);
        exportButton = new JButton("Xuất File");
        exportButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportButton.setPreferredSize(new Dimension(150, 40));
        selectionPanel.add(exportButton, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Kết quả Xuất File"));
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        logPanel.add(progressBar, BorderLayout.SOUTH);

        add(selectionPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);

        exportButton.addActionListener(e -> exportExam());
    }

    /**
     * Tải danh sách các đề thi vào ComboBox.
     * Đã đổi thành public để MainAppFrame có thể gọi.
     */
    public void loadExams() { // <<< SỬA private THÀNH public
        System.out.println("loadExams() called in ExportExamPanel");
        examComboBox.removeAllItems();
        List<Exam> exams = examService.getAllExams();
        if (exams == null || exams.isEmpty()) {
            logArea.setText("Không có đề thi nào trong hệ thống để xuất.\nVui lòng tạo đề thi trước.");
            exportButton.setEnabled(false);
            System.out.println("No exams found or list is null.");
        } else {
            System.out.println("Found " + exams.size() + " exams.");
            for (Exam exam : exams) {
                examComboBox.addItem(new ExamWrapper(exam));
            }
            exportButton.setEnabled(true);
            logArea.setText("Sẵn sàng để xuất file. Vui lòng chọn đề thi và định dạng.");
        }
    }

    private void exportExam() {
        ExamWrapper selectedExamWrapper = (ExamWrapper) examComboBox.getSelectedItem();
        if (selectedExamWrapper == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xuất.", "Chưa chọn Đề thi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Exam selectedExamObject = selectedExamWrapper.getExam();
        String selectedFormat = (String) formatComboBox.getSelectedItem();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu File Đề thi");

        String originalExamName = selectedExamObject.getExamName() != null ? selectedExamObject.getExamName() : "DeThiKhongTen";
        // Sử dụng lại hàm tiện ích từ CreateExamPanel nếu nó là public static
        // Hoặc bạn có thể copy hàm đó vào đây hoặc tạo một lớp Utils
        String suggestedFileName = CreateExamPanel.toUrlFriendlyString(originalExamName);

        fileChooser.setSelectedFile(new File(suggestedFileName + "." + selectedFormat.toLowerCase()));
        fileChooser.setFileFilter(new FileNameExtensionFilter(selectedFormat.toUpperCase() + " Files", selectedFormat.toLowerCase()));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + selectedFormat.toLowerCase())) {
                fileToSave = new File(filePath + "." + selectedFormat.toLowerCase());
            }
            final File finalFileToSave = fileToSave;

            logArea.setText("Đang chuẩn bị xuất đề thi: " + selectedExamObject.getExamName() + "\nĐịnh dạng: " + selectedFormat + "\nLưu tại: " + finalFileToSave.getAbsolutePath() + "\n\n");
            progressBar.setValue(0);
            progressBar.setVisible(true);
            exportButton.setEnabled(false);

            SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    publish("Đang lấy chi tiết đề thi ID: " + selectedExamObject.getExamID() + "...");
                    Exam examDetails = examService.getExamById(selectedExamObject.getExamID());
                    if (examDetails == null) {
                        publish("Lỗi: Không thể lấy chi tiết đề thi ID " + selectedExamObject.getExamID());
                        return false;
                    }
                    publish("Đã lấy chi tiết đề thi. Bắt đầu tạo file...");
                    setProgress(10);

                    boolean result = false;
                    if ("PDF".equals(selectedFormat)) {
                        result = examExportService.exportExamToPdf(examDetails, finalFileToSave.getAbsolutePath(), this::publish, (obj, val) -> setProgress(val));
                    } else if ("DOCX".equals(selectedFormat)) {
                        result = examExportService.exportExamToDocx(examDetails, finalFileToSave.getAbsolutePath(), this::publish, (obj, val) -> setProgress(val));
                    }
                    return result;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String message : chunks) {
                        logArea.append(message + "\n");
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean exportSuccess = get();
                        if (exportSuccess) {
                            logArea.append("\nXuất file THÀNH CÔNG!\n");
                            JOptionPane.showMessageDialog(ExportExamPanel.this,
                                    "Đề thi đã được xuất thành công!\nLưu tại: " + finalFileToSave.getAbsolutePath(),
                                    "Xuất File Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            logArea.append("\nXuất file THẤT BẠI. Vui lòng xem log để biết chi tiết.\n");
                            JOptionPane.showMessageDialog(ExportExamPanel.this,
                                    "Xuất đề thi thất bại. Vui lòng kiểm tra log.",
                                    "Lỗi Xuất File", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        logArea.append("\nLỗi trong quá trình xuất file: " + e.getMessage() + "\n");
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(ExportExamPanel.this,
                                "Đã xảy ra lỗi: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        progressBar.setValue(100);
                        exportButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }

    private static class ExamWrapper {
        private Exam exam;
        public ExamWrapper(Exam exam) {
            if (exam == null) {
                throw new IllegalArgumentException("Exam không được null cho ExamWrapper");
            }
            this.exam = exam;
        }
        public Exam getExam() { return exam; }
        @Override
        public String toString() {
            String name = exam.getExamName() != null ? exam.getExamName() : "Đề thi không tên";
            return name + " (ID: " + exam.getExamID() + ")";
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExamWrapper that = (ExamWrapper) o;
            return exam.getExamID() == that.exam.getExamID();
        }
        @Override
        public int hashCode() { return Long.hashCode(exam.getExamID()); }
    }
}

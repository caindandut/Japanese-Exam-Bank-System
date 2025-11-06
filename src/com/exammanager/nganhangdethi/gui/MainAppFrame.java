package com.exammanager.nganhangdethi.gui;

import javax.swing.*;
import java.awt.*;
// Import các panel quản lý và chức năng
import com.exammanager.nganhangdethi.gui.LevelManagementPanel;
import com.exammanager.nganhangdethi.gui.QuestionTypeManagementPanel;
import com.exammanager.nganhangdethi.gui.QuestionManagementPanel;
import com.exammanager.nganhangdethi.gui.ExamManagementPanel;
import com.exammanager.nganhangdethi.gui.CreateExamPanel;
import com.exammanager.nganhangdethi.gui.ExportExamPanel;


/**
 * Cửa sổ chính của ứng dụng Quản lý Ngân hàng Đề thi.
 */
public class MainAppFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Lưu trữ tham chiếu đến các panel cần refresh
    private ExamManagementPanel examManagementPanel;
    private ExportExamPanel exportExamPanel;
    // Thêm các panel khác nếu cần refresh

    public MainAppFrame() {
        setTitle("Chương trình Quản lý Ngân hàng Đề thi Tiếng Nhật");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem homeMenuItem = new JMenuItem("Trang chủ");
        menuBar.add(homeMenuItem);
        JMenu manageMenu = new JMenu("Quản lý");
        JMenuItem levelMenuItem = new JMenuItem("Quản lý Cấp độ");
        JMenuItem questionTypeMenuItem = new JMenuItem("Quản lý Loại Câu hỏi");
        JMenuItem questionMenuItem = new JMenuItem("Quản lý Câu hỏi");
        JMenuItem examMenuItem = new JMenuItem("Quản lý Đề thi");

        manageMenu.add(levelMenuItem);
        manageMenu.add(questionTypeMenuItem);
        manageMenu.add(new JSeparator());
        manageMenu.add(questionMenuItem);
        manageMenu.add(examMenuItem);
        menuBar.add(manageMenu);

        JMenu functionMenu = new JMenu("Chức năng");
        JMenuItem createExamMenuItem = new JMenuItem("Tạo Đề thi từ Ngân hàng");
        JMenuItem exportExamMenuItem = new JMenuItem("Xuất Đề thi (PDF/DOC)");
        functionMenu.add(createExamMenuItem);
        functionMenu.add(exportExamMenuItem);
        menuBar.add(functionMenu);

        // JMenu helpMenu = new JMenu("Trợ giúp");
        // JMenuItem aboutMenuItem = new JMenuItem("Thông tin");
        // helpMenu.add(aboutMenuItem);
        // menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Thiết kế lại Welcome Panel (Tối giản) ---
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(new Color(240, 245, 250)); 
        GridBagConstraints gbcWelcome = new GridBagConstraints();
        gbcWelcome.gridwidth = GridBagConstraints.REMAINDER;
        gbcWelcome.anchor = GridBagConstraints.CENTER;
        gbcWelcome.insets = new Insets(10, 20, 10, 20);

        JLabel iconLabel = new JLabel("📖", SwingConstants.CENTER); 
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120)); 
        gbcWelcome.insets = new Insets(50, 20, 20, 20);
        welcomePanel.add(iconLabel, gbcWelcome);
        
        JLabel welcomeTitleLabel = new JLabel("Hệ thống Quản lý Ngân hàng Đề thi Tiếng Nhật", SwingConstants.CENTER);
        welcomeTitleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        welcomeTitleLabel.setForeground(new Color(50, 50, 150)); 
        gbcWelcome.insets = new Insets(10, 20, 40, 20);
        welcomePanel.add(welcomeTitleLabel, gbcWelcome);

        JPanel quickAccessPanel = new JPanel(new GridBagLayout());
        quickAccessPanel.setOpaque(false);
        GridBagConstraints gbcQuick = new GridBagConstraints();
        gbcQuick.insets = new Insets(10, 15, 10, 15); 
        gbcQuick.fill = GridBagConstraints.HORIZONTAL;

        // Sử dụng màu sắc nhã nhặn hơn cho các nút
        Color defaultButtonColor = new Color(220, 220, 220); // Xám nhạt
        Color hoverButtonColor = new Color(200, 200, 200);   // Xám đậm hơn khi di chuột
        Color textColor = Color.BLACK; // Chữ màu đen

        JButton goToManageQuestionsButton = createQuickAccessButton("Quản lý Câu hỏi", "QUESTION_MANAGEMENT_PANEL", defaultButtonColor, hoverButtonColor, textColor);
        JButton goToCreateExamButton = createQuickAccessButton("Tạo Đề thi Mới", "CREATE_EXAM_PANEL", defaultButtonColor, hoverButtonColor, textColor);
        JButton goToExportExamButton = createQuickAccessButton("Xuất Đề thi", "EXPORT_EXAM_PANEL", defaultButtonColor, hoverButtonColor, textColor);

        gbcQuick.gridx = 0; gbcQuick.gridy = 0;
        quickAccessPanel.add(goToManageQuestionsButton, gbcQuick);
        gbcQuick.gridx = 1; gbcQuick.gridy = 0;
        quickAccessPanel.add(goToCreateExamButton, gbcQuick);
        gbcQuick.gridx = 2; gbcQuick.gridy = 0;
        quickAccessPanel.add(goToExportExamButton, gbcQuick);
        
        gbcWelcome.fill = GridBagConstraints.NONE; 
        gbcWelcome.anchor = GridBagConstraints.CENTER;
        gbcWelcome.insets = new Insets(10, 20, 50, 20);
        welcomePanel.add(quickAccessPanel, gbcWelcome);

        mainPanel.add(welcomePanel, "WELCOME_PANEL");
        // --- Kết thúc thiết kế Welcome Panel ---


        try {
            LevelManagementPanel levelManagementPanel = new LevelManagementPanel();
            mainPanel.add(levelManagementPanel, "LEVEL_MANAGEMENT_PANEL");
        } catch (Exception e) {
            addErrorPanel("LEVEL_MANAGEMENT_PANEL_ERROR", "Lỗi khi tải Quản lý Cấp độ: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            QuestionTypeManagementPanel questionTypeManagementPanel = new QuestionTypeManagementPanel();
            mainPanel.add(questionTypeManagementPanel, "QUESTION_TYPE_MANAGEMENT_PANEL");
        } catch (Exception e) {
            addErrorPanel("QUESTION_TYPE_MANAGEMENT_PANEL_ERROR", "Lỗi khi tải Quản lý Loại Câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            QuestionManagementPanel questionManagementPanel = new QuestionManagementPanel();
            mainPanel.add(questionManagementPanel, "QUESTION_MANAGEMENT_PANEL");
        } catch (Exception e) {
            addErrorPanel("QUESTION_MANAGEMENT_PANEL_ERROR", "Lỗi khi tải Quản lý Câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            examManagementPanel = new ExamManagementPanel();
            mainPanel.add(examManagementPanel, "EXAM_MANAGEMENT_PANEL");
        } catch (Exception e) {
            addErrorPanel("EXAM_MANAGEMENT_PANEL_ERROR", "Lỗi khi tải Quản lý Đề thi: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            CreateExamPanel createExamPanel = new CreateExamPanel(this);
            mainPanel.add(createExamPanel, "CREATE_EXAM_PANEL");
        } catch (Exception e) {
            addErrorPanel("CREATE_EXAM_PANEL_ERROR", "Lỗi khi tải chức năng Tạo Đề thi: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            exportExamPanel = new ExportExamPanel();
            mainPanel.add(exportExamPanel, "EXPORT_EXAM_PANEL");
        } catch (Exception e) {
            addErrorPanel("EXPORT_EXAM_PANEL_ERROR", "Lỗi khi tải chức năng Xuất Đề thi: " + e.getMessage());
            e.printStackTrace();
        }

        levelMenuItem.addActionListener(e -> switchToPanel("LEVEL_MANAGEMENT_PANEL"));
        questionTypeMenuItem.addActionListener(e -> switchToPanel("QUESTION_TYPE_MANAGEMENT_PANEL"));
        questionMenuItem.addActionListener(e -> switchToPanel("QUESTION_MANAGEMENT_PANEL"));
        examMenuItem.addActionListener(e -> switchToPanel("EXAM_MANAGEMENT_PANEL"));
        createExamMenuItem.addActionListener(e -> switchToPanel("CREATE_EXAM_PANEL"));
        exportExamMenuItem.addActionListener(e -> switchToPanel("EXPORT_EXAM_PANEL"));
        homeMenuItem.addActionListener(e -> switchToPanel("WELCOME_PANEL"));

        // aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(MainAppFrame.this, 
        //         "Chương trình Quản lý Ngân hàng Đề thi Tiếng Nhật\nPhiên bản 1.0\nPhát triển bởi [Tên của bạn]",
        //         "Thông tin Chương trình", JOptionPane.INFORMATION_MESSAGE));

        add(mainPanel);
        cardLayout.show(mainPanel, "WELCOME_PANEL");
    }
    
    // Cập nhật phương thức helper để tạo các nút truy cập nhanh với màu sắc tùy chỉnh
    private JButton createQuickAccessButton(String text, String panelName, Color bgColor, Color hoverColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(bgColor);
        button.setForeground(textColor); // Đặt màu chữ
        button.setFocusPainted(false);
        // Sử dụng một Border đơn giản hơn hoặc không có border nếu muốn
        // button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Viền xám mỏng

        button.addActionListener(e -> switchToPanel(panelName));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void addErrorPanel(String panelName, String errorMessage) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        JLabel errorLabel = new JLabel(errorMessage, SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(errorPanel, panelName);
    }

    public void switchToPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public void refreshAllExamRelatedPanels() {
        System.out.println("MainAppFrame: refreshAllExamRelatedPanels() called.");
        if (examManagementPanel != null) {
            System.out.println("MainAppFrame: Refreshing ExamManagementPanel...");
            examManagementPanel.loadExams();
        }
        if (exportExamPanel != null) {
            System.out.println("MainAppFrame: Refreshing ExportExamPanel...");
            exportExamPanel.loadExams();
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainAppFrame mainFrame = new MainAppFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}

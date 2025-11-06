package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.Level;
import com.exammanager.nganhangdethi.service.LevelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LevelManagementPanel extends JPanel {

    private LevelService levelService;
    private JTable levelTable;
    private DefaultTableModel tableModel;
    private JTextField levelNameField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton clearButton; // Nút để xóa trắng form

    private Level selectedLevel = null; // Lưu trữ level đang được chọn để sửa/xóa

    public LevelManagementPanel() {
        this.levelService = new LevelService();
        initComponents();
        loadLevels(); // Tải dữ liệu ban đầu
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Layout chính với khoảng cách
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Thêm padding

        // --- Panel hiển thị danh sách Levels (sử dụng JTable) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách Cấp độ"));

        String[] columnNames = {"ID Cấp độ", "Tên Cấp độ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        levelTable = new JTable(tableModel);
        levelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        levelTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        levelTable.setFont(new Font("Arial", Font.PLAIN, 13));
        levelTable.setRowHeight(25);


        // Bắt sự kiện khi người dùng chọn một dòng trên bảng
        levelTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && levelTable.getSelectedRow() != -1) {
                int selectedRow = levelTable.getSelectedRow();
                long levelId = (long) tableModel.getValueAt(selectedRow, 0);
                String levelName = (String) tableModel.getValueAt(selectedRow, 1);

                selectedLevel = new Level(levelId, levelName);
                levelNameField.setText(levelName);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                addButton.setEnabled(false); // Không cho thêm khi đang chọn để sửa
            } else {
                // Nếu không có dòng nào được chọn (ví dụ sau khi xóa hoặc clear)
                if (levelTable.getSelectedRow() == -1) {
                     clearForm(); // Gọi hàm clear form
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(levelTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // --- Panel Nhập liệu và Nút bấm ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Cấp độ"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các component
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên Cấp độ
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Tên Cấp độ:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Cho phép field mở rộng theo chiều ngang
        levelNameField = new JTextField(20);
        formPanel.add(levelNameField, gbc);

        // Panel chứa các nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Thêm");
        updateButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        refreshButton = new JButton("Làm mới DS");
        clearButton = new JButton("Xóa Form");

        // Vô hiệu hóa nút Sửa và Xóa ban đầu
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);


        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Nút bấm chiếm 2 cột
        gbc.weightx = 0; // Không cho panel nút mở rộng
        gbc.fill = GridBagConstraints.NONE; // Không fill
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // --- Thêm các panel vào layout chính ---
        add(tablePanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);


        // --- Xử lý sự kiện cho các nút ---
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLevel();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLevel();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteLevel();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLevels();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFormAndSelection();
            }
        });
    }

    private void loadLevels() {
        // Xóa dữ liệu cũ trên bảng
        tableModel.setRowCount(0);
        // Tải dữ liệu mới
        List<Level> levels = levelService.getAllLevels();
        for (Level level : levels) {
            tableModel.addRow(new Object[]{level.getLevelID(), level.getLevelName()});
        }
        clearFormAndSelection(); // Xóa form và lựa chọn sau khi tải lại
    }

    private void addLevel() {
        String levelName = levelNameField.getText().trim();
        if (levelName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên cấp độ không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Level newLevel = new Level(levelName);
        try {
            if (levelService.addLevel(newLevel)) {
                JOptionPane.showMessageDialog(this, "Thêm cấp độ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadLevels(); // Tải lại danh sách
            } else {
                JOptionPane.showMessageDialog(this, "Thêm cấp độ thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateLevel() {
        if (selectedLevel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cấp độ để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newLevelName = levelNameField.getText().trim();
        if (newLevelName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên cấp độ không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedLevel.setLevelName(newLevelName); // Cập nhật tên cho đối tượng đang chọn
        try {
            if (levelService.updateLevel(selectedLevel)) {
                JOptionPane.showMessageDialog(this, "Cập nhật cấp độ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadLevels();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật cấp độ thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteLevel() {
        if (selectedLevel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cấp độ để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa cấp độ '" + selectedLevel.getLevelName() + "' không?",
                "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                if (levelService.deleteLevel(selectedLevel.getLevelID())) {
                    JOptionPane.showMessageDialog(this, "Xóa cấp độ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadLevels();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa cấp độ thất bại. Có thể cấp độ này đang được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void clearForm() {
        levelNameField.setText("");
        selectedLevel = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        addButton.setEnabled(true); // Cho phép thêm mới
    }

    private void clearFormAndSelection() {
        levelTable.clearSelection(); // Bỏ chọn trên bảng
        clearForm(); // Gọi hàm clear form
    }
}

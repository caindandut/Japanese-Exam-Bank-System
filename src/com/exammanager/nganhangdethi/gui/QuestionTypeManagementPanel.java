package com.exammanager.nganhangdethi.gui;

import com.exammanager.nganhangdethi.model.QuestionType;
import com.exammanager.nganhangdethi.service.QuestionTypeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class QuestionTypeManagementPanel extends JPanel {

    private QuestionTypeService questionTypeService;
    private JTable questionTypeTable;
    private DefaultTableModel tableModel;
    private JTextField typeNameField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton clearButton;

    private QuestionType selectedQuestionType = null;

    public QuestionTypeManagementPanel() {
        this.questionTypeService = new QuestionTypeService();
        initComponents();
        loadQuestionTypes();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel hiển thị danh sách QuestionTypes ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách Loại Câu hỏi"));

        String[] columnNames = {"ID Loại", "Tên Loại Câu hỏi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionTypeTable = new JTable(tableModel);
        questionTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTypeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        questionTypeTable.setFont(new Font("Arial", Font.PLAIN, 13));
        questionTypeTable.setRowHeight(25);

        questionTypeTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && questionTypeTable.getSelectedRow() != -1) {
                int selectedRow = questionTypeTable.getSelectedRow();
                long typeId = (long) tableModel.getValueAt(selectedRow, 0);
                String typeName = (String) tableModel.getValueAt(selectedRow, 1);

                selectedQuestionType = new QuestionType(typeId, typeName);
                typeNameField.setText(typeName);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                addButton.setEnabled(false);
            } else {
                if (questionTypeTable.getSelectedRow() == -1) {
                    clearForm();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(questionTypeTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // --- Panel Nhập liệu và Nút bấm ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Loại Câu hỏi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Tên Loại:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        typeNameField = new JTextField(20);
        formPanel.add(typeNameField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Thêm");
        updateButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        refreshButton = new JButton("Làm mới DS");
        clearButton = new JButton("Xóa Form");

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        add(tablePanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

        // --- Xử lý sự kiện cho các nút ---
        addButton.addActionListener(e -> addQuestionType());
        updateButton.addActionListener(e -> updateQuestionType());
        deleteButton.addActionListener(e -> deleteQuestionType());
        refreshButton.addActionListener(e -> loadQuestionTypes());
        clearButton.addActionListener(e -> clearFormAndSelection());
    }

    private void loadQuestionTypes() {
        tableModel.setRowCount(0);
        List<QuestionType> types = questionTypeService.getAllQuestionTypes();
        for (QuestionType type : types) {
            tableModel.addRow(new Object[]{type.getQuestionTypeID(), type.getTypeName()});
        }
        clearFormAndSelection();
    }

    private void addQuestionType() {
        String typeName = typeNameField.getText().trim();
        if (typeName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại câu hỏi không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        QuestionType newType = new QuestionType(typeName);
        try {
            if (questionTypeService.addQuestionType(newType)) {
                JOptionPane.showMessageDialog(this, "Thêm loại câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadQuestionTypes();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm loại câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateQuestionType() {
        if (selectedQuestionType == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một loại câu hỏi để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newTypeName = typeNameField.getText().trim();
        if (newTypeName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại câu hỏi không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedQuestionType.setTypeName(newTypeName);
        try {
            if (questionTypeService.updateQuestionType(selectedQuestionType)) {
                JOptionPane.showMessageDialog(this, "Cập nhật loại câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadQuestionTypes();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật loại câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteQuestionType() {
        if (selectedQuestionType == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một loại câu hỏi để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa loại câu hỏi '" + selectedQuestionType.getTypeName() + "' không?",
                "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                if (questionTypeService.deleteQuestionType(selectedQuestionType.getQuestionTypeID())) {
                    JOptionPane.showMessageDialog(this, "Xóa loại câu hỏi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadQuestionTypes();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa loại câu hỏi thất bại. Có thể loại này đang được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void clearForm() {
        typeNameField.setText("");
        selectedQuestionType = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        addButton.setEnabled(true);
    }

    private void clearFormAndSelection() {
        questionTypeTable.clearSelection();
        clearForm();
    }
}

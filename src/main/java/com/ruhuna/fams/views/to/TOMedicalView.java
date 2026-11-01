package com.ruhuna.fams.views.to;

import com.ruhuna.fams.controllers.TechnicalOfficerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TOMedicalView extends JPanel {
    private final TechnicalOfficerController controller;
    private final User user;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtStudent;
    private JTextField txtCourse;
    private JTextField txtDate;
    private JTextField txtReason;
    private JTextField txtStatus;
    private int selectedMedicalId = -1;

    public TOMedicalView() {
        this(null);
    }

    public TOMedicalView(User user) {
        this.user = user;
        this.controller = new TechnicalOfficerController();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("MEDICAL RECORDS MANAGEMENT");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        String[] columns = {"ID", "Student (TG)", "Student Name", "Course", "Date", "Reason", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JLabel("All Medical Submissions:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("REFRESH LIST");
        btnRefresh.addActionListener(e -> loadMedicals());
        leftPanel.add(btnRefresh, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Review Selected Medical Record"));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 8, 12));
        formGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtStudent = new JTextField();
        txtStudent.setEditable(false);
        txtCourse = new JTextField();
        txtCourse.setEditable(false);
        txtDate = new JTextField();
        txtDate.setEditable(false);
        txtReason = new JTextField();
        txtReason.setEditable(false);
        txtStatus = new JTextField();
        txtStatus.setEditable(false);

        formGrid.add(new JLabel("Student TG No:"));
        formGrid.add(txtStudent);
        formGrid.add(new JLabel("Course:"));
        formGrid.add(txtCourse);
        formGrid.add(new JLabel("Medical Date:"));
        formGrid.add(txtDate);
        formGrid.add(new JLabel("Reason:"));
        formGrid.add(txtReason);
        formGrid.add(new JLabel("Current Status:"));
        formGrid.add(txtStatus);

        rightPanel.add(formGrid, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnApprove = new JButton("APPROVE");
        btnApprove.setBackground(new Color(46, 204, 113));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setPreferredSize(new Dimension(120, 38));

        JButton btnReject = new JButton("REJECT");
        btnReject.setBackground(new Color(231, 76, 60));
        btnReject.setForeground(Color.WHITE);
        btnReject.setPreferredSize(new Dimension(120, 38));

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedMedicalId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    txtStudent.setText(tableModel.getValueAt(row, 1).toString());
                    txtCourse.setText(tableModel.getValueAt(row, 3).toString());
                    txtDate.setText(tableModel.getValueAt(row, 4).toString());
                    txtReason.setText(tableModel.getValueAt(row, 5).toString());
                    txtStatus.setText(tableModel.getValueAt(row, 6).toString());
                }
            }
        });

        btnApprove.addActionListener(e -> {
            if (selectedMedicalId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.",
                        "No Record Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int approverId = user != null ? user.getUserId() : 1;
            boolean ok = controller.approveMedical(selectedMedicalId, approverId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Medical Record #" + selectedMedicalId + " APPROVED successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicals();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update medical record in database.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnReject.addActionListener(e -> {
            if (selectedMedicalId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.",
                        "No Record Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int approverId = user != null ? user.getUserId() : 1;
            boolean ok = controller.rejectMedical(selectedMedicalId, approverId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Medical Record #" + selectedMedicalId + " REJECTED successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicals();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update medical record in database.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadMedicals();
    }

    private void loadMedicals() {
        tableModel.setRowCount(0);
        List<Object[]> medicals = controller.getAllMedicals();
        for (Object[] m : medicals) {
            tableModel.addRow(new Object[]{
                m[0],
                m[1],
                m[2],
                m[3],
                m[4],
                m[5],
                m[6]
            });
        }
        clearForm();
    }

    private void clearForm() {
        selectedMedicalId = -1;
        txtStudent.setText("");
        txtCourse.setText("");
        txtDate.setText("");
        txtReason.setText("");
        txtStatus.setText("");
    }
}

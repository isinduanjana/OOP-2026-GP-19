package com.ruhuna.fams.views.to;

import com.ruhuna.fams.controllers.TechnicalOfficerController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TOAttendanceView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private final TechnicalOfficerController controller;
    private User user;

    public TOAttendanceView() {
        this(null);
    }

    public TOAttendanceView(User user) {
        this.user = user;
        this.controller = new TechnicalOfficerController();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("ATTENDANCE MANAGEMENT");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(title, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("REFRESH");
        JButton btnAddAttendance = new JButton("+ MARK ATTENDANCE");
        btnAddAttendance.setBackground(new Color(39, 174, 96));
        btnAddAttendance.setForeground(Color.WHITE);

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAddAttendance);
        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Attendance ID", "Student ID", "Status", "Medical ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("SAVE SELECTED STATUS");
        btnSave.setBackground(new Color(39, 174, 96));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(200, 35));
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadAttendance());

        btnSave.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select an attendance record row from the table.",
                        "No Row Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int attendanceId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                String status = tableModel.getValueAt(selectedRow, 2).toString();

                boolean updated = controller.updateAttendanceStatus(attendanceId, status);
                if (updated) {
                    JOptionPane.showMessageDialog(this,
                            "Attendance record #" + attendanceId + " updated to " + status + " successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAttendance();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update attendance record.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating attendance: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAddAttendance.addActionListener(e -> showMarkAttendanceDialog());

        loadAttendance();
    }

    private void loadAttendance() {
        tableModel.setRowCount(0);
        List<Object[]> records = controller.getAttendanceRecords();
        for (Object[] row : records) {
            tableModel.addRow(row);
        }
    }

    private void showMarkAttendanceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Mark Student Attendance", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JComboBox<String> cmbSession = new JComboBox<>();
        List<Object[]> sessions = controller.getAllSessions();
        for (Object[] s : sessions) {
            cmbSession.addItem(s[0] + " - " + s[1] + " (" + s[4] + ")");
        }
        if (sessions.isEmpty()) {
            cmbSession.addItem("1 - Session 01");
        }

        JComboBox<String> cmbStudent = new JComboBox<>();
        List<Object[]> students = controller.getAllStudents();
        for (Object[] st : students) {
            cmbStudent.addItem(st[0] + " - " + st[1] + " (" + st[2] + ")");
        }
        if (students.isEmpty()) {
            cmbStudent.addItem("1 - TG2144");
        }

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});

        dialog.add(new JLabel("Select Session:"));
        dialog.add(cmbSession);
        dialog.add(new JLabel("Select Student:"));
        dialog.add(cmbStudent);
        dialog.add(new JLabel("Attendance Status:"));
        dialog.add(cmbStatus);

        JButton btnSubmit = new JButton("SAVE");
        btnSubmit.setBackground(new Color(39, 174, 96));
        btnSubmit.setForeground(Color.WHITE);

        JButton btnCancel = new JButton("CANCEL");

        dialog.add(btnSubmit);
        dialog.add(btnCancel);

        btnSubmit.addActionListener(e -> {
            try {
                String sessionItem = (String) cmbSession.getSelectedItem();
                String studentItem = (String) cmbStudent.getSelectedItem();
                if (sessionItem == null || studentItem == null) return;

                int sessionId = Integer.parseInt(sessionItem.split(" - ")[0].trim());
                int studentId = Integer.parseInt(studentItem.split(" - ")[0].trim());
                String status = (String) cmbStatus.getSelectedItem();

                boolean success = controller.saveAttendance(sessionId, studentId, status);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Attendance recorded successfully!");
                    dialog.dispose();
                    loadAttendance();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Could not save attendance.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}

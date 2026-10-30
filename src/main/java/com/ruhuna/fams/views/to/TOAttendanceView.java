package com.ruhuna.fams.views.to;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TOAttendanceView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public TOAttendanceView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("ATTENDANCE MANAGEMENT - ICT1201");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Attendance ID", "Student ID", "Status", "Medical ID"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnSave = new JButton("SAVE ATTENDANCE");
        btnSave.setBackground(new Color(39, 174, 96));
        btnSave.setForeground(Color.WHITE);
        add(btnSave, BorderLayout.SOUTH);

        loadAttendance();
    }

    private void loadAttendance() {
        tableModel.setRowCount(0);
        String sql = "SELECT attendance_id, student_id, status, medical_id FROM attendance LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("attendance_id"),
                    "TG" + rs.getInt("student_id"),
                    rs.getString("status"),
                    rs.getObject("medical_id") != null ? rs.getInt("medical_id") : "None"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

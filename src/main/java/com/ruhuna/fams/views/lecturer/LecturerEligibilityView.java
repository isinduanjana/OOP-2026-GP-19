package com.ruhuna.fams.views.lecturer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LecturerEligibilityView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public LecturerEligibilityView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("ELIGIBILITY CHECK - ICT1201");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Student TG", "Attendance %", "CA Mark", "Eligibility"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnExport = new JButton("EXPORT REPORT");
        add(btnExport, BorderLayout.SOUTH);

        loadEligibilityData();
    }

    private void loadEligibilityData() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"TG2144", "85%", "75.00", "ELIGIBLE"});
        tableModel.addRow(new Object[]{"TG2145", "78%", "55.00", "NOT ELIGIBLE"});
        tableModel.addRow(new Object[]{"TG2146", "80%", "64.00", "ELIGIBLE"});
    }
}

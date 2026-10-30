package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.TimetableController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TimetableManagementView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TimetableController timetableController;

    public TimetableManagementView() {
        timetableController = new TimetableController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Timetable Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Day", "Time", "Course", "Type", "Room"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);
        loadTimetableData();
    }

    private void loadTimetableData() {
        tableModel.setRowCount(0);
        List<Object[]> timetable = timetableController.getTimetable();
        for (Object[] row : timetable) {
            tableModel.addRow(row);
        }
    }
}

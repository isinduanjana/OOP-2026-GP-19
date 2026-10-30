package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.CourseController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManagementView extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private CourseController courseController;

    public CourseManagementView() {
        courseController = new CourseController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Course Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton btnAddCourse = new JButton("+ Add Course");
        btnAddCourse.setBackground(new Color(52, 152, 219));
        btnAddCourse.setForeground(Color.WHITE);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnAddCourse, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Code", "Course Name", "Credits", "Actions"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadCourseData();
    }

    private void loadCourseData() {
        tableModel.setRowCount(0);
        List<Object[]> courses = courseController.getAllCourses();
        for (Object[] row : courses) {
            tableModel.addRow(row);
        }
    }
}

package com.ruhuna.fams.views.student;

import com.ruhuna.fams.models.User;
import javax.swing.*;
import java.awt.*;

/** Student module entry point. The authenticated User is passed from LoginView. */
public class StudentDashboardView extends JFrame {
    public StudentDashboardView(User user) {
        setTitle("FAMS - Student Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 83, 132));
        JLabel title = new JLabel("  UNIVERSITY OF RUHUNA  |  STUDENT PORTAL");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(18, 8, 18, 8));
        JLabel welcome = new JLabel("Welcome, " + user.getFullName() + "   ");
        welcome.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);

       /* JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", new StudentProfilePanel(user.getUserId()));
        tabs.addTab("Attendance", new StudentAttendancePanel(user.getUserId()));
        tabs.addTab("Grades", new StudentGradesPanel(user.getUserId()));
        tabs.addTab("SGPA / CGPA", new StudentGpaPanel(user.getUserId()));*/

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new com.ruhuna.fams.views.LoginView().setVisible(true);
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(logout);
        add(header, BorderLayout.NORTH);
        //add(tabs, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}

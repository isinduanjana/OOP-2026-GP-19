package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.AdminController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import java.awt.*;

public class AdminDashboardView extends JFrame {
    private User currentUser;
    private AdminController adminController;

    public AdminDashboardView(User user) {
        this.currentUser = user;
        this.adminController = new AdminController();

        setTitle("Faculty Academic System - Admin Dashboard");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(850, 50));

        JLabel lblTitle = new JLabel("  Faculty Academic System");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel lblUser = new JLabel("Admin: " + currentUser.getFullName() + " | ");
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);
        rightHeader.add(lblUser);
        rightHeader.add(btnLogout);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(8, 1, 5, 5));
        sidebar.setBackground(new Color(236, 240, 241));
        sidebar.setPreferredSize(new Dimension(180, 500));

        String[] navButtons = {"Dashboard", "Users", "Courses", "Notices", "Timetable", "Reports", "Profile", "Logout"};
        for (String btnText : navButtons) {
            JButton btn = new JButton(btnText);
            if (btnText.equals("Logout")) {
                btn.addActionListener(e -> {
                    new LoginView().setVisible(true);
                    this.dispose();
                });
            } else {
                btn.addActionListener(e -> JOptionPane.showMessageDialog(this, btnText + " module clicked."));
            }
            sidebar.add(btn);
        }
        add(sidebar, BorderLayout.WEST);

        // Main Content Area
        JPanel mainContent = new JPanel(new GridLayout(2, 2, 15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainContent.add(createCard("USERS", String.valueOf(adminController.getCount("users")), new Color(52, 152, 219)));
        mainContent.add(createCard("COURSES", String.valueOf(adminController.getCount("courses")), new Color(46, 204, 113)));
        mainContent.add(createCard("NOTICES", String.valueOf(adminController.getCount("notices")), new Color(155, 89, 182)));
        mainContent.add(createCard("TIMETABLE", String.valueOf(adminController.getCount("timetables")), new Color(230, 126, 34)));

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String count, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblCount = new JLabel(count, SwingConstants.CENTER);
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 36));
        lblCount.setForeground(Color.WHITE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblCount, BorderLayout.CENTER);
        return card;
    }
}
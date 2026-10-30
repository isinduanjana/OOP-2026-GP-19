package com.ruhuna.fams.views.to;

import com.ruhuna.fams.models.User;
import com.ruhuna.fams.views.LoginView;
import javax.swing.*;
import java.awt.*;

public class TODashboardView extends JFrame {
    private User user;

    public TODashboardView(User user) {
        this.user = user;
        setTitle("Technical Officer Dashboard - Faculty System");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(39, 174, 96));
        header.setPreferredSize(new Dimension(850, 50));
        JLabel title = new JLabel("  Technical Officer: " + user.getFullName());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginView().setVisible(true);
            this.dispose();
        });

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        CardLayout cardLayout = new CardLayout();
        JPanel centerPanel = new JPanel(cardLayout);

        centerPanel.add(new TOAttendanceView(), "Attendance");
        centerPanel.add(new TOMedicalView(), "Medical");

        JPanel sidebar = new JPanel(new GridLayout(6, 1, 5, 5));
        sidebar.setPreferredSize(new Dimension(180, 500));
        String[] btns = {"Attendance Management", "Medical Management", "Logout"};
        for (String b : btns) {
            JButton btn = new JButton(b);
            btn.addActionListener(e -> {
                if(b.equals("Logout")) {
                    new LoginView().setVisible(true);
                    this.dispose();
                } else if(b.equals("Attendance Management")) {
                    cardLayout.show(centerPanel, "Attendance");
                } else if(b.equals("Medical Management")) {
                    cardLayout.show(centerPanel, "Medical");
                }
            });
            sidebar.add(btn);
        }

        add(sidebar, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }
}

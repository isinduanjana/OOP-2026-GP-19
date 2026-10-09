package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.AuthController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private AuthController authController;

    public LoginView() {
        authController = new AuthController();

        setTitle("Faculty Academic Management System - Login");
        setSize(450, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle1 = new JLabel("UNIVERSITY OF RUHUNA", SwingConstants.CENTER);
        lblTitle1.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel lblTitle2 = new JLabel("FACULTY OF TECHNOLOGY", SwingConstants.CENTER);
        lblTitle2.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel lblTitle3 = new JLabel("FACULTY ACADEMIC MANAGEMENT SYSTEM", SwingConstants.CENTER);
        lblTitle3.setFont(new Font("SansSerif", Font.PLAIN, 11));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle1, gbc);
        gbc.gridy = 1;
        panel.add(lblTitle2, gbc);
        gbc.gridy = 2;
        panel.add(lblTitle3, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        txtUsername = new JTextField(18);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        txtPassword = new JPasswordField(18);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        btnLogin = new JButton("LOGIN");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        add(panel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            User user = authController.login(username, password);
           /* if (user != null) {
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    new AdminDashboardView(user).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Logged in as " + user.getRole() + ". Module assigned to team member.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Error", JOptionPane.ERROR_MESSAGE);
            }*/
        });
    }
}

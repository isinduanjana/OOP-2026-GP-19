package com.ruhuna.fams.views;

import com.ruhuna.fams.controllers.AuthController;
import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginView extends JFrame {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final AuthController authController;

    // Color Palette
    private static final Color COLOR_BG_DARK = new Color(0x0D, 0x1B, 0x2A);
    private static final Color COLOR_BG_GRADIENT_END = new Color(0x16, 0x2A, 0x45);
    private static final Color COLOR_CARD_BG = new Color(0x1B, 0x2B, 0x3B);
    private static final Color COLOR_CARD_BORDER = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_ACCENT = new Color(0x4F, 0xC3, 0xF7);
    private static final Color COLOR_ACCENT_HOVER = new Color(0x81, 0xD4, 0xFA);
    private static final Color COLOR_TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_TEXT_MUTED = new Color(0x90, 0xA4, 0xAE);
    private static final Color COLOR_FIELD_BG = new Color(0x13, 0x22, 0x32);
    private static final Color COLOR_FIELD_BORDER = new Color(0x37, 0x47, 0x4F);

    public LoginView() {
        authController = new AuthController();

        setTitle("Faculty Academic Management System - Login");
        setSize(480, 580);
        setMinimumSize(new Dimension(420, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Background Panel with Deep Gradient
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_DARK, 0, getHeight(), COLOR_BG_GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        // Center Card Panel
        JPanel cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(COLOR_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(410, 480));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // University Emblem Logo Area
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 52;
                int x = (getWidth() - size) / 2;
                int y = 0;
                GradientPaint gp = new GradientPaint(x, y, new Color(0x0D, 0x47, 0xA1), x + size, y + size, new Color(0x19, 0x76, 0xD2));
                g2.setPaint(gp);
                g2.fillOval(x, y, size, size);
                g2.setColor(COLOR_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);
                g2.setColor(COLOR_TEXT_WHITE);
                g2.setFont(createFont(Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = "UoR";
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(100, 56));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        cardPanel.add(logoPanel, gbc);

        // Titles
        JLabel lblTitle1 = new JLabel("UNIVERSITY OF RUHUNA", SwingConstants.CENTER);
        lblTitle1.setFont(createFont(Font.BOLD, 12));
        lblTitle1.setForeground(COLOR_ACCENT);

        JLabel lblTitle2 = new JLabel("FACULTY OF TECHNOLOGY", SwingConstants.CENTER);
        lblTitle2.setFont(createFont(Font.BOLD, 16));
        lblTitle2.setForeground(COLOR_TEXT_WHITE);

        JLabel lblTitle3 = new JLabel("Faculty Academic Management System", SwingConstants.CENTER);
        lblTitle3.setFont(createFont(Font.PLAIN, 12));
        lblTitle3.setForeground(COLOR_TEXT_MUTED);

        gbc.gridy = 1;
        cardPanel.add(lblTitle1, gbc);
        gbc.gridy = 2;
        cardPanel.add(lblTitle2, gbc);
        gbc.gridy = 3;
        cardPanel.add(lblTitle3, gbc);

        // Spacer / Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_CARD_BORDER);
        sep.setBackground(COLOR_CARD_BORDER);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 6, 14, 6);
        cardPanel.add(sep, gbc);

        // Username Field
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.gridwidth = 2;
        gbc.gridy = 5;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(createFont(Font.BOLD, 12));
        lblUser.setForeground(COLOR_TEXT_WHITE);
        cardPanel.add(lblUser, gbc);

        txtUsername = new JTextField(18);
        styleField(txtUsername);
        gbc.gridy = 6;
        cardPanel.add(txtUsername, gbc);

        // Password Field
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 6, 4, 6);
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(createFont(Font.BOLD, 12));
        lblPass.setForeground(COLOR_TEXT_WHITE);
        cardPanel.add(lblPass, gbc);

        txtPassword = new JPasswordField(18);
        styleField(txtPassword);
        gbc.gridy = 8;
        gbc.insets = new Insets(4, 6, 16, 6);
        cardPanel.add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = createStyledButton("LOGIN", COLOR_ACCENT, COLOR_ACCENT_HOVER, new Color(0x0D, 0x1B, 0x2A));
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 6, 8, 6);
        cardPanel.add(btnLogin, gbc);

        // Footer helper text
        JLabel lblFooter = new JLabel("Student & Staff Portal Sign-in", SwingConstants.CENTER);
        lblFooter.setFont(createFont(Font.PLAIN, 11));
        lblFooter.setForeground(COLOR_TEXT_MUTED);
        gbc.gridy = 10;
        gbc.insets = new Insets(6, 6, 4, 6);
        cardPanel.add(lblFooter, gbc);

        bgPanel.add(cardPanel);
        add(bgPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);
    }

    private static Font createFont(int style, int size) {
        Font f = new Font("Segoe UI", style, size);
        if (!f.getFamily().equalsIgnoreCase("Segoe UI")) {
            f = new Font("SansSerif", style, size);
        }
        return f;
    }

    private static void styleField(JTextField field) {
        field.setFont(createFont(Font.PLAIN, 13));
        field.setBackground(COLOR_FIELD_BG);
        field.setForeground(COLOR_TEXT_WHITE);
        field.setCaretColor(COLOR_ACCENT);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        field.setBorder(normalBorder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focusBorder);
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
            }
        });
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverBg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(createFont(Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authController.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password, or account is inactive.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = user.getRole();
            if (role == null) {
                JOptionPane.showMessageDialog(this, "This account has no assigned role.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("ADMIN".equalsIgnoreCase(role)) {
                new AdminDashboardView(user).setVisible(true);
                dispose();
            } else if ("UNDERGRADUATE".equalsIgnoreCase(role)) {
                new com.ruhuna.fams.views.student.StudentDashboardView(user).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Login successful, but the " + role + " dashboard is not connected yet.",
                        "Module Not Connected", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not log in. Check your database connection and AuthController.\nDetails: "
                            + ex.getMessage(),
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.ruhuna.fams.views.student;

import com.ruhuna.fams.models.User;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StudentDashboardView extends JFrame {

    // Color Palette
    private static final Color COLOR_BG_DARK = new Color(0x0D, 0x1B, 0x2A);
    private static final Color COLOR_CARD_BG = new Color(0x1B, 0x2B, 0x3B);
    private static final Color COLOR_CARD_BORDER = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_ACCENT = new Color(0x4F, 0xC3, 0xF7);
    private static final Color COLOR_TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_TEXT_MUTED = new Color(0x90, 0xA4, 0xAE);
    private static final Color COLOR_HEADER_START = new Color(0x0D, 0x1B, 0x2A);
    private static final Color COLOR_HEADER_END = new Color(0x1B, 0x4B, 0x6B);
    private static final Color COLOR_SUCCESS = new Color(0x00, 0xC8, 0x96);
    private static final Color COLOR_DANGER = new Color(0xEF, 0x53, 0x50);
    private static final Color COLOR_DANGER_HOVER = new Color(0xD3, 0x2F, 0x2F);

    public StudentDashboardView(User user) {
        setTitle("FAMS - Student Dashboard");
        setSize(1150, 750);
        setMinimumSize(new Dimension(980, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // Header Panel with Gradient from #0D1B2A to #1B4B6B
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_HEADER_START, getWidth(), getHeight(), COLOR_HEADER_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle accent line at bottom of header
                g2.setColor(COLOR_ACCENT);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Header Left: University branding & portal title
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel uniSubtitle = new JLabel("UNIVERSITY OF RUHUNA  •  FACULTY OF TECHNOLOGY");
        uniSubtitle.setForeground(COLOR_ACCENT);
        uniSubtitle.setFont(createFont(Font.BOLD, 11));

        JLabel title = new JLabel("STUDENT ACADEMIC PORTAL");
        title.setForeground(COLOR_TEXT_WHITE);
        title.setFont(createFont(Font.BOLD, 20));

        titlePanel.add(uniSubtitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(title);

        // Header Right: User welcome and role badge
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        JPanel userText = new JPanel();
        userText.setOpaque(false);
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));

        JLabel welcomePrefix = new JLabel("Welcome back,");
        welcomePrefix.setForeground(COLOR_TEXT_MUTED);
        welcomePrefix.setFont(createFont(Font.PLAIN, 12));
        welcomePrefix.setAlignmentX(Component.RIGHT_ALIGNMENT);

        String displayName = (user.getFullName() != null && !user.getFullName().trim().isEmpty())
                ? user.getFullName()
                : user.getUsername();
        JLabel welcomeName = new JLabel(displayName);
        welcomeName.setForeground(COLOR_TEXT_WHITE);
        welcomeName.setFont(createFont(Font.BOLD, 15));
        welcomeName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userText.add(welcomePrefix);
        userText.add(welcomeName);

        // Role Pill Badge
        JLabel rolePill = new JLabel("STUDENT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x00, 0xC8, 0x96, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(COLOR_SUCCESS);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rolePill.setForeground(COLOR_SUCCESS);
        rolePill.setFont(createFont(Font.BOLD, 11));
        rolePill.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        userPanel.add(userText);
        userPanel.add(rolePill);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        // Set UIManager properties for TabbedPane prior to creation
        UIManager.put("TabbedPane.background", COLOR_BG_DARK);
        UIManager.put("TabbedPane.foreground", COLOR_TEXT_MUTED);
        UIManager.put("TabbedPane.selected", COLOR_CARD_BG);
        UIManager.put("TabbedPane.contentAreaColor", COLOR_BG_DARK);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 24, 10, 24));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(COLOR_BG_DARK);
        tabs.setForeground(COLOR_TEXT_WHITE);
        tabs.setFont(createFont(Font.BOLD, 13));
        tabs.setOpaque(true);

        // Custom UI for sleek modern dark tabs
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(10, 26, 10, 26);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(2, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(COLOR_CARD_BG);
                } else {
                    g2.setColor(new Color(0x10, 0x20, 0x30));
                }
                g2.fillRect(x, y, w, h);
                if (isSelected) {
                    g2.setColor(COLOR_ACCENT);
                    g2.fillRect(x, y + h - 3, w, 3);
                }
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String tabTitle, Rectangle textRect, boolean isSelected) {
                g.setFont(isSelected ? font.deriveFont(Font.BOLD) : font);
                g.setColor(isSelected ? COLOR_ACCENT : COLOR_TEXT_MUTED);
                g.drawString(tabTitle, textRect.x, textRect.y + metrics.getAscent());
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // Borderless modern look
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawLine(0, 0, tabPane.getWidth(), 0);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // Suppress dotted focus outline
            }
        });

        tabs.addTab("My Profile", new StudentProfilePanel(user.getUserId()));
        tabs.addTab("Attendance", new StudentAttendancePanel(user.getUserId()));
        tabs.addTab("Grades", new StudentGradesPanel(user.getUserId()));
        tabs.addTab("SGPA / CGPA", new StudentGpaPanel(user.getUserId()));

        // Modern Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_BG_DARK);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));

        JLabel footerBrand = new JLabel("Faculty Academic Management System  •  University of Ruhuna");
        footerBrand.setForeground(COLOR_TEXT_MUTED);
        footerBrand.setFont(createFont(Font.PLAIN, 12));

        JButton logout = createStyledButton("Logout", COLOR_DANGER, COLOR_DANGER_HOVER, COLOR_TEXT_WHITE);
        logout.addActionListener(e -> {
            dispose();
            new com.ruhuna.fams.views.LoginView().setVisible(true);
        });

        footer.add(footerBrand, BorderLayout.WEST);
        footer.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private static Font createFont(int style, int size) {
        Font f = new Font("Segoe UI", style, size);
        if (!f.getFamily().equalsIgnoreCase("Segoe UI")) {
            f = new Font("SansSerif", style, size);
        }
        return f;
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
        btn.setFont(createFont(Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 20));
        return btn;
    }
}

package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.*;

public class StudentProfilePanel extends JPanel {
    private final int userId;
    private final JTextField username = new JTextField(24);
    private final JTextField tgNo = new JTextField(24);
    private final JTextField batch = new JTextField(24);
    private final JTextField semester = new JTextField(24);
    private final JTextField fullName = new JTextField(24);
    private final JTextField email = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextArea address = new JTextArea(3, 24);

    // Color Palette
    private static final Color COLOR_BG_DARK = new Color(0x0D, 0x1B, 0x2A);
    private static final Color COLOR_CARD_BG = new Color(0x1B, 0x2B, 0x3B);
    private static final Color COLOR_CARD_BORDER = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_ACCENT = new Color(0x4F, 0xC3, 0xF7);
    private static final Color COLOR_ACCENT_HOVER = new Color(0x81, 0xD4, 0xFA);
    private static final Color COLOR_TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_TEXT_MUTED = new Color(0x90, 0xA4, 0xAE);
    private static final Color COLOR_FIELD_BG = new Color(0x13, 0x22, 0x32);
    private static final Color COLOR_READONLY_BG = new Color(0x11, 0x1E, 0x2C);
    private static final Color COLOR_FIELD_BORDER = new Color(0x37, 0x47, 0x4F);

    public StudentProfilePanel(int userId) {
        this.userId = userId;
        setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // Card Container Panel for Form
        JPanel cardPanel = new JPanel(new BorderLayout(16, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        // Card Header
        JPanel cardHeader = new JPanel();
        cardHeader.setOpaque(false);
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("My Student Profile");
        titleLabel.setForeground(COLOR_TEXT_WHITE);
        titleLabel.setFont(createFont(Font.BOLD, 18));

        JLabel subtitleLabel = new JLabel("View your registered student record and update your personal contact details");
        subtitleLabel.setForeground(COLOR_TEXT_MUTED);
        subtitleLabel.setFont(createFont(Font.PLAIN, 12));

        cardHeader.add(titleLabel);
        cardHeader.add(Box.createVerticalStrut(4));
        cardHeader.add(subtitleLabel);
        cardHeader.add(Box.createVerticalStrut(14));

        // Horizontal separator line
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_CARD_BORDER);
        sep.setBackground(COLOR_CARD_BORDER);
        cardHeader.add(sep);

        // Form Layout using GridBagLayout
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Section 1 Header: Academic Information
        addSectionHeader(form, g, 0, "ACADEMIC DETAILS (OFFICIAL RECORD)");
        addRow(form, g, 1, "Username (read only):", username);
        addRow(form, g, 2, "Registration / TG No (read only):", tgNo);
        addRow(form, g, 3, "Batch (read only):", batch);
        addRow(form, g, 4, "Current Semester (read only):", semester);

        // Section 2 Header: Contact Details
        addSectionHeader(form, g, 5, "PERSONAL & CONTACT INFORMATION");
        addRow(form, g, 6, "Full Name:", fullName);
        addRow(form, g, 7, "Email Address:", email);
        addRow(form, g, 8, "Phone Number:", phone);

        // Address Field Styling
        styleTextArea(address);
        JScrollPane addressScroll = new JScrollPane(address);
        addressScroll.setBackground(COLOR_FIELD_BG);
        addressScroll.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1));
        addressScroll.getViewport().setBackground(COLOR_FIELD_BG);
        addRow(form, g, 9, "Residential Address:", addressScroll);

        // Mark read-only fields
        username.setEditable(false);
        tgNo.setEditable(false);
        batch.setEditable(false);
        semester.setEditable(false);

        styleField(username, false);
        styleField(tgNo, false);
        styleField(batch, false);
        styleField(semester, false);
        styleField(fullName, true);
        styleField(email, true);
        styleField(phone, true);

        // Action Buttons
        JButton refresh = createStyledButton("Reload Profile", new Color(0x22, 0x33, 0x44), new Color(0x2D, 0x43, 0x58), COLOR_ACCENT, true);
        JButton save = createStyledButton("Update Profile", COLOR_ACCENT, COLOR_ACCENT_HOVER, new Color(0x0D, 0x1B, 0x2A), false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(save);

        cardPanel.add(cardHeader, BorderLayout.NORTH);
        cardPanel.add(form, BorderLayout.CENTER);
        cardPanel.add(actions, BorderLayout.SOUTH);

        // Centering container with scroll support
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints gbcWrap = new GridBagConstraints();
        gbcWrap.gridx = 0;
        gbcWrap.gridy = 0;
        gbcWrap.weightx = 1.0;
        gbcWrap.fill = GridBagConstraints.HORIZONTAL;
        gbcWrap.insets = new Insets(20, 40, 20, 40);
        centerWrapper.add(cardPanel, gbcWrap);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_BG_DARK);
        scrollPane.getViewport().setBackground(COLOR_BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);

        refresh.addActionListener(e -> loadProfile());
        save.addActionListener(e -> updateProfile());
        loadProfile();
    }

    private void addSectionHeader(JPanel p, GridBagConstraints g, int row, String title) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.insets = new Insets(row == 0 ? 10 : 20, 8, 8, 8);
        JLabel lbl = new JLabel(title);
        lbl.setFont(createFont(Font.BOLD, 12));
        lbl.setForeground(COLOR_ACCENT);
        p.add(lbl, g);
        g.gridwidth = 1;
        g.insets = new Insets(6, 8, 6, 8);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, Component field) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(createFont(Font.PLAIN, 13));
        lbl.setForeground(COLOR_TEXT_WHITE);
        lbl.setPreferredSize(new Dimension(200, 28));
        p.add(lbl, g);

        g.gridx = 1;
        g.weightx = 1;
        p.add(field, g);
    }

    private void styleField(JTextField field, boolean editable) {
        field.setFont(createFont(Font.PLAIN, 13));
        field.setCaretColor(COLOR_ACCENT);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(editable ? COLOR_FIELD_BORDER : new Color(0x22, 0x32, 0x44), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );

        if (editable) {
            field.setBackground(COLOR_FIELD_BG);
            field.setForeground(COLOR_TEXT_WHITE);
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
        } else {
            field.setBackground(COLOR_READONLY_BG);
            field.setForeground(COLOR_TEXT_MUTED);
            field.setBorder(normalBorder);
        }
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(createFont(Font.PLAIN, 13));
        area.setBackground(COLOR_FIELD_BG);
        area.setForeground(COLOR_TEXT_WHITE);
        area.setCaretColor(COLOR_ACCENT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private static Font createFont(int style, int size) {
        Font f = new Font("Segoe UI", style, size);
        if (!f.getFamily().equalsIgnoreCase("Segoe UI")) {
            f = new Font("SansSerif", style, size);
        }
        return f;
    }

    private static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg, boolean isOutline) {
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
                if (isOutline) {
                    g2.setColor(COLOR_ACCENT);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
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
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        return btn;
    }

    private void loadProfile() {
        String sql = "SELECT u.username, up.full_name, up.email, up.phone, up.address, " +
                "ug.tg_no, ug.batch, ug.semester FROM users u " +
                "LEFT JOIN user_profiles up ON up.user_id=u.user_id " +
                "LEFT JOIN undergraduates ug ON ug.user_id=u.user_id WHERE u.user_id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    username.setText(value(rs, "username"));
                    fullName.setText(value(rs, "full_name"));
                    email.setText(value(rs, "email"));
                    phone.setText(value(rs, "phone"));
                    address.setText(value(rs, "address"));
                    tgNo.setText(value(rs, "tg_no"));
                    batch.setText(value(rs, "batch"));
                    semester.setText(value(rs, "semester"));
                } else {
                    message("Student profile was not found.");
                }
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void updateProfile() {
        if (fullName.getText().trim().isEmpty()) {
            message("Full name is required.");
            return;
        }
        String em = email.getText().trim();
        if (!em.isEmpty() && !em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            message("Enter a valid email address.");
            return;
        }
        String sql = "INSERT INTO user_profiles(user_id,full_name,email,phone,address) VALUES(?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), email=VALUES(email), phone=VALUES(phone), address=VALUES(address)";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, fullName.getText().trim());
                ps.setString(3, em.isEmpty() ? null : em);
                ps.setString(4, phone.getText().trim().isEmpty() ? null : phone.getText().trim());
                ps.setString(5, address.getText().trim().isEmpty() ? null : address.getText().trim());
                ps.executeUpdate();
                message("Profile updated successfully.");
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private String value(ResultSet rs, String col) throws SQLException {
        Object v = rs.getObject(col);
        return v == null ? "" : String.valueOf(v);
    }

    private void message(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

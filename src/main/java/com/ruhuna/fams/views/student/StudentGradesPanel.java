package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class StudentGradesPanel extends JPanel {
    private final int userId;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Course Code", "Course Name", "Credits", "Total Marks", "Grade", "Grade Point", "Stored CGPA"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    // Color Palette
    private static final Color COLOR_BG_DARK = new Color(0x0D, 0x1B, 0x2A);
    private static final Color COLOR_CARD_BG = new Color(0x1B, 0x2B, 0x3B);
    private static final Color COLOR_CARD_BORDER = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_ACCENT = new Color(0x4F, 0xC3, 0xF7);
    private static final Color COLOR_ACCENT_HOVER = new Color(0x81, 0xD4, 0xFA);
    private static final Color COLOR_TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_TEXT_MUTED = new Color(0x90, 0xA4, 0xAE);
    private static final Color COLOR_TABLE_HEADER_BG = new Color(0x0D, 0x47, 0xA1);
    private static final Color COLOR_ROW_EVEN = new Color(0x1B, 0x2B, 0x3B);
    private static final Color COLOR_ROW_ODD = new Color(0x16, 0x23, 0x30);
    private static final Color COLOR_ROW_SELECTED = new Color(0x22, 0x42, 0x63);
    private static final Color COLOR_SUCCESS = new Color(0x00, 0xC8, 0x96);
    private static final Color COLOR_WARNING = new Color(0xFF, 0xB7, 0x4D);
    private static final Color COLOR_DANGER = new Color(0xEF, 0x53, 0x50);

    public StudentGradesPanel(int userId) {
        this.userId = userId;
        setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Academic Grades & Performance");
        titleLbl.setFont(createFont(Font.BOLD, 17));
        titleLbl.setForeground(COLOR_TEXT_WHITE);

        JLabel subLbl = new JLabel("Official published grades, marks, and weighted grade points per course");
        subLbl.setFont(createFont(Font.PLAIN, 12));
        subLbl.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subLbl);

        JButton refresh = createStyledButton("Refresh Grades", COLOR_ACCENT, COLOR_ACCENT_HOVER, new Color(0x0D, 0x1B, 0x2A));
        refresh.addActionListener(e -> load());

        topBar.add(titleBox, BorderLayout.WEST);
        topBar.add(refresh, BorderLayout.EAST);

        // Table Setup
        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_BG_DARK);
        scrollPane.setBackground(COLOR_BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1));

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        load();
    }

    private void styleTable(JTable table) {
        table.setFont(createFont(Font.PLAIN, 13));
        table.setForeground(COLOR_TEXT_WHITE);
        table.setBackground(COLOR_CARD_BG);
        table.setSelectionBackground(COLOR_ROW_SELECTED);
        table.setSelectionForeground(COLOR_TEXT_WHITE);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0x23, 0x36, 0x48));
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 38));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                lbl.setFont(createFont(Font.BOLD, 13));
                lbl.setBackground(COLOR_TABLE_HEADER_BG);
                lbl.setForeground(COLOR_TEXT_WHITE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_ACCENT),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                return lbl;
            }
        });

        // Default Cell Renderer with Alternating Rows
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setFont(createFont(Font.PLAIN, 13));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (isSel) {
                    lbl.setBackground(COLOR_ROW_SELECTED);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                } else {
                    lbl.setBackground((row % 2 == 0) ? COLOR_ROW_EVEN : COLOR_ROW_ODD);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                }

                // Alignments
                if (col == 1) {
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                }

                // Stored CGPA highlighting
                if (col == 6 && val != null) {
                    lbl.setFont(createFont(Font.BOLD, 13));
                    lbl.setForeground(COLOR_ACCENT);
                }

                return lbl;
            }
        };

        // Grade Column Renderer (col 4)
        DefaultTableCellRenderer gradeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setFont(createFont(Font.BOLD, 14));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (isSel) {
                    lbl.setBackground(COLOR_ROW_SELECTED);
                } else {
                    lbl.setBackground((row % 2 == 0) ? COLOR_ROW_EVEN : COLOR_ROW_ODD);
                }

                String grade = val == null ? "" : val.toString().trim().toUpperCase();
                if (grade.startsWith("A")) {
                    lbl.setForeground(COLOR_SUCCESS);
                } else if (grade.startsWith("B")) {
                    lbl.setForeground(COLOR_ACCENT);
                } else if (grade.startsWith("C")) {
                    lbl.setForeground(new Color(0x81, 0xD4, 0xFA));
                } else if (grade.startsWith("D")) {
                    lbl.setForeground(COLOR_WARNING);
                } else if (grade.startsWith("E") || grade.startsWith("F")) {
                    lbl.setForeground(COLOR_DANGER);
                } else {
                    lbl.setForeground(COLOR_TEXT_WHITE);
                }
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 4) {
                table.getColumnModel().getColumn(i).setCellRenderer(gradeRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }
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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private void load() {
        model.setRowCount(0);
        String sql = "SELECT c.course_code,c.course_name,c.credits,g.total_marks,g.grade,g.grade_point,g.cgpa " +
                "FROM undergraduates ug JOIN grades g ON g.student_id=ug.student_id " +
                "JOIN courses c ON c.course_id=g.course_id WHERE ug.user_id=? ORDER BY c.course_code";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getInt("credits"),
                                rs.getObject("total_marks"),
                                rs.getString("grade"),
                                rs.getObject("grade_point"),
                                rs.getObject("cgpa")
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Grades Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

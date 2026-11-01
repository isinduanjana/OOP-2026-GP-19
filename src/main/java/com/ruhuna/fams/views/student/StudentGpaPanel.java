package com.ruhuna.fams.views.student;

import com.ruhuna.fams.config.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class StudentGpaPanel extends JPanel {
    private final int userId;
    private final JComboBox<String> academicYear = new JComboBox<>();
    private final JComboBox<Integer> semester = new JComboBox<>();
    private final JLabel sgpaLabel = new JLabel("SGPA: —");
    private final JLabel cgpaLabel = new JLabel("CGPA: —");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Academic Year", "Semester", "Credits", "Weighted Grade Points", "GPA"}, 0) {
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
    private static final Color COLOR_FIELD_BG = new Color(0x13, 0x22, 0x32);
    private static final Color COLOR_FIELD_BORDER = new Color(0x37, 0x47, 0x4F);

    public StudentGpaPanel(int userId) {
        this.userId = userId;
        setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top Area: Filter and Stat Cards
        JPanel topContainer = new JPanel(new BorderLayout(16, 16));
        topContainer.setOpaque(false);

        // Filter Card
        JPanel filterCard = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(COLOR_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        filterCard.setOpaque(false);
        filterCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // Filter Controls Title
        JLabel filterTitle = new JLabel("Academic Standing & Performance Overview");
        filterTitle.setFont(createFont(Font.BOLD, 15));
        filterTitle.setForeground(COLOR_TEXT_WHITE);

        // Dropdowns and action button
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        controls.setOpaque(false);

        JLabel lblYear = new JLabel("Academic Year:");
        lblYear.setFont(createFont(Font.BOLD, 12));
        lblYear.setForeground(COLOR_TEXT_WHITE);

        styleComboBox(academicYear);
        academicYear.setPreferredSize(new Dimension(140, 34));

        JLabel lblSem = new JLabel("Semester:");
        lblSem.setFont(createFont(Font.BOLD, 12));
        lblSem.setForeground(COLOR_TEXT_WHITE);

        styleComboBox(semester);
        semester.setPreferredSize(new Dimension(80, 34));

        JButton calc = createStyledButton("Calculate SGPA / CGPA", COLOR_ACCENT, COLOR_ACCENT_HOVER, new Color(0x0D, 0x1B, 0x2A));

        controls.add(lblYear);
        controls.add(academicYear);
        controls.add(lblSem);
        controls.add(semester);
        controls.add(calc);

        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(controls, BorderLayout.CENTER);

        // Stat Cards for SGPA and CGPA
        JPanel statCardsPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        statCardsPanel.setOpaque(false);

        // SGPA Card
        JPanel sgpaCard = createGpaStatCard("SEMESTER GPA", sgpaLabel, COLOR_ACCENT);
        // CGPA Card
        JPanel cgpaCard = createGpaStatCard("CUMULATIVE GPA", cgpaLabel, COLOR_SUCCESS);

        statCardsPanel.add(sgpaCard);
        statCardsPanel.add(cgpaCard);

        topContainer.add(filterCard, BorderLayout.CENTER);
        topContainer.add(statCardsPanel, BorderLayout.EAST);

        // Table Setup
        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_BG_DARK);
        scrollPane.setBackground(COLOR_BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1));

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        academicYear.addActionListener(e -> loadSemesters());
        semester.addActionListener(e -> calculate());
        calc.addActionListener(e -> calculate());

        loadAcademicYears();
    }

    private JPanel createGpaStatCard(String headerText, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(COLOR_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(200, 85));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel(headerText);
        title.setFont(createFont(Font.BOLD, 11));
        title.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(createFont(Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(createFont(Font.PLAIN, 13));
        box.setBackground(COLOR_FIELD_BG);
        box.setForeground(COLOR_TEXT_WHITE);
        box.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(createFont(Font.PLAIN, 13));
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                if (isSelected) {
                    lbl.setBackground(COLOR_ROW_SELECTED);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                } else {
                    lbl.setBackground(COLOR_FIELD_BG);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                }
                return lbl;
            }
        });
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

        // Alternating Rows Cell Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                lbl.setFont(createFont(Font.PLAIN, 13));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSel) {
                    lbl.setBackground(COLOR_ROW_SELECTED);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                } else {
                    lbl.setBackground((row % 2 == 0) ? COLOR_ROW_EVEN : COLOR_ROW_ODD);
                    lbl.setForeground(COLOR_TEXT_WHITE);
                }

                // GPA Column (col 4) Highlighting
                if (col == 4 && val != null) {
                    lbl.setFont(createFont(Font.BOLD, 14));
                    lbl.setForeground(COLOR_ACCENT);
                }

                return lbl;
            }
        });
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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private void loadAcademicYears() {
        academicYear.removeAllItems();
        String sql = "SELECT DISTINCT e.academic_year FROM enrollments e JOIN undergraduates ug ON ug.student_id=e.student_id WHERE ug.user_id=? ORDER BY e.academic_year";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        academicYear.addItem(rs.getString(1));
                    }
                }
            }
            loadSemesters();
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void loadSemesters() {
        String year = (String) academicYear.getSelectedItem();
        if (year == null) return;
        Object old = semester.getSelectedItem();
        semester.removeAllItems();
        String sql = "SELECT DISTINCT e.semester FROM enrollments e JOIN undergraduates ug ON ug.student_id=e.student_id WHERE ug.user_id=? AND e.academic_year=? ORDER BY e.semester";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (c == null) throw new SQLException("Could not connect to database.");
            ps.setInt(1, userId);
            ps.setString(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    semester.addItem(rs.getInt(1));
                }
            }
            if (old instanceof Integer && containsSemester((Integer) old)) {
                semester.setSelectedItem(old);
            }
            calculate();
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private boolean containsSemester(Integer v) {
        for (int i = 0; i < semester.getItemCount(); i++) {
            if (semester.getItemAt(i).equals(v)) return true;
        }
        return false;
    }

    private void calculate() {
        String year = (String) academicYear.getSelectedItem();
        Integer sem = (Integer) semester.getSelectedItem();
        if (year == null || sem == null) {
            model.setRowCount(0);
            sgpaLabel.setText("SGPA: N/A");
            cgpaLabel.setText("CGPA: N/A");
            return;
        }
        model.setRowCount(0);
        BigDecimal totalWeighted = BigDecimal.ZERO;
        int totalCredits = 0;
        BigDecimal selectedWeighted = BigDecimal.ZERO;
        int selectedCredits = 0;

        String sql = "SELECT e.academic_year,e.semester,c.credits,g.grade_point FROM undergraduates ug " +
                "JOIN enrollments e ON e.student_id=ug.student_id AND e.status IN ('ENROLLED','COMPLETED') " +
                "JOIN courses c ON c.course_id=e.course_id LEFT JOIN grades g ON g.student_id=e.student_id AND g.course_id=e.course_id " +
                "WHERE ug.user_id=? ORDER BY e.academic_year,e.semester,c.course_code";
        Map<String, BigDecimal[]> groups = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException("Could not connect to database. Check DBConnection settings.");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String y = rs.getString("academic_year");
                        int s = rs.getInt("semester");
                        int cr = rs.getInt("credits");
                        BigDecimal gp = rs.getBigDecimal("grade_point");
                        if (gp == null || cr <= 0) continue;
                        BigDecimal weighted = gp.multiply(BigDecimal.valueOf(cr));
                        String key = y + "|" + s;
                        BigDecimal[] pair = groups.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                        pair[0] = pair[0].add(BigDecimal.valueOf(cr));
                        pair[1] = pair[1].add(weighted);
                        totalCredits += cr;
                        totalWeighted = totalWeighted.add(weighted);
                        if (y.equals(year) && s == sem) {
                            selectedCredits += cr;
                            selectedWeighted = selectedWeighted.add(weighted);
                        }
                    }
                }
            }
            for (Map.Entry<String, BigDecimal[]> entry : groups.entrySet()) {
                String[] parts = entry.getKey().split("\\|");
                BigDecimal credits = entry.getValue()[0];
                BigDecimal weighted = entry.getValue()[1];
                BigDecimal gpa = weighted.divide(credits, 2, RoundingMode.HALF_UP);
                model.addRow(new Object[]{parts[0], parts[1], credits.intValue(), weighted.setScale(2, RoundingMode.HALF_UP), gpa});
            }
            sgpaLabel.setText("SGPA: " + (selectedCredits == 0 ? "N/A" : selectedWeighted.divide(BigDecimal.valueOf(selectedCredits), 2, RoundingMode.HALF_UP)));
            cgpaLabel.setText("CGPA: " + (totalCredits == 0 ? "N/A" : totalWeighted.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP)));
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "GPA Calculation Error", JOptionPane.ERROR_MESSAGE);
    }
}

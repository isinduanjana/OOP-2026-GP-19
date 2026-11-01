package lk.ac.ruh.fot.ams.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.ui.Theme;

/**
 * [COMMON PART] Factory of ready-styled Swing components and dialogs, so
 * every member builds screens the same way:
 *
 *   JTable table = UI.table(model);          // striped modern table
 *   panel.add(UI.scroll(table));
 *   UI.error(this, ex);                      // friendly error dialog
 */
public final class UI {

    private UI() {
    }

    // ------------------------------------------------------------------ labels
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_TITLE);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADING);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    // ------------------------------------------------------------------ inputs
    public static JTextField textField(int columns) {
        JTextField f = new JTextField(columns);
        f.putClientProperty("JComponent.roundRect", true);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 36));
        return f;
    }

    public static JTextField searchField(String placeholder) {
        JTextField f = textField(18);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.putClientProperty("JTextField.leadingIcon", Icons.get("search", 16, Theme.TEXT_MUTED));
        f.putClientProperty("JTextField.showClearButton", true);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.putClientProperty("JPasswordField.showRevealButton", true);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 36));
        return f;
    }

    public static JTextArea textArea(int rows) {
        JTextArea a = new JTextArea(rows, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return a;
    }

    // ------------------------------------------------------------------ buttons
    public static ModernButton primary(String text, String icon) {
        return new ModernButton(text, icon, ModernButton.Style.PRIMARY);
    }

    public static ModernButton secondary(String text, String icon) {
        return new ModernButton(text, icon, ModernButton.Style.SECONDARY);
    }

    public static ModernButton danger(String text, String icon) {
        return new ModernButton(text, icon, ModernButton.Style.DANGER);
    }

    public static ModernButton success(String text, String icon) {
        return new ModernButton(text, icon, ModernButton.Style.SUCCESS);
    }

    /** Transparent horizontal row of components (toolbars, filters). */
    public static JPanel row(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (Component c : components) p.add(c);
        return p;
    }

    public static JPanel rowRight(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        for (Component c : components) p.add(c);
        return p;
    }

    /** Toolbar with components on the left and right. */
    public static JPanel toolbar(JComponent left, JComponent right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        if (left != null) p.add(left, BorderLayout.WEST);
        if (right != null) p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Two-column form: label | field. Usage:
     *   JPanel form = UI.form(new String[]{"Name", "Email"}, nameField, emailField);
     */
    public static JPanel form(String[] labels, JComponent... fields) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 10);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = fields[i] instanceof JScrollPane ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
            p.add(label(labels[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(fields[i], c);
        }
        return p;
    }

    // ------------------------------------------------------------------ tables
    /** Creates a sortable, striped table with modern styling. */
    public static JTable table(DataTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(Theme.BORDER);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(false);
        t.setRowSorter(new TableRowSorter<>(model));
        t.setDefaultRenderer(Object.class, new StatusRenderer());
        t.setDefaultRenderer(Double.class, new StatusRenderer());
        t.setDefaultRenderer(Integer.class, new StatusRenderer());
        JTableHeader h = t.getTableHeader();
        h.setFont(Theme.FONT_BOLD);
        h.setBackground(Theme.tint(Theme.PRIMARY, 0.93));
        h.setForeground(Theme.TEXT);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 38));
        h.setReorderingAllowed(false);
        return t;
    }

    /** Index of the selected row in the MODEL (handles sorting), or -1. */
    public static int selectedModelRow(JTable t) {
        int v = t.getSelectedRow();
        return v < 0 ? -1 : t.convertRowIndexToModel(v);
    }

    /** Filters the table rows by the given text (search box). */
    @SuppressWarnings("unchecked")
    public static void filter(JTable t, String text) {
        TableRowSorter<DataTableModel> sorter = (TableRowSorter<DataTableModel>) t.getRowSorter();
        if (text == null || text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text.trim())));
        }
    }

    public static void setColumnWidths(JTable t, int... widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++) {
            if (widths[i] > 0) t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ------------------------------------------------------------------ dialogs
    public static void error(Component parent, AppException e) {
        JOptionPane.showMessageDialog(parent, e.getUserMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ------------------------------------------------------------------ badge
    /** Coloured rounded label, e.g. UI.badge("Eligible", Theme.SUCCESS). */
    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.tint(color, 0.85));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setFont(Theme.FONT_SMALL.deriveFont(java.awt.Font.BOLD));
        l.setForeground(Theme.darker(color, 0.15));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    /** Colour used for a status word (Eligible, Approved, Absent ...). */
    public static Color statusColor(String text) {
        if (text == null) return null;
        String t = text.toLowerCase().trim();
        if (t.length() > 40 || t.contains("/") || t.contains(".pdf")) {
            return null;            // long text / file paths are never status words
        }
        if (t.startsWith("absent (medical - approved")) {
            return Theme.INFO;      // absence excused by an approved medical
        }
        if (t.startsWith("not eligible") || t.startsWith("absent") || t.equals("rejected") || t.startsWith("repeat req")
                || t.equals("inactive") || t.startsWith("< 80") || t.equals("fail") || t.equals("e")) {
            return Theme.DANGER;
        }
        if (t.startsWith("≥ 80")) {
            return Theme.SUCCESS;
        }
        if (t.equals("pending") || t.contains("pending") || t.startsWith("repeat") || t.contains("medical")
                || t.equals("exactly 80%")) {
            return Theme.WARNING;
        }
        if (t.equals("eligible") || t.equals("approved") || t.equals("present") || t.equals("pass")
                || t.equals("active") || t.startsWith("above 80") || t.startsWith("≥ 80") || t.equals("yes")) {
            return Theme.SUCCESS;
        }
        return null;
    }

    /** Table renderer: zebra rows + coloured status words. */
    public static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Object shown = value instanceof Double d ? String.format("%.2f", d) : value;
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, shown, isSelected, false, row, column);
            c.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            c.setFont(Theme.FONT);
            c.setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.TABLE_STRIPE);
                Color sc = value instanceof String s ? statusColor(s) : null;
                c.setForeground(sc == null ? Theme.TEXT : sc);
                if (sc != null) c.setFont(Theme.FONT_BOLD);
            }
            return c;
        }
    }
}

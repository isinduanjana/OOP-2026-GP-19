package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Small static helpers shared by the admin screens
 * (on top of the common UI factory class). Utility class: private constructor.
 */
final class AdminUI {

    private AdminUI() {
    }

    /** "MONDAY" -> "Monday". */
    static String capital(String s) {
        if (s == null || s.isEmpty()) return "";
        String t = s.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    /** Combo of departments; a null item is shown as the given text ("All departments" / "None"). */
    static JComboBox<Department> departmentCombo(List<Department> departments, String nullText) {
        JComboBox<Department> combo = new JComboBox<>();
        if (nullText != null) combo.addItem(null);
        for (Department d : departments) combo.addItem(d);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setText(value == null ? nullText : value.toString());
                return this;
            }
        });
        return combo;
    }

    /** Short department code (ICT, ET ...) for a department id, "-" when none. */
    static String deptCode(List<Department> departments, Integer departmentId) {
        if (departmentId != null) {
            for (Department d : departments) {
                if (d.getDepartmentId() == departmentId) return d.getCode();
            }
        }
        return "-";
    }

    /** Selects the department with the given id (or the null item). */
    static void selectDepartment(JComboBox<Department> combo, Integer departmentId) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Department d = combo.getItemAt(i);
            if ((d == null && departmentId == null) || (d != null && departmentId != null && d.getDepartmentId() == departmentId)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    static Integer selectedDepartmentId(JComboBox<Department> combo) {
        Department d = (Department) combo.getSelectedItem();
        return d == null ? null : d.getDepartmentId();
    }

    /** 08:00, 08:30 ... 18:00 for the timetable time combos. */
    static List<LocalTime> halfHourSlots() {
        List<LocalTime> list = new ArrayList<>();
        for (LocalTime t = LocalTime.of(8, 0); !t.isAfter(LocalTime.of(18, 0)); t = t.plusMinutes(30)) {
            list.add(t);
        }
        return list;
    }

    /** UI.form(...) from common, but with a fixed label column width so stacked forms line up. */
    static JPanel form(int labelWidth, String[] labels, JComponent... fields) {
        JPanel p = UI.form(labels, fields);
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel l) {
                Dimension d = l.getPreferredSize();
                l.setPreferredSize(new Dimension(Math.max(labelWidth, d.width), d.height));
            }
        }
        return p;
    }

    /** Text area inside a bordered scroll pane of the given height. */
    static JScrollPane areaScroll(JTextArea area, int width, int height) {
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(width, height));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return sp;
    }

    /** Heading + thin line used to split a dialog into sections. */
    static JComponent section(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel l = new JLabel(title.toUpperCase());
        l.setFont(Theme.FONT_SMALL.deriveFont(java.awt.Font.BOLD, 11f));
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 4, 6, 0)));
        p.add(l, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** Muted hint text that wraps (used under some form fields). */
    static JLabel hint(String text) {
        JLabel l = UI.muted("<html>" + text + "</html>");
        l.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        return l;
    }

    /** Coloured status word used in tables ("Active" / "Inactive" are auto-coloured by the common renderer). */
    static String statusWord(String dbStatus) {
        return capital(dbStatus);
    }

    static Color roleColor(lk.ac.ruh.fot.ams.model.enums.Role role) {
        return switch (role) {
            case ADMIN -> Theme.ADMIN_ACCENT;
            case LECTURER -> Theme.LECTURER_ACCENT;
            case TO -> Theme.TO_ACCENT;
            case UNDERGRADUATE -> Theme.STUDENT_ACCENT;
        };
    }
}

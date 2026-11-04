package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M4 - UNDERGRADUATE MODULE] Table cell renderer for the day-by-day
 * attendance status. The common UI.StatusRenderer paints every word starting
 * with "Absent" red; for a student it is important to see that an absence
 * covered by an APPROVED medical is fine (amber) and a PENDING one is waiting.
 *
 * OOP: Inheritance (extends the common UI.StatusRenderer and re-uses its zebra
 * rows / fonts via super), Polymorphism (overrides getTableCellRendererComponent).
 */
public class AttendanceStatusRenderer extends UI.StatusRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected && value instanceof String s) {
            if (s.contains("Medical - APPROVED")) {
                c.setForeground(Theme.INFO);
                c.setToolTipText("Absent, but covered by an approved medical - counted as attended");
            } else if (s.contains("Medical - PENDING")) {
                c.setForeground(Theme.WARNING);
                c.setToolTipText("Medical is waiting for approval - not counted yet");
            } else if (s.contains("Medical - REJECTED")) {
                c.setToolTipText("Medical was rejected - counted as absent");
            } else {
                c.setToolTipText(null);
            }
        }
        return c;
    }
}

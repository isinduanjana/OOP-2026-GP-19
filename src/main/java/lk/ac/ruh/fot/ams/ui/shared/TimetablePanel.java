package lk.ac.ruh.fot.ams.ui.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [COMMON PART] Read-only weekly timetable (week grid + list).
 * The caller decides WHICH timetable to show by passing a loader:
 *
 *   new TimetablePanel(user, accent, "Department timetable",
 *         () -> timetableDAO.findByDepartment(user.getDepartmentId()));
 */
public class TimetablePanel extends BasePanel {

    /** Supplies the timetable rows (lambda). */
    @FunctionalInterface
    public interface Loader {
        List<TimetableEntry> load() throws AppException;
    }

    private static final Color[] COLORS = {
            new Color(0x2F54EB), new Color(0x0D9488), new Color(0xEA580C), new Color(0x7C3AED),
            new Color(0xDB2777), new Color(0x0891B2), new Color(0x65A30D), new Color(0xCA8A04)};

    private final Loader loader;
    private final String subtitle;
    private final WeekGrid grid = new WeekGrid();
    private final DataTableModel model = new DataTableModel("Day", "Start", "End", "Course", "Course name",
            "Component", "Room", "Department");

    public TimetablePanel(User user, Color accent, String subtitle, Loader loader) {
        super(user, accent);
        this.loader = loader;
        this.subtitle = subtitle;
        JTabbedPane tabs = new JTabbedPane();
        CardPanel gridCard = new CardPanel();
        gridCard.add(grid, BorderLayout.CENTER);
        tabs.addTab("Week view", gridCard);
        JTable table = UI.table(model);
        UI.setColumnWidths(table, 90, 60, 60, 80, 240, 80, 80, 160);
        CardPanel listCard = new CardPanel();
        listCard.add(UI.scroll(table), BorderLayout.CENTER);
        tabs.addTab("List view", listCard);
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Timetable";
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public void refresh() {
        safe(() -> {
            List<TimetableEntry> entries = loader.load();
            grid.setEntries(entries);
            model.clear();
            for (TimetableEntry e : entries) {
                model.addRow(e, capital(e.getDay()), Format.time(e.getStartTime()), Format.time(e.getEndTime()),
                        e.getCourseCode(), e.getCourseName(), e.getComponent().toString(), e.getRoom(),
                        e.getDepartmentName());
            }
            model.refresh();
        });
    }

    private static String capital(String s) {
        return s == null || s.isEmpty() ? "" : s.charAt(0) + s.substring(1).toLowerCase();
    }

    /** Paints Monday-Friday x 08:00-17:00 with coloured course blocks. */
    private static final class WeekGrid extends JComponent {
        private static final int START_HOUR = 8;
        private static final int END_HOUR = 17;
        private final List<TimetableEntry> entries = new ArrayList<>();

        WeekGrid() {
            setPreferredSize(new Dimension(800, 520));
        }

        void setEntries(List<TimetableEntry> list) {
            entries.clear();
            entries.addAll(list);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int left = 56;
            int top = 30;
            int w = getWidth() - left - 4;
            int h = getHeight() - top - 4;
            double colW = w / 5.0;
            double hourH = h / (double) (END_HOUR - START_HOUR);
            g.setFont(Theme.FONT_BOLD);
            FontMetrics fm = g.getFontMetrics();
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
            for (int d = 0; d < 5; d++) {
                int x = left + (int) (d * colW);
                g.setColor(Theme.TEXT);
                g.drawString(days[d], x + (int) (colW - fm.stringWidth(days[d])) / 2, 18);
                g.setColor(Theme.BORDER);
                g.drawLine(x, top, x, top + h);
            }
            g.setFont(Theme.FONT_SMALL);
            for (int hr = START_HOUR; hr <= END_HOUR; hr++) {
                int y = top + (int) ((hr - START_HOUR) * hourH);
                g.setColor(Theme.BORDER);
                g.drawLine(left, y, left + w, y);
                g.setColor(Theme.TEXT_MUTED);
                g.drawString(String.format("%02d:00", hr), 8, y + 4);
            }
            for (TimetableEntry e : entries) {
                int d = e.getDayIndex();
                if (d < 0 || e.getStartTime() == null || e.getEndTime() == null) continue;
                double s = e.getStartTime().getHour() + e.getStartTime().getMinute() / 60.0;
                double en = e.getEndTime().getHour() + e.getEndTime().getMinute() / 60.0;
                int x = left + (int) (d * colW) + 4;
                int y = top + (int) ((s - START_HOUR) * hourH) + 2;
                int bw = (int) colW - 8;
                int bh = (int) ((en - s) * hourH) - 4;
                Color c = COLORS[Math.abs(e.getCourseCode().hashCode()) % COLORS.length];
                g.setColor(Theme.tint(c, 0.85));
                g.fillRoundRect(x, y, bw, bh, 12, 12);
                g.setColor(c);
                g.fillRoundRect(x, y, 5, bh, 5, 5);
                g.setColor(Theme.darker(c, 0.25));
                g.setFont(Theme.FONT_BOLD);
                g.drawString(e.getCourseCode(), x + 12, y + 18);
                g.setFont(Theme.FONT_SMALL);
                g.setColor(Theme.TEXT);
                String line2 = (e.getComponent() == Component.THEORY ? "Theory" : "Practical") + " • " + e.getRoom();
                if (bh > 34) g.drawString(line2, x + 12, y + 34);
                if (bh > 50) g.drawString(Format.time(e.getStartTime()) + " - " + Format.time(e.getEndTime()), x + 12, y + 50);
            }
            g.dispose();
        }
    }
}

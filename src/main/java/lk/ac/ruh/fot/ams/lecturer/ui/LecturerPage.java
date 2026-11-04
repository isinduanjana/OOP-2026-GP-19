package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;


public abstract class LecturerPage extends BasePanel {

    protected final CourseDAO courseDAO = new CourseDAO();

    protected LecturerPage(User user, Color accent) {
        super(user, accent);
    }

    /** The logged-in user is always a Lecturer in this module (see LecturerDashboard). */
    protected Lecturer lecturer() {
        return (Lecturer) user;
    }

    protected int lecturerId() {
        return lecturer().getLecturerId();
    }

    /** Courses this lecturer teaches (common CourseDAO.findByLecturer). */
    protected List<Course> myCourses() throws AppException {
        return courseDAO.findByLecturer(lecturerId());
    }

    /** Refills a combo box and keeps the previously selected item when it still exists. */
    protected static <T> void fillCombo(JComboBox<T> box, List<T> items) {
        Object old = box.getSelectedItem();
        box.removeAllItems();
        for (T t : items) box.addItem(t);
        if (old != null) {
            for (T t : items) {
                if (Objects.equals(t, old) || t.toString().equals(old.toString())) {
                    box.setSelectedItem(t);
                    break;
                }
            }
        }
    }

    /** A row of stat cards with equal width. */
    protected static JPanel statRow(StatCard... cards) {
        JPanel p = new JPanel(new GridLayout(1, cards.length, 14, 0));
        p.setOpaque(false);
        for (StatCard c : cards) p.add(c);
        return p;
    }

    /**
     * Stat cards as a 2 x 2 grid on the left and a chart card on the right - saves
     * vertical space so the table below gets more rows.
     */
    protected static JPanel statsWithChart(JComponent chartCard, StatCard... cards) {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        for (StatCard c : cards) grid.add(c);
        grid.setPreferredSize(new Dimension(500, 220));
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setOpaque(false);
        p.add(grid, BorderLayout.WEST);
        p.add(chartCard, BorderLayout.CENTER);
        return p;
    }

    /**
     * Wide tables: columns keep their preferred widths and a horizontal scroll bar
     * appears when the window is too narrow; otherwise the table fills the width.
     */
    protected static JScrollPane fitOrScroll(JTable table, int... widths) {
        UI.setColumnWidths(table, widths);
        JScrollPane sp = UI.scroll(table);
        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int total = 0;
                for (int i = 0; i < table.getColumnCount(); i++) {
                    total += table.getColumnModel().getColumn(i).getPreferredWidth();
                }
                // squeeze a little (up to 3%) before switching to horizontal scrolling
                table.setAutoResizeMode(sp.getViewport().getWidth() >= total * 0.97
                        ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF);
            }
        });
        return sp;
    }

    /** Opens another page of the dashboard (uses the common BaseDashboard.showPage). */
    protected void openPage(String menuText) {
        if (SwingUtilities.getWindowAncestor(this) instanceof BaseDashboard d) {
            d.showPage(menuText);
        }
    }

    /** Last 4 characters of a TG number, used as short chart labels. */
    protected static String shortTg(String tg) {
        return tg == null ? "" : tg.substring(Math.max(0, tg.length() - 4));
    }

    protected static String eligibleText(boolean ok) {
        return ok ? "Eligible" : "Not eligible";
    }
}

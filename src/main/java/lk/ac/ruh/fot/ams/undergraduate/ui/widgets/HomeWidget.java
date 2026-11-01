package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ProgressRing;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;

/**
 * [M4 - UNDERGRADUATE MODULE] Abstract home tile: title on top, a common
 * ProgressRing in the middle and a coloured status badge underneath.
 *
 * OOP:
 *  - Inheritance : extends the common CardPanel (rounded white card).
 *  - Abstraction : update(...) and summaryLine() from DashboardWidget are left
 *                  abstract - each concrete widget decides what to show.
 *  - Encapsulation: the ring / badge are private; subclasses only use the
 *                  protected helpers showRing(...) and showStatus(...).
 */
public abstract class HomeWidget extends CardPanel implements DashboardWidget {

    private final ProgressRing ring = new ProgressRing(100);
    private final JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    protected final Color accent;

    protected HomeWidget(String title, Color accent) {
        super(new BorderLayout(0, 6));
        this.accent = accent;
        setPreferredSize(new Dimension(220, 192));
        JLabel t = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        t.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        t.setForeground(Theme.TEXT_MUTED);
        statusRow.setOpaque(false);
        add(t, BorderLayout.NORTH);
        add(ring, BorderLayout.CENTER);
        add(statusRow, BorderLayout.SOUTH);
    }

    @Override
    public abstract void update(StudentSnapshot snapshot);

    @Override
    public abstract String summaryLine();

    /** Sets the ring value (0..max), the big centre text and the small caption. */
    protected void showRing(double value, double max, String center, String caption, Color color) {
        ring.setValue(Double.isNaN(value) ? 0 : value, max, center, caption, color);
    }

    /** Replaces the status badge under the ring. */
    protected void showStatus(String text, Color color) {
        statusRow.removeAll();
        statusRow.add(UI.badge(text, color));
        statusRow.revalidate();
        statusRow.repaint();
    }

    /** Green when ok, red when not - used by several widgets. */
    protected static Color okColor(boolean ok) {
        return ok ? Theme.SUCCESS : Theme.DANGER;
    }
}

package lk.ac.ruh.fot.ams.ui.components;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * [COMMON PART] Panel that always fits the width of its scroll pane
 * (so wrapped text inside it wraps instead of scrolling sideways).
 */
public class ScrollablePanel extends JPanel implements Scrollable {

    public ScrollablePanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
        return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
        return 120;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}

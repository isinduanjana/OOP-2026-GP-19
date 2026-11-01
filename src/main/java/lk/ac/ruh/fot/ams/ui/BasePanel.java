package lk.ac.ruh.fot.ams.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [COMMON PART] Abstract parent of EVERY page shown inside a dashboard
 * (all M1, M2, M3, M4 screens extend this class).
 *
 * OOP:
 *  - Abstraction : each page MUST say its title and how it loads data (refresh()).
 *  - Inheritance : pages inherit the logged-in user, accent colour, the
 *                  safe() error-handling helper and accent buttons.
 *  - Polymorphism: the dashboard keeps a list of BasePanel and simply calls
 *                  panel.onShow() - each page reloads its own data its own way.
 */
public abstract class BasePanel extends JPanel implements Scrollable {

    // Scrollable: page always fits the window width; it fits the height too
    // unless the window is smaller than the page's minimum height.
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
        return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
        return 120;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport vp && vp.getHeight() >= getMinimumSize().height;
    }

    protected final User user;
    protected final Color accent;

    protected BasePanel(User user, Color accent) {
        super(new BorderLayout(0, 16));
        this.user = user;
        this.accent = accent;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
    }

    /** Title shown in the dashboard header and side menu. */
    public abstract String getTitle();

    /** Smaller text under the title. */
    public String getSubtitle() {
        return "";
    }

    /** (Re)load the data of this page from the database. */
    public abstract void refresh();

    /** Called by the dashboard every time the page becomes visible. */
    public void onShow() {
        refresh();
    }

    /** A database / validation action that may fail. */
    @FunctionalInterface
    protected interface Action {
        void run() throws AppException;
    }

    /**
     * Runs an action and shows a friendly dialog if it throws.
     * Exception handling in one place for all screens.
     * @return true if the action finished without error
     */
    protected boolean safe(Action action) {
        try {
            action.run();
            return true;
        } catch (AppException e) {
            UI.error(this, e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            UI.error(this, "Unexpected error: " + e.getMessage());
        }
        return false;
    }

    /** Primary button painted with this dashboard's accent colour. */
    protected ModernButton accentButton(String text, String icon) {
        ModernButton b = UI.primary(text, icon);
        b.setBaseColor(accent);
        return b;
    }

    /** Lets a page update the name/picture in the side bar after a profile change. */
    protected void notifyUserChanged() {
        if (SwingUtilities.getWindowAncestor(this) instanceof BaseDashboard d) {
            d.refreshUserCard();
        }
    }
}

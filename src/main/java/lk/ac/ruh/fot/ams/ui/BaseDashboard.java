package lk.ac.ruh.fot.ams.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.components.Avatar;
import lk.ac.ruh.fot.ams.ui.components.Icons;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [COMMON PART] Abstract main window shared by all four roles.
 *
 *   +-----------+---------------------------------------+
 *   | side menu |  header (page title, date)            |
 *   |  pages... |---------------------------------------|
 *   |           |  current page (a BasePanel)           |
 *   | user card |                                       |
 *   +-----------+---------------------------------------+
 *
 * Each member creates ONE subclass:
 *   M1 AdminDashboard, M2 LecturerDashboard, M3 TODashboard, M4 UndergraduateDashboard
 * and only implements the three abstract methods (Template Method pattern).
 *
 * OOP: Abstraction, Inheritance (extends JFrame), Polymorphism (abstract
 * methods are implemented differently by each role).
 */
public abstract class BaseDashboard extends JFrame {

    protected final User user;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel nav = new JPanel();
    private final List<NavItem> items = new ArrayList<>();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();
    private final Avatar avatar = new Avatar(40);
    private final JLabel userName = new JLabel();
    private final JLabel userRole = new JLabel();
    private NavItem current;

    protected BaseDashboard(User user) {
        this.user = user;
    }

    // ------------------------------------------------------------ to implement by each role
    /** e.g. "Administrator Portal". */
    protected abstract String getRoleTitle();

    /** Accent colour of this role. */
    protected abstract Color getAccentColor();

    /** Add the pages of this role with addSection(...) / addPage(...). */
    protected abstract void registerPages();

    // ------------------------------------------------------------ building
    /** Builds the window. Called by DashboardFactory right after the constructor. */
    public final void init() {
        setTitle(AppConfig.APP_NAME + "  |  " + getRoleTitle());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1360, 820);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.add(buildHeader(), BorderLayout.NORTH);
        content.setBackground(Theme.BG);
        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);

        registerPages();
        refreshUserCard();
        if (!items.isEmpty()) {
            select(items.get(0));
        }
    }

    /** Adds a small grey heading in the side menu. */
    protected void addSection(String title) {
        JLabel l = new JLabel(title.toUpperCase());
        l.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 10.5f));
        l.setForeground(new Color(0x6B7280));
        l.setBorder(BorderFactory.createEmptyBorder(16, 22, 6, 10));
        l.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(l);
    }

    /** Adds a page to the side menu and to the card layout. */
    protected void addPage(String menuText, String iconName, BasePanel page) {
        NavItem item = new NavItem(menuText, iconName, page);
        items.add(item);
        nav.add(item);
        JScrollPane sp = new JScrollPane(page);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getViewport().setBackground(Theme.BG);
        content.add(sp, menuText);
    }

    /** Programmatically open a page (e.g. from a dashboard shortcut). */
    public void showPage(String menuText) {
        for (NavItem i : items) {
            if (i.text.equals(menuText)) {
                select(i);
                return;
            }
        }
    }

    private void select(NavItem item) {
        if (current != null) current.setActive(false);
        current = item;
        item.setActive(true);
        pageTitle.setText(item.page.getTitle());
        pageSubtitle.setText(item.page.getSubtitle());
        cards.show(content, item.text);
        item.page.onShow();          // polymorphic call
    }

    /** Reloads name / picture of the logged-in user (after profile edits). */
    public void refreshUserCard() {
        try {
            User fresh = new UserDAO().findById(user.getUserId());
            if (fresh != null) {
                user.setFullName(fresh.getFullName());
                user.setProfilePicture(fresh.getProfilePicture());
                user.setEmail(fresh.getEmail());
                user.setPhone(fresh.getPhone());
                user.setAddress(fresh.getAddress());
            }
        } catch (Exception ignored) {
            // keep the old values if the DB is not reachable
        }
        avatar.setUser(user.getInitials(), user.getProfilePicture(), getAccentColor());
        userName.setText(user.getFullName());
        userRole.setText(user.getRole().getDisplayName());
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(250, 10));

        // --- brand
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(22, 20, 12, 16));
        JComponent logo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getAccentColor());
                g.fillRoundRect(0, 0, 40, 40, 12, 12);
                Icons.get("cap", 24, Color.WHITE).paintIcon(this, g, 8, 8);
                g.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(40, 40));
        JLabel name = new JLabel("<html><b style='font-size:12px'>FOT AMS</b><br>"
                + "<span style='color:#9CA3AF;font-size:9px'>" + AppConfig.UNIVERSITY + "</span></html>");
        name.setForeground(Color.WHITE);
        name.setFont(Theme.FONT);
        brand.add(logo, BorderLayout.WEST);
        brand.add(name, BorderLayout.CENTER);

        JLabel portal = UI.badge(getRoleTitle(), getAccentColor());
        JPanel portalRow = new JPanel(new BorderLayout());
        portalRow.setOpaque(false);
        portalRow.setBorder(BorderFactory.createEmptyBorder(0, 20, 4, 20));
        portalRow.add(portal, BorderLayout.WEST);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        portalRow.setAlignmentX(LEFT_ALIGNMENT);
        top.add(brand);
        top.add(portalRow);

        // --- navigation
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        JPanel navWrap = new JPanel(new BorderLayout());
        navWrap.setOpaque(false);
        navWrap.add(nav, BorderLayout.NORTH);
        JScrollPane navScroll = new JScrollPane(navWrap);
        navScroll.setBorder(null);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);

        // --- user card + logout
        JPanel userCard = new JPanel(new BorderLayout(10, 0));
        userCard.setOpaque(false);
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x1F2937)),
                BorderFactory.createEmptyBorder(14, 18, 14, 14)));
        userName.setForeground(Color.WHITE);
        userName.setFont(Theme.FONT_BOLD);
        userRole.setForeground(new Color(0x9CA3AF));
        userRole.setFont(Theme.FONT_SMALL);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(userName);
        names.add(userRole);
        JLabel logout = new JLabel(Icons.get("logout", 20, new Color(0x9CA3AF)));
        logout.setToolTipText("Log out");
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                logout();
            }
        });
        userCard.add(avatar, BorderLayout.WEST);
        userCard.add(names, BorderLayout.CENTER);
        userCard.add(logout, BorderLayout.EAST);

        side.add(top, BorderLayout.NORTH);
        side.add(navScroll, BorderLayout.CENTER);
        side.add(userCard, BorderLayout.SOUTH);
        return side;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));
        pageTitle.setFont(Theme.FONT_TITLE);
        pageTitle.setForeground(Theme.TEXT);
        pageSubtitle.setFont(Theme.FONT_SMALL);
        pageSubtitle.setForeground(Theme.TEXT_MUTED);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(pageSubtitle);

        JLabel date = new JLabel(Format.today(), Icons.get("calendar", 16, Theme.TEXT_MUTED), SwingConstants.LEFT);
        date.setFont(Theme.FONT_SMALL);
        date.setForeground(Theme.TEXT_MUTED);
        JLabel year = UI.badge("Academic year " + AppConfig.CURRENT_ACADEMIC_YEAR, getAccentColor());
        header.add(titles, BorderLayout.WEST);
        header.add(UI.rowRight(date, year), BorderLayout.EAST);
        return header;
    }

    private void logout() {
        if (UI.confirm(this, "Do you want to log out?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** One clickable entry of the side menu (inner class: encapsulation). */
    private final class NavItem extends JPanel {
        private final String text;
        private final BasePanel page;
        private final JLabel label;
        private final String iconName;
        private boolean active;
        private boolean hover;

        NavItem(String text, String iconName, BasePanel page) {
            super(new BorderLayout());
            this.text = text;
            this.page = page;
            this.iconName = iconName;
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(230, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label = new JLabel(text, Icons.get(iconName, 18, Theme.TEXT_ON_DARK), SwingConstants.LEFT);
            label.setIconTextGap(12);
            label.setFont(Theme.FONT);
            label.setForeground(new Color(0xD1D5DB));
            label.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 10));
            add(label, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    select(NavItem.this);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            label.setForeground(active ? Color.WHITE : new Color(0xD1D5DB));
            label.setFont(active ? Theme.FONT_BOLD : Theme.FONT);
            label.setIcon(Icons.get(iconName, 18, active ? Color.WHITE : new Color(0x9CA3AF)));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g.setColor(getAccentColor());
                g.fillRoundRect(10, 3, getWidth() - 20, getHeight() - 6, 12, 12);
            } else if (hover) {
                g.setColor(Theme.SIDEBAR_HOVER);
                g.fillRoundRect(10, 3, getWidth() - 20, getHeight() - 6, 12, 12);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
}

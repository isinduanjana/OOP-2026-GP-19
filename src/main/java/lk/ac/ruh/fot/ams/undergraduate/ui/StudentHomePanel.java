package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.Avatar;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.Icons;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;
import lk.ac.ruh.fot.ams.undergraduate.service.StudentPortalService;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.AttendanceWidget;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.DashboardWidget;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.EligibilityWidget;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.GpaWidget;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.HomeWidget;
import lk.ac.ruh.fot.ams.undergraduate.ui.widgets.SgpaWidget;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] Student Home page: welcome banner, four
 * progress widgets, this semester's courses, today's classes and the latest
 * notices.
 *
 * OOP:
 *  - Inheritance : extends StudentViewPanel (-> BasePanel from common).
 *  - Polymorphism: the four widgets are kept in a List&lt;DashboardWidget&gt; and
 *                  updated with one loop - each widget draws itself differently.
 *  - Classes & objects: one StudentSnapshot object carries all loaded data.
 */
public class StudentHomePanel extends StudentViewPanel {

    private final StudentPortalService service = new StudentPortalService();

    /** Polymorphic list - the page does not care which widget is which. */
    private final List<DashboardWidget> widgets = new ArrayList<>();

    private final Avatar avatar = new Avatar(76);
    private final JLabel nameLabel = new JLabel();
    private final JLabel infoLabel = new JLabel();
    private final JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JPanel glance = new JPanel();
    private final JPanel courseGrid = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel classList = new JPanel();
    private final JPanel noticeList = new JPanel();
    private final CardPanel classesCard = CardPanel.titled("Classes");

    public StudentHomePanel(User user, Color accent) {
        super(user, accent);
        widgets.add(new GpaWidget(accent));
        widgets.add(new SgpaWidget(accent));
        widgets.add(new AttendanceWidget(accent));
        widgets.add(new EligibilityWidget(accent));
        setMinimumPageHeight(800);      // scroll instead of squeezing the cards
        buildUI();
    }

    @Override
    public String getTitle() {
        return "Home";
    }

    @Override
    public String getSubtitle() {
        return "Your semester at a glance";
    }

    // ------------------------------------------------------------------ layout
    private void buildUI() {
        add(buildBanner(), BorderLayout.NORTH);

        JPanel widgetRow = new JPanel(new GridLayout(1, widgets.size(), 14, 0));
        widgetRow.setOpaque(false);
        for (DashboardWidget w : widgets) {
            widgetRow.add((HomeWidget) w);
        }

        // left: courses of this semester
        CardPanel coursesCard = new CardPanel(new BorderLayout(0, 12));
        JLabel coursesTitle = UI.heading("My courses this semester");
        JLabel more = link("Open My Attendance  ›", "My Attendance");
        coursesCard.add(UI.toolbar(coursesTitle, more), BorderLayout.NORTH);
        courseGrid.setOpaque(false);
        JPanel gridHolder = StudentUI.clear(0, 0);     // NORTH = tiles keep their natural height
        gridHolder.add(courseGrid, BorderLayout.NORTH);
        coursesCard.add(gridHolder, BorderLayout.CENTER);

        // right: classes + notices
        classList.setOpaque(false);
        classList.setLayout(new BoxLayout(classList, BoxLayout.Y_AXIS));
        classesCard.add(classList, BorderLayout.CENTER);

        CardPanel noticesCard = new CardPanel(new BorderLayout(0, 12));
        noticesCard.add(UI.toolbar(UI.heading("Latest notices"), link("All notices  ›", "Notices")),
                BorderLayout.NORTH);
        noticeList.setOpaque(false);
        noticeList.setLayout(new BoxLayout(noticeList, BoxLayout.Y_AXIS));
        noticesCard.add(noticeList, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints rc = new GridBagConstraints();
        rc.gridx = 0;
        rc.fill = GridBagConstraints.BOTH;
        rc.weightx = 1;
        rc.gridy = 0;
        rc.weighty = 0;
        rc.insets = new Insets(0, 0, 14, 0);
        right.add(classesCard, rc);
        rc.gridy = 1;
        rc.weighty = 1;
        rc.insets = new Insets(0, 0, 0, 0);
        right.add(noticesCard, rc);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.62;
        c.insets = new Insets(0, 0, 0, 14);
        coursesCard.setPreferredSize(new Dimension(520, 300));
        bottom.add(coursesCard, c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        right.setPreferredSize(new Dimension(320, 300));
        bottom.add(right, c);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(widgetRow, BorderLayout.NORTH);
        center.add(bottom, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    /** Gradient welcome card in the student accent colour. */
    private JComponent buildBanner() {
        JPanel banner = new JPanel(new BorderLayout(18, 0)) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, Theme.darker(accent, 0.12), getWidth(), getHeight(),
                        new Color(0xDB2777)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                // soft decorative circles
                g.setColor(new Color(255, 255, 255, 22));
                g.fillOval(getWidth() - 220, -90, 260, 260);
                g.fillOval(getWidth() - 380, getHeight() - 60, 160, 160);
                g.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        nameLabel.setFont(Theme.FONT_TITLE.deriveFont(24f));
        nameLabel.setForeground(Color.WHITE);
        infoLabel.setFont(Theme.FONT);
        infoLabel.setForeground(new Color(255, 255, 255, 225));
        badgeRow.setOpaque(false);

        JLabel hello = new JLabel("Welcome back,");
        hello.setFont(Theme.FONT);
        hello.setForeground(new Color(255, 255, 255, 200));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        for (JComponent j : new JComponent[]{hello, nameLabel, infoLabel, badgeRow}) {
            j.setAlignmentX(LEFT_ALIGNMENT);
        }
        text.add(hello);
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(infoLabel);
        text.add(Box.createVerticalStrut(10));
        text.add(badgeRow);

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);

        glance.setOpaque(false);
        glance.setLayout(new BoxLayout(glance, BoxLayout.Y_AXIS));

        banner.add(avatarWrap, BorderLayout.WEST);
        banner.add(text, BorderLayout.CENTER);
        banner.add(glance, BorderLayout.EAST);
        return banner;
    }

    /** Clickable small text that opens another page of the dashboard. */
    private JLabel link(String text, String page) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        l.setForeground(accent);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPage(page);
            }
        });
        return l;
    }

    private void openPage(String page) {
        if (SwingUtilities.getWindowAncestor(this) instanceof BaseDashboard d) {
            d.showPage(page);
        }
    }

    // ------------------------------------------------------------------ data
    @Override
    protected void loadStudentData() throws AppException {
        StudentSnapshot snap = service.loadSnapshot(getStudent());
        fillBanner(snap);
        for (DashboardWidget w : widgets) {
            w.update(snap);                 // polymorphic call
        }
        fillGlance();
        fillCourses(snap);
        fillClasses(snap);
        fillNotices(snap);
        revalidate();
        repaint();
    }

    private void fillBanner(StudentSnapshot snap) {
        Undergraduate s = snap.getStudent();
        avatar.setUser(s.getInitials(), s.getProfilePicture(), Theme.darker(accent, 0.25));
        nameLabel.setText(s.getFullName());
        infoLabel.setText(s.getTgNo() + "   •   Batch " + s.getBatch() + "   •   Level " + s.getLevel()
                + ", Semester " + s.getSemester() + "   •   " + Format.nullToDash(s.getDepartmentName()));
        badgeRow.removeAll();
        badgeRow.add(UI.badge(StudentUI.typeText(s.getStudentType()), StudentUI.typeColor(s.getStudentType())));
        badgeRow.add(StudentUI.glassBadge("Academic year " + AppConfig.CURRENT_ACADEMIC_YEAR));
        badgeRow.add(StudentUI.glassBadge(snap.getCurrentResults().size() + " courses  •  "
                + snap.getEnrolledCredits() + " credits"));
        badgeRow.revalidate();
    }

    /** "At a glance" lines in the banner - one per widget (polymorphic summaryLine()). */
    private void fillGlance() {
        glance.removeAll();
        JLabel head = new JLabel("AT A GLANCE");
        head.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        head.setForeground(new Color(255, 255, 255, 190));
        glance.add(Box.createVerticalGlue());
        glance.add(head);
        glance.add(Box.createVerticalStrut(6));
        for (DashboardWidget w : widgets) {
            JLabel l = new JLabel(w.summaryLine(), Icons.get("check", 14, Color.WHITE), JLabel.LEFT);
            l.setIconTextGap(8);
            l.setFont(Theme.FONT_SMALL);
            l.setForeground(Color.WHITE);
            l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            glance.add(l);
        }
        glance.add(Box.createVerticalGlue());
        glance.revalidate();
    }

    private void fillCourses(StudentSnapshot snap) {
        courseGrid.removeAll();
        if (snap.getCurrentResults().isEmpty()) {
            courseGrid.setLayout(new GridLayout(1, 1));
            courseGrid.add(StudentUI.empty("You are not enrolled in any course this academic year."));
        } else {
            courseGrid.setLayout(new GridLayout(0, 2, 12, 12));
            for (CourseResult r : snap.getCurrentResults()) {
                courseGrid.add(courseTile(r, snap.getAttendanceFor(r.getCourse().getCourseId())));
            }
        }
        courseGrid.revalidate();
    }

    /** Compact course card: code, name, attendance bar and eligibility badge. */
    private JComponent courseTile(CourseResult r, AttendanceSummary att) {
        Course c = r.getCourse();
        JPanel tile = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.tint(accent, 0.95));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g.setColor(Theme.tint(accent, 0.80));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.setToolTipText(c + "  -  click to see attendance");
        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPage("My Attendance");
            }
        });

        JLabel code = new JLabel(c.getCourseCode());
        code.setFont(Theme.FONT_BOLD.deriveFont(14f));
        code.setForeground(Theme.darker(accent, 0.1));
        JLabel badge = UI.badge(r.isEligible() ? "Eligible" : "Not eligible", r.isEligible() ? Theme.SUCCESS : Theme.DANGER);
        badge.setToolTipText(r.getEligibilityText());
        JLabel name = UI.muted(c.getCourseName());
        name.setToolTipText(c.getCourseName() + "  •  " + c.getCredits() + " credits");
        JLabel credits = UI.muted(c.getCredits() + " cr");
        JPanel codeRow = StudentUI.clear(8, 0);
        codeRow.add(code, BorderLayout.WEST);
        codeRow.add(credits, BorderLayout.CENTER);
        JPanel top = StudentUI.clear(8, 0);
        top.add(codeRow, BorderLayout.CENTER);
        top.add(badge, BorderLayout.EAST);

        double pct = att == null ? Double.NaN : att.getCombinedPercentage(true);
        boolean held = att != null && StudentSnapshot.sessionsHeld(att) > 0;
        JLabel pctLabel = UI.label(held ? Format.percent(pct) : "-");
        pctLabel.setFont(Theme.FONT_BOLD.deriveFont(12f));
        JPanel barRow = StudentUI.clear(10, 0);
        JPanel barWrap = new JPanel(new GridBagLayout());
        barWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        barWrap.add(StudentUI.bar(held ? pct : 0, accent, AppConfig.MIN_ATTENDANCE_PERCENT), gc);
        barRow.add(UI.muted("Attendance"), BorderLayout.WEST);
        barRow.add(barWrap, BorderLayout.CENTER);
        barRow.add(pctLabel, BorderLayout.EAST);

        JPanel mid = StudentUI.clear(0, 6);
        mid.add(name, BorderLayout.NORTH);
        mid.add(barRow, BorderLayout.SOUTH);

        tile.add(top, BorderLayout.NORTH);
        tile.add(mid, BorderLayout.CENTER);
        return tile;
    }

    private void fillClasses(StudentSnapshot snap) {
        ((JLabel) classesCard.getComponent(0)).setText("Classes  -  " + snap.getClassesDayLabel());
        classList.removeAll();
        if (snap.getClasses().isEmpty()) {
            classList.add(StudentUI.empty("No classes scheduled. Enjoy your day!"));
        }
        for (TimetableEntry e : snap.getClasses()) {
            classList.add(classRow(e));
            classList.add(Box.createVerticalStrut(8));
        }
        classList.revalidate();
    }

    private JComponent classRow(TimetableEntry e) {
        JPanel row = StudentUI.clear(12, 0);
        row.setAlignmentX(LEFT_ALIGNMENT);
        Color c = e.getComponent() == Component.PRACTICAL ? Theme.TO_ACCENT : accent;
        JLabel time = new JLabel("<html><b>" + Format.time(e.getStartTime()) + "</b><br><span style='color:#6B7280'>"
                + Format.time(e.getEndTime()) + "</span></html>");
        time.setFont(Theme.FONT_SMALL);
        time.setPreferredSize(new Dimension(46, 34));
        JComponent stripe = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(c);
                g.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        stripe.setPreferredSize(new Dimension(4, 34));
        JLabel what = new JLabel("<html><b>" + e.getCourseCode() + "</b> " + escape(e.getCourseName())
                + "<br><span style='color:#6B7280'>"
                + (e.getComponent() == Component.THEORY ? "Theory" : "Practical") + "  •  "
                + Format.nullToDash(e.getRoom()) + "</span></html>");
        what.setFont(Theme.FONT_SMALL);
        JPanel west = StudentUI.clear(10, 0);
        west.add(time, BorderLayout.WEST);
        west.add(stripe, BorderLayout.EAST);
        row.add(west, BorderLayout.WEST);
        row.add(what, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return row;
    }

    private void fillNotices(StudentSnapshot snap) {
        noticeList.removeAll();
        if (snap.getLatestNotices().isEmpty()) {
            noticeList.add(StudentUI.empty("No notices right now."));
        }
        for (Notice n : snap.getLatestNotices()) {
            JPanel row = StudentUI.clear(10, 0);
            row.setAlignmentX(LEFT_ALIGNMENT);
            JLabel icon = new JLabel(Icons.get("bell", 16, accent));
            icon.setVerticalAlignment(JLabel.TOP);
            JLabel t = new JLabel("<html><b>" + escape(n.getTitle()) + "</b><br><span style='color:#6B7280'>"
                    + Format.dateTime(n.getCreatedAt()) + "</span></html>");
            t.setFont(Theme.FONT_SMALL);
            t.setToolTipText(n.getDescription());
            row.add(icon, BorderLayout.WEST);
            row.add(t, BorderLayout.CENTER);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            noticeList.add(row);
            noticeList.add(Box.createVerticalStrut(10));
        }
        noticeList.revalidate();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lk.ac.ruh.fot.ams.admin.dao.AdminNoticeDAO;
import lk.ac.ruh.fot.ams.admin.dao.AdminStatsDAO;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] Home page of the administrator: key numbers (StatCards),
 * two bar charts, the latest notices and quick-action buttons.
 *
 * Connection to common: extends BasePanel; quick actions call BaseDashboard.showPage(...)
 * through the AdminDashboard (callback interface -> loose coupling).
 *
 * OOP: Inheritance (BasePanel), Polymorphism (refresh() override), Abstraction
 *      (QuickActions interface: the page does not need to know the dashboard class).
 */
public class AdminHomePanel extends BasePanel {

    /** What the quick-action buttons can ask the dashboard to do. */
    public interface QuickActions {
        void open(String pageName);

        void addUser();

        void addCourse();

        void addNotice();

        void addTimetableSlot();
    }

    private final AdminStatsDAO stats = new AdminStatsDAO();
    private final AdminNoticeDAO noticeDAO = new AdminNoticeDAO();
    private final QuickActions actions;

    private final StatCard usersCard = new StatCard("Total users", "-", "users", Theme.ADMIN_ACCENT);
    private final StatCard lecturersCard = new StatCard("Lecturers", "-", "user", Theme.LECTURER_ACCENT);
    private final StatCard tosCard = new StatCard("Technical officers", "-", "settings", Theme.TO_ACCENT);
    private final StatCard studentsCard = new StatCard("Undergraduates", "-", "cap", Theme.STUDENT_ACCENT);
    private final StatCard coursesCard = new StatCard("Courses", "-", "book", Theme.INFO);
    private final StatCard noticesCard = new StatCard("Active notices", "-", "bell", Theme.WARNING);

    private final BarChart roleChart = new BarChart();
    private final BarChart typeChart = new BarChart();
    private final JPanel noticeList = new JPanel();
    private final JLabel welcome = UI.heading("");

    public AdminHomePanel(User user, Color accent, QuickActions actions) {
        super(user, accent);
        this.actions = actions;
        build();
    }

    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public String getSubtitle() {
        return "Overview of the Faculty of Technology academic system";
    }

    private void build() {
        // ---- welcome banner + quick actions
        CardPanel banner = new CardPanel(new BorderLayout(16, 0));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(welcome);
        text.add(Box.createVerticalStrut(4));
        text.add(UI.muted("Academic year " + AppConfig.CURRENT_ACADEMIC_YEAR + "  ·  current semester: Level "
                + AppConfig.CURRENT_LEVEL + ", Semester " + AppConfig.CURRENT_SEMESTER));
        ModernButton addUser = accentButton("Add user", "plus");
        addUser.addActionListener(e -> actions.addUser());
        ModernButton addCourse = UI.secondary("Add course", "book");
        addCourse.addActionListener(e -> actions.addCourse());
        ModernButton addNotice = UI.secondary("New notice", "bell");
        addNotice.addActionListener(e -> actions.addNotice());
        ModernButton addSlot = UI.secondary("Add timetable slot", "calendar");
        addSlot.addActionListener(e -> actions.addTimetableSlot());
        banner.add(text, BorderLayout.CENTER);
        banner.add(UI.rowRight(addUser, addCourse, addNotice, addSlot), BorderLayout.EAST);

        // ---- stat cards (3 x 2)
        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setOpaque(false);
        for (StatCard c : new StatCard[]{usersCard, lecturersCard, tosCard, studentsCard, coursesCard, noticesCard}) {
            c.setPreferredSize(new Dimension(220, 96));
            cards.add(c);
        }
        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(banner, BorderLayout.NORTH);
        north.add(cards, BorderLayout.CENTER);

        // ---- charts
        roleChart.setAccent(accent);
        typeChart.setAccent(Theme.STUDENT_ACCENT);
        CardPanel roleCard = CardPanel.titled("Users by role");
        roleCard.add(roleChart, BorderLayout.CENTER);
        CardPanel typeCard = CardPanel.titled("Undergraduates by type");
        typeCard.add(typeChart, BorderLayout.CENTER);
        typeCard.add(UI.muted("Normal, repeat and batch-missed students"), BorderLayout.SOUTH);
        roleCard.add(UI.muted("All accounts in the system"), BorderLayout.SOUTH);

        // ---- recent notices
        CardPanel noticesCardPanel = new CardPanel(new BorderLayout(0, 12));
        ModernButton all = UI.secondary("Manage", "bell");
        all.addActionListener(e -> actions.open("Notices"));
        noticesCardPanel.add(UI.toolbar(UI.heading("Recent notices"), all), BorderLayout.NORTH);
        noticeList.setOpaque(false);
        noticeList.setLayout(new BoxLayout(noticeList, BoxLayout.Y_AXIS));
        noticesCardPanel.add(noticeList, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 3, 16, 0));
        bottom.setOpaque(false);
        bottom.add(roleCard);
        bottom.add(typeCard);
        bottom.add(noticesCardPanel);
        bottom.setPreferredSize(new Dimension(900, 300));
        bottom.setMinimumSize(new Dimension(600, 260));

        add(north, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);
        setMinimumSize(new Dimension(900, 640));
    }

    @Override
    public void refresh() {
        welcome.setText("Welcome back, " + user.getFullName());
        safe(() -> {
            Map<Role, Integer> byRole = stats.usersByRole();
            int total = stats.countUsers();
            int inactive = stats.countInactiveUsers();
            usersCard.setValue(String.valueOf(total));
            usersCard.setCaption(inactive == 0 ? "all accounts active" : inactive + " inactive");
            lecturersCard.setValue(String.valueOf(byRole.get(Role.LECTURER)));
            lecturersCard.setCaption("teaching staff");
            tosCard.setValue(String.valueOf(byRole.get(Role.TO)));
            tosCard.setCaption("attendance & medicals");
            studentsCard.setValue(String.valueOf(byRole.get(Role.UNDERGRADUATE)));
            Map<StudentType, Integer> byType = stats.studentsByType();
            studentsCard.setCaption(byType.get(StudentType.REPEAT) + " repeat, " + byType.get(StudentType.BATCH_MISSED)
                    + " batch missed");
            coursesCard.setValue(String.valueOf(stats.countCourses()));
            coursesCard.setCaption(stats.countCurrentSemesterCourses(AppConfig.CURRENT_LEVEL, AppConfig.CURRENT_SEMESTER)
                    + " in L" + AppConfig.CURRENT_LEVEL + " S" + AppConfig.CURRENT_SEMESTER);
            noticesCard.setValue(String.valueOf(stats.countActiveNotices()));
            noticesCard.setCaption(stats.countTimetableSlots() + " timetable slots");

            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            double max = 1;
            for (Map.Entry<Role, Integer> e : byRole.entrySet()) {
                labels.add(shortRole(e.getKey()));
                values.add((double) e.getValue());
                max = Math.max(max, e.getValue());
            }
            roleChart.setMax(niceMax(max));
            roleChart.setData(labels, values);

            labels = new ArrayList<>();
            values = new ArrayList<>();
            max = 1;
            for (Map.Entry<StudentType, Integer> e : byType.entrySet()) {
                labels.add(e.getKey().toString());
                values.add((double) e.getValue());
                max = Math.max(max, e.getValue());
            }
            typeChart.setMax(niceMax(max));
            typeChart.setData(labels, values);

            fillNotices(noticeDAO.findRecent(3));
        });
    }

    private void fillNotices(List<Notice> notices) {
        noticeList.removeAll();
        if (notices.isEmpty()) noticeList.add(UI.muted("No notices yet."));
        for (Notice n : notices) {
            JPanel row = new JPanel(new BorderLayout(10, 2));
            row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(7, 0, 7, 0)));
            String t = n.getTitle().length() > 40 ? n.getTitle().substring(0, 38) + "…" : n.getTitle();
            JLabel title = UI.label(t);
            title.setToolTipText(n.getTitle());
            boolean active = "ACTIVE".equals(n.getStatus());
            row.add(title, BorderLayout.NORTH);
            row.add(UI.muted(n.getTargetRole() + "  ·  " + Format.date(n.getCreatedAt() == null ? null
                    : n.getCreatedAt().toLocalDate()) + (active ? "" : "  ·  hidden")), BorderLayout.CENTER);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            noticeList.add(row);
        }
        noticeList.revalidate();
        noticeList.repaint();
    }

    private static String shortRole(Role r) {
        return switch (r) {
            case ADMIN -> "Admins";
            case LECTURER -> "Lecturers";
            case TO -> "TOs";
            case UNDERGRADUATE -> "Students";
        };
    }

    /** Rounds the chart maximum up to a multiple of 4 so the grid labels are whole numbers. */
    private static double niceMax(double v) {
        int m = (int) Math.ceil(v * 1.15);
        return Math.max(4, ((m + 3) / 4) * 4);
    }
}

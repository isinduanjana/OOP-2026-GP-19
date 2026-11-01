package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.NoticeDAO;
import lk.ac.ruh.fot.ams.model.AttendanceSession;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.techofficer.dao.TOAttendanceDAO;
import lk.ac.ruh.fot.ams.techofficer.dao.TOMedicalDAO;
import lk.ac.ruh.fot.ams.techofficer.model.ApproveDecision;
import lk.ac.ruh.fot.ams.techofficer.model.MedicalDecision;
import lk.ac.ruh.fot.ams.techofficer.model.RejectDecision;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Home page of the TO portal: four StatCards,
 * average attendance per course (BarChart with the 80% line), pending medicals
 * with quick Approve / Reject, and the latest notices.
 *
 * Connection to common: extends BasePanel (title / refresh / safe), uses the common
 * AttendanceService for percentages and the common UI kit (StatCard, BarChart ...).
 * OOP: Inheritance, Polymorphism (decision buttons work through the MedicalDecision
 * interface), Encapsulation (private helper methods).
 */
public class TOHomePanel extends BasePanel {

    private final CourseDAO courseDAO = new CourseDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final TOAttendanceDAO attendanceDAO = new TOAttendanceDAO();
    private final TOMedicalDAO medicalDAO = new TOMedicalDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    private final StatCard sessionsCard;
    private final StatCard lastCard;
    private final StatCard pendingCard;
    private final StatCard belowCard;
    private final BarChart chart = new BarChart();
    private final DataTableModel pendingModel = new DataTableModel("TG No", "Course", "Date", "Reason");
    private final JTable pendingTable = UI.table(pendingModel);
    private final JPanel noticeList = new JPanel();

    public TOHomePanel(User user, Color accent) {
        super(user, accent);
        sessionsCard = new StatCard("Sessions recorded", "-", "clipboard", accent);
        lastCard = new StatCard("Last session", "-", "calendar", Theme.INFO);
        pendingCard = new StatCard("Pending medicals", "-", "medical", Theme.WARNING);
        belowCard = new StatCard("Below 80%", "-", "users", Theme.DANGER);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(sessionsCard);
        stats.add(lastCard);
        stats.add(pendingCard);
        stats.add(belowCard);

        // ---- chart card
        CardPanel chartCard = CardPanel.titled("Average attendance per course (this semester)");
        chart.setAccent(accent);
        chart.setThreshold(AppConfig.MIN_ATTENDANCE_PERCENT, "80% eligibility");
        chart.setPreferredSize(new Dimension(420, 250));
        chartCard.add(chart, BorderLayout.CENTER);
        chartCard.add(UI.muted("Combined theory + practical, approved medicals counted"), BorderLayout.SOUTH);

        // ---- pending medicals card (quick approve / reject)
        CardPanel pendingCardPanel = new CardPanel(new BorderLayout(0, 10));
        ModernButton openAll = UI.secondary("All medicals", "medical");
        openAll.addActionListener(e -> goTo("Medicals"));
        pendingCardPanel.add(UI.toolbar(UI.heading("Pending medicals"), UI.rowRight(openAll)), BorderLayout.NORTH);
        UI.setColumnWidths(pendingTable, 122, 90, 100, 150);
        pendingCardPanel.add(UI.scroll(pendingTable), BorderLayout.CENTER);
        List<ModernButton> buttons = new ArrayList<>();
        for (MedicalDecision d : new MedicalDecision[]{new ApproveDecision(), new RejectDecision()}) {
            ModernButton b = d instanceof ApproveDecision ? UI.success(d.getActionName(), d.getIconName())
                    : UI.danger(d.getActionName(), d.getIconName());
            b.addActionListener(e -> decide(d));
            buttons.add(b);
        }
        pendingCardPanel.add(UI.rowRight(buttons.toArray(new ModernButton[0])), BorderLayout.SOUTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 16, 0));
        middle.setOpaque(false);
        middle.add(chartCard);
        middle.add(pendingCardPanel);
        middle.setPreferredSize(new Dimension(800, 330));

        // ---- notices
        CardPanel notices = new CardPanel(new BorderLayout(0, 10));
        ModernButton openNotices = UI.secondary("All notices", "bell");
        openNotices.addActionListener(e -> goTo("Notices"));
        ModernButton mark = accentButton("Mark attendance", "clipboard");
        mark.addActionListener(e -> goTo("Mark Attendance"));
        notices.add(UI.toolbar(UI.heading("Latest notices"), UI.rowRight(mark, openNotices)), BorderLayout.NORTH);
        noticeList.setOpaque(false);
        noticeList.setLayout(new BoxLayout(noticeList, BoxLayout.Y_AXIS));
        notices.add(noticeList, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(middle, BorderLayout.CENTER);
        center.add(notices, BorderLayout.SOUTH);

        add(stats, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public String getSubtitle() {
        return "Welcome, " + user.getFullName() + "  |  Level " + AppConfig.CURRENT_LEVEL + " Semester "
                + AppConfig.CURRENT_SEMESTER + " attendance & medical overview";
    }

    @Override
    public void refresh() {
        safe(() -> {
            List<Course> courses = courseDAO.findCurrentSemester();

            // sessions recorded vs planned (15 per component)
            int held = attendanceDAO.countCurrentSemesterSessions();
            int planned = attendanceDAO.countPlannedSessions(courses);
            sessionsCard.setValue(held + " / " + planned);
            sessionsCard.setCaption(courses.size() + " courses this semester");

            AttendanceSession last = attendanceDAO.findLatestSession();
            if (last == null) {
                lastCard.setValue("None");
                lastCard.setCaption("no sessions recorded yet");
            } else {
                boolean today = LocalDate.now().equals(last.getSessionDate());
                lastCard.setValue(last.getCourseCode());
                lastCard.setCaption((today ? "Today - " : Format.date(last.getSessionDate()) + " - ")
                        + last.getComponent() + " #" + last.getSessionNumber());
            }

            // average % per course + students below 80% in at least one course
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            Set<Integer> below = new HashSet<>();
            for (Course c : courses) {
                List<AttendanceSummary> batch = attendanceService.getBatchSummary(c);
                double sum = 0;
                for (AttendanceSummary s : batch) {
                    sum += s.getCombinedPercentage(true);
                    if (!s.isEligible()) below.add(s.getStudentId());
                }
                labels.add(c.getCourseCode());
                values.add(batch.isEmpty() ? 0 : sum / batch.size());
            }
            chart.setData(labels, values);
            belowCard.setValue(String.valueOf(below.size()));
            belowCard.setCaption("students, any course");

            loadPending();

            noticeList.removeAll();
            List<Notice> list = noticeDAO.findActiveForRole(user.getRole());
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                noticeList.add(noticeRow(list.get(i)));
                noticeList.add(Box.createVerticalStrut(8));
            }
            if (list.isEmpty()) noticeList.add(UI.muted("No notices."));
            noticeList.revalidate();
            noticeList.repaint();
        });
    }

    private void loadPending() throws lk.ac.ruh.fot.ams.exception.AppException {
        List<Medical> pending = medicalDAO.findPending();
        pendingModel.clear();
        for (Medical m : pending) {
            pendingModel.addRow(m, m.getTgNo(),
                    m.getCourseCode() == null ? "All courses" : m.getCourseCode(),
                    Format.date(m.getMedicalDate()), Format.nullToDash(m.getReason()));
        }
        pendingModel.refresh();
        pendingCard.setValue(String.valueOf(pending.size()));
        pendingCard.setCaption(pending.isEmpty() ? "all medicals reviewed" : "awaiting your decision");
    }

    /** Same code for approve and reject - the decision object decides (polymorphism). */
    private void decide(MedicalDecision decision) {
        Medical m = pendingModel.getItem(UI.selectedModelRow(pendingTable));
        if (m == null) {
            UI.warn(this, "Select a pending medical first.");
            return;
        }
        if (!UI.confirm(this, decision.confirmMessage(m))) return;
        if (safe(() -> medicalDAO.applyDecision(m, decision, user.getUserId()))) {
            refresh();
        }
    }

    private JPanel noticeRow(Notice n) {
        JPanel row = new JPanel(new BorderLayout(12, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createEmptyBorder(2, 12, 2, 0)));
        JLabel title = UI.label(n.getTitle());
        String desc = n.getDescription() == null ? "" : n.getDescription();
        if (desc.length() > 140) desc = desc.substring(0, 137) + "...";
        row.add(title, BorderLayout.NORTH);
        row.add(UI.muted(desc), BorderLayout.CENTER);
        row.add(UI.muted(Format.dateTime(n.getCreatedAt())), BorderLayout.EAST);
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    private void goTo(String page) {
        if (SwingUtilities.getWindowAncestor(this) instanceof BaseDashboard d) {
            d.showPage(page);
        }
    }
}

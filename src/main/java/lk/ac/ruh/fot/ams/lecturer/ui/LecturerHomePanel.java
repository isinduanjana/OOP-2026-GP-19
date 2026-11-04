package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.NoticeDAO;
import lk.ac.ruh.fot.ams.lecturer.dao.CourseMaterialDAO;
import lk.ac.ruh.fot.ams.lecturer.dao.LecturerCourseDAO;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.service.ResultService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] Lecturer home page: stat cards, my courses as tiles, a bar chart of
 * final-exam eligibility per course and the latest 3 notices.
 *
 * OOP: Inheritance (LecturerPage -> BasePanel), Composition (uses common ResultService,
 * NoticeDAO and M2 DAOs), Encapsulation (private helper methods build each part).
 */
public class LecturerHomePanel extends LecturerPage {

    private final ResultService resultService = new ResultService();
    private final LecturerCourseDAO lecturerCourseDAO = new LecturerCourseDAO();
    private final CourseMaterialDAO materialDAO = new CourseMaterialDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();

    private final StatCard coursesCard;
    private final StatCard studentsCard;
    private final StatCard caCard;
    private final StatCard notEligibleCard;
    private final JPanel courseTiles = new JPanel(new GridLayout(0, 3, 12, 12));
    private final BarChart chart = new BarChart();
    private final JPanel noticeList = new JPanel();
    private final JLabel welcome = UI.title("");

    public LecturerHomePanel(User user, Color accent) {
        super(user, accent);
        coursesCard = new StatCard("My courses", "-", "book", accent);
        studentsCard = new StatCard("My students", "-", "users", Theme.INFO);
        caCard = new StatCard("Average CA", "-", "chart", Theme.SUCCESS);
        notEligibleCard = new StatCard("Not eligible", "-", "medical", Theme.DANGER);

        // --- welcome banner + stat row
        ModernButton marks = accentButton("Enter marks", "clipboard");
        marks.addActionListener(e -> openPage("Marks Entry"));
        ModernButton results = UI.secondary("Results & grades", "grade");
        results.addActionListener(e -> openPage("Results & Grades"));
        JPanel banner = new JPanel(new BorderLayout());
        banner.setOpaque(false);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(welcome);
        names.add(Box.createVerticalStrut(2));
        names.add(UI.muted(Format.nullToDash(user.getDepartmentName()) + "   •   Academic year "
                + AppConfig.CURRENT_ACADEMIC_YEAR + "   •   current semester: Level " + AppConfig.CURRENT_LEVEL
                + " Semester " + AppConfig.CURRENT_SEMESTER));
        banner.add(names, BorderLayout.WEST);
        banner.add(UI.rowRight(results, marks), BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(banner, BorderLayout.NORTH);
        north.add(statRow(coursesCard, studentsCard, caCard, notEligibleCard), BorderLayout.CENTER);

        // --- my courses tiles
        courseTiles.setOpaque(false);
        CardPanel coursesBox = CardPanel.titled("My courses");
        coursesBox.add(courseTiles, BorderLayout.CENTER);

        // --- chart
        chart.setAccent(accent);
        chart.setThreshold(50, "50%");
        chart.setPreferredSize(new Dimension(380, 230));
        chart.setEmptyText("No students enrolled in your courses this year");
        CardPanel chartCard = CardPanel.titled("Final-exam eligibility % per course");
        chartCard.add(chart, BorderLayout.CENTER);
        chartCard.add(UI.muted("Eligible = CA ≥ 40% and attendance ≥ 80% (current academic year)"), BorderLayout.SOUTH);

        // --- notices
        noticeList.setOpaque(false);
        noticeList.setLayout(new BoxLayout(noticeList, BoxLayout.Y_AXIS));
        ModernButton allNotices = UI.secondary("All notices", "bell");
        allNotices.addActionListener(e -> openPage("Notices"));
        CardPanel noticeCard = CardPanel.titled("Latest notices");
        noticeCard.add(noticeList, BorderLayout.CENTER);
        noticeCard.add(UI.rowRight(allNotices), BorderLayout.SOUTH);
        noticeCard.setPreferredSize(new Dimension(380, 300));

        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0));
        lower.setOpaque(false);
        lower.add(chartCard);
        lower.add(noticeCard);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(coursesBox, BorderLayout.NORTH);
        center.add(lower, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public String getSubtitle() {
        return "Overview of your courses, students and notices";
    }

    @Override
    public void refresh() {
        welcome.setText("Welcome back, " + user.getFullName());
        safe(() -> {
            List<Course> courses = myCourses();
            Map<Integer, Integer> counts = lecturerCourseDAO.studentCounts(lecturerId());
            int students = lecturerCourseDAO.findMyStudents(lecturerId()).size();

            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            double caSum = 0;
            int caCount = 0;
            int notEligible = 0;
            courseTiles.removeAll();
            for (Course c : courses) {
                int n = counts.getOrDefault(c.getCourseId(), 0);
                courseTiles.add(tile(c, n));
                if (n == 0) continue;
                List<CourseResult> results = resultService.getBatchResults(c);
                int eligible = 0;
                for (CourseResult r : results) {
                    if (r.isEligible()) eligible++;
                    else notEligible++;
                    if (r.getCaWeight() > 0 && !r.getRawMarks().isEmpty()) {
                        caSum += r.getCaPercentage();
                        caCount++;
                    }
                }
                labels.add(c.getCourseCode());
                values.add(results.isEmpty() ? 0 : eligible * 100.0 / results.size());
            }
            chart.setData(labels, values);
            courseTiles.revalidate();
            courseTiles.repaint();

            coursesCard.setValue(String.valueOf(courses.size()));
            coursesCard.setCaption(materialDAO.countForLecturer(lecturerId()) + " materials uploaded");
            studentsCard.setValue(String.valueOf(students));
            studentsCard.setCaption("enrolled in " + AppConfig.CURRENT_ACADEMIC_YEAR);
            caCard.setValue(caCount == 0 ? "-" : Format.percent(caSum / caCount));
            caCard.setCaption("CA % of my students");
            notEligibleCard.setValue(String.valueOf(notEligible));
            notEligibleCard.setCaption("to sit final exams");

            noticeList.removeAll();
            List<Notice> notices = noticeDAO.findActiveForRole(user.getRole());
            for (int i = 0; i < Math.min(3, notices.size()); i++) {
                noticeList.add(noticeRow(notices.get(i)));
                noticeList.add(Box.createVerticalStrut(10));
            }
            if (notices.isEmpty()) noticeList.add(UI.muted("No notices."));
            noticeList.revalidate();
            noticeList.repaint();
        });
    }

    /** Small clickable tile of one course. */
    private JPanel tile(Course c, int students) {
        JPanel t = new JPanel(new BorderLayout(0, 6));
        t.setBackground(Theme.tint(accent, 0.93));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JLabel code = UI.label(c.getCourseCode());
        code.setForeground(Theme.darker(accent, 0.25));
        top.add(code, BorderLayout.WEST);
        top.add(UI.badge(c.getLevelSemesterLabel(), accent), BorderLayout.EAST);
        JLabel name = new JLabel(c.getCourseName());
        name.setFont(Theme.FONT);
        name.setForeground(Theme.TEXT);
        JLabel meta = UI.muted(c.getCredits() + " credits   •   " + c.getComponentLabel() + "   •   " + students + " students");
        t.add(top, BorderLayout.NORTH);
        t.add(name, BorderLayout.CENTER);
        t.add(meta, BorderLayout.SOUTH);
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t.setToolTipText("Open course details and materials");
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPage("My Courses");
            }
        });
        return t;
    }

    private JPanel noticeRow(Notice n) {
        JPanel p = new JPanel(new BorderLayout(10, 2));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel title = UI.label(n.getTitle());
        String d = n.getDescription() == null ? "" : n.getDescription();
        if (d.length() > 110) d = d.substring(0, 107) + "...";
        JLabel desc = UI.muted("<html>" + d + "</html>");
        JLabel date = UI.muted(Format.date(n.getCreatedAt() == null ? null : n.getCreatedAt().toLocalDate()));
        JPanel head = new JPanel(new BorderLayout(8, 0));
        head.setOpaque(false);
        head.add(title, BorderLayout.CENTER);
        head.add(date, BorderLayout.EAST);
        p.add(head, BorderLayout.NORTH);
        p.add(desc, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.tint(accent, 0.5)),
                BorderFactory.createEmptyBorder(2, 10, 2, 0)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return p;
    }
}

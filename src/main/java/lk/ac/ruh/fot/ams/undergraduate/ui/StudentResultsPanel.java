package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.model.report.Transcript;
import lk.ac.ruh.fot.ams.service.ResultService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ProgressRing;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] "Results &amp; GPA" - the student's transcript:
 * CGPA + class, SGPA chart, and one tab per semester with every course's
 * CA %, final exam, total, grade, grade point and remarks. The current
 * semester tab also shows CA / attendance eligibility for the final exam.
 *
 * All calculations are done by the COMMON ResultService.getTranscript(...)
 * (CA &ge; 40%, attendance &ge; 80%, repeat students capped at C, SGPA/CGPA).
 *
 * OOP: Inheritance (extends StudentViewPanel), Polymorphism (own
 * loadStudentData()), Classes &amp; objects (Transcript -&gt; SemesterResult -&gt; CourseResult).
 */
public class StudentResultsPanel extends StudentViewPanel {

    private final ResultService resultService = new ResultService();
    private final CourseDAO courseDAO = new CourseDAO();

    private final ProgressRing cgpaRing = new ProgressRing(130);
    private final JLabel standingLabel = new JLabel();
    private final JLabel creditsLabel = UI.muted("");
    private final JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final BarChart sgpaChart = new BarChart();
    private final JTabbedPane tabs = new JTabbedPane();

    public StudentResultsPanel(User user, Color accent) {
        super(user, accent);
        buildUI();
    }

    @Override
    public String getTitle() {
        return "Results & GPA";
    }

    @Override
    public String getSubtitle() {
        return "Grades, SGPA per semester and your CGPA (UGC grading scale)";
    }

    private void buildUI() {
        // ------- CGPA card
        CardPanel cgpaCard = new CardPanel(new BorderLayout(0, 6));
        cgpaCard.setPreferredSize(new Dimension(260, 270));
        JLabel t = new JLabel("CUMULATIVE GPA", JLabel.CENTER);
        t.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        t.setForeground(Theme.TEXT_MUTED);
        standingLabel.setFont(Theme.FONT_HEADING);
        standingLabel.setHorizontalAlignment(JLabel.CENTER);
        creditsLabel.setHorizontalAlignment(JLabel.CENTER);
        typeRow.setOpaque(false);
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{standingLabel, creditsLabel, typeRow}) {
            c.setAlignmentX(CENTER_ALIGNMENT);
        }
        south.add(standingLabel);
        south.add(Box.createVerticalStrut(2));
        south.add(creditsLabel);
        south.add(Box.createVerticalStrut(8));
        south.add(typeRow);
        cgpaCard.add(t, BorderLayout.NORTH);
        cgpaCard.add(cgpaRing, BorderLayout.CENTER);
        cgpaCard.add(south, BorderLayout.SOUTH);

        // ------- SGPA chart
        CardPanel chartCard = CardPanel.titled("SGPA per semester");
        sgpaChart.setMax(4);
        sgpaChart.setAccent(accent);
        sgpaChart.setThreshold(2.0, "2.00 pass");
        sgpaChart.setEmptyText("No graded semesters yet");
        sgpaChart.setPreferredSize(new Dimension(400, 210));
        chartCard.add(sgpaChart, BorderLayout.CENTER);

        // ------- grade scale legend
        CardPanel scaleCard = CardPanel.titled("Grade scale");
        scaleCard.setPreferredSize(new Dimension(230, 270));
        JPanel scale = new JPanel(new GridLayout(0, 2, 10, 2));
        scale.setOpaque(false);
        for (GradeScale g : GradeScale.values()) {
            JLabel l = new JLabel("<html><b>" + g.getLetter() + "</b> <span style='color:#6B7280'>"
                    + (g == GradeScale.E ? "&lt;30" : "≥" + g.getMinMark()) + "</span></html>");
            l.setFont(Theme.FONT_SMALL);
            JLabel p = UI.muted(Format.gpa(g.getGradePoint()));
            scale.add(l);
            scale.add(p);
        }
        scaleCard.add(scale, BorderLayout.CENTER);
        scaleCard.add(UI.muted("Repeat students: max grade C"), BorderLayout.SOUTH);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 14);
        top.add(cgpaCard, c);
        c.gridx = 1;
        c.weightx = 1;
        top.add(chartCard, c);
        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        top.add(scaleCard, c);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    protected void loadStudentData() throws AppException {
        Transcript tr = resultService.getTranscript(getStudent());
        Set<Integer> currentIds = new HashSet<>();
        for (Course course : courseDAO.findByStudent(getStudentId())) currentIds.add(course.getCourseId());

        // ---- CGPA card
        double cgpa = tr.getCgpa();
        boolean graded = tr.getTotalCredits() > 0;
        Color ringColor = !graded ? Theme.TEXT_MUTED : cgpa >= 3.0 ? Theme.SUCCESS : cgpa >= 2.0 ? accent : Theme.DANGER;
        cgpaRing.setValue(cgpa, 4.0, graded ? Format.gpa(cgpa) : "-", "out of 4.00", ringColor);
        standingLabel.setText(graded ? tr.getStanding() : "No grades yet");
        standingLabel.setForeground(graded ? Theme.darker(ringColor, 0.1) : Theme.TEXT_MUTED);
        creditsLabel.setText(tr.getTotalCredits() + " graded credits  •  " + tr.getSemesters().size() + " semester(s)");
        typeRow.removeAll();
        typeRow.add(UI.badge(StudentUI.typeText(getStudent().getStudentType()),
                StudentUI.typeColor(getStudent().getStudentType())));
        typeRow.revalidate();

        // ---- SGPA chart (label shows the exact SGPA because bars print whole numbers)
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (SemesterResult s : tr.getSemesters()) {
            if (s.getGradedCredits() == 0) continue;
            labels.add("L" + s.getLevel() + "S" + s.getSemester() + " · " + Format.gpa(s.getSgpa()));
            values.add(s.getSgpa());
        }
        sgpaChart.setData(labels, values);

        // ---- one tab per semester (newest first)
        int keep = tabs.getSelectedIndex();
        int previousCount = tabs.getTabCount();
        tabs.removeAll();
        List<SemesterResult> sems = new ArrayList<>(tr.getSemesters());
        java.util.Collections.reverse(sems);
        for (SemesterResult s : sems) {
            boolean current = isCurrent(s, currentIds);
            tabs.addTab(s.getLabel() + (current ? "  (current)" : ""), semesterCard(s, current, currentIds));
        }
        if (sems.isEmpty()) {
            CardPanel empty = new CardPanel();
            empty.add(StudentUI.empty("No results yet. Results appear here when lecturers enter marks."));
            tabs.addTab("Results", empty);
        }
        if (keep >= 0 && keep < tabs.getTabCount() && previousCount == tabs.getTabCount()) {
            tabs.setSelectedIndex(keep);
        }
    }

    private static boolean isCurrent(SemesterResult s, Set<Integer> currentIds) {
        for (CourseResult r : s.getResults()) {
            if (currentIds.contains(r.getCourse().getCourseId())) return true;
        }
        return false;
    }

    /** Card with the result table and SGPA footer of one semester. */
    private JComponent semesterCard(SemesterResult s, boolean current, Set<Integer> currentIds) {
        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        DataTableModel model = current
                ? new DataTableModel("Code", "Course name", "Cr", "CA %", "CA ok", "Attend.",
                "Final", "Total", "Grade", "GP", "Exam eligibility", "Remarks")
                : new DataTableModel("Code", "Course name", "Credits", "CA %", "Final exam", "Total", "Grade",
                "Grade point", "Remarks");
        int eligible = 0;
        int followingNow = 0;
        for (CourseResult r : s.getResults()) {
            Course c = r.getCourse();
            String ca = r.getCaWeight() == 0 ? "-" : Format.percent(r.getCaPercentage());
            String fin = r.getFinalExamRaw() == null ? "-" : Format.num(r.getFinalExamRaw());
            String total = r.getTotalMarks() == null ? "-" : Format.num(r.getTotalMarks());
            String gp = r.hasGrade() ? Format.gpa(r.getGradePoint()) : "-";
            String remarks = Format.nullToDash(r.getRemarks());
            boolean thisYear = currentIds.contains(c.getCourseId());
            if (current && !thisYear) {
                // e.g. a REPEAT student: course of this semester already completed in an earlier year
                model.addRow(r, c.getCourseCode(), c.getCourseName(), c.getCredits(), ca, "-", "-", fin, total,
                        r.getGradeLetter(), gp, "Completed earlier", remarks);
            } else if (current) {
                followingNow++;
                if (r.isEligible()) eligible++;
                model.addRow(r, c.getCourseCode(), c.getCourseName(), c.getCredits(), ca,
                        r.getCaWeight() == 0 ? "-" : (r.isCaEligible() ? "Yes" : "No"),
                        Format.percent(r.getAttendancePercent()), fin, total, r.getGradeLetter(), gp,
                        r.getEligibilityText(), remarks);
            } else {
                model.addRow(r, c.getCourseCode(), c.getCourseName(), c.getCredits(), ca, fin, total,
                        r.getGradeLetter(), gp, remarks);
            }
        }
        model.refresh();
        JTable table = UI.table(model);
        if (current) {
            UI.setColumnWidths(table, 80, 170, 32, 62, 52, 64, 58, 58, 50, 48, 145, 180);
        } else {
            UI.setColumnWidths(table, 80, 260, 60, 70, 80, 70, 60, 85, 160);
        }
        JScrollPane sp = UI.scroll(table);
        sp.setPreferredSize(new Dimension(400, 34 * Math.max(3, s.getResults().size()) + 44));
        card.add(sp, BorderLayout.CENTER);

        // footer: SGPA + credits (+ eligibility for the current semester)
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        foot.setOpaque(false);
        boolean graded = s.getGradedCredits() > 0;
        JLabel sgpa = UI.heading("SGPA  " + (graded ? Format.gpa(s.getSgpa()) : "-"));
        sgpa.setForeground(Theme.darker(accent, 0.1));
        foot.add(sgpa);
        foot.add(UI.muted(s.getGradedCredits() + " graded credits of " + totalCredits(s)));
        if (current) {
            int n = followingNow;
            foot.add(UI.badge(eligible + "/" + n + " eligible for final exams",
                    eligible == n ? Theme.SUCCESS : Theme.WARNING));
            foot.add(UI.muted("Rule: CA ≥ " + (int) AppConfig.MIN_CA_PERCENT + "% and attendance ≥ "
                    + (int) AppConfig.MIN_ATTENDANCE_PERCENT + "%"));
        } else {
            foot.add(UI.badge("Published results", Theme.INFO));
        }
        foot.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private static int totalCredits(SemesterResult s) {
        int n = 0;
        for (CourseResult r : s.getResults()) n += r.getCourse().getCredits();
        return n;
    }
}

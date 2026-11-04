package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.lecturer.dao.GradePublishDAO;
import lk.ac.ruh.fot.ams.lecturer.service.GradePolicy;
import lk.ac.ruh.fot.ams.lecturer.service.MarkParser;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;


public class ResultsGradesPanel extends ResultReportPanel {

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final GradePublishDAO publishDAO = new GradePublishDAO();
    private final JCheckBox allCourses = new JCheckBox("All my courses (individual)");
    private final ModernButton publishBtn;

    private final StatCard gradedCard;
    private final StatCard avgCard;
    private final StatCard passCard;
    private final StatCard publishedCard;

    private List<CourseResult> lastBatch = new ArrayList<>();

    public ResultsGradesPanel(User user, Color accent) {
        super(user, accent);
        gradedCard = new StatCard("Graded", "-", "grade", accent);
        avgCard = new StatCard("Average mark", "-", "chart", Theme.INFO);
        passCard = new StatCard("Passed (C or above)", "-", "check", Theme.SUCCESS);
        publishedCard = new StatCard("Published", "-", "upload", Theme.WARNING);
        allCourses.setOpaque(false);
        allCourses.addActionListener(e -> render());
        publishBtn = accentButton("Publish grades", "upload");
        publishBtn.addActionListener(e -> publish());
        addFilter(allCourses, publishBtn);
        setHint("Final mark = CA + final exam (out of 100)  •  UGC Circular 12-2024 grading  •  not eligible = E");
    }

    @Override
    public String getTitle() {
        return "Results & Grades";
    }

    @Override
    public String getSubtitle() {
        return "Marks of every assessment, final marks, grades and grade points";
    }

    // ------------------------------------------------------------ batch
    @Override
    protected void renderBatch(Course course) throws AppException {
        publishBtn.setEnabled(true);
        List<Assessment> assessments = assessmentDAO.findByCourse(course.getCourseId());
        List<CourseResult> results = resultService.getBatchResults(course);
        lastBatch = results;

        List<Assessment> caParts = new ArrayList<>();
        for (Assessment a : assessments) {
            if (a.getType().isContinuousAssessment()) caParts.add(a);   // final exam has its own column
        }
        List<String> cols = new ArrayList<>(List.of("TG No", "Name"));
        for (Assessment a : caParts) cols.add(a.getName());
        cols.addAll(List.of("CA total", "Final exam", "Total /100", "Grade", "GP", "Grading rule", "Remarks"));
        DataTableModel model = new DataTableModel(cols.toArray(new String[0]));

        Map<String, Integer> dist = new LinkedHashMap<>();
        for (GradeScale g : GradeScale.values()) dist.put(g.getLetter(), 0);
        int graded = 0;
        int passed = 0;
        double sum = 0;
        int withTotal = 0;
        for (CourseResult r : results) {
            List<Object> row = new ArrayList<>(List.of(r.getTgNo(), r.getStudentName()));
            for (Assessment a : caParts) {
                Double m = r.getRawMarks().get(a.getName());
                row.add(m == null ? "-" : MarkParser.format(m));
            }
            row.add(Format.num(r.getCaObtained()));
            row.add(r.getFinalExamRaw() == null ? "-" : MarkParser.format(r.getFinalExamRaw()));
            row.add(Format.num(r.getTotalMarks()));
            row.add(r.getGradeLetter());
            row.add(r.hasGrade() ? Format.gpa(r.getGradePoint()) : "-");
            row.add(GradePolicy.forStudent(r.getStudentType()).getName());   // polymorphic call
            row.add(r.getRemarks());
            model.addRow(r, row.toArray());
            if (r.hasGrade()) {
                graded++;
                dist.merge(r.getGradeLetter(), 1, Integer::sum);
                if (r.getGrade().isPass()) passed++;
            }
            if (r.getTotalMarks() != null) {
                sum += r.getTotalMarks();
                withTotal++;
            }
        }
        model.refresh();

        gradedCard.setValue(graded + " / " + results.size());
        gradedCard.setCaption("students with a grade");
        avgCard.setValue(withTotal == 0 ? "-" : Format.num(sum / withTotal));
        avgCard.setCaption("average final mark /100");
        passCard.setValue(String.valueOf(passed));
        passCard.setCaption(graded == 0 ? "" : Format.percent(passed * 100.0 / graded) + " of graded");
        publishedCard.setValue(String.valueOf(publishDAO.countPublished(course.getCourseId())));
        publishedCard.setCaption("rows in grades table");

        int maxCount = 0;
        List<Double> values = new ArrayList<>();
        for (int v : dist.values()) {
            values.add((double) v);
            maxCount = Math.max(maxCount, v);
        }
        BarChart chart = new BarChart();
        chart.setAccent(accent);
        chart.setMax(Math.max(4, (int) Math.ceil(maxCount / 4.0) * 4));
        chart.setData(new ArrayList<>(dist.keySet()), graded == 0 ? List.of() : values);
        chart.setEmptyText("No grades yet - enter the final exam marks first");
        chart.setPreferredSize(new Dimension(400, 170));
        CardPanel chartCard = CardPanel.titled("Grade distribution (" + course.getCourseCode() + ")");
        chartCard.add(chart, BorderLayout.CENTER);

        JTable table = UI.table(model);
        int n = caParts.size();
        int[] widths = new int[cols.size()];
        widths[0] = 110;
        widths[1] = 140;
        for (int i = 2; i < 2 + n; i++) widths[i] = 60;
        int[] tail = {65, 70, 70, 50, 45, 105, 150};   // CA, final, total, grade, GP, rule, remarks
        System.arraycopy(tail, 0, widths, 2 + n, tail.length);
        CardPanel tableCard = CardPanel.titled("Marks & grades - whole batch (" + results.size() + " students)");
        tableCard.add(fitOrScroll(table, widths), BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(400, 380));

        body.add(statsWithChart(chartCard, gradedCard, avgCard, passCard, publishedCard), BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------ individual
    @Override
    protected void renderIndividual(Course course, Undergraduate s) throws AppException {
        publishBtn.setEnabled(false);
        List<Course> courses = allCourses.isSelected() ? coursesOf(s) : List.of(course);
        GradePolicy policy = GradePolicy.forStudent(s.getStudentType());

        DataTableModel model = new DataTableModel("Course", "Credits", "Assessment marks", "CA",
                "Final exam", "Total /100", "Grade", "GP", "Eligibility", "Remarks");
        double points = 0;
        int credits = 0;
        for (Course c : courses) {
            CourseResult r = resultService.getResult(s, c);
            StringBuilder marks = new StringBuilder();
            for (Map.Entry<String, Double> e : r.getRawMarks().entrySet()) {
                if (marks.length() > 0) marks.append(",  ");
                marks.append(e.getKey()).append(": ").append(MarkParser.format(e.getValue()));
            }
            model.addRow(r, c.getCourseCode(), c.getCredits(), marks.length() == 0 ? "-" : marks.toString(),
                    Format.num(r.getCaObtained()) + " / " + (int) r.getCaWeight(),
                    r.getFinalExamRaw() == null ? "-" : MarkParser.format(r.getFinalExamRaw()),
                    Format.num(r.getTotalMarks()), r.getGradeLetter(),
                    r.hasGrade() ? Format.gpa(r.getGradePoint()) : "-", r.getEligibilityText(), r.getRemarks());
            if (r.hasGrade()) {
                points += r.getGradePoint() * c.getCredits();
                credits += c.getCredits();
            }
        }
        model.refresh();

        String gpaNote = credits == 0 ? "No graded courses yet."
                : "Weighted grade point of these course(s): <b>" + Format.gpa(points / credits) + "</b> over "
                + credits + " credits.";
        body.add(studentHeader(s, policy.describe() + "<br>" + gpaNote), BorderLayout.NORTH);

        JTable table = UI.table(model);
        CardPanel tableCard = CardPanel.titled(allCourses.isSelected()
                ? "Results in all my courses (" + courses.size() + ")" : "Result in " + course);
        tableCard.add(fitOrScroll(table, 75, 55, 300, 80, 75, 75, 55, 55, 165, 150), BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(400, 300));
        body.add(tableCard, BorderLayout.CENTER);
    }

    /** My courses that this student is enrolled in this year. */
    private List<Course> coursesOf(Undergraduate s) throws AppException {
        List<Course> taken = courseDAO.findByStudent(s.getStudentId());
        List<Course> list = new ArrayList<>();
        for (Course c : myCourses()) {
            if (taken.contains(c)) list.add(c);
        }
        return list;
    }

    // ------------------------------------------------------------ publish
    private void publish() {
        Course course = selectedCourse();
        if (course == null || !isBatchView()) return;
        long withGrade = lastBatch.stream().filter(CourseResult::hasGrade).count();
        if (withGrade == 0) {
            UI.warn(this, "No student of " + course.getCourseCode() + " has a grade yet.\n"
                    + "Enter the final exam marks in Marks Entry first.");
            return;
        }
        if (!UI.confirm(this, "Publish " + withGrade + " grade(s) of " + course.getCourseCode() + " for "
                + lk.ac.ruh.fot.ams.config.AppConfig.CURRENT_ACADEMIC_YEAR
                + "?\nExisting published grades of these students will be replaced.")) {
            return;
        }
        int[] n = {0};
        if (safe(() -> n[0] = publishDAO.publish(lastBatch))) {
            UI.info(this, n[0] + " grade(s) published to the grades table.");
            render();
        }
    }
}

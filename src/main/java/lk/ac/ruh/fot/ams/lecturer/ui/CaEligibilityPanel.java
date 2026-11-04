package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.lecturer.service.MarkParser;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.AssessmentType;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.ProgressRing;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;


public class CaEligibilityPanel extends ResultReportPanel {

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    private final StatCard totalCard;
    private final StatCard eligibleCard;
    private final StatCard caCard;
    private final StatCard attCard;

    public CaEligibilityPanel(User user, Color accent) {
        super(user, accent);
        totalCard = new StatCard("Students", "-", "users", accent);
        eligibleCard = new StatCard("Eligible for final", "-", "check", Theme.SUCCESS);
        caCard = new StatCard("Not eligible - CA", "-", "clipboard", Theme.DANGER);
        attCard = new StatCard("Not eligible - attendance", "-", "calendar", Theme.WARNING);
        setHint("Rule: CA ≥ " + (int) AppConfig.MIN_CA_PERCENT + "% of the CA marks  AND  attendance ≥ "
                + (int) AppConfig.MIN_ATTENDANCE_PERCENT + "% (approved medicals counted)");
    }

    @Override
    public String getTitle() {
        return "CA & Eligibility";
    }

    @Override
    public String getSubtitle() {
        return "Continuous assessment summary and final-exam eligibility - batch or individual";
    }

    /** CA assessments only (everything except the final exam). */
    private List<Assessment> caAssessments(Course course) throws AppException {
        List<Assessment> list = new ArrayList<>();
        for (Assessment a : assessmentDAO.findByCourse(course.getCourseId())) {
            if (a.getType().isContinuousAssessment()) list.add(a);
        }
        return list;
    }

    @Override
    protected void renderBatch(Course course) throws AppException {
        List<Assessment> ca = caAssessments(course);
        List<CourseResult> results = resultService.getBatchResults(course);

        // dynamic columns: one per CA assessment
        List<String> cols = new ArrayList<>(List.of("TG No", "Name", "Type"));
        for (Assessment a : ca) cols.add(a.getName() + " (" + fmtWeight(a.getPercentage()) + "%)");
        cols.addAll(List.of("CA marks", "CA %", "CA ≥ 40%", "Attendance", "Final exam eligibility"));
        DataTableModel model = new DataTableModel(cols.toArray(new String[0]));

        int eligible = 0;
        int notCa = 0;
        int notAtt = 0;
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (CourseResult r : results) {
            List<Object> row = new ArrayList<>(List.of(r.getTgNo(), r.getStudentName(), r.getStudentType().toString()));
            for (Assessment a : ca) {
                Double m = r.getRawMarks().get(a.getName());
                row.add(m == null ? "-" : MarkParser.format(m));
            }
            row.add(Format.num(r.getCaObtained()) + " / " + fmtWeight(r.getCaWeight()));
            row.add(Format.percent(r.getCaPercentage()));
            row.add(r.isCaEligible() ? "Eligible" : "Not eligible");
            row.add(Format.percent(r.getAttendancePercent()));
            row.add(r.getEligibilityText());
            model.addRow(r, row.toArray());
            if (r.isEligible()) eligible++;
            if (!r.isCaEligible()) notCa++;
            if (!r.isAttendanceEligible()) notAtt++;
            labels.add(shortTg(r.getTgNo()));
            values.add(r.getCaPercentage());
        }
        model.refresh();

        totalCard.setValue(String.valueOf(results.size()));
        totalCard.setCaption(course.getCourseCode() + " enrolled");
        eligibleCard.setValue(String.valueOf(eligible));
        eligibleCard.setCaption(results.isEmpty() ? "" : Format.percent(eligible * 100.0 / results.size()) + " of batch");
        caCard.setValue(String.valueOf(notCa));
        caCard.setCaption("CA below " + (int) AppConfig.MIN_CA_PERCENT + "%");
        attCard.setValue(String.valueOf(notAtt));
        attCard.setCaption("attendance below " + (int) AppConfig.MIN_ATTENDANCE_PERCENT + "%");

        BarChart chart = new BarChart();
        chart.setAccent(accent);
        chart.setThreshold(AppConfig.MIN_CA_PERCENT, "40%");
        chart.setData(labels, values);
        chart.setPreferredSize(new Dimension(400, 170));
        CardPanel chartCard = CardPanel.titled("CA % of each student (" + course.getCourseCode() + ")");
        chartCard.add(chart, BorderLayout.CENTER);

        JTable table = UI.table(model);
        int[] widths = new int[cols.size()];
        widths[0] = 110;
        widths[1] = 135;
        widths[2] = 80;
        for (int i = 3; i < 3 + ca.size(); i++) widths[i] = 58;
        widths[3 + ca.size()] = 80;     // CA marks
        widths[4 + ca.size()] = 58;     // CA %
        widths[5 + ca.size()] = 80;     // CA eligible
        widths[6 + ca.size()] = 75;     // attendance
        widths[7 + ca.size()] = 165;    // overall
        CardPanel tableCard = CardPanel.titled("CA summary - whole batch (" + results.size() + " students)");
        tableCard.add(fitOrScroll(table, widths), BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(400, 380));

        body.add(statsWithChart(chartCard, totalCard, eligibleCard, caCard, attCard), BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
    }

    @Override
    protected void renderIndividual(Course course, Undergraduate s) throws AppException {
        List<Assessment> all = assessmentDAO.findByCourse(course.getCourseId());
        CourseResult r = resultService.getResult(s, course);
        AttendanceSummary att = attendanceService.getStudentSummary(s, course);

        // --- header with two progress rings
        ProgressRing caRing = new ProgressRing(120);
        caRing.setValue(r.getCaPercentage(), 100, Format.percent(r.getCaPercentage()), "CA",
                r.isCaEligible() ? Theme.SUCCESS : Theme.DANGER);
        double attPct = r.getAttendancePercent();
        ProgressRing attRing = new ProgressRing(120);
        attRing.setValue(Double.isNaN(attPct) ? 0 : attPct, 100, Format.percent(attPct), "Attendance",
                r.isAttendanceEligible() ? Theme.SUCCESS : Theme.DANGER);
        String verdict = "<b>" + r.getEligibilityText() + "</b> to sit the final exam of " + course.getCourseCode()
                + ".  CA " + Format.num(r.getCaObtained()) + " / " + fmtWeight(r.getCaWeight())
                + " marks (" + Format.percent(r.getCaPercentage()) + ", needs " + (int) AppConfig.MIN_CA_PERCENT + "%).";
        body.add(studentHeader(s, verdict, caRing, attRing), BorderLayout.NORTH);

        // --- CA breakdown
        DataTableModel model = new DataTableModel("Assessment", "Type", "Weight", "Mark /100", "Points", "Part");
        for (Assessment a : all) {
            Double m = r.getRawMarks().get(a.getName());
            boolean isCa = a.getType() != AssessmentType.FINAL;
            model.addRow(a, a.getName(), a.getType().toString(), fmtWeight(a.getPercentage()),
                    m == null ? "Not entered" : MarkParser.format(m),
                    m == null ? "-" : Format.num(a.contribution(m)),
                    isCa ? "CA" : "Final");
        }
        model.addRow(null, "CA total", "", fmtWeight(r.getCaWeight()), Format.percent(r.getCaPercentage()),
                Format.num(r.getCaObtained()), "CA");
        model.refresh();
        JTable table = UI.table(model);
        UI.setColumnWidths(table, 170, 95, 60, 80, 60, 50);
        CardPanel breakdown = CardPanel.titled("CA marks breakdown");
        breakdown.add(UI.scroll(table), BorderLayout.CENTER);

        // --- eligibility checklist
        DataTableModel check = new DataTableModel("Check", "Required", "Student", "Result");
        check.addRow(null, "Continuous assessment", "≥ " + (int) AppConfig.MIN_CA_PERCENT + "%",
                Format.percent(r.getCaPercentage()), eligibleText(r.isCaEligible()));
        check.addRow(null, "Attendance + medical", "≥ " + (int) AppConfig.MIN_ATTENDANCE_PERCENT + "%",
                Format.percent(attPct), eligibleText(r.isAttendanceEligible()));
        check.addRow(null, "Attendance only", "-", Format.percent(att.getCombinedPercentage(false)),
                att.getCategory());
        check.addRow(null, "Medical (appr. / pend.)", "-",
                att.getApprovedMedicalCount() + " / " + att.getPendingMedicalCount(), "");
        check.addRow(null, "Overall", "both", "", r.getEligibilityText());
        check.refresh();
        JTable checkTable = UI.table(check);
        UI.setColumnWidths(checkTable, 165, 70, 70, 135);
        CardPanel checkCard = CardPanel.titled("Eligibility check");
        checkCard.add(UI.scroll(checkTable), BorderLayout.CENTER);

        JPanel grid = new JPanel(new BorderLayout(14, 0));
        grid.setOpaque(false);
        grid.add(breakdown, BorderLayout.CENTER);
        checkCard.setPreferredSize(new Dimension(500, 300));
        grid.add(checkCard, BorderLayout.EAST);
        grid.setPreferredSize(new Dimension(400, 300));
        body.add(grid, BorderLayout.CENTER);
    }

    private static String fmtWeight(double w) {
        return w == Math.rint(w) ? String.valueOf((int) w) : Format.num(w);
    }
}

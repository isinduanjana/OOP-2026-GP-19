package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.report.AttendanceRecord;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.ComponentAttendance;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] "My Attendance" - the student's own attendance:
 *  - summary per course (theory %, practical %, combined %, with medical %, eligibility)
 *  - bar chart with the 80% line (view: Combined / Theory / Practical)
 *  - day-by-day list of the selected course (Present / Absent / Absent (Medical - APPROVED))
 *
 * All numbers come from the COMMON AttendanceService, so they are exactly the
 * same as the lecturer / TO reports.
 *
 * OOP: Inheritance (extends StudentViewPanel), Polymorphism (own
 * loadStudentData()), Encapsulation (private models, combo boxes).
 */
public class StudentAttendancePanel extends StudentViewPanel {

    private static final String COMBINED = "Combined";
    private static final String THEORY = "Theory";
    private static final String PRACTICAL = "Practical";

    private final AttendanceService attendanceService = new AttendanceService();

    private final StatCard averageCard;
    private final StatCard eligibleCard;
    private final StatCard medicalCard;
    private final StatCard sessionsCard;

    private final JComboBox<String> viewBox = new JComboBox<>(new String[]{COMBINED, THEORY, PRACTICAL});
    private final BarChart chart = new BarChart();
    private final DataTableModel summaryModel = new DataTableModel("Course", "Course name", "Theory %",
            "Practical %", "Combined %", "With medical %", "Medicals", "Status");
    private final JTable summaryTable = UI.table(summaryModel);

    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final JComboBox<String> recordFilter = new JComboBox<>(new String[]{"All sessions", THEORY, PRACTICAL});
    private final DataTableModel recordModel = new DataTableModel("Date", "Component", "Session", "Status");
    private final JLabel recordInfo = UI.muted("");

    private List<AttendanceSummary> summaries = new ArrayList<>();
    private boolean loading;

    public StudentAttendancePanel(User user, Color accent) {
        super(user, accent);
        averageCard = new StatCard("Average attendance", "-", "chart", accent);
        eligibleCard = new StatCard("Eligible courses", "-", "check", Theme.SUCCESS);
        medicalCard = new StatCard("Medical sessions", "-", "medical", Theme.WARNING);
        sessionsCard = new StatCard("Sessions attended", "-", "calendar", Theme.INFO);
        setMinimumPageHeight(900);      // scroll instead of squeezing chart + day-by-day list
        buildUI();
    }

    @Override
    public String getTitle() {
        return "My Attendance";
    }

    @Override
    public String getSubtitle() {
        return "At least " + (int) AppConfig.MIN_ATTENDANCE_PERCENT
                + "% attendance (approved medicals counted) is needed to sit the final exam";
    }

    private void buildUI() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(averageCard);
        stats.add(eligibleCard);
        stats.add(medicalCard);
        stats.add(sessionsCard);

        // ------- chart card
        CardPanel chartCard = new CardPanel(new BorderLayout(0, 10));
        viewBox.setPreferredSize(new Dimension(150, 32));
        viewBox.setToolTipText("Which component to show in the chart");
        chartCard.add(UI.toolbar(UI.heading("By course"), UI.rowRight(viewBox)), BorderLayout.NORTH);
        chart.setAccent(accent);
        chart.setThreshold(AppConfig.MIN_ATTENDANCE_PERCENT, "80% required");
        chart.setPreferredSize(new Dimension(380, 230));
        chartCard.add(chart, BorderLayout.CENTER);

        // ------- day by day card
        CardPanel recordCard = new CardPanel(new BorderLayout(0, 10));
        courseBox.setPreferredSize(new Dimension(250, 32));
        recordFilter.setPreferredSize(new Dimension(120, 32));
        recordCard.add(UI.toolbar(UI.heading("Day-by-day"), UI.rowRight(courseBox, recordFilter)), BorderLayout.NORTH);
        JTable recordTable = UI.table(recordModel);
        UI.setColumnWidths(recordTable, 90, 80, 60, 210);
        // M4 renderer subclass: approved-medical absences are not shown as plain red "Absent"
        recordTable.getColumnModel().getColumn(3).setCellRenderer(new AttendanceStatusRenderer());
        JScrollPane rs = UI.scroll(recordTable);
        rs.setPreferredSize(new Dimension(380, 230));
        recordCard.add(rs, BorderLayout.CENTER);
        recordCard.add(recordInfo, BorderLayout.SOUTH);

        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 0, 14);
        middle.add(chartCard, c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        middle.add(recordCard, c);

        // ------- summary table
        CardPanel summaryCard = CardPanel.titled("Summary per course");
        UI.setColumnWidths(summaryTable, 80, 220, 80, 85, 90, 105, 120, 110);
        summaryTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || loading) return;
            AttendanceSummary s = summaryModel.getItem(UI.selectedModelRow(summaryTable));
            if (s != null) courseBox.setSelectedItem(s.getCourse());
        });
        JScrollPane ss = UI.scroll(summaryTable);
        ss.setPreferredSize(new Dimension(400, 36 * 6 + 42));
        summaryCard.add(ss, BorderLayout.CENTER);
        summaryCard.add(UI.muted("Combined % is weighted by hours (1 session = " + AppConfig.HOURS_PER_SESSION
                + " hours). Pending / rejected medicals are not counted."), BorderLayout.SOUTH);

        JPanel north = StudentUI.clear(0, 14);
        north.add(stats, BorderLayout.NORTH);
        north.add(summaryCard, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);

        viewBox.addActionListener(e -> renderChart());
        courseBox.addActionListener(e -> { if (!loading) loadRecords(); });
        recordFilter.addActionListener(e -> { if (!loading) loadRecords(); });
    }

    @Override
    protected void loadStudentData() throws AppException {
        loading = true;
        try {
            Course selected = (Course) courseBox.getSelectedItem();
            summaries = attendanceService.getStudentSummaries(getStudent());
            summaryModel.clear();
            courseBox.removeAllItems();
            int eligible = 0;
            int medical = 0;
            int present = 0;
            int held = 0;
            double sum = 0;
            int counted = 0;
            for (AttendanceSummary s : summaries) {
                Course c = s.getCourse();
                summaryModel.addRow(s, c.getCourseCode(), c.getCourseName(), pct(s.getTheory(), false),
                        pct(s.getPractical(), false), Format.percent(s.getCombinedPercentage(false)),
                        Format.percent(s.getCombinedPercentage(true)), s.getApprovedMedicalCount(),
                        s.isEligible() ? "Eligible" : "Not eligible");
                courseBox.addItem(c);
                if (s.isEligible()) eligible++;
                medical += s.getApprovedMedicalCount();
                for (ComponentAttendance ca : new ComponentAttendance[]{s.getTheory(), s.getPractical()}) {
                    if (ca != null) {
                        present += ca.getPresent();
                        held += ca.getSessionsHeld();
                    }
                }
                if (StudentSnapshot.sessionsHeld(s) > 0) {
                    sum += s.getCombinedPercentage(true);
                    counted++;
                }
            }
            summaryModel.refresh();
            double avg = counted == 0 ? Double.NaN : sum / counted;
            averageCard.setValue(Format.percent(avg));
            averageCard.setCaption("with approved medicals");
            eligibleCard.setValue(eligible + " / " + summaries.size());
            eligibleCard.setCaption(eligible == summaries.size() ? "all courses ≥ 80%" : "courses ≥ 80%");
            medicalCard.setValue(String.valueOf(medical));
            medicalCard.setCaption("absences with approved medical");
            sessionsCard.setValue(present + " / " + held);
            sessionsCard.setCaption("theory + practical sessions");
            if (selected != null) courseBox.setSelectedItem(selected);
        } finally {
            loading = false;
        }
        renderChart();
        loadRecords();
    }

    private Component selectedComponent(String v) {
        return THEORY.equals(v) ? Component.THEORY : PRACTICAL.equals(v) ? Component.PRACTICAL : null;
    }

    /** Bar per course for the chosen component (approved medicals counted). */
    private void renderChart() {
        Component comp = selectedComponent((String) viewBox.getSelectedItem());
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (AttendanceSummary s : summaries) {
            double v = s.getPercentage(comp, true);
            if (Double.isNaN(v)) continue;      // course has no such component
            labels.add(s.getCourse().getCourseCode());
            values.add(v);
        }
        chart.setEmptyText(comp == null ? "No attendance recorded yet" : "None of your courses has a " + comp + " part");
        chart.setData(labels, values);
    }

    /** Day-by-day rows of the selected course (common AttendanceService.getRecords). */
    private void loadRecords() {
        Course c = (Course) courseBox.getSelectedItem();
        recordModel.clear();
        if (c == null) {
            recordInfo.setText("");
            return;
        }
        Component comp = selectedComponent((String) recordFilter.getSelectedItem());
        safe(() -> {
            int present = 0;
            int shown = 0;
            for (AttendanceRecord r : attendanceService.getRecords(getStudentId(), c.getCourseId())) {
                if (comp != null && r.getComponent() != comp) continue;
                recordModel.addRow(r, Format.date(r.getSessionDate()),
                        r.getComponent() == Component.THEORY ? THEORY : PRACTICAL,
                        "#" + r.getSessionNumber(), r.getDisplayStatus());
                shown++;
                if (r.isPresent()) present++;
            }
            recordModel.refresh();
            recordInfo.setText(shown == 0 ? "No sessions recorded yet for " + c.getCourseCode()
                    : "Present in " + present + " of " + shown + " session(s) of " + c.getCourseCode());
        });
    }

    private static String pct(ComponentAttendance c, boolean withMedical) {
        if (c == null) return "-";
        if (c.getSessionsHeld() == 0) return "No sessions";
        return Format.percent(withMedical ? c.getPercentageWithMedical() : c.getPercentage());
    }
}

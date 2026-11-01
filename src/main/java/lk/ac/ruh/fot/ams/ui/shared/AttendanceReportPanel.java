package lk.ac.ruh.fot.ams.ui.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.report.AttendanceRecord;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.ComponentAttendance;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.ui.BasePanel;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.BarChart;
import lk.ac.ruh.fot.ams.ui.components.CardPanel;
import lk.ac.ruh.fot.ams.ui.components.DataTableModel;
import lk.ac.ruh.fot.ams.ui.components.StatCard;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [COMMON PART] Attendance report used by the Lecturer (M2) and the
 * Technical Officer (M3). Covers the handout requirements:
 *   - summary for the WHOLE BATCH and for INDIVIDUALS
 *   - THEORY only, PRACTICAL only, or COMBINED
 *   - with / without approved medicals, 80% eligibility, 5 scenarios
 *
 *   new AttendanceReportPanel(user, accent, () -> courseDAO.findByLecturer(id));
 */
public class AttendanceReportPanel extends BasePanel {

    /** Supplies the courses the user may look at. */
    @FunctionalInterface
    public interface CourseLoader {
        List<Course> load() throws AppException;
    }

    private static final String COMBINED = "Combined (Theory + Practical)";
    private static final String THEORY = "Theory only";
    private static final String PRACTICAL = "Practical only";

    private final CourseLoader courseLoader;
    private final AttendanceService attendanceService = new AttendanceService();
    private final StudentDAO studentDAO = new StudentDAO();

    private final JComboBox<Course> courseBox = new JComboBox<>();
    private final JComboBox<String> viewBox = new JComboBox<>(new String[]{COMBINED, THEORY, PRACTICAL});
    private final JCheckBox withMedical = new JCheckBox("Count approved medicals", true);
    private final JRadioButton batchRadio = new JRadioButton("Whole batch", true);
    private final JRadioButton individualRadio = new JRadioButton("Individual");
    private final JComboBox<Undergraduate> studentBox = new JComboBox<>();

    private final StatCard totalCard;
    private final StatCard eligibleCard;
    private final StatCard notEligibleCard;
    private final StatCard averageCard;
    private final BarChart chart = new BarChart();
    private final DataTableModel batchModel = new DataTableModel("TG No", "Name", "Type", "Theory %",
            "Practical %", "Combined %", "Selected view %", "Medicals (appr/pend)", "Scenario", "Eligibility");
    private final DataTableModel recordModel = new DataTableModel("Component", "Session", "Date", "Status");
    private final JTable batchTable = UI.table(batchModel);
    private final JTable recordTable = UI.table(recordModel);
    private final CardPanel tableCard = new CardPanel();
    private boolean loading;

    public AttendanceReportPanel(User user, Color accent, CourseLoader courseLoader) {
        super(user, accent);
        this.courseLoader = courseLoader;
        totalCard = new StatCard("Students", "-", "users", accent);
        eligibleCard = new StatCard("Eligible (≥80%)", "-", "check", Theme.SUCCESS);
        notEligibleCard = new StatCard("Not eligible", "-", "medical", Theme.DANGER);
        averageCard = new StatCard("Average", "-", "chart", Theme.INFO);

        ButtonGroup g = new ButtonGroup();
        g.add(batchRadio);
        g.add(individualRadio);
        for (JRadioButton r : new JRadioButton[]{batchRadio, individualRadio}) r.setOpaque(false);
        withMedical.setOpaque(false);
        courseBox.setPreferredSize(new Dimension(300, 34));
        studentBox.setPreferredSize(new Dimension(260, 34));
        studentBox.setEnabled(false);

        CardPanel filters = new CardPanel(new GridLayout(2, 1, 0, 10));
        filters.add(UI.row(UI.label("Course"), courseBox, UI.label("View"), viewBox, withMedical));
        filters.add(UI.row(UI.label("Show"), batchRadio, individualRadio, studentBox));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(totalCard);
        stats.add(eligibleCard);
        stats.add(notEligibleCard);
        stats.add(averageCard);

        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(filters, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        chart.setAccent(accent);
        chart.setThreshold(80, "80% required");
        chart.setPreferredSize(new Dimension(400, 220));
        CardPanel chartCard = CardPanel.titled("Attendance chart");
        chartCard.add(chart, BorderLayout.CENTER);

        UI.setColumnWidths(batchTable, 110, 170, 90, 75, 85, 90, 105, 125, 150, 170);
        tableCard.add(UI.scroll(batchTable), BorderLayout.CENTER);
        tableCard.setPreferredSize(new Dimension(400, 320));

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(chartCard, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        courseBox.addActionListener(e -> { if (!loading) loadStudents(); });
        viewBox.addActionListener(e -> { if (!loading) render(); });
        withMedical.addActionListener(e -> render());
        batchRadio.addActionListener(e -> { studentBox.setEnabled(false); render(); });
        individualRadio.addActionListener(e -> { studentBox.setEnabled(true); render(); });
        studentBox.addActionListener(e -> { if (!loading) render(); });
    }

    @Override
    public String getTitle() {
        return "Attendance Report";
    }

    @Override
    public String getSubtitle() {
        return "Theory / practical / combined attendance with medicals - batch or individual";
    }

    @Override
    public void refresh() {
        safe(() -> {
            loading = true;
            Object selected = courseBox.getSelectedItem();
            courseBox.removeAllItems();
            for (Course c : courseLoader.load()) courseBox.addItem(c);
            if (selected != null) courseBox.setSelectedItem(selected);
            loading = false;
            loadStudents();
        });
    }

    private void loadStudents() {
        Course c = (Course) courseBox.getSelectedItem();
        loading = true;
        studentBox.removeAllItems();
        if (c != null) {
            safe(() -> {
                for (Undergraduate s : studentDAO.findByCourse(c.getCourseId())) studentBox.addItem(s);
            });
        }
        loading = false;
        render();
    }

    private Component selectedComponent() {
        String v = (String) viewBox.getSelectedItem();
        return THEORY.equals(v) ? Component.THEORY : PRACTICAL.equals(v) ? Component.PRACTICAL : null;
    }

    private void render() {
        Course course = (Course) courseBox.getSelectedItem();
        if (course == null) return;
        safe(() -> {
            if (individualRadio.isSelected()) {
                renderIndividual(course);
            } else {
                renderBatch(course);
            }
        });
    }

    private void renderBatch(Course course) throws AppException {
        tableCard.removeAll();
        tableCard.add(UI.scroll(batchTable), BorderLayout.CENTER);
        tableCard.revalidate();
        tableCard.repaint();

        Component comp = selectedComponent();
        boolean med = withMedical.isSelected();
        List<AttendanceSummary> list = attendanceService.getBatchSummary(course);
        batchModel.clear();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        int eligible = 0;
        double sum = 0;
        int counted = 0;
        for (AttendanceSummary s : list) {
            double sel = s.getPercentage(comp, med);
            batchModel.addRow(s, s.getTgNo(), s.getStudentName(), s.getStudentType().toString(),
                    pct(s.getTheory(), med), pct(s.getPractical(), med),
                    Format.percent(s.getCombinedPercentage(med)), Format.percent(sel),
                    s.getApprovedMedicalCount() + " / " + s.getPendingMedicalCount(),
                    s.getCategory(), s.isEligible() ? "Eligible" : "Not eligible");
            String tg = s.getTgNo();
            labels.add(tg.substring(Math.max(0, tg.length() - 4)));
            values.add(Double.isNaN(sel) ? 0 : sel);
            if (s.isEligible()) eligible++;
            if (!Double.isNaN(sel)) {
                sum += sel;
                counted++;
            }
        }
        batchModel.refresh();
        chart.setData(labels, values);
        chart.setEmptyText(comp == null ? "No attendance recorded" : "This course has no " + comp + " component");
        totalCard.setValue(String.valueOf(list.size()));
        totalCard.setCaption(course.getCourseCode());
        eligibleCard.setValue(String.valueOf(eligible));
        eligibleCard.setCaption("combined, with medical");
        notEligibleCard.setValue(String.valueOf(list.size() - eligible));
        notEligibleCard.setCaption("below 80%");
        averageCard.setValue(counted == 0 ? "-" : Format.percent(sum / counted));
        averageCard.setCaption(viewBox.getSelectedItem().toString());
    }

    private void renderIndividual(Course course) throws AppException {
        Undergraduate s = (Undergraduate) studentBox.getSelectedItem();
        tableCard.removeAll();
        tableCard.add(UI.scroll(recordTable), BorderLayout.CENTER);
        tableCard.revalidate();
        tableCard.repaint();
        recordModel.clear();
        if (s == null) {
            chart.setData(List.of(), List.of());
            return;
        }
        boolean med = withMedical.isSelected();
        AttendanceSummary sum = attendanceService.getStudentSummary(s, course);
        Component comp = selectedComponent();
        for (AttendanceRecord r : attendanceService.getRecords(s.getStudentId(), course.getCourseId())) {
            if (comp != null && r.getComponent() != comp) continue;
            recordModel.addRow(r, r.getComponent().toString(), "#" + r.getSessionNumber(),
                    Format.date(r.getSessionDate()), r.getDisplayStatus());
        }
        recordModel.refresh();

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        if (sum.getTheory() != null) {
            labels.add("Theory");
            values.add(sum.getPercentage(Component.THEORY, med));
        }
        if (sum.getPractical() != null) {
            labels.add("Practical");
            values.add(sum.getPercentage(Component.PRACTICAL, med));
        }
        labels.add("Combined");
        values.add(sum.getCombinedPercentage(med));
        chart.setData(labels, values);

        totalCard.setValue(s.getTgNo().substring(Math.max(0, s.getTgNo().length() - 4)));
        totalCard.setCaption(s.getFullName());
        eligibleCard.setValue(sum.isEligible() ? "Yes" : "No");
        eligibleCard.setCaption(sum.getCategory());
        notEligibleCard.setValue(sum.getApprovedMedicalCount() + "");
        notEligibleCard.setCaption("approved medical session(s)");
        averageCard.setValue(Format.percent(sum.getPercentage(comp, med)));
        averageCard.setCaption(viewBox.getSelectedItem().toString());
    }

    private static String pct(ComponentAttendance c, boolean med) {
        if (c == null) return "-";
        return Format.percent(med ? c.getPercentageWithMedical() : c.getPercentage());
    }
}

package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import lk.ac.ruh.fot.ams.admin.dao.AdminCourseDAO;
import lk.ac.ruh.fot.ams.admin.dao.AdminStatsDAO;
import lk.ac.ruh.fot.ams.admin.dao.AdminUserDAO;
import lk.ac.ruh.fot.ams.dao.DepartmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] "Course Management" page: create and maintain course units,
 * assign lecturers, define assessments (evaluation criteria) and manage enrolments.
 *
 * OOP: Inheritance + Polymorphism - a CrudPanel&lt;Course&gt; subclass that overrides the
 *      abstract methods; extra buttons open AssessmentDialog / EnrollmentDialog.
 */
public class CourseManagementPanel extends CrudPanel<Course> {

    private static final String[] LEVEL_SEM = {"All levels", "L1 S1", "L1 S2", "L2 S1", "L2 S2", "L3 S1", "L3 S2",
            "L4 S1", "L4 S2"};

    private final AdminCourseDAO dao = new AdminCourseDAO();
    private final AdminUserDAO userDAO = new AdminUserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final JComboBox<String> levelFilter = new JComboBox<>(LEVEL_SEM);
    private final JComboBox<Department> deptFilter = new JComboBox<>();
    private final AdminStatsDAO statsDAO = new AdminStatsDAO();
    private List<Department> departments = new ArrayList<>();
    private Map<Integer, Integer> studentCounts = new HashMap<>();   // course_id -> enrolled this year

    public CourseManagementPanel(User user, Color accent) {
        super(user, accent, "course");
        levelFilter.setPreferredSize(new Dimension(120, 36));
        deptFilter.setPreferredSize(new Dimension(200, 36));
        deptFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v == null ? "All departments" : ((Department) v).getCode() + " - " + ((Department) v).getName());
                return this;
            }
        });
        levelFilter.addActionListener(e -> applyFilter());
        deptFilter.addActionListener(e -> applyFilter());
        addFilter(levelFilter);
        addFilter(deptFilter);

        ModernButton assessments = UI.secondary("Assessments", "clipboard");
        assessments.setToolTipText("Define the evaluation criteria (quizzes, assignments, mid, final and their weights)");
        assessments.addActionListener(e -> {
            Course c = getSelected();
            if (c != null) new AssessmentDialog(this, accent, c).open();
        });
        ModernButton enrollments = UI.secondary("Enrollments", "users");
        enrollments.addActionListener(e -> {
            Course c = getSelected();
            if (c != null) {
                new EnrollmentDialog(this, accent, c).open();
                refresh();
            }
        });
        addAction(assessments, true);
        addAction(enrollments, true);
    }

    @Override
    public String getTitle() {
        return "Course Management";
    }

    @Override
    public String getSubtitle() {
        return "Course units, lecturers, evaluation criteria and enrolments";
    }

    @Override
    protected String[] columns() {
        return new String[]{"Code", "Course name", "Credits", "Theory", "Practical", "L / S", "Dept.",
                "Enrolled", "Lecturers"};
    }

    @Override
    protected int[] columnWidths() {
        return new int[]{80, 280, 60, 60, 70, 55, 60, 65, 300};
    }

    @Override
    protected void beforeLoad() throws AppException {
        departments = departmentDAO.findAll();
        Department keep = (Department) deptFilter.getSelectedItem();
        if (deptFilter.getItemCount() != departments.size() + 1) {
            deptFilter.removeAllItems();
            deptFilter.addItem(null);
            for (Department d : departments) deptFilter.addItem(d);
            deptFilter.setSelectedItem(keep);
        }
    }

    @Override
    protected List<Course> loadItems() throws AppException {
        studentCounts = statsDAO.enrolledPerCourse();
        return dao.findAll();
    }

    @Override
    protected boolean accept(Course c) {
        int ls = levelFilter.getSelectedIndex();
        if (ls > 0) {
            int level = (ls + 1) / 2;
            int sem = ls % 2 == 1 ? 1 : 2;
            if (c.getLevel() != level || c.getSemester() != sem) return false;
        }
        Department d = (Department) deptFilter.getSelectedItem();
        return d == null || (c.getDepartmentId() != null && c.getDepartmentId() == d.getDepartmentId());
    }

    @Override
    protected Object[] toRow(Course c) {
        return new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredits(),
                c.getTheoryHours() == 0 ? "-" : c.getTheoryHours() + " h",
                c.getPracticalHours() == 0 ? "-" : c.getPracticalHours() + " h",
                c.getLevelSemesterLabel(), AdminUI.deptCode(departments, c.getDepartmentId()),
                studentCounts.getOrDefault(c.getCourseId(), 0),
                Format.nullToDash(c.getLecturerNames())};
    }

    @Override
    protected boolean openEditor(Course c) throws AppException {
        List<Lecturer> lecturers = new ArrayList<>();
        for (User u : userDAO.findByRole(Role.LECTURER)) lecturers.add((Lecturer) u);
        Course fresh = c == null ? null : dao.findById(c.getCourseId());
        return new CourseDialog(this, accent, dao, departments, lecturers, fresh).showDialog();
    }

    @Override
    protected String deleteWarning(Course c) throws AppException {
        int[] n = dao.countDependents(c.getCourseId());
        if (n[0] + n[1] + n[2] + n[3] == 0) return "";
        return String.format("%n%nThis will ALSO delete: %d enrolment(s), %d attendance session(s), "
                + "%d mark(s) and %d timetable slot(s).", n[0], n[1], n[2], n[3]);
    }

    @Override
    protected void deleteItem(Course c) throws AppException {
        dao.delete(c.getCourseId());
    }

    @Override
    protected String describe(Course c) {
        return c.getCourseCode() + " - " + c.getCourseName();
    }

    @Override
    protected int idOf(Course c) {
        return c.getCourseId();
    }
}

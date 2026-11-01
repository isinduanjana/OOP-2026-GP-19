package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import lk.ac.ruh.fot.ams.admin.dao.AdminCourseDAO;
import lk.ac.ruh.fot.ams.admin.dao.AdminTimetableDAO;
import lk.ac.ruh.fot.ams.dao.DepartmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] "Timetable Management" page: weekly timetable slots per department.
 * "Week view" opens the common TimetablePanel for the chosen department.
 *
 * OOP: Inheritance + Polymorphism - CrudPanel&lt;TimetableEntry&gt; subclass; the week view
 *      passes a lambda (TimetablePanel.Loader) that loads only this department.
 */
public class TimetableManagementPanel extends CrudPanel<TimetableEntry> {

    private final AdminTimetableDAO dao = new AdminTimetableDAO();
    private final AdminCourseDAO courseDAO = new AdminCourseDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final JComboBox<Department> deptFilter = new JComboBox<>();
    private final JComboBox<String> dayFilter = new JComboBox<>();
    private List<Department> departments = new ArrayList<>();

    public TimetableManagementPanel(User user, Color accent) {
        super(user, accent, "slot");
        deptFilter.setPreferredSize(new Dimension(250, 36));
        dayFilter.setPreferredSize(new Dimension(130, 36));
        dayFilter.addItem("All days");
        for (String d : TimetableEntry.DAYS) dayFilter.addItem(AdminUI.capital(d));
        deptFilter.addActionListener(e -> applyFilter());
        dayFilter.addActionListener(e -> applyFilter());
        addFilter(deptFilter);
        addFilter(dayFilter);
        ModernButton week = UI.secondary("Week view", "calendar");
        week.addActionListener(e -> openWeekView());
        addAction(week, false);
        addButton.setText("Add slot");
    }

    @Override
    public String getTitle() {
        return "Timetable Management";
    }

    @Override
    public String getSubtitle() {
        return "Weekly lecture and practical slots of each department";
    }

    @Override
    protected String[] columns() {
        return new String[]{"Day", "Start", "End", "Course", "Course name", "Component", "Room", "Department"};
    }

    @Override
    protected int[] columnWidths() {
        return new int[]{100, 60, 60, 90, 260, 90, 80, 220};
    }

    @Override
    protected void beforeLoad() throws AppException {
        if (deptFilter.getItemCount() == 0) {
            departments = departmentDAO.findAll();
            JComboBox<Department> tmp = AdminUI.departmentCombo(departments, "All departments");
            deptFilter.setRenderer(tmp.getRenderer());
            for (int i = 0; i < tmp.getItemCount(); i++) deptFilter.addItem(tmp.getItemAt(i));
        }
    }

    @Override
    protected List<TimetableEntry> loadItems() throws AppException {
        return dao.findAll();
    }

    @Override
    protected boolean accept(TimetableEntry e) {
        Department d = (Department) deptFilter.getSelectedItem();
        if (d != null && e.getDepartmentId() != d.getDepartmentId()) return false;
        int day = dayFilter.getSelectedIndex();
        return day <= 0 || e.getDayIndex() == day - 1;
    }

    @Override
    protected Object[] toRow(TimetableEntry e) {
        return new Object[]{AdminUI.capital(e.getDay()), Format.time(e.getStartTime()), Format.time(e.getEndTime()),
                e.getCourseCode(), e.getCourseName(), e.getComponent().toString(), e.getRoom(), e.getDepartmentName()};
    }

    @Override
    protected boolean openEditor(TimetableEntry e) throws AppException {
        if (departments.isEmpty()) departments = departmentDAO.findAll();
        TimetableEntry fresh = e == null ? null : dao.findById(e.getTimetableId());
        return new TimetableDialog(this, accent, dao, departments, courseDAO.findAll(), fresh,
                AdminUI.selectedDepartmentId(deptFilter)).showDialog();
    }

    @Override
    protected void deleteItem(TimetableEntry e) throws AppException {
        dao.delete(e.getTimetableId());
    }

    @Override
    protected String describe(TimetableEntry e) {
        return e.getCourseCode() + " " + e.getComponent() + ", " + AdminUI.capital(e.getDay()) + " "
                + Format.time(e.getStartTime()) + "-" + Format.time(e.getEndTime());
    }

    @Override
    protected int idOf(TimetableEntry e) {
        return e.getTimetableId();
    }

    /** Opens the common week grid for the selected department (or all). */
    private void openWeekView() {
        Department d = (Department) deptFilter.getSelectedItem();
        String title = d == null ? "All departments" : d.getName();
        new WeekViewDialog(this, accent, user, title,
                () -> d == null ? dao.findAll() : dao.findByDepartment(d.getDepartmentId())).open();
    }
}

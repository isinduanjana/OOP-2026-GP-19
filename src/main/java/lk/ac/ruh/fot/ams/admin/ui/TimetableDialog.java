package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.GridLayout;
import java.time.LocalTime;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import lk.ac.ruh.fot.ams.admin.dao.AdminTimetableDAO;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] Add / Edit one weekly timetable slot.
 * Start/end are chosen from half-hour slots (08:00 - 18:00). Saving runs the rules in
 * AdminTimetableDAO.validate(): end after start, the course must have that component, and
 * no clash in the same room / for the same course (TimetableClashException).
 *
 * OOP: Inheritance (AdminFormDialog), Polymorphism (save()), Exception handling.
 */
public class TimetableDialog extends AdminFormDialog {

    private final AdminTimetableDAO dao;
    private final TimetableEntry entry;
    private final List<Course> allCourses;
    private final JComboBox<Department> deptCombo;
    private final JComboBox<Course> courseCombo = new JComboBox<>();
    private final JCheckBox allCoursesBox = new JCheckBox("Show courses of every department / semester");
    private final JComboBox<String> dayCombo = new JComboBox<>();
    private final JComboBox<LocalTime> startCombo = new JComboBox<>();
    private final JComboBox<LocalTime> endCombo = new JComboBox<>();
    private final JComboBox<String> roomCombo = new JComboBox<>();
    private final JComboBox<Component> componentCombo = new JComboBox<>(Component.values());
    private final JLabel info = UI.muted(" ");

    public TimetableDialog(java.awt.Component parent, Color accent, AdminTimetableDAO dao, List<Department> departments,
                           List<Course> courses, TimetableEntry existing, Integer defaultDepartmentId) throws AppException {
        super(parent, existing == null ? "Add timetable slot" : "Edit timetable slot",
                "One weekly class of a course (1 session = " + AppConfig.HOURS_PER_SESSION + " hours)", "calendar", accent);
        this.dao = dao;
        this.entry = existing == null ? new TimetableEntry() : existing;
        this.allCourses = courses;
        deptCombo = AdminUI.departmentCombo(departments, null);

        for (String d : TimetableEntry.DAYS) dayCombo.addItem(d);
        dayCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(AdminUI.capital((String) v));
                return this;
            }
        });
        DefaultListCellRenderer timeRenderer = new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v == null ? "" : Format.time((LocalTime) v));
                return this;
            }
        };
        for (LocalTime t : AdminUI.halfHourSlots()) {
            startCombo.addItem(t);
            endCombo.addItem(t);
        }
        startCombo.setRenderer(timeRenderer);
        endCombo.setRenderer(timeRenderer);
        startCombo.setMaximumRowCount(12);
        endCombo.setMaximumRowCount(12);
        roomCombo.setEditable(true);
        for (String r : dao.findRooms()) roomCombo.addItem(r);
        allCoursesBox.setOpaque(false);

        // ---- layout
        JPanel times = new JPanel(new GridLayout(1, 3, 8, 0));
        times.setOpaque(false);
        times.add(startCombo);
        times.add(new JLabel("to", JLabel.CENTER));
        times.add(endCombo);
        JPanel coursePanel = new JPanel(new BorderLayout(0, 4));
        coursePanel.setOpaque(false);
        coursePanel.add(courseCombo, BorderLayout.NORTH);
        coursePanel.add(allCoursesBox, BorderLayout.SOUTH);
        JPanel form = UI.form(new String[]{"Department", "Course", "Component", "Day", "Time", "Room"},
                deptCombo, coursePanel, componentCombo, dayCombo, times, roomCombo);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(form, BorderLayout.CENTER);
        p.add(info, BorderLayout.SOUTH);
        setContent(p);

        // ---- initial values
        if (existing != null) {
            AdminUI.selectDepartment(deptCombo, existing.getDepartmentId());
            dayCombo.setSelectedItem(existing.getDay());
            startCombo.setSelectedItem(existing.getStartTime());
            endCombo.setSelectedItem(existing.getEndTime());
            roomCombo.setSelectedItem(existing.getRoom());
            componentCombo.setSelectedItem(existing.getComponent());
        } else {
            AdminUI.selectDepartment(deptCombo, defaultDepartmentId == null ? firstId(departments) : defaultDepartmentId);
            startCombo.setSelectedItem(LocalTime.of(8, 0));
            endCombo.setSelectedItem(LocalTime.of(8 + AppConfig.HOURS_PER_SESSION, 0));
            roomCombo.setSelectedItem("");
        }
        fillCourses();
        if (existing != null) selectCourse(existing.getCourseId());
        courseChanged();

        // ---- listeners
        deptCombo.addActionListener(e -> fillCourses());
        allCoursesBox.addActionListener(e -> fillCourses());
        courseCombo.addActionListener(e -> courseChanged());
        startCombo.addActionListener(e -> startChanged());
        saveButton.setText(existing == null ? "Add slot" : "Save changes");
    }

    private static Integer firstId(List<Department> list) {
        return list.isEmpty() ? null : list.get(0).getDepartmentId();
    }

    /** Course list: department's courses + interdisciplinary ones of the current semester (or all). */
    private void fillCourses() {
        Course keep = (Course) courseCombo.getSelectedItem();
        Integer deptId = AdminUI.selectedDepartmentId(deptCombo);
        courseCombo.removeAllItems();
        for (Course c : allCourses) {
            boolean ok = allCoursesBox.isSelected()
                    || ((c.getDepartmentId() == null || c.getDepartmentId().equals(deptId) || isInterdisciplinary(c))
                    && c.getLevel() == AppConfig.CURRENT_LEVEL && c.getSemester() == AppConfig.CURRENT_SEMESTER);
            if (ok) courseCombo.addItem(c);
        }
        if (keep != null) courseCombo.setSelectedItem(keep);
        if (entry.getTimetableId() != 0 && courseCombo.getSelectedItem() == null) {
            allCoursesBox.setSelected(true);           // editing an old slot: make sure its course is listed
            fillCourses();
        }
    }

    private static boolean isInterdisciplinary(Course c) {
        return c.getDepartmentName() != null && c.getDepartmentName().toLowerCase().contains("interdisciplinary");
    }

    private void selectCourse(int courseId) {
        for (int i = 0; i < courseCombo.getItemCount(); i++) {
            if (courseCombo.getItemAt(i).getCourseId() == courseId) {
                courseCombo.setSelectedIndex(i);
                return;
            }
        }
        allCoursesBox.setSelected(true);
        fillCourses();
        for (int i = 0; i < courseCombo.getItemCount(); i++) {
            if (courseCombo.getItemAt(i).getCourseId() == courseId) courseCombo.setSelectedIndex(i);
        }
    }

    /** Picks a component the course really has and shows a hint. */
    private void courseChanged() {
        Course c = (Course) courseCombo.getSelectedItem();
        if (c == null) {
            info.setText("No course for this department in L" + AppConfig.CURRENT_LEVEL + " S" + AppConfig.CURRENT_SEMESTER
                    + " - tick 'Show courses of every department' to see all.");
            info.setForeground(Theme.WARNING);
            return;
        }
        if (componentCombo.getSelectedItem() == Component.THEORY && !c.hasTheory()) componentCombo.setSelectedItem(Component.PRACTICAL);
        if (componentCombo.getSelectedItem() == Component.PRACTICAL && !c.hasPractical()) componentCombo.setSelectedItem(Component.THEORY);
        info.setText(c.getCourseCode() + " has: " + c.getComponentLabel() + "  ·  L" + c.getLevel() + " S" + c.getSemester()
                + "  ·  " + Format.nullToDash(c.getLecturerNames()));
        info.setForeground(Theme.TEXT_MUTED);
    }

    /** When the start moves past the end, keep a 2-hour session. */
    private void startChanged() {
        LocalTime s = (LocalTime) startCombo.getSelectedItem();
        LocalTime e = (LocalTime) endCombo.getSelectedItem();
        if (s != null && (e == null || !e.isAfter(s))) {
            LocalTime proposed = s.plusHours(AppConfig.HOURS_PER_SESSION);
            endCombo.setSelectedItem(proposed.isAfter(LocalTime.of(18, 0)) ? LocalTime.of(18, 0) : proposed);
        }
    }

    @Override
    protected void save() throws AppException {
        Course c = (Course) courseCombo.getSelectedItem();
        if (c == null) throw new ValidationException("Course", "Please select a course.");
        Integer deptId = AdminUI.selectedDepartmentId(deptCombo);
        if (deptId == null) throw new ValidationException("Department", "Please select a department.");
        Object roomObj = roomCombo.getEditor().getItem();
        String room = roomObj == null ? "" : roomObj.toString().trim();
        if (room.isEmpty()) throw new ValidationException("Room", "Please enter the room / lab (e.g. LH-01, Lab-02).");
        if (room.length() > 20) throw new ValidationException("Room", "Room name can have at most 20 characters.");

        entry.setDepartmentId(deptId);
        entry.setCourseId(c.getCourseId());
        entry.setCourseCode(c.getCourseCode());
        entry.setDay((String) dayCombo.getSelectedItem());
        entry.setStartTime((LocalTime) startCombo.getSelectedItem());
        entry.setEndTime((LocalTime) endCombo.getSelectedItem());
        entry.setRoom(room);
        entry.setComponent((Component) componentCombo.getSelectedItem());
        dao.save(entry, c);           // throws ValidationException / TimetableClashException
    }
}

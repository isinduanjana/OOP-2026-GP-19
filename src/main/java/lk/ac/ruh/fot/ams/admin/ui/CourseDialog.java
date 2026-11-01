package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import lk.ac.ruh.fot.ams.admin.dao.AdminCourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M1 - ADMIN MODULE] Add / Edit a course unit and choose its lecturers.
 * Hours are per SESSION (handout: 1 session = 2 hours); 0 means the course has no
 * theory / practical part (e.g. ICT2132 is practical only).
 *
 * OOP: Inheritance (AdminFormDialog), Polymorphism (save()), Generics (CheckBoxList&lt;Lecturer&gt;).
 */
public class CourseDialog extends AdminFormDialog {

    private final AdminCourseDAO dao;
    private final Course course;
    private final JTextField codeField = UI.textField(12);
    private final JTextField nameField = UI.textField(24);
    private final JSpinner creditsSpin = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
    private final JSpinner theorySpin = new JSpinner(new SpinnerNumberModel(2, 0, 6, 1));
    private final JSpinner practicalSpin = new JSpinner(new SpinnerNumberModel(2, 0, 6, 1));
    private final JComboBox<Integer> levelCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
    private final JComboBox<Integer> semesterCombo = new JComboBox<>(new Integer[]{1, 2});
    private final JComboBox<Department> deptCombo;
    private final JTextArea descArea = UI.textArea(4);
    private final CheckBoxList<Lecturer> lecturerList;
    private final JLabel lecturerCount = UI.muted("");

    public CourseDialog(Component parent, Color accent, AdminCourseDAO dao, List<Department> departments,
                        List<Lecturer> lecturers, Course existing) throws AppException {
        super(parent, existing == null ? "Add course" : "Edit course",
                existing == null ? "Create a new course unit and assign its lecturers"
                        : "Update " + existing.getCourseCode() + " - " + existing.getCourseName(),
                "book", accent);
        this.dao = dao;
        this.course = existing == null ? new Course() : existing;
        deptCombo = AdminUI.departmentCombo(departments, "None");
        // each row: "Dr. Nuwan Perera  (ICT)" - department code looked up from the department list
        lecturerList = new CheckBoxList<>(l -> l.getFullName() + "   (" + AdminUI.deptCode(departments, l.getDepartmentId()) + ")");
        lecturerList.setItems(lecturers);
        if (existing != null) {
            List<Integer> assigned = dao.findLecturerIds(existing.getCourseId());
            for (Lecturer l : lecturers) lecturerList.setChecked(l, assigned.contains(l.getLecturerId()));
        }
        lecturerList.onChange(this::updateCount);
        build();
        fill();
    }

    private void build() {
        codeField.putClientProperty("JTextField.placeholderText", "ICT2132");
        nameField.putClientProperty("JTextField.placeholderText", "Object Oriented Programming Practicum");
        JPanel levelSem = new JPanel(new GridLayout(1, 3, 8, 0));
        levelSem.setOpaque(false);
        levelSem.add(levelCombo);
        levelSem.add(new JLabel("Semester", JLabel.RIGHT));
        levelSem.add(semesterCombo);
        JPanel hours = new JPanel(new GridLayout(1, 3, 8, 0));
        hours.setOpaque(false);
        hours.add(theorySpin);
        hours.add(new JLabel("Practical", JLabel.RIGHT));
        hours.add(practicalSpin);
        JPanel details = UI.form(new String[]{"Course code", "Course name", "Credits", "Theory h/session", "Level",
                        "Department", "Description"},
                codeField, nameField, creditsSpin, hours, levelSem, deptCombo, AdminUI.areaScroll(descArea, 280, 84));
        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(AdminUI.section("Course details", details), BorderLayout.NORTH);
        left.add(AdminUI.hint("Hours are per session (1 session = 2 hours). Put 0 when the course has no "
                + "theory or no practical component."), BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(lecturerList);
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
        sp.setPreferredSize(new Dimension(290, 260));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        right.add(sp, BorderLayout.CENTER);
        right.add(lecturerCount, BorderLayout.SOUTH);

        JPanel cols = new JPanel(new BorderLayout(24, 0));
        cols.setOpaque(false);
        cols.add(left, BorderLayout.CENTER);
        cols.add(AdminUI.section("Lecturers (tick one or more)", right), BorderLayout.EAST);
        setContent(cols);
        saveButton.setText(course.getCourseId() == 0 ? "Create course" : "Save changes");
    }

    private void fill() {
        if (course.getCourseId() != 0) {
            codeField.setText(course.getCourseCode());
            nameField.setText(course.getCourseName());
            creditsSpin.setValue(Math.max(1, Math.min(6, course.getCredits())));
            theorySpin.setValue(course.getTheoryHours());
            practicalSpin.setValue(course.getPracticalHours());
            levelCombo.setSelectedItem(course.getLevel());
            semesterCombo.setSelectedItem(course.getSemester());
            descArea.setText(course.getDescription());
        } else {
            levelCombo.setSelectedItem(lk.ac.ruh.fot.ams.config.AppConfig.CURRENT_LEVEL);
            semesterCombo.setSelectedItem(lk.ac.ruh.fot.ams.config.AppConfig.CURRENT_SEMESTER);
            if (deptCombo.getItemCount() > 1) deptCombo.setSelectedIndex(1);
        }
        if (course.getCourseId() != 0) AdminUI.selectDepartment(deptCombo, course.getDepartmentId());
        updateCount();
    }

    private void updateCount() {
        int n = lecturerList.getCheckedItems().size();
        lecturerCount.setText(n == 0 ? "No lecturer assigned yet" : n + " lecturer" + (n == 1 ? "" : "s") + " selected");
    }

    @Override
    protected void save() throws AppException {
        String code = Validator.required(codeField.getText(), "Course code").toUpperCase().replace(" ", "");
        if (!code.matches("^[A-Z]{2,5}\\d{4}$")) {
            throw new ValidationException("Course code", "Course code must look like ICT2132 (2-5 letters + 4 digits).");
        }
        String name = Validator.required(nameField.getText(), "Course name");
        int theory = (Integer) theorySpin.getValue();
        int practical = (Integer) practicalSpin.getValue();
        if (theory == 0 && practical == 0) {
            throw new ValidationException("Hours", "A course needs a theory part, a practical part or both "
                    + "(theory and practical hours cannot both be 0).");
        }
        course.setCourseCode(code);
        course.setCourseName(name);
        course.setCredits((Integer) creditsSpin.getValue());
        course.setTheoryHours(theory);
        course.setPracticalHours(practical);
        course.setLevel((Integer) levelCombo.getSelectedItem());
        course.setSemester((Integer) semesterCombo.getSelectedItem());
        course.setDepartmentId(AdminUI.selectedDepartmentId(deptCombo));
        course.setDescription(descArea.getText().isBlank() ? null : descArea.getText().trim());
        List<Integer> ids = new ArrayList<>();
        for (Lecturer l : lecturerList.getCheckedItems()) ids.add(l.getLecturerId());
        dao.save(course, ids);
    }
}

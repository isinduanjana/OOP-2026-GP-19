package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lk.ac.ruh.fot.ams.admin.dao.AdminUserDAO;
import lk.ac.ruh.fot.ams.admin.model.UserForm;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M1 - ADMIN MODULE] Add / Edit dialog for a user of ANY role.
 * Choosing the role switches the "role details" part (CardLayout):
 *   staff (Admin, Lecturer, TO) -> employee number + department
 *   undergraduate               -> TG number, batch, level, semester, type, department
 * The role can not be changed after the account is created.
 *
 * OOP: Inheritance (extends AdminFormDialog, implements save()), Encapsulation (the typed
 *      values go into a UserForm object which validates itself), Exception handling.
 */
public class UserDialog extends AdminFormDialog {

    private static final String STAFF = "staff";
    private static final String STUDENT = "student";
    private static final int LABEL_W = 130;

    private final AdminUserDAO dao;
    private final UserForm form;
    private final User existing;

    // ---- account
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private final JTextField usernameField = UI.textField(20);
    private final JPasswordField passwordField = UI.passwordField(20);
    private final JPasswordField confirmField = UI.passwordField(20);
    // ---- personal
    private final JTextField fullNameField = UI.textField(20);
    private final JTextField emailField = UI.textField(20);
    private final JTextField phoneField = UI.textField(20);
    private final JTextArea addressArea = UI.textArea(3);
    // ---- staff
    private final JTextField employeeNoField = UI.textField(16);
    private final JComboBox<Department> staffDeptCombo;
    // ---- undergraduate
    private final JTextField tgField = UI.textField(16);
    private final JTextField batchField = UI.textField(8);
    private final JComboBox<Integer> levelCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
    private final JComboBox<Integer> semesterCombo = new JComboBox<>(new Integer[]{1, 2});
    private final JComboBox<StudentType> typeCombo = new JComboBox<>(StudentType.values());
    private final JComboBox<Department> studentDeptCombo;
    private final JCheckBox enrollBox = new JCheckBox("Enroll in current semester courses");

    private final CardLayout roleCards = new CardLayout();
    private final JPanel rolePanel = new JPanel(roleCards);
    private String lastSuggestion = "";

    /**
     * @param existing null = add a new user
     */
    public UserDialog(Component parent, Color accent, AdminUserDAO dao, List<Department> departments, User existing) {
        super(parent, existing == null ? "Add user" : "Edit user",
                existing == null ? "Create an account for an administrator, lecturer, technical officer or undergraduate"
                        : "Update the account of " + existing.getFullName() + " (" + existing.getRole() + ")",
                existing == null ? "plus" : "edit", accent);
        this.dao = dao;
        this.existing = existing;
        this.form = existing == null ? new UserForm() : new UserForm(existing);
        staffDeptCombo = AdminUI.departmentCombo(departments, "None");
        studentDeptCombo = AdminUI.departmentCombo(departments, null);
        buildForm();
        loadForm();
    }

    private void buildForm() {
        // ---------------- left column: account + personal details
        usernameField.putClientProperty("JTextField.placeholderText", "e.g. lec.kumara or tg1050");
        emailField.putClientProperty("JTextField.placeholderText", "name@fot.ruh.ac.lk");
        phoneField.putClientProperty("JTextField.placeholderText", "07XXXXXXXX (optional)");
        JPanel account;
        if (existing == null) {
            account = AdminUI.form(LABEL_W, new String[]{"Role", "Username", "Password", "Confirm password"},
                    roleCombo, usernameField, passwordField, confirmField);
        } else {
            roleCombo.setEnabled(false);
            roleCombo.setToolTipText("The role of an account cannot be changed after it is created");
            account = AdminUI.form(LABEL_W, new String[]{"Role", "Username"}, roleCombo, usernameField);
        }
        JPanel personal = AdminUI.form(LABEL_W, new String[]{"Full name", "Email", "Phone", "Address"},
                fullNameField, emailField, phoneField, AdminUI.areaScroll(addressArea, 260, 66));

        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);
        left.add(AdminUI.section("Account", account), BorderLayout.NORTH);
        left.add(AdminUI.section("Personal details", personal), BorderLayout.CENTER);

        // ---------------- right column: role specific details
        employeeNoField.putClientProperty("JTextField.placeholderText", "EMP/LEC/007");
        JPanel staff = new JPanel(new BorderLayout(0, 6));
        staff.setOpaque(false);
        staff.add(AdminUI.form(100, new String[]{"Employee no", "Department"}, employeeNoField, staffDeptCombo),
                BorderLayout.NORTH);
        JPanel staffHint = new JPanel(new BorderLayout());
        staffHint.setOpaque(false);
        staffHint.add(AdminUI.hint("A free employee number is suggested for new staff. "
                + "Lecturers and technical officers must belong to a department."), BorderLayout.NORTH);
        staff.add(staffHint, BorderLayout.CENTER);

        tgField.putClientProperty("JTextField.placeholderText", "TG/2023/1234");
        batchField.putClientProperty("JTextField.placeholderText", "2023");
        enrollBox.setOpaque(false);
        enrollBox.setSelected(true);
        JPanel levelSem = new JPanel(new GridLayout(1, 3, 8, 0));
        levelSem.setOpaque(false);
        levelSem.add(levelCombo);
        levelSem.add(new JLabel("Semester", JLabel.RIGHT));
        levelSem.add(semesterCombo);
        JPanel student = new JPanel(new BorderLayout(0, 6));
        student.setOpaque(false);
        JPanel sForm = AdminUI.form(100, new String[]{"TG number", "Batch", "Level", "Student type", "Department"},
                tgField, batchField, levelSem, typeCombo, studentDeptCombo);
        student.add(sForm, BorderLayout.NORTH);
        if (existing == null) {
            JPanel enroll = new JPanel(new BorderLayout(0, 2));
            enroll.setOpaque(false);
            enroll.add(enrollBox, BorderLayout.NORTH);
            enroll.add(AdminUI.hint("Enrols the student in the courses of the selected level & semester "
                    + "(own department + common courses) for " + AppConfig.CURRENT_ACADEMIC_YEAR + "."),
                    BorderLayout.CENTER);
            student.add(enroll, BorderLayout.CENTER);
        }
        // auto-fill batch from the TG number: TG/2023/1234 -> 2023
        tgField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String tg = tgField.getText().trim().toUpperCase();
                if (tg.matches("TG/\\d{4}/.*") && batchField.getText().isBlank()) batchField.setText(tg.substring(3, 7));
            }
        });

        rolePanel.setOpaque(false);
        rolePanel.add(staff, STAFF);
        rolePanel.add(student, STUDENT);
        JPanel rightWrap = new JPanel(new BorderLayout());
        rightWrap.setOpaque(false);
        rightWrap.add(rolePanel, BorderLayout.NORTH);
        JComponent right = AdminUI.section("Role details", rightWrap);
        right.setPreferredSize(new Dimension(380, 10));

        roleCombo.addActionListener(e -> roleChanged());

        JPanel columns = new JPanel(new BorderLayout(26, 0));
        columns.setOpaque(false);
        columns.add(left, BorderLayout.CENTER);
        columns.add(right, BorderLayout.EAST);
        setContent(columns);
        saveButton.setText(existing == null ? "Create user" : "Save changes");
    }

    /** Copies the UserForm values into the fields. */
    private void loadForm() {
        roleCombo.setSelectedItem(form.getRole());
        usernameField.setText(form.getUsername());
        fullNameField.setText(form.getFullName());
        emailField.setText(form.getEmail());
        phoneField.setText(form.getPhone());
        addressArea.setText(form.getAddress());
        employeeNoField.setText(form.getEmployeeNo());
        tgField.setText(form.getTgNo());
        batchField.setText(form.getBatch());
        levelCombo.setSelectedItem(form.getLevel());
        semesterCombo.setSelectedItem(form.getSemester());
        typeCombo.setSelectedItem(form.getStudentType());
        AdminUI.selectDepartment(staffDeptCombo, form.getDepartmentId());
        if (form.getDepartmentId() != null) AdminUI.selectDepartment(studentDeptCombo, form.getDepartmentId());
        roleChanged();
    }

    /** Shows the right role card; suggests an employee number for new staff. */
    private void roleChanged() {
        Role role = (Role) roleCombo.getSelectedItem();
        boolean student = role == Role.UNDERGRADUATE;
        roleCards.show(rolePanel, student ? STUDENT : STAFF);
        if (existing == null && !student) {
            String current = employeeNoField.getText().trim();
            if (current.isEmpty() || current.equals(lastSuggestion)) {
                try {
                    lastSuggestion = dao.suggestEmployeeNo(role);
                    employeeNoField.setText(lastSuggestion);
                } catch (DatabaseException ignored) {
                    // no suggestion - the admin types the number
                }
            }
        }
    }

    /** Copies the fields into the UserForm, validates and saves (one transaction in the DAO). */
    @Override
    protected void save() throws AppException {
        Role role = (Role) roleCombo.getSelectedItem();
        if (existing == null) {
            form.setRole(role);
            String pw = new String(passwordField.getPassword());
            if (!pw.equals(new String(confirmField.getPassword()))) {
                throw new lk.ac.ruh.fot.ams.exception.ValidationException("Password", "The two passwords do not match.");
            }
            form.setPassword(pw);
            form.setEnrollInCurrentCourses(enrollBox.isSelected());
        }
        form.setUsername(usernameField.getText());
        form.setFullName(fullNameField.getText());
        form.setEmail(emailField.getText());
        form.setPhone(phoneField.getText());
        form.setAddress(addressArea.getText());
        if (role == Role.UNDERGRADUATE) {
            form.setTgNo(tgField.getText());
            form.setBatch(batchField.getText());
            form.setLevel((Integer) levelCombo.getSelectedItem());
            form.setSemester((Integer) semesterCombo.getSelectedItem());
            form.setStudentType((StudentType) typeCombo.getSelectedItem());
            form.setDepartmentId(AdminUI.selectedDepartmentId(studentDeptCombo));
        } else {
            form.setEmployeeNo(employeeNoField.getText());
            form.setDepartmentId(AdminUI.selectedDepartmentId(staffDeptCombo));
        }
        dao.save(form);
    }
}

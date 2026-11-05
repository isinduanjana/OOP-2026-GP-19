package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lk.ac.ruh.fot.ams.admin.dao.AdminUserDAO;
import lk.ac.ruh.fot.ams.admin.model.UserForm;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.components.UI;

public class UserDialog extends AdminFormDialog {

    private static final String STAFF = "staff";
    private static final String STUDENT = "student";
    private static final int LABEL_W = 130;

    private static final DateTimeFormatter DOB_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AdminUserDAO dao;
    private final UserForm form;
    private final User existing;

    // Account fields
    private final JComboBox<Role> roleCombo =
            new JComboBox<>(Role.values());

    private final JTextField usernameField = UI.textField(20);
    private final JPasswordField passwordField = UI.passwordField(20);
    private final JPasswordField confirmField = UI.passwordField(20);

    // Personal fields
    private final JTextField fullNameField = UI.textField(20);
    private final JTextField emailField = UI.textField(20);
    private final JTextField phoneField = UI.textField(20);
    private final JTextArea addressArea = UI.textArea(3);

    // NIC fields
    private final JTextField nicField = UI.textField(20);
    private final JTextField dobField = UI.textField(20);
    private final JTextField ageField = UI.textField(20);

    // Staff fields
    private final JTextField employeeNoField = UI.textField(16);
    private final JComboBox<Department> staffDeptCombo;

    // Undergraduate fields
    private final JTextField tgField = UI.textField(16);
    private final JTextField batchField = UI.textField(8);

    private final JComboBox<Integer> levelCombo =
            new JComboBox<>(new Integer[]{1, 2, 3, 4});

    private final JComboBox<Integer> semesterCombo =
            new JComboBox<>(new Integer[]{1, 2});

    private final JComboBox<StudentType> typeCombo =
            new JComboBox<>(StudentType.values());

    private final JComboBox<Department> studentDeptCombo;

    private final JCheckBox enrollBox =
            new JCheckBox("Enroll in current semester courses");

    private final CardLayout roleCards = new CardLayout();
    private final JPanel rolePanel = new JPanel(roleCards);

    private String lastSuggestion = "";

    public UserDialog(
            Component parent,
            Color accent,
            AdminUserDAO dao,
            List<Department> departments,
            User existing
    ) {

        super(
                parent,
                existing == null ? "Add user" : "Edit user",
                existing == null
                        ? "Create an account for a system user"
                        : "Update the account of " + existing.getFullName(),
                existing == null ? "plus" : "edit",
                accent
        );

        this.dao = dao;
        this.existing = existing;

        this.form = existing == null
                ? new UserForm()
                : new UserForm(existing);

        staffDeptCombo = AdminUI.departmentCombo(departments, "None");
        studentDeptCombo = AdminUI.departmentCombo(departments, null);

        dobField.setEditable(false);
        ageField.setEditable(false);

        dobField.setBackground(new Color(240, 240, 240));
        ageField.setBackground(new Color(240, 240, 240));

        buildForm();
        loadForm();
        setupNICListener();
    }

    private void buildForm() {

        usernameField.putClientProperty(
                "JTextField.placeholderText",
                "e.g. lec.kumara"
        );

        emailField.putClientProperty(
                "JTextField.placeholderText",
                "name@fot.ruh.ac.lk"
        );

        phoneField.putClientProperty(
                "JTextField.placeholderText",
                "07XXXXXXXX"
        );

        nicField.putClientProperty(
                "JTextField.placeholderText",
                "e.g. 200012345678 or 123456789V"
        );

        // Account section
        JPanel account;

        if (existing == null) {

            account = AdminUI.form(
                    LABEL_W,
                    new String[]{
                            "Role",
                            "Username",
                            "Password",
                            "Confirm password"
                    },
                    roleCombo,
                    usernameField,
                    passwordField,
                    confirmField
            );

        } else {

            roleCombo.setEnabled(false);

            account = AdminUI.form(
                    LABEL_W,
                    new String[]{"Role", "Username"},
                    roleCombo,
                    usernameField
            );
        }

        // Personal details section
        JPanel personal = AdminUI.form(
                LABEL_W,
                new String[]{
                        "Full name",
                        "Email",
                        "Phone",
                        "Address",
                        "NIC",
                        "Date of birth",
                        "Age"
                },
                fullNameField,
                emailField,
                phoneField,
                AdminUI.areaScroll(addressArea, 260, 66),
                nicField,
                dobField,
                ageField
        );

        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);

        left.add(
                AdminUI.section("Account", account),
                BorderLayout.NORTH
        );

        left.add(
                AdminUI.section("Personal details", personal),
                BorderLayout.CENTER
        );

        // Staff section
        employeeNoField.putClientProperty(
                "JTextField.placeholderText",
                "EMP/LEC/007"
        );

        JPanel staff = new JPanel(new BorderLayout(0, 6));
        staff.setOpaque(false);

        staff.add(
                AdminUI.form(
                        100,
                        new String[]{"Employee no", "Department"},
                        employeeNoField,
                        staffDeptCombo
                ),
                BorderLayout.NORTH
        );

        JPanel staffHint = new JPanel(new BorderLayout());
        staffHint.setOpaque(false);

        staffHint.add(
                AdminUI.hint(
                        "Lecturers and technical officers must belong to a department."
                ),
                BorderLayout.NORTH
        );

        staff.add(staffHint, BorderLayout.CENTER);

        // Undergraduate section
        tgField.putClientProperty(
                "JTextField.placeholderText",
                "TG/2023/1234"
        );

        batchField.putClientProperty(
                "JTextField.placeholderText",
                "2023"
        );

        enrollBox.setOpaque(false);
        enrollBox.setSelected(true);

        JPanel levelSem = new JPanel(
                new GridLayout(1, 3, 8, 0)
        );

        levelSem.setOpaque(false);
        levelSem.add(levelCombo);
        levelSem.add(new JLabel("Semester", JLabel.RIGHT));
        levelSem.add(semesterCombo);

        JPanel student = new JPanel(new BorderLayout(0, 6));
        student.setOpaque(false);

        JPanel sForm = AdminUI.form(
                100,
                new String[]{
                        "TG number",
                        "Batch",
                        "Level",
                        "Student type",
                        "Department"
                },
                tgField,
                batchField,
                levelSem,
                typeCombo,
                studentDeptCombo
        );

        student.add(sForm, BorderLayout.NORTH);

        if (existing == null) {

            JPanel enroll = new JPanel(new BorderLayout(0, 2));
            enroll.setOpaque(false);

            enroll.add(enrollBox, BorderLayout.NORTH);

            enroll.add(
                    AdminUI.hint(
                            "Enrolls the student in current semester courses."
                    ),
                    BorderLayout.CENTER
            );

            student.add(enroll, BorderLayout.CENTER);
        }

        // Auto-fill batch
        tgField.addFocusListener(
                new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusLost(
                            java.awt.event.FocusEvent e
                    ) {

                        String tg = tgField.getText()
                                .trim()
                                .toUpperCase();

                        if (tg.matches("TG/\\d{4}/.*")
                                && batchField.getText().isBlank()) {

                            batchField.setText(tg.substring(3, 7));
                        }
                    }
                }
        );

        rolePanel.setOpaque(false);
        rolePanel.add(staff, STAFF);
        rolePanel.add(student, STUDENT);

        JPanel rightWrap = new JPanel(new BorderLayout());
        rightWrap.setOpaque(false);
        rightWrap.add(rolePanel, BorderLayout.NORTH);

        JComponent right = AdminUI.section(
                "Role details",
                rightWrap
        );

        right.setPreferredSize(new Dimension(380, 10));

        roleCombo.addActionListener(e -> roleChanged());

        JPanel columns = new JPanel(
                new BorderLayout(26, 0)
        );

        columns.setOpaque(false);
        columns.add(left, BorderLayout.CENTER);
        columns.add(right, BorderLayout.EAST);

        setContent(columns);

        saveButton.setText(
                existing == null
                        ? "Create user"
                        : "Save changes"
        );
    }

    /**
     * Automatically updates DOB and age when NIC changes.
     */
    private void setupNICListener() {

        nicField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateNICDetails();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateNICDetails();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateNICDetails();
                    }
                }
        );
    }

    private void updateNICDetails() {

        form.setNic(nicField.getText());

        if (form.getDateOfBirth() == null) {

            dobField.setText("");
            ageField.setText("");

            return;
        }

        dobField.setText(
                form.getDateOfBirth().format(DOB_FORMAT)
        );

        ageField.setText(
                String.valueOf(form.getAge())
        );
    }

    private void loadForm() {

        roleCombo.setSelectedItem(form.getRole());

        usernameField.setText(form.getUsername());
        fullNameField.setText(form.getFullName());
        emailField.setText(form.getEmail());
        phoneField.setText(form.getPhone());
        addressArea.setText(form.getAddress());

        nicField.setText(form.getNic());

        employeeNoField.setText(form.getEmployeeNo());

        tgField.setText(form.getTgNo());
        batchField.setText(form.getBatch());

        levelCombo.setSelectedItem(form.getLevel());
        semesterCombo.setSelectedItem(form.getSemester());
        typeCombo.setSelectedItem(form.getStudentType());

        AdminUI.selectDepartment(
                staffDeptCombo,
                form.getDepartmentId()
        );

        if (form.getDepartmentId() != null) {

            AdminUI.selectDepartment(
                    studentDeptCombo,
                    form.getDepartmentId()
            );
        }

        updateNICDetails();
        roleChanged();
    }

    private void roleChanged() {

        Role role = (Role) roleCombo.getSelectedItem();

        if (role == null) {
            return;
        }

        boolean student = role == Role.UNDERGRADUATE;

        roleCards.show(
                rolePanel,
                student ? STUDENT : STAFF
        );

        if (existing == null && !student) {

            String current = employeeNoField.getText().trim();

            if (current.isEmpty() || current.equals(lastSuggestion)) {

                try {

                    lastSuggestion = dao.suggestEmployeeNo(role);
                    employeeNoField.setText(lastSuggestion);

                } catch (DatabaseException ignored) {
                    // Admin can enter employee number manually.
                }
            }
        }
    }

    @Override
    protected void save() throws AppException {

        Role role = (Role) roleCombo.getSelectedItem();

        if (existing == null) {

            form.setRole(role);

            String pw = new String(passwordField.getPassword());

            String confirm = new String(confirmField.getPassword());

            if (!pw.equals(confirm)) {

                throw new ValidationException(
                        "Password",
                        "The two passwords do not match."
                );
            }

            form.setPassword(pw);
            form.setEnrollInCurrentCourses(enrollBox.isSelected());
        }

        form.setUsername(usernameField.getText());
        form.setFullName(fullNameField.getText());
        form.setEmail(emailField.getText());
        form.setPhone(phoneField.getText());
        form.setAddress(addressArea.getText());

        form.setNic(nicField.getText());

        if (role == Role.UNDERGRADUATE) {

            form.setTgNo(tgField.getText());
            form.setBatch(batchField.getText());

            form.setLevel((Integer) levelCombo.getSelectedItem());
            form.setSemester((Integer) semesterCombo.getSelectedItem());

            form.setStudentType(
                    (StudentType) typeCombo.getSelectedItem()
            );

            form.setDepartmentId(
                    AdminUI.selectedDepartmentId(studentDeptCombo)
            );

        } else {

            form.setEmployeeNo(employeeNoField.getText());

            form.setDepartmentId(
                    AdminUI.selectedDepartmentId(staffDeptCombo)
            );
        }

        dao.save(form);
    }
}
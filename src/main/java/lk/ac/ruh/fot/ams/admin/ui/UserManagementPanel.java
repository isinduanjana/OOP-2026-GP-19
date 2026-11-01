package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import lk.ac.ruh.fot.ams.admin.dao.AdminUserDAO;
import lk.ac.ruh.fot.ams.dao.DepartmentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Department;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.ui.components.ModernButton;
import lk.ac.ruh.fot.ams.ui.components.UI;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] "User Management" page: create and maintain the profiles of
 * admins, lecturers, technical officers and undergraduates.
 *
 * OOP: Inheritance + Polymorphism - extends the generic CrudPanel&lt;User&gt; and overrides its
 *      abstract methods. The rows hold User objects whose real class (Admin, Lecturer ...)
 *      decides what getIdentityNumber() returns (employee no or TG no).
 */
public class UserManagementPanel extends CrudPanel<User> {

    private final AdminUserDAO dao = new AdminUserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final JComboBox<Role> roleFilter = new JComboBox<>();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All statuses", "Active", "Inactive"});
    private final ModernButton toggleButton = UI.secondary("Deactivate", "eye");
    private final ModernButton resetButton = UI.secondary("Reset password", "settings");
    private List<Department> departments = new ArrayList<>();

    public UserManagementPanel(User user, Color accent) {
        super(user, accent, "user");
        roleFilter.addItem(null);
        for (Role r : Role.values()) roleFilter.addItem(r);
        roleFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v == null ? "All roles" : v.toString());
                return this;
            }
        });
        roleFilter.setPreferredSize(new Dimension(170, 36));
        statusFilter.setPreferredSize(new Dimension(130, 36));
        roleFilter.addActionListener(e -> applyFilter());
        statusFilter.addActionListener(e -> applyFilter());
        addFilter(roleFilter);
        addFilter(statusFilter);

        toggleButton.addActionListener(e -> toggleStatus());
        resetButton.addActionListener(e -> resetPassword());
        addAction(toggleButton, true);
        addAction(resetButton, true);
    }

    @Override
    public String getTitle() {
        return "User Management";
    }

    @Override
    public String getSubtitle() {
        return "Create and maintain admin, lecturer, technical officer and undergraduate profiles";
    }

    /** Lets the dashboard open the page already filtered (quick actions). */
    public void showRole(Role role) {
        roleFilter.setSelectedItem(role);
    }

    // ------------------------------------------------------------ CrudPanel implementation
    @Override
    protected String[] columns() {
        return new String[]{"Username", "Full name", "Role", "ID number", "Dept.", "Status", "Email", "Phone"};
    }

    @Override
    protected int[] columnWidths() {
        return new int[]{115, 200, 150, 125, 60, 70, 210, 110};
    }

    @Override
    protected void beforeLoad() throws AppException {
        departments = departmentDAO.findAll();
    }

    @Override
    protected List<User> loadItems() throws AppException {
        return dao.findAll();
    }

    @Override
    protected boolean accept(User u) {
        Role role = (Role) roleFilter.getSelectedItem();
        if (role != null && u.getRole() != role) return false;
        int st = statusFilter.getSelectedIndex();
        return st == 0 || (st == 1) == u.isActive();
    }

    @Override
    protected Object[] toRow(User u) {
        // polymorphism: getIdentityNumber() returns the employee no for staff and the TG no for students
        String role = u instanceof Undergraduate s && s.getStudentType() != StudentType.NORMAL
                ? s.getStudentType() + " student" : u.getRole().getDisplayName();
        return new Object[]{u.getUsername(), u.getFullName(), role, Format.nullToDash(u.getIdentityNumber()),
                AdminUI.deptCode(departments, u.getDepartmentId()), AdminUI.statusWord(u.getStatus()),
                Format.nullToDash(u.getEmail()), Format.nullToDash(u.getPhone())};
    }

    @Override
    protected boolean openEditor(User u) throws AppException {
        if (departments.isEmpty()) departments = departmentDAO.findAll();
        // re-read so we edit the newest data
        User fresh = u == null ? null : dao.findById(u.getUserId());
        if (u != null && fresh == null) throw new ValidationException("This user no longer exists.");
        boolean saved = new UserDialog(this, accent, dao, departments, fresh).showDialog();
        if (saved && fresh != null && fresh.getUserId() == user.getUserId()) {
            notifyUserChanged();          // own name changed -> update the side bar
        }
        return saved;
    }

    @Override
    protected void checkCanDelete(User u) throws AppException {
        if (u.getUserId() == user.getUserId()) {
            throw new ValidationException("You cannot delete your own account while you are logged in.");
        }
    }

    @Override
    protected String deleteWarning(User u) {
        return u instanceof Undergraduate
                ? "\nThe student's enrolments, attendance, marks, medicals and grades will also be removed."
                : "\nThe profile and role details will also be removed.";
    }

    @Override
    protected void deleteItem(User u) throws AppException {
        dao.deleteUser(u, user);
    }

    @Override
    protected String describe(User u) {
        return u.getFullName() + " (" + u.getUsername() + ")";
    }

    @Override
    protected int idOf(User u) {
        return u.getUserId();
    }

    @Override
    protected void onSelectionChanged(User u) {
        boolean self = u != null && u.getUserId() == user.getUserId();
        toggleButton.setText(u != null && !u.isActive() ? "Activate" : "Deactivate");
        toggleButton.setEnabled(u != null && !self);
        deleteButton.setEnabled(u != null && !self);
        toggleButton.setToolTipText(self ? "You cannot deactivate your own account" : null);
    }

    // ------------------------------------------------------------ extra actions
    private void toggleStatus() {
        User u = getSelected();
        if (u == null) return;
        String verb = u.isActive() ? "Deactivate" : "Activate";
        String extra = u.isActive() ? "\nThe user will not be able to log in until the account is activated again." : "";
        if (!UI.confirm(this, verb + " the account of " + describe(u) + "?" + extra)) return;
        safe(() -> {
            String status = dao.toggleStatus(u, user);
            refresh();
            flash(describe(u) + " is now " + AdminUI.capital(status).toLowerCase() + ".");
        });
    }

    private void resetPassword() {
        User u = getSelected();
        if (u == null) return;
        if (new ResetPasswordDialog(this, accent, dao, u).showDialog()) {
            flash("Password of " + describe(u) + " was reset.");
        }
    }
}

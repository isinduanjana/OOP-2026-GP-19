package lk.ac.ruh.fot.ams.admin.dao;

import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.admin.DuplicateRecordException;
import lk.ac.ruh.fot.ams.admin.DuplicateUsernameException;
import lk.ac.ruh.fot.ams.admin.model.UserForm;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Staff;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.util.PasswordUtil;

/**
 * [M1 - ADMIN MODULE] Creates, edits, deletes and (de)activates user accounts of
 * all four roles.
 *
 * Connection to common: "extends BaseDAO" gives queryList / queryNumber / executeUpdate /
 * inTransaction; reading re-uses UserDAO.BASE_SELECT + UserDAO.mapUser (the common factory
 * that returns Admin / Lecturer / TechnicalOfficer / Undergraduate objects).
 *
 * OOP: Inheritance (BaseDAO), Abstraction + Generics (implements CrudDAO&lt;User&gt;),
 *      Polymorphism (works with "User" and checks the real subclass with instanceof),
 *      Exception handling (DuplicateUsernameException, DuplicateRecordException, transactions).
 */
public class AdminUserDAO extends BaseDAO implements CrudDAO<User> {

    /** Password given to accounts created through the plain CrudDAO.insert(User). */
    public static final String DEFAULT_PASSWORD = "1234";

    private static final String ORDER =
            "ORDER BY FIELD(u.role,'ADMIN','LECTURER','TO','UNDERGRADUATE'), p.full_name";

    // ------------------------------------------------------------ CrudDAO<User>
    @Override
    public List<User> findAll() throws DatabaseException {
        return queryList(UserDAO.BASE_SELECT + ORDER, UserDAO::mapUser);
    }

    @Override
    public User findById(int userId) throws DatabaseException {
        return queryOne(UserDAO.BASE_SELECT + "WHERE u.user_id = ?", UserDAO::mapUser, userId);
    }

    /** Inserts with the default password and no auto enrolment. */
    @Override
    public int insert(User user) throws DatabaseException {
        return create(user, DEFAULT_PASSWORD, false);
    }

    /** Saves account, profile and role-table details in ONE transaction. */
    @Override
    public void update(User u) throws DatabaseException {
        inTransaction(c -> {
            update(c, "UPDATE users SET username = ? WHERE user_id = ?", u.getUsername(), u.getUserId());
            update(c, "UPDATE user_profiles SET full_name = ?, email = ?, phone = ?, address = ? WHERE user_id = ?",
                    u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getUserId());
            if (u instanceof Undergraduate s) {
                update(c, "UPDATE undergraduates SET tg_no = ?, batch = ?, level = ?, semester = ?, student_type = ?, "
                                + "department_id = ? WHERE user_id = ?",
                        s.getTgNo(), s.getBatch(), s.getLevel(), s.getSemester(), s.getStudentType(),
                        s.getDepartmentId(), s.getUserId());
            } else if (u instanceof Staff st) {
                // table name comes from our own constant (never from user input)
                update(c, "UPDATE " + roleTable(u.getRole()) + " SET employee_no = ?, department_id = ? WHERE user_id = ?",
                        st.getEmployeeNo(), st.getDepartmentId(), st.getUserId());
            }
        });
    }

    /** users row is deleted; profile, role row, enrolments, marks ... follow by ON DELETE CASCADE. */
    @Override
    public void delete(int userId) throws DatabaseException {
        executeUpdate("DELETE FROM users WHERE user_id = ?", userId);
    }

    // ------------------------------------------------------------ admin operations
    /**
     * Validates the form, checks unique values and inserts / updates the user.
     * @return the user_id
     */
    public int save(UserForm form) throws AppException {
        form.validate();
        checkDuplicates(form);
        User u = form.toUser();
        if (form.isNew()) {
            return create(u, form.getPassword(), form.isEnrollInCurrentCourses());
        }
        update(u);
        return u.getUserId();
    }

    /**
     * Inserts users + user_profiles + ONE role-table row (+ optional enrolments)
     * as a single transaction: either everything is saved or nothing.
     */
    public int create(User u, String plainPassword, boolean enrollInCourses) throws DatabaseException {
        // course list is read BEFORE the transaction starts
        List<Integer> courseIds = new ArrayList<>();
        int semesterOfCourses = 0;
        if (enrollInCourses && u instanceof Undergraduate s) {
            courseIds = findCourseIdsFor(s.getLevel(), s.getSemester(), s.getDepartmentId());
            semesterOfCourses = s.getSemester();
        }
        final List<Integer> courses = courseIds;
        final int sem = semesterOfCourses;
        final int[] newId = new int[1];

        inTransaction(c -> {
            int userId = insert(c, "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, ?)",
                    u.getUsername(), PasswordUtil.hash(plainPassword), u.getRole(),
                    u.getStatus() == null ? "ACTIVE" : u.getStatus());
            insert(c, "INSERT INTO user_profiles (user_id, full_name, email, phone, address) VALUES (?, ?, ?, ?, ?)",
                    userId, u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress());
            if (u instanceof Undergraduate s) {
                int studentId = insert(c, "INSERT INTO undergraduates (user_id, tg_no, batch, level, semester, "
                                + "student_type, department_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        userId, s.getTgNo(), s.getBatch(), s.getLevel(), s.getSemester(), s.getStudentType(),
                        s.getDepartmentId());
                for (int courseId : courses) {
                    insert(c, "INSERT INTO enrollments (student_id, course_id, academic_year, semester, status) "
                                    + "VALUES (?, ?, ?, ?, 'ENROLLED') ON DUPLICATE KEY UPDATE status = VALUES(status)",
                            studentId, courseId, AppConfig.CURRENT_ACADEMIC_YEAR, sem);
                }
            } else if (u instanceof Staff st) {
                insert(c, "INSERT INTO " + roleTable(u.getRole()) + " (user_id, employee_no, department_id) VALUES (?, ?, ?)",
                        userId, st.getEmployeeNo(), st.getDepartmentId());
            }
            newId[0] = userId;
        });
        return newId[0];
    }

    /** Deletes a user after the safety checks (not yourself, not the last admin). */
    public void deleteUser(User target, User loggedIn) throws AppException {
        if (target.getUserId() == loggedIn.getUserId()) {
            throw new ValidationException("You cannot delete your own account while you are logged in.");
        }
        if (target.getRole() == Role.ADMIN && countAdmins() <= 1) {
            throw new ValidationException("This is the last administrator account and cannot be deleted.");
        }
        delete(target.getUserId());
    }

    /** ACTIVE <-> INACTIVE. Inactive users cannot log in (see AuthService in common). */
    public String toggleStatus(User target, User loggedIn) throws AppException {
        if (target.getUserId() == loggedIn.getUserId()) {
            throw new ValidationException("You cannot deactivate your own account.");
        }
        String newStatus = target.isActive() ? "INACTIVE" : "ACTIVE";
        executeUpdate("UPDATE users SET status = ? WHERE user_id = ?", newStatus, target.getUserId());
        return newStatus;
    }

    public void resetPassword(int userId, String plainPassword) throws DatabaseException {
        executeUpdate("UPDATE users SET password = ? WHERE user_id = ?", PasswordUtil.hash(plainPassword), userId);
    }

    public List<User> findByRole(Role role) throws DatabaseException {
        return queryList(UserDAO.BASE_SELECT + "WHERE u.role = ? ORDER BY p.full_name", UserDAO::mapUser, role);
    }

    public int countAdmins() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
    }

    /** Suggests the next free employee number, e.g. EMP/LEC/007. */
    public String suggestEmployeeNo(Role role) throws DatabaseException {
        String prefix = switch (role) {
            case ADMIN -> "EMP/ADM/";
            case LECTURER -> "EMP/LEC/";
            default -> "EMP/TO/";
        };
        List<String> used = queryList("SELECT employee_no FROM " + roleTable(role) + " WHERE employee_no LIKE ?",
                rs -> rs.getString(1), prefix + "%");
        int max = 0;
        for (String no : used) {
            try {
                max = Math.max(max, Integer.parseInt(no.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                // a hand-typed number with another format - skip it
            }
        }
        return prefix + String.format("%03d", max + 1);
    }

    /** Courses a new student should be enrolled in: own department + interdisciplinary (IDS) ones. */
    public List<Integer> findCourseIdsFor(int level, int semester, Integer departmentId) throws DatabaseException {
        return queryList("SELECT course_id FROM courses WHERE level = ? AND semester = ? AND (department_id = ? "
                        + " OR department_id IS NULL "
                        + " OR department_id IN (SELECT department_id FROM departments WHERE department_code = 'IDS')) "
                        + "ORDER BY course_code",
                rs -> rs.getInt(1), level, semester, departmentId);
    }

    // ------------------------------------------------------------ helpers
    private void checkDuplicates(UserForm f) throws AppException {
        if (queryNumber("SELECT COUNT(*) FROM users WHERE username = ? AND user_id <> ?",
                f.getUsername(), f.getUserId()) > 0) {
            throw new DuplicateUsernameException(f.getUsername());
        }
        if (f.getRole() == Role.UNDERGRADUATE) {
            if (queryNumber("SELECT COUNT(*) FROM undergraduates WHERE tg_no = ? AND user_id <> ?",
                    f.getTgNo(), f.getUserId()) > 0) {
                throw new DuplicateRecordException("TG number", f.getTgNo());
            }
        } else if (queryNumber("SELECT COUNT(*) FROM " + roleTable(f.getRole()) + " WHERE employee_no = ? AND user_id <> ?",
                f.getEmployeeNo(), f.getUserId()) > 0) {
            throw new DuplicateRecordException("Employee number", f.getEmployeeNo());
        }
    }

    /** Name of the role table for a staff role (fixed values only). */
    private static String roleTable(Role role) {
        return switch (role) {
            case ADMIN -> "admins";
            case LECTURER -> "lecturers";
            case TO -> "tech_officers";
            case UNDERGRADUATE -> "undergraduates";
        };
    }
}

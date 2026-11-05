package lk.ac.ruh.fot.ams.admin.dao;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

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
 * [M1 - ADMIN MODULE]
 *
 * Handles:
 * 1. Create users
 * 2. Update users
 * 3. Delete users
 * 4. Activate / Deactivate users
 * 5. Reset passwords
 * 6. Check duplicate records
 * 7. Save NIC and Date of Birth
 *
 * OOP Concepts:
 *
 * Inheritance:
 * Extends BaseDAO.
 *
 * Abstraction:
 * Implements CrudDAO<User>.
 *
 * Polymorphism:
 * Handles Admin, Lecturer, TechnicalOfficer and Undergraduate
 * objects through the common User type.
 *
 * Encapsulation:
 * Database operations are handled inside this class.
 *
 * Exception Handling:
 * Uses DatabaseException and ValidationException.
 */
public class AdminUserDAO extends BaseDAO implements CrudDAO<User> {

    public static final String DEFAULT_PASSWORD = "1234";

    private static final String ORDER =
            "ORDER BY FIELD(u.role,'ADMIN','LECTURER','TO','UNDERGRADUATE'), p.full_name";


    // ---------------------------------------------------------
    // CRUD OPERATIONS
    // ---------------------------------------------------------

    @Override
    public List<User> findAll() throws DatabaseException {

        return queryList(
                UserDAO.BASE_SELECT + ORDER,
                UserDAO::mapUser
        );
    }


    @Override
    public User findById(int userId) throws DatabaseException {

        return queryOne(
                UserDAO.BASE_SELECT + "WHERE u.user_id = ?",
                UserDAO::mapUser,
                userId
        );
    }


    /**
     * Inserts a user using the default password.
     */
    @Override
    public int insert(User user) throws DatabaseException {

        return create(user, DEFAULT_PASSWORD, false);
    }


    /**
     * Updates an existing user.
     *
     * This method is used by the common CrudDAO interface.
     */
    @Override
    public void update(User user) throws DatabaseException {

        inTransaction(connection -> {

            update(
                    connection,
                    "UPDATE users SET username = ? WHERE user_id = ?",
                    user.getUsername(),
                    user.getUserId()
            );

            update(
                    connection,
                    "UPDATE user_profiles SET "
                            + "full_name = ?, "
                            + "email = ?, "
                            + "phone = ?, "
                            + "address = ? "
                            + "WHERE user_id = ?",

                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getUserId()
            );

            updateRoleDetails(connection, user);
        });
    }


    /**
     * Updates an existing user including NIC and DOB.
     *
     * This method is used by the Admin UserForm.
     */
    private void update(User user, UserForm form)
            throws DatabaseException {

        inTransaction(connection -> {

            update(
                    connection,
                    "UPDATE users SET username = ? WHERE user_id = ?",
                    user.getUsername(),
                    user.getUserId()
            );

            update(
                    connection,
                    "UPDATE user_profiles SET "
                            + "full_name = ?, "
                            + "email = ?, "
                            + "phone = ?, "
                            + "address = ?, "
                            + "nic = COALESCE(?, nic), "
                            + "date_of_birth = COALESCE(?, date_of_birth) "
                            + "WHERE user_id = ?",

                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    form.getNic(),
                    form.getDateOfBirth(),
                    user.getUserId()
            );

            updateRoleDetails(connection, user);
        });
    }


    /**
     * Updates role-specific information.
     */
    private void updateRoleDetails(
            java.sql.Connection connection,
            User user
    ) throws java.sql.SQLException {

        if (user instanceof Undergraduate student) {

            update(
                    connection,
                    "UPDATE undergraduates SET "
                            + "tg_no = ?, "
                            + "batch = ?, "
                            + "level = ?, "
                            + "semester = ?, "
                            + "student_type = ?, "
                            + "department_id = ? "
                            + "WHERE user_id = ?",

                    student.getTgNo(),
                    student.getBatch(),
                    student.getLevel(),
                    student.getSemester(),
                    student.getStudentType(),
                    student.getDepartmentId(),
                    student.getUserId()
            );

        } else if (user instanceof Staff staff) {

            update(
                    connection,
                    "UPDATE " + roleTable(user.getRole())
                            + " SET employee_no = ?, department_id = ? "
                            + "WHERE user_id = ?",

                    staff.getEmployeeNo(),
                    staff.getDepartmentId(),
                    staff.getUserId()
            );
        }
    }


    /**
     * Deletes a user.
     */
    @Override
    public void delete(int userId) throws DatabaseException {

        executeUpdate(
                "DELETE FROM users WHERE user_id = ?",
                userId
        );
    }


    // ---------------------------------------------------------
    // ADMIN SAVE OPERATION
    // ---------------------------------------------------------

    /**
     * Validates and saves the user form.
     *
     * NIC and DOB are saved in user_profiles.
     */
    public int save(UserForm form) throws AppException {

        // Step 1: Validate form
        form.validate();

        // Step 2: Check duplicate records
        checkDuplicates(form);

        // Step 3: Create correct User subclass
        User user = form.toUser();

        // Step 4: Create or update
        if (form.isNew()) {

            return create(
                    user,
                    form.getPassword(),
                    form.isEnrollInCurrentCourses(),
                    form.getNic(),
                    form.getDateOfBirth()
            );

        } else {

            update(user, form);

            return user.getUserId();
        }
    }


    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------

    /**
     * General create method.
     *
     * Used by CrudDAO.insert().
     */
    public int create(
            User user,
            String plainPassword,
            boolean enrollInCourses
    ) throws DatabaseException {

        return create(
                user,
                plainPassword,
                enrollInCourses,
                null,
                null
        );
    }


    /**
     * Creates a user with NIC and DOB.
     *
     * All related database records are saved
     * inside one transaction.
     */
    private int create(
            User user,
            String plainPassword,
            boolean enrollInCourses,
            String nic,
            LocalDate dateOfBirth
    ) throws DatabaseException {

        // Course enrollment preparation
        List<Integer> courseIds = new ArrayList<>();

        int semesterOfCourses = 0;

        if (enrollInCourses && user instanceof Undergraduate student) {

            courseIds = findCourseIdsFor(
                    student.getLevel(),
                    student.getSemester(),
                    student.getDepartmentId()
            );

            semesterOfCourses = student.getSemester();
        }

        final List<Integer> courses = courseIds;
        final int semester = semesterOfCourses;

        final int[] newUserId = new int[1];

        // Database transaction
        inTransaction(connection -> {

            // 1. Create login account
            int userId = insert(
                    connection,
                    "INSERT INTO users "
                            + "(username, password, role, status) "
                            + "VALUES (?, ?, ?, ?)",

                    user.getUsername(),
                    PasswordUtil.hash(plainPassword),
                    user.getRole(),
                    user.getStatus() == null
                            ? "ACTIVE"
                            : user.getStatus()
            );


            // 2. Create user profile
            insert(
                    connection,
                    "INSERT INTO user_profiles "
                            + "(user_id, full_name, email, phone, address, nic, date_of_birth) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",

                    userId,
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    nic,
                    dateOfBirth
            );


            // 3. Create undergraduate details
            if (user instanceof Undergraduate student) {

                int studentId = insert(
                        connection,
                        "INSERT INTO undergraduates "
                                + "(user_id, tg_no, batch, level, semester, "
                                + "student_type, department_id) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)",

                        userId,
                        student.getTgNo(),
                        student.getBatch(),
                        student.getLevel(),
                        student.getSemester(),
                        student.getStudentType(),
                        student.getDepartmentId()
                );


                // 4. Enroll in current courses
                for (int courseId : courses) {

                    insert(
                            connection,
                            "INSERT INTO enrollments "
                                    + "(student_id, course_id, academic_year, semester, status) "
                                    + "VALUES (?, ?, ?, ?, 'ENROLLED') "
                                    + "ON DUPLICATE KEY UPDATE status = VALUES(status)",

                            studentId,
                            courseId,
                            AppConfig.CURRENT_ACADEMIC_YEAR,
                            semester
                    );
                }

            }

            // 5. Create staff details
            else if (user instanceof Staff staff) {

                insert(
                        connection,
                        "INSERT INTO " + roleTable(user.getRole())
                                + " (user_id, employee_no, department_id) "
                                + "VALUES (?, ?, ?)",

                        userId,
                        staff.getEmployeeNo(),
                        staff.getDepartmentId()
                );
            }

            newUserId[0] = userId;
        });

        return newUserId[0];
    }


    // ---------------------------------------------------------
    // DUPLICATE VALIDATION
    // ---------------------------------------------------------

    private void checkDuplicates(UserForm form)
            throws AppException {

        // Username duplicate check
        if (queryNumber(
                "SELECT COUNT(*) FROM users "
                        + "WHERE username = ? AND user_id <> ?",

                form.getUsername(),
                form.getUserId()
        ) > 0) {

            throw new DuplicateUsernameException(
                    form.getUsername()
            );
        }


        // NIC duplicate check
        if (form.getNic() != null && !form.getNic().isBlank()) {

            if (queryNumber(
                    "SELECT COUNT(*) FROM user_profiles "
                            + "WHERE nic = ? AND user_id <> ?",

                    form.getNic(),
                    form.getUserId()
            ) > 0) {

                throw new ValidationException(
                        "NIC",
                        "This NIC number is already registered."
                );
            }
        }


        // Undergraduate duplicate check
        if (form.getRole() == Role.UNDERGRADUATE) {

            if (queryNumber(
                    "SELECT COUNT(*) FROM undergraduates "
                            + "WHERE tg_no = ? AND user_id <> ?",

                    form.getTgNo(),
                    form.getUserId()
            ) > 0) {

                throw new DuplicateRecordException(
                        "TG number",
                        form.getTgNo()
                );
            }

        }

        // Staff duplicate check
        else {

            if (queryNumber(
                    "SELECT COUNT(*) FROM " + roleTable(form.getRole())
                            + " WHERE employee_no = ? AND user_id <> ?",

                    form.getEmployeeNo(),
                    form.getUserId()
            ) > 0) {

                throw new DuplicateRecordException(
                        "Employee number",
                        form.getEmployeeNo()
                );
            }
        }
    }


    // ---------------------------------------------------------
    // DELETE USER SAFETY
    // ---------------------------------------------------------

    /**
     * Prevents deleting the logged-in admin.
     * Prevents deleting the last admin account.
     */
    public void deleteUser(
            User target,
            User loggedIn
    ) throws AppException {

        if (target.getUserId() == loggedIn.getUserId()) {

            throw new ValidationException(
                    "You cannot delete your own account while you are logged in."
            );
        }

        if (target.getRole() == Role.ADMIN && countAdmins() <= 1) {

            throw new ValidationException(
                    "This is the last administrator account and cannot be deleted."
            );
        }

        delete(target.getUserId());
    }


    // ---------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // ---------------------------------------------------------

    public String toggleStatus(
            User target,
            User loggedIn
    ) throws AppException {

        if (target.getUserId() == loggedIn.getUserId()) {

            throw new ValidationException(
                    "You cannot deactivate your own account."
            );
        }

        String newStatus = target.isActive()
                ? "INACTIVE"
                : "ACTIVE";

        executeUpdate(
                "UPDATE users SET status = ? WHERE user_id = ?",
                newStatus,
                target.getUserId()
        );

        return newStatus;
    }


    // ---------------------------------------------------------
    // PASSWORD RESET
    // ---------------------------------------------------------

    public void resetPassword(
            int userId,
            String plainPassword
    ) throws DatabaseException {

        executeUpdate(
                "UPDATE users SET password = ? WHERE user_id = ?",
                PasswordUtil.hash(plainPassword),
                userId
        );
    }


    // ---------------------------------------------------------
    // SEARCH BY ROLE
    // ---------------------------------------------------------

    public List<User> findByRole(Role role)
            throws DatabaseException {

        return queryList(
                UserDAO.BASE_SELECT
                        + "WHERE u.role = ? ORDER BY p.full_name",

                UserDAO::mapUser,
                role
        );
    }


    // ---------------------------------------------------------
    // COUNT ADMINS
    // ---------------------------------------------------------

    public int countAdmins() throws DatabaseException {

        return (int) queryNumber(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'"
        );
    }


    // ---------------------------------------------------------
    // EMPLOYEE NUMBER GENERATION
    // ---------------------------------------------------------

    public String suggestEmployeeNo(Role role)
            throws DatabaseException {

        String prefix = switch (role) {

            case ADMIN -> "EMP/ADM/";

            case LECTURER -> "EMP/LEC/";

            default -> "EMP/TO/";
        };

        List<String> used = queryList(
                "SELECT employee_no FROM "
                        + roleTable(role)
                        + " WHERE employee_no LIKE ?",

                resultSet -> resultSet.getString(1),

                prefix + "%"
        );

        int max = 0;

        for (String number : used) {

            try {

                max = Math.max(
                        max,
                        Integer.parseInt(
                                number.substring(prefix.length())
                        )
                );

            } catch (NumberFormatException ignored) {

                // Ignore manually entered employee numbers.
            }
        }

        return prefix + String.format("%03d", max + 1);
    }


    // ---------------------------------------------------------
    // COURSE ENROLLMENT
    // ---------------------------------------------------------

    public List<Integer> findCourseIdsFor(
            int level,
            int semester,
            Integer departmentId
    ) throws DatabaseException {

        return queryList(
                "SELECT course_id FROM courses "
                        + "WHERE level = ? AND semester = ? "
                        + "AND (department_id = ? "
                        + "OR department_id IS NULL "
                        + "OR department_id IN "
                        + "(SELECT department_id FROM departments "
                        + "WHERE department_code = 'IDS')) "
                        + "ORDER BY course_code",

                resultSet -> resultSet.getInt(1),

                level,
                semester,
                departmentId
        );
    }


    // ---------------------------------------------------------
    // ROLE TABLE HELPER
    // ---------------------------------------------------------

    private static String roleTable(Role role) {

        return switch (role) {

            case ADMIN -> "admins";

            case LECTURER -> "lecturers";

            case TO -> "tech_officers";

            case UNDERGRADUATE -> "undergraduates";
        };
    }
}
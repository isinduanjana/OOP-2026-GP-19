package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Admin;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.Staff;
import lk.ac.ruh.fot.ams.model.TechnicalOfficer;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [COMMON PART] Reads users with their profile and role details, and saves
 * profile changes. Used by login and by every role's "My Profile" screen.
 *
 * mapUser() is a small FACTORY: it looks at users.role and creates the
 * correct subclass (Admin / Lecturer / TechnicalOfficer / Undergraduate)
 * -> polymorphism: the rest of the program just works with "User".
 */
public class UserDAO extends BaseDAO {

    /** One SELECT that joins users, profile, all role tables and department. */
    public static final String BASE_SELECT =
            "SELECT u.user_id, u.username, u.role, u.status, u.created_at, "
            + " p.full_name, p.email, p.phone, p.address, p.profile_picture, "
            + " a.admin_id, l.lecturer_id, t.to_id, "
            + " s.student_id, s.tg_no, s.batch, s.level, s.semester, s.student_type, "
            + " COALESCE(a.employee_no, l.employee_no, t.employee_no) AS employee_no, "
            + " d.department_id, d.department_name "
            + "FROM users u "
            + "LEFT JOIN user_profiles p ON p.user_id = u.user_id "
            + "LEFT JOIN admins a ON a.user_id = u.user_id "
            + "LEFT JOIN lecturers l ON l.user_id = u.user_id "
            + "LEFT JOIN tech_officers t ON t.user_id = u.user_id "
            + "LEFT JOIN undergraduates s ON s.user_id = u.user_id "
            + "LEFT JOIN departments d ON d.department_id = "
            + "     COALESCE(a.department_id, l.department_id, t.department_id, s.department_id) ";

    /** Returns the user if username + password hash match, otherwise null. */
    public User findByCredentials(String username, String passwordHash) throws DatabaseException {
        return queryOne(BASE_SELECT + "WHERE u.username = ? AND u.password = ?",
                UserDAO::mapUser, username, passwordHash);
    }

    public User findById(int userId) throws DatabaseException {
        return queryOne(BASE_SELECT + "WHERE u.user_id = ?", UserDAO::mapUser, userId);
    }

    public List<User> findAll() throws DatabaseException {
        return queryList(BASE_SELECT + "ORDER BY FIELD(u.role,'ADMIN','LECTURER','TO','UNDERGRADUATE'), p.full_name",
                UserDAO::mapUser);
    }

    public List<User> findByRole(Role role) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE u.role = ? ORDER BY p.full_name", UserDAO::mapUser, role);
    }

    public boolean usernameExists(String username) throws DatabaseException {
        return queryNumber("SELECT COUNT(*) FROM users WHERE username = ?", username) > 0;
    }

    /** Saves the editable profile fields (name, contact details, picture). */
    public void updateProfile(User u) throws DatabaseException {
        executeUpdate("UPDATE user_profiles SET full_name = ?, email = ?, phone = ?, address = ?, profile_picture = ? "
                        + "WHERE user_id = ?",
                u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getProfilePicture(), u.getUserId());
    }

    public boolean verifyPassword(int userId, String passwordHash) throws DatabaseException {
        return queryNumber("SELECT COUNT(*) FROM users WHERE user_id = ? AND password = ?", userId, passwordHash) > 0;
    }

    public void changePassword(int userId, String passwordHash) throws DatabaseException {
        executeUpdate("UPDATE users SET password = ? WHERE user_id = ?", passwordHash, userId);
    }

    /** Factory method: builds the right User subclass for one row. */
    public static User mapUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user;
        switch (role) {
            case ADMIN -> {
                Admin a = new Admin();
                a.setAdminId(rs.getInt("admin_id"));
                user = a;
            }
            case LECTURER -> {
                Lecturer l = new Lecturer();
                l.setLecturerId(rs.getInt("lecturer_id"));
                user = l;
            }
            case TO -> {
                TechnicalOfficer t = new TechnicalOfficer();
                t.setToId(rs.getInt("to_id"));
                user = t;
            }
            default -> {
                Undergraduate s = new Undergraduate();
                s.setStudentId(rs.getInt("student_id"));
                s.setTgNo(rs.getString("tg_no"));
                s.setBatch(rs.getString("batch"));
                s.setLevel(rs.getInt("level"));
                s.setSemester(rs.getInt("semester"));
                String type = rs.getString("student_type");
                s.setStudentType(type == null ? StudentType.NORMAL : StudentType.valueOf(type));
                user = s;
            }
        }
        if (user instanceof Staff staff) {
            staff.setEmployeeNo(rs.getString("employee_no"));
        }
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(getDateTime(rs, "created_at"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setProfilePicture(rs.getString("profile_picture"));
        user.setDepartmentId(getInteger(rs, "department_id"));
        user.setDepartmentName(rs.getString("department_name"));
        return user;
    }
}

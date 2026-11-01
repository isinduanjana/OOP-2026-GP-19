package lk.ac.ruh.fot.ams.admin.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [M1 - ADMIN MODULE] Counts shown on the admin home page (stat cards + charts).
 *
 * OOP: Inheritance - only uses the inherited BaseDAO helpers (queryNumber / queryList).
 */
public class AdminStatsDAO extends BaseDAO {

    public int countUsers() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM users");
    }

    public int countByRole(Role role) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM users WHERE role = ?", role);
    }

    public int countInactiveUsers() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM users WHERE status <> 'ACTIVE'");
    }

    public int countCourses() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM courses");
    }

    public int countCurrentSemesterCourses(int level, int semester) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM courses WHERE level = ? AND semester = ?", level, semester);
    }

    public int countActiveNotices() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM notices WHERE status = 'ACTIVE'");
    }

    public int countTimetableSlots() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM timetables");
    }

    /** Undergraduates per type (Normal / Repeat / Batch missed) - keeps enum order. */
    public Map<StudentType, Integer> studentsByType() throws DatabaseException {
        Map<StudentType, Integer> map = new LinkedHashMap<>();
        for (StudentType t : StudentType.values()) {
            map.put(t, 0);
        }
        queryList("SELECT student_type, COUNT(*) FROM undergraduates GROUP BY student_type", rs -> {
            map.put(StudentType.valueOf(rs.getString(1)), rs.getInt(2));
            return null;
        });
        return map;
    }

    /** Users per role, in enum order. */
    public Map<Role, Integer> usersByRole() throws DatabaseException {
        Map<Role, Integer> map = new LinkedHashMap<>();
        for (Role r : Role.values()) {
            map.put(r, 0);
        }
        queryList("SELECT role, COUNT(*) FROM users GROUP BY role", rs -> {
            map.put(Role.valueOf(rs.getString(1)), rs.getInt(2));
            return null;
        });
        return map;
    }

    /** course_id -> number of students enrolled this academic year (not withdrawn). */
    public Map<Integer, Integer> enrolledPerCourse() throws DatabaseException {
        Map<Integer, Integer> map = new HashMap<>();
        queryList("SELECT course_id, COUNT(*) FROM enrollments WHERE academic_year = ? AND status <> 'WITHDRAWN' "
                + "GROUP BY course_id", rs -> {
                    map.put(rs.getInt(1), rs.getInt(2));
                    return null;
                }, AppConfig.CURRENT_ACADEMIC_YEAR);
        return map;
    }

    /** Course count per department code (ICT, ET, ...). */
    public Map<String, Integer> coursesByDepartment() throws DatabaseException {
        Map<String, Integer> map = new LinkedHashMap<>();
        queryList("SELECT d.department_code, COUNT(c.course_id) FROM departments d "
                + "LEFT JOIN courses c ON c.department_id = d.department_id "
                + "GROUP BY d.department_id, d.department_code ORDER BY d.department_id", rs -> {
                    map.put(rs.getString(1), rs.getInt(2));
                    return null;
                });
        return map;
    }
}

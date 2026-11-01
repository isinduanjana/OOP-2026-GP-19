package lk.ac.ruh.fot.ams.admin.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.admin.DuplicateRecordException;
import lk.ac.ruh.fot.ams.admin.model.Enrollment;
import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [M1 - ADMIN MODULE] Enrols undergraduates in courses / withdraws them.
 * The enrolment table decides which students M2, M3 and M4 see for a course.
 *
 * OOP: Inheritance (BaseDAO), Abstraction/Generics (CrudDAO&lt;Enrollment&gt;), RowMapper lambda.
 */
public class AdminEnrollmentDAO extends BaseDAO implements CrudDAO<Enrollment> {

    private static final String SELECT =
            "SELECT e.*, s.tg_no, s.student_type, p.full_name FROM enrollments e "
            + "JOIN undergraduates s ON s.student_id = e.student_id "
            + "LEFT JOIN user_profiles p ON p.user_id = s.user_id ";

    @Override
    public List<Enrollment> findAll() throws DatabaseException {
        return queryList(SELECT + "ORDER BY e.academic_year DESC, s.tg_no", AdminEnrollmentDAO::map);
    }

    @Override
    public Enrollment findById(int id) throws DatabaseException {
        return queryOne(SELECT + "WHERE e.enrollment_id = ?", AdminEnrollmentDAO::map, id);
    }

    public List<Enrollment> findByCourse(int courseId) throws DatabaseException {
        return queryList(SELECT + "WHERE e.course_id = ? ORDER BY e.academic_year DESC, s.tg_no",
                AdminEnrollmentDAO::map, courseId);
    }

    @Override
    public int insert(Enrollment e) throws DatabaseException {
        return executeInsert("INSERT INTO enrollments (student_id, course_id, academic_year, semester, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                e.getStudentId(), e.getCourseId(), e.getAcademicYear(), e.getSemester(),
                e.getStatus() == null ? "ENROLLED" : e.getStatus());
    }

    @Override
    public void update(Enrollment e) throws DatabaseException {
        executeUpdate("UPDATE enrollments SET status = ? WHERE enrollment_id = ?", e.getStatus(), e.getEnrollmentId());
    }

    @Override
    public void delete(int id) throws DatabaseException {
        executeUpdate("DELETE FROM enrollments WHERE enrollment_id = ?", id);
    }

    /** Enrols a student in a course for the current academic year. */
    public void enroll(Undergraduate s, Course c) throws AppException {
        if (queryNumber("SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND academic_year = ? "
                + "AND status <> 'WITHDRAWN'", s.getStudentId(), c.getCourseId(), AppConfig.CURRENT_ACADEMIC_YEAR) > 0) {
            throw new DuplicateRecordException("Enrolment", s.getTgNo(),
                    s.getTgNo() + " is already enrolled in " + c.getCourseCode() + " for " + AppConfig.CURRENT_ACADEMIC_YEAR + ".");
        }
        // a withdrawn row of this year is simply switched back to ENROLLED
        executeUpdate("INSERT INTO enrollments (student_id, course_id, academic_year, semester, status) "
                        + "VALUES (?, ?, ?, ?, 'ENROLLED') ON DUPLICATE KEY UPDATE status = VALUES(status)",
                s.getStudentId(), c.getCourseId(), AppConfig.CURRENT_ACADEMIC_YEAR, c.getSemester());
    }

    public void setStatus(int enrollmentId, String status) throws DatabaseException {
        executeUpdate("UPDATE enrollments SET status = ? WHERE enrollment_id = ?", status, enrollmentId);
    }

    /** Undergraduates who are NOT enrolled in the course this year (for the "add" combo). */
    public List<Undergraduate> findNotEnrolled(int courseId) throws DatabaseException {
        List<User> users = queryList(UserDAO.BASE_SELECT + "WHERE u.role = 'UNDERGRADUATE' AND s.student_id NOT IN "
                        + "(SELECT student_id FROM enrollments WHERE course_id = ? AND academic_year = ? "
                        + " AND status <> 'WITHDRAWN') ORDER BY s.tg_no",
                UserDAO::mapUser, courseId, AppConfig.CURRENT_ACADEMIC_YEAR);
        List<Undergraduate> list = new ArrayList<>();
        for (User u : users) {
            list.add((Undergraduate) u);       // safe: the query only returns undergraduates
        }
        return list;
    }

    private static Enrollment map(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setTgNo(rs.getString("tg_no"));
        e.setStudentName(rs.getString("full_name"));
        e.setStudentType(StudentType.valueOf(rs.getString("student_type")));
        e.setAcademicYear(rs.getString("academic_year"));
        e.setSemester(rs.getInt("semester"));
        e.setStatus(rs.getString("status"));
        return e;
    }
}

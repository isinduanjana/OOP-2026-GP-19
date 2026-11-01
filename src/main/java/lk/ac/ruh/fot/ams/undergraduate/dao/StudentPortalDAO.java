package lk.ac.ruh.fot.ams.undergraduate.dao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.TimetableDAO;
import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.undergraduate.StudentDataNotFoundException;

/**
 * [M4 - UNDERGRADUATE MODULE] Small read-only queries used only by the
 * Student Portal (home page counters, profile details, today's classes).
 *
 * OOP: Inheritance - "extends BaseDAO" from common, so every method is one
 * line using the inherited queryNumber(...) / queryList(...) helpers (they use
 * PreparedStatements and turn SQLException into DatabaseException).
 * Exception handling - requireStudent(...) throws our own
 * StudentDataNotFoundException when the student row is missing.
 */
public class StudentPortalDAO extends BaseDAO {

    /** Same filter the common DAOs use for "courses of this academic year". */
    private static final String CURRENT_ENROLMENT =
            "FROM enrollments e JOIN courses c ON c.course_id = e.course_id "
            + "WHERE e.student_id = ? AND e.academic_year = ? AND e.status <> 'WITHDRAWN'";

    /**
     * Re-reads the student (re-uses common UserDAO.BASE_SELECT + mapUser factory).
     * @throws StudentDataNotFoundException if the row was deleted
     */
    public Undergraduate requireStudent(int studentId) throws DatabaseException, StudentDataNotFoundException {
        User u = queryOne(UserDAO.BASE_SELECT + "WHERE s.student_id = ?", UserDAO::mapUser, studentId);
        if (!(u instanceof Undergraduate s)) {
            throw new StudentDataNotFoundException(studentId);
        }
        return s;
    }

    /** Number of this student's medicals per status (APPROVED / PENDING / REJECTED). */
    public Map<MedicalStatus, Integer> countMedicalsByStatus(int studentId) throws DatabaseException {
        Map<MedicalStatus, Integer> map = new EnumMap<>(MedicalStatus.class);
        for (MedicalStatus st : MedicalStatus.values()) map.put(st, 0);
        queryList("SELECT status, COUNT(*) FROM medicals WHERE student_id = ? GROUP BY status", rs -> {
            map.put(MedicalStatus.valueOf(rs.getString(1)), rs.getInt(2));
            return null;
        }, studentId);
        return map;
    }

    /** Total credits of the courses the student follows this academic year. */
    public int countEnrolledCredits(int studentId) throws DatabaseException {
        return (int) queryNumber("SELECT COALESCE(SUM(c.credits), 0) " + CURRENT_ENROLMENT,
                studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    /** Number of courses the student follows this academic year. */
    public int countEnrolledCourses(int studentId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) " + CURRENT_ENROLMENT, studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    /** Number of course materials available for the student's current courses. */
    public int countAvailableMaterials(int studentId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM course_materials m WHERE m.course_id IN "
                        + "(SELECT e.course_id " + CURRENT_ENROLMENT + ")",
                studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    /**
     * Classes of the student on one weekday (e.g. "MONDAY"), ordered by time.
     * Re-uses the common TimetableDAO.BASE_SELECT and its mapEntry row mapper.
     */
    public List<TimetableEntry> findClassesOnDay(int studentId, String day) throws DatabaseException {
        return queryList(TimetableDAO.BASE_SELECT
                        + "WHERE t.day = ? AND t.course_id IN (SELECT e.course_id " + CURRENT_ENROLMENT + ") "
                        + "ORDER BY t.start_time",
                TimetableDAO::mapEntry, day, studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }
}

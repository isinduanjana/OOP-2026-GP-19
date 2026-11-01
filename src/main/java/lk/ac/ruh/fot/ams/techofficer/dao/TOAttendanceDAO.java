package lk.ac.ruh.fot.ams.techofficer.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.AttendanceDAO;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.AttendanceSession;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.techofficer.SessionLimitExceededException;
import lk.ac.ruh.fot.ams.techofficer.model.RegisterRow;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Adds and maintains attendance (the common
 * AttendanceDAO is read-only, so all WRITES to attendance_sessions / attendance
 * happen here).
 *
 * Connection to common: "extends BaseDAO" -> inherits queryList / queryOne /
 * queryNumber / executeUpdate / executeInsert / inTransaction, so this class has
 * no JDBC boiler-plate; reuses AttendanceDAO.mapSession and StudentDAO.findByCourse.
 *
 * OOP: Inheritance, Encapsulation (validation rules are private helpers),
 * Exception handling (custom SessionLimitExceededException), JDBC transactions.
 */
public class TOAttendanceDAO extends BaseDAO {

    /**
     * Picks THE medical that covers one attendance row (same student, same date,
     * same course or a whole-day medical). Approved first, then pending, then rejected;
     * a course-specific medical wins over a whole-day one. Used by both M3 DAOs.
     */
    static final String BEST_MEDICAL_SUBQUERY =
            "(SELECT m.medical_id FROM medicals m WHERE m.student_id = a.student_id "
            + " AND m.medical_date = ses.session_date AND (m.course_id IS NULL OR m.course_id = ses.course_id) "
            + " ORDER BY FIELD(m.status, 'APPROVED', 'PENDING', 'REJECTED'), m.course_id IS NULL, m.medical_id LIMIT 1)";

    private static final String SESSION_SELECT = "SELECT ses.*, c.course_code, "
            + " (SELECT COUNT(*) FROM attendance a WHERE a.session_id = ses.session_id AND a.status = 'PRESENT') AS present_count, "
            + " (SELECT COUNT(*) FROM attendance a WHERE a.session_id = ses.session_id AND a.status = 'ABSENT') AS absent_count "
            + "FROM attendance_sessions ses JOIN courses c ON c.course_id = ses.course_id ";

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();   // common read-only DAO
    private final StudentDAO studentDAO = new StudentDAO();

    // ================================================================= sessions
    public List<AttendanceSession> findSessions(int courseId, Component component) throws DatabaseException {
        return attendanceDAO.findSessions(courseId, component);
    }

    public AttendanceSession findSession(int sessionId) throws DatabaseException {
        return queryOne(SESSION_SELECT + "WHERE ses.session_id = ?", AttendanceDAO::mapSession, sessionId);
    }

    public int countSessions(int courseId, Component component) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM attendance_sessions WHERE course_id = ? AND component = ?",
                courseId, component);
    }

    /**
     * Smallest free session number (1..15) of a course component.
     * @throws SessionLimitExceededException when all 15 sessions already exist
     */
    public int nextSessionNumber(Course course, Component component) throws AppException {
        Set<Integer> used = new HashSet<>(queryList(
                "SELECT session_number FROM attendance_sessions WHERE course_id = ? AND component = ?",
                rs -> rs.getInt(1), course.getCourseId(), component));
        for (int n = 1; n <= AppConfig.SESSIONS_PER_COMPONENT; n++) {
            if (!used.contains(n)) return n;
        }
        throw new SessionLimitExceededException(course.getCourseCode(), component);
    }

    /** Creates a session after checking the handout's 15-session rule. Returns the new id. */
    public int createSession(Course course, AttendanceSession s) throws AppException {
        checkComponent(course, s.getComponent());
        checkSessionNumber(s.getSessionNumber());
        checkDate(s.getSessionDate());
        checkHours(s.getHours());
        if (countSessions(course.getCourseId(), s.getComponent()) >= AppConfig.SESSIONS_PER_COMPONENT) {
            throw new SessionLimitExceededException(course.getCourseCode(), s.getComponent());
        }
        if (queryNumber("SELECT COUNT(*) FROM attendance_sessions WHERE course_id = ? AND component = ? AND session_number = ?",
                course.getCourseId(), s.getComponent(), s.getSessionNumber()) > 0) {
            throw new ValidationException("Session number", s.getComponent() + " session #" + s.getSessionNumber()
                    + " of " + course.getCourseCode() + " already exists. Choose another number.");
        }
        return executeInsert("INSERT INTO attendance_sessions (course_id, session_date, component, session_number, "
                        + "total_sessions, hours) VALUES (?, ?, ?, ?, ?, ?)",
                course.getCourseId(), s.getSessionDate(), s.getComponent(), s.getSessionNumber(),
                AppConfig.SESSIONS_PER_COMPONENT, s.getHours());
    }

    /** Changes date / hours of a session and re-links medicals (they depend on the date). */
    public void updateSession(AttendanceSession s) throws AppException {
        checkDate(s.getSessionDate());
        checkHours(s.getHours());
        inTransaction(c -> {
            update(c, "UPDATE attendance_sessions SET session_date = ?, hours = ? WHERE session_id = ?",
                    s.getSessionDate(), s.getHours(), s.getSessionId());
            relinkSession(c, s.getSessionId());
        });
    }

    /** Deletes a session; its attendance rows go with it (ON DELETE CASCADE). */
    public void deleteSession(int sessionId) throws DatabaseException {
        executeUpdate("DELETE FROM attendance_sessions WHERE session_id = ?", sessionId);
    }

    // ================================================================= register
    /** Every enrolled student of the course + what is saved for this session. */
    public List<RegisterRow> findRegister(AttendanceSession session) throws DatabaseException {
        // saved rows of this session: studentId -> {status, linked medical status}
        Map<Integer, String[]> saved = new HashMap<>();
        queryList("SELECT a.student_id, a.status, m.status AS med_status FROM attendance a "
                + "LEFT JOIN medicals m ON m.medical_id = a.medical_id WHERE a.session_id = ?", rs -> {
                    saved.put(rs.getInt("student_id"), new String[]{rs.getString("status"), rs.getString("med_status")});
                    return null;
                }, session.getSessionId());

        // medicals submitted for that day (best one per student comes first)
        Map<Integer, MedicalStatus> dayMedicals = new HashMap<>();
        queryList("SELECT m.student_id, m.status FROM medicals m WHERE m.medical_date = ? "
                + "AND (m.course_id IS NULL OR m.course_id = ?) "
                + "ORDER BY FIELD(m.status, 'APPROVED', 'PENDING', 'REJECTED'), m.course_id IS NULL, m.medical_id", rs -> {
                    dayMedicals.putIfAbsent(rs.getInt("student_id"), MedicalStatus.valueOf(rs.getString("status")));
                    return null;
                }, session.getSessionDate(), session.getCourseId());

        List<RegisterRow> rows = new ArrayList<>();
        for (Undergraduate u : studentDAO.findByCourse(session.getCourseId())) {
            RegisterRow r = new RegisterRow();
            r.setStudentId(u.getStudentId());
            r.setTgNo(u.getTgNo());
            r.setStudentName(u.getFullName());
            r.setStudentType(u.getStudentType());
            String[] s = saved.get(u.getStudentId());
            if (s != null) {
                r.setPresent("PRESENT".equals(s[0]));
                r.setLinkedMedical(s[1] == null ? null : MedicalStatus.valueOf(s[1]));
            }
            r.setDayMedical(dayMedicals.get(u.getStudentId()));
            rows.add(r);
        }
        return rows;
    }

    /**
     * Saves the whole register in ONE transaction (all rows or none).
     * Absent students are automatically linked to a medical of that day;
     * present students never keep a medical link.
     * @param presentByStudent studentId -> true (present) / false (absent)
     */
    public void saveRegister(int sessionId, Map<Integer, Boolean> presentByStudent) throws DatabaseException {
        inTransaction(c -> {
            for (Map.Entry<Integer, Boolean> e : presentByStudent.entrySet()) {
                update(c, "INSERT INTO attendance (session_id, student_id, status) VALUES (?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE status = VALUES(status)",
                        sessionId, e.getKey(), Boolean.TRUE.equals(e.getValue()) ? "PRESENT" : "ABSENT");
            }
            relinkSession(c, sessionId);
        });
    }

    // ================================================================= medical links (shared with TOMedicalDAO)
    /** Re-computes attendance.medical_id for every row of one session. */
    static void relinkSession(Connection c, int sessionId) throws SQLException {
        update(c, "UPDATE attendance a JOIN attendance_sessions ses ON ses.session_id = a.session_id "
                + "SET a.medical_id = CASE WHEN a.status = 'ABSENT' THEN " + BEST_MEDICAL_SUBQUERY + " ELSE NULL END "
                + "WHERE a.session_id = ?", sessionId);
    }

    /** Re-computes attendance.medical_id for all sessions of one student on one date. */
    static void relinkStudentDay(Connection c, int studentId, LocalDate date) throws SQLException {
        update(c, "UPDATE attendance a JOIN attendance_sessions ses ON ses.session_id = a.session_id "
                + "SET a.medical_id = CASE WHEN a.status = 'ABSENT' THEN " + BEST_MEDICAL_SUBQUERY + " ELSE NULL END "
                + "WHERE a.student_id = ? AND ses.session_date = ?", studentId, date);
    }

    // ================================================================= dashboard / profile numbers
    /** Sessions recorded for current level/semester courses. */
    public int countCurrentSemesterSessions() throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM attendance_sessions ses JOIN courses c ON c.course_id = ses.course_id "
                + "WHERE c.level = ? AND c.semester = ?", AppConfig.CURRENT_LEVEL, AppConfig.CURRENT_SEMESTER);
    }

    /** Planned sessions (15 per component) of current courses. */
    public int countPlannedSessions(List<Course> courses) {
        int n = 0;
        for (Course c : courses) {
            if (c.hasTheory()) n += AppConfig.SESSIONS_PER_COMPONENT;
            if (c.hasPractical()) n += AppConfig.SESSIONS_PER_COMPONENT;
        }
        return n;
    }

    /** Latest session (today's if one exists), or null. */
    public AttendanceSession findLatestSession() throws DatabaseException {
        return queryOne(SESSION_SELECT
                        + "WHERE c.level = ? AND c.semester = ? AND ses.session_date <= ? "
                        + "ORDER BY ses.session_date DESC, ses.session_id DESC LIMIT 1",
                AttendanceDAO::mapSession, AppConfig.CURRENT_LEVEL, AppConfig.CURRENT_SEMESTER, LocalDate.now());
    }

    /** Sessions of courses in a department (null = all departments). */
    public int countSessionsOfDepartment(Integer departmentId) throws DatabaseException {
        if (departmentId == null) {
            return (int) queryNumber("SELECT COUNT(*) FROM attendance_sessions");
        }
        return (int) queryNumber("SELECT COUNT(*) FROM attendance_sessions ses JOIN courses c ON c.course_id = ses.course_id "
                + "WHERE c.department_id = ?", departmentId);
    }

    // ================================================================= validation helpers (encapsulated)
    private static void checkComponent(Course course, Component component) throws ValidationException {
        if (component == null) throw new ValidationException("Component", "Please choose theory or practical.");
        if (component == Component.THEORY && !course.hasTheory()
                || component == Component.PRACTICAL && !course.hasPractical()) {
            throw new ValidationException("Component", course.getCourseCode() + " has no " + component + " component.");
        }
    }

    private static void checkSessionNumber(int n) throws SessionLimitExceededException {
        if (n < 1 || n > AppConfig.SESSIONS_PER_COMPONENT) {
            throw new SessionLimitExceededException(n);
        }
    }

    private static void checkDate(LocalDate d) throws ValidationException {
        if (d == null) throw new ValidationException("Date", "Session date is required.");
        if (d.isAfter(LocalDate.now())) {
            throw new ValidationException("Date", "Attendance cannot be recorded for a future date (" + d + ").");
        }
    }

    private static void checkHours(int hours) throws ValidationException {
        if (hours < 1 || hours > 8) {
            throw new ValidationException("Hours", "Hours must be between 1 and 8 (normally 1 session = "
                    + AppConfig.HOURS_PER_SESSION + " hours).");
        }
    }
}

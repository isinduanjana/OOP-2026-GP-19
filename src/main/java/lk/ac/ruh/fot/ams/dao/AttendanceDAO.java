package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.AttendanceSession;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.model.report.AttendanceRecord;
import lk.ac.ruh.fot.ams.model.report.ComponentAttendance;

/**
 * [COMMON PART] READ-ONLY attendance queries used for reports
 * (Lecturer M2, Technical Officer M3, Undergraduate M4).
 * Adding / editing attendance is done by M3's TOAttendanceDAO.
 */
public class AttendanceDAO extends BaseDAO {

    /** Sessions of a course (optionally only one component) with present/absent counts. */
    public List<AttendanceSession> findSessions(int courseId, Component component) throws DatabaseException {
        String sql = "SELECT ses.*, c.course_code, "
                + " (SELECT COUNT(*) FROM attendance a WHERE a.session_id = ses.session_id AND a.status = 'PRESENT') AS present_count, "
                + " (SELECT COUNT(*) FROM attendance a WHERE a.session_id = ses.session_id AND a.status = 'ABSENT') AS absent_count "
                + "FROM attendance_sessions ses JOIN courses c ON c.course_id = ses.course_id "
                + "WHERE ses.course_id = ? " + (component == null ? "" : "AND ses.component = ? ")
                + "ORDER BY ses.component, ses.session_number";
        return component == null
                ? queryList(sql, AttendanceDAO::mapSession, courseId)
                : queryList(sql, AttendanceDAO::mapSession, courseId, component);
    }

    /**
     * Per-student attendance counts of a course.
     * @return studentId -> {theory stats, practical stats}
     */
    public Map<Integer, Map<Component, ComponentAttendance>> findCourseStats(int courseId) throws DatabaseException {
        // 1) how many sessions were held per component
        Map<Component, int[]> held = new HashMap<>();   // component -> {count, hours}
        queryList("SELECT component, COUNT(*) AS cnt, MAX(hours) AS hrs FROM attendance_sessions "
                + "WHERE course_id = ? GROUP BY component", rs -> {
                    held.put(Component.valueOf(rs.getString("component")),
                            new int[]{rs.getInt("cnt"), rs.getInt("hrs")});
                    return null;
                }, courseId);

        // 2) each student's present / medical counts
        Map<Integer, Map<Component, ComponentAttendance>> result = new HashMap<>();
        queryList("SELECT a.student_id, ses.component, "
                + " SUM(a.status = 'PRESENT') AS present, "
                + " SUM(a.status = 'ABSENT' AND m.status = 'APPROVED') AS med_ok, "
                + " SUM(a.status = 'ABSENT' AND m.medical_id IS NOT NULL AND m.status <> 'APPROVED') AS med_other "
                + "FROM attendance a JOIN attendance_sessions ses ON ses.session_id = a.session_id "
                + "LEFT JOIN medicals m ON m.medical_id = a.medical_id "
                + "WHERE ses.course_id = ? GROUP BY a.student_id, ses.component", rs -> {
                    Component comp = Component.valueOf(rs.getString("component"));
                    ComponentAttendance ca = new ComponentAttendance(comp);
                    int[] h = held.getOrDefault(comp, new int[]{0, 2});
                    ca.setSessionsHeld(h[0]);
                    ca.setHoursPerSession(h[1] == 0 ? 2 : h[1]);
                    ca.setPresent(rs.getInt("present"));
                    ca.setMedicalApproved(rs.getInt("med_ok"));
                    ca.setMedicalPending(rs.getInt("med_other"));
                    result.computeIfAbsent(rs.getInt("student_id"), k -> new HashMap<>()).put(comp, ca);
                    return null;
                }, courseId);

        // remember the held counts for students with no rows yet (key -1)
        Map<Component, ComponentAttendance> empty = new HashMap<>();
        for (Map.Entry<Component, int[]> e : held.entrySet()) {
            ComponentAttendance ca = new ComponentAttendance(e.getKey());
            ca.setSessionsHeld(e.getValue()[0]);
            ca.setHoursPerSession(e.getValue()[1] == 0 ? 2 : e.getValue()[1]);
            empty.put(e.getKey(), ca);
        }
        result.put(-1, empty);
        return result;
    }

    /** Day-by-day records of one student in one course. */
    public List<AttendanceRecord> findStudentRecords(int studentId, int courseId) throws DatabaseException {
        return queryList("SELECT a.attendance_id, a.session_id, a.student_id, a.status, a.medical_id, m.status AS med_status, "
                        + " ses.session_date, ses.component, ses.session_number "
                        + "FROM attendance a JOIN attendance_sessions ses ON ses.session_id = a.session_id "
                        + "LEFT JOIN medicals m ON m.medical_id = a.medical_id "
                        + "WHERE a.student_id = ? AND ses.course_id = ? "
                        + "ORDER BY ses.component, ses.session_number",
                AttendanceDAO::mapRecord, studentId, courseId);
    }

    public static AttendanceSession mapSession(ResultSet rs) throws SQLException {
        AttendanceSession s = new AttendanceSession();
        s.setSessionId(rs.getInt("session_id"));
        s.setCourseId(rs.getInt("course_id"));
        s.setCourseCode(rs.getString("course_code"));
        s.setSessionDate(getDate(rs, "session_date"));
        s.setComponent(Component.valueOf(rs.getString("component")));
        s.setSessionNumber(rs.getInt("session_number"));
        s.setTotalSessions(rs.getInt("total_sessions"));
        s.setHours(rs.getInt("hours"));
        s.setPresentCount(rs.getInt("present_count"));
        s.setAbsentCount(rs.getInt("absent_count"));
        return s;
    }

    public static AttendanceRecord mapRecord(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setAttendanceId(rs.getInt("attendance_id"));
        r.setSessionId(rs.getInt("session_id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setStatus(rs.getString("status"));
        r.setMedicalId(getInteger(rs, "medical_id"));
        String ms = rs.getString("med_status");
        r.setMedicalStatus(ms == null ? null : MedicalStatus.valueOf(ms));
        r.setSessionDate(getDate(rs, "session_date"));
        r.setComponent(Component.valueOf(rs.getString("component")));
        r.setSessionNumber(rs.getInt("session_number"));
        return r;
    }
}

package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/**
 * [COMMON PART] READ-ONLY medical queries (M2 lecturer view, M4 student view).
 * Adding / approving medicals is done by M3's TOMedicalDAO.
 */
public class MedicalDAO extends BaseDAO {

    public static final String BASE_SELECT =
            "SELECT m.*, s.tg_no, sp.full_name AS student_name, c.course_code, ap.full_name AS approved_by_name "
            + "FROM medicals m JOIN undergraduates s ON s.student_id = m.student_id "
            + "JOIN user_profiles sp ON sp.user_id = s.user_id "
            + "LEFT JOIN courses c ON c.course_id = m.course_id "
            + "LEFT JOIN user_profiles ap ON ap.user_id = m.approved_by ";

    public List<Medical> findAll() throws DatabaseException {
        return queryList(BASE_SELECT + "ORDER BY m.medical_date DESC, s.tg_no", MedicalDAO::mapMedical);
    }

    public List<Medical> findByStudent(int studentId) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE m.student_id = ? ORDER BY m.medical_date DESC",
                MedicalDAO::mapMedical, studentId);
    }

    /** Medicals for a course (including "whole day" medicals of its students). */
    public List<Medical> findByCourse(int courseId) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE m.course_id = ? OR (m.course_id IS NULL AND m.student_id IN "
                        + "(SELECT student_id FROM enrollments WHERE course_id = ? AND academic_year = ?)) "
                        + "ORDER BY m.medical_date DESC",
                MedicalDAO::mapMedical, courseId, courseId, lk.ac.ruh.fot.ams.config.AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    public static Medical mapMedical(ResultSet rs) throws SQLException {
        Medical m = new Medical();
        m.setMedicalId(rs.getInt("medical_id"));
        m.setStudentId(rs.getInt("student_id"));
        m.setTgNo(rs.getString("tg_no"));
        m.setStudentName(rs.getString("student_name"));
        m.setCourseId(getInteger(rs, "course_id"));
        m.setCourseCode(rs.getString("course_code"));
        m.setMedicalDate(getDate(rs, "medical_date"));
        m.setFromDate(getDate(rs, "from_date"));
        m.setToDate(getDate(rs, "to_date"));
        m.setSubmittedAt(getDateTime(rs, "created_at"));
        m.setReason(rs.getString("reason"));
        m.setDocumentPath(rs.getString("document_path"));
        m.setStatus(MedicalStatus.valueOf(rs.getString("status")));
        m.setApprovedBy(getInteger(rs, "approved_by"));
        m.setApprovedByName(rs.getString("approved_by_name"));
        m.setApprovedAt(getDateTime(rs, "approved_at"));
        return m;
    }
}

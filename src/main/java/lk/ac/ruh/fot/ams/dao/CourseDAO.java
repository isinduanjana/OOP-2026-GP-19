package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.CourseMaterial;

/** [COMMON PART] Read queries for courses and course materials. */
public class CourseDAO extends BaseDAO {

    public static final String BASE_SELECT =
            "SELECT c.*, d.department_name, "
            + " (SELECT GROUP_CONCAT(p.full_name ORDER BY p.full_name SEPARATOR ', ') "
            + "    FROM course_lecturers cl JOIN lecturers l ON l.lecturer_id = cl.lecturer_id "
            + "    JOIN user_profiles p ON p.user_id = l.user_id WHERE cl.course_id = c.course_id) AS lecturer_names "
            + "FROM courses c LEFT JOIN departments d ON d.department_id = c.department_id ";

    public List<Course> findAll() throws DatabaseException {
        return queryList(BASE_SELECT + "ORDER BY c.level, c.semester, c.course_code", CourseDAO::mapCourse);
    }

    public Course findById(int courseId) throws DatabaseException {
        return queryOne(BASE_SELECT + "WHERE c.course_id = ?", CourseDAO::mapCourse, courseId);
    }

    /** Courses of the current level/semester (the "current timetable" modules). */
    public List<Course> findCurrentSemester() throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE c.level = ? AND c.semester = ? ORDER BY c.course_code",
                CourseDAO::mapCourse, AppConfig.CURRENT_LEVEL, AppConfig.CURRENT_SEMESTER);
    }

    /** Courses a lecturer is assigned to. */
    public List<Course> findByLecturer(int lecturerId) throws DatabaseException {
        return queryList(BASE_SELECT
                        + "WHERE c.course_id IN (SELECT course_id FROM course_lecturers WHERE lecturer_id = ?) "
                        + "ORDER BY c.level DESC, c.semester DESC, c.course_code",
                CourseDAO::mapCourse, lecturerId);
    }

    /** Courses a student is enrolled in this academic year. */
    public List<Course> findByStudent(int studentId) throws DatabaseException {
        return queryList(BASE_SELECT
                        + "WHERE c.course_id IN (SELECT course_id FROM enrollments "
                        + "   WHERE student_id = ? AND academic_year = ? AND status <> 'WITHDRAWN') "
                        + "ORDER BY c.course_code",
                CourseDAO::mapCourse, studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    /** Every course the student has ever been enrolled in (all years). */
    public List<Course> findAllTakenByStudent(int studentId) throws DatabaseException {
        return queryList(BASE_SELECT
                        + "WHERE c.course_id IN (SELECT course_id FROM enrollments WHERE student_id = ? AND status <> 'WITHDRAWN') "
                        + "ORDER BY c.level, c.semester, c.course_code",
                CourseDAO::mapCourse, studentId);
    }

    public List<CourseMaterial> findMaterials(int courseId) throws DatabaseException {
        return queryList("SELECT m.*, c.course_code, p.full_name AS lecturer_name FROM course_materials m "
                        + "JOIN courses c ON c.course_id = m.course_id "
                        + "LEFT JOIN lecturers l ON l.lecturer_id = m.lecturer_id "
                        + "LEFT JOIN user_profiles p ON p.user_id = l.user_id "
                        + "WHERE m.course_id = ? ORDER BY m.covers_from IS NULL, m.covers_from, m.uploaded_at DESC",
                CourseDAO::mapMaterial, courseId);
    }

    public static Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setCredits(rs.getInt("credits"));
        c.setTheoryHours(rs.getInt("theory_hours"));
        c.setPracticalHours(rs.getInt("practical_hours"));
        c.setDescription(rs.getString("description"));
        c.setDepartmentId(getInteger(rs, "department_id"));
        c.setDepartmentName(rs.getString("department_name"));
        c.setLevel(rs.getInt("level"));
        c.setSemester(rs.getInt("semester"));
        c.setLecturerNames(rs.getString("lecturer_names"));
        return c;
    }

    public static CourseMaterial mapMaterial(ResultSet rs) throws SQLException {
        CourseMaterial m = new CourseMaterial();
        m.setMaterialId(rs.getInt("material_id"));
        m.setCourseId(rs.getInt("course_id"));
        m.setCourseCode(rs.getString("course_code"));
        m.setLecturerId(getInteger(rs, "lecturer_id"));
        m.setLecturerName(rs.getString("lecturer_name"));
        m.setTitle(rs.getString("title"));
        m.setFilePath(rs.getString("file_path"));
        m.setCoversFrom(getDate(rs, "covers_from"));   // date range the material covers
        m.setCoversTo(getDate(rs, "covers_to"));
        m.setUploadedAt(getDateTime(rs, "uploaded_at"));
        return m;
    }
}

package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.enums.Component;

/** [COMMON PART] Reads timetables. */
public class TimetableDAO extends BaseDAO {

    public static final String BASE_SELECT =
            "SELECT t.*, d.department_name, c.course_code, c.course_name, c.level, c.semester "
            + "FROM timetables t JOIN departments d ON d.department_id = t.department_id "
            + "JOIN courses c ON c.course_id = t.course_id ";

    private static final String ORDER =
            " ORDER BY FIELD(t.day,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'), t.start_time";

    public List<TimetableEntry> findAll() throws DatabaseException {
        return queryList(BASE_SELECT + ORDER, TimetableDAO::mapEntry);
    }

    /** Timetable of one department (Technical Officer view). */
    public List<TimetableEntry> findByDepartment(int departmentId) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE t.department_id = ?" + ORDER, TimetableDAO::mapEntry, departmentId);
    }

    /** Timetable of the courses a student follows this year (Undergraduate view). */
    public List<TimetableEntry> findByStudent(int studentId) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE t.course_id IN (SELECT course_id FROM enrollments "
                        + "WHERE student_id = ? AND academic_year = ? AND status <> 'WITHDRAWN')" + ORDER,
                TimetableDAO::mapEntry, studentId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }

    /** Timetable of a lecturer's courses. */
    public List<TimetableEntry> findByLecturer(int lecturerId) throws DatabaseException {
        return queryList(BASE_SELECT + "WHERE t.course_id IN (SELECT course_id FROM course_lecturers WHERE lecturer_id = ?)"
                + ORDER, TimetableDAO::mapEntry, lecturerId);
    }

    public static TimetableEntry mapEntry(ResultSet rs) throws SQLException {
        TimetableEntry e = new TimetableEntry();
        e.setTimetableId(rs.getInt("timetable_id"));
        e.setDepartmentId(rs.getInt("department_id"));
        e.setDepartmentName(rs.getString("department_name"));
        e.setCourseId(rs.getInt("course_id"));
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseName(rs.getString("course_name"));
        e.setDay(rs.getString("day"));
        e.setStartTime(getTime(rs, "start_time"));
        e.setEndTime(getTime(rs, "end_time"));
        e.setRoom(rs.getString("room"));
        e.setComponent(Component.valueOf(rs.getString("component")));
        e.setLevel(rs.getInt("level"));
        e.setSemester(rs.getInt("semester"));
        return e;
    }
}

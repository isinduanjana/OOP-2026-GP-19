package lk.ac.ruh.fot.ams.lecturer.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.UserDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Undergraduate;


public class LecturerCourseDAO extends BaseDAO {


    public Map<Integer, Integer> studentCounts(int lecturerId) throws DatabaseException {
        Map<Integer, Integer> map = new HashMap<>();
        queryList("SELECT e.course_id, COUNT(*) FROM enrollments e "
                + "WHERE e.academic_year = ? AND e.status <> 'WITHDRAWN' AND e.course_id IN "
                + "(SELECT course_id FROM course_lecturers WHERE lecturer_id = ?) GROUP BY e.course_id", rs -> {
                    map.put(rs.getInt(1), rs.getInt(2));
                    return null;
                }, AppConfig.CURRENT_ACADEMIC_YEAR, lecturerId);
        return map;
    }


    public List<Undergraduate> findMyStudents(int lecturerId) throws DatabaseException {
        List<Undergraduate> list = new java.util.ArrayList<>();
        queryList(UserDAO.BASE_SELECT + "WHERE s.student_id IN (SELECT e.student_id FROM enrollments e "
                        + "JOIN course_lecturers cl ON cl.course_id = e.course_id "
                        + "WHERE cl.lecturer_id = ? AND e.academic_year = ? AND e.status <> 'WITHDRAWN') ORDER BY s.tg_no",
                UserDAO::mapUser, lecturerId, AppConfig.CURRENT_ACADEMIC_YEAR)
                .forEach(u -> list.add((Undergraduate) u));
        return list;
    }


    public void updateDescription(int courseId, int lecturerId, String description)
            throws DatabaseException, ValidationException {
        boolean teaches = queryNumber("SELECT COUNT(*) FROM course_lecturers WHERE course_id = ? AND lecturer_id = ?",
                courseId, lecturerId) > 0;
        if (!teaches) {
            throw new ValidationException("You can only edit the description of courses you teach.");
        }
        executeUpdate("UPDATE courses SET description = ? WHERE course_id = ?", description, courseId);
    }
}

package lk.ac.ruh.fot.ams.dao;

import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;

/** [COMMON PART] Read-only queries for undergraduates (used by M2, M3, M4). */
public class StudentDAO extends BaseDAO {

    public List<Undergraduate> findAll() throws DatabaseException {
        return cast(queryList(UserDAO.BASE_SELECT + "WHERE u.role = 'UNDERGRADUATE' ORDER BY s.tg_no",
                UserDAO::mapUser));
    }

    public Undergraduate findById(int studentId) throws DatabaseException {
        User u = queryOne(UserDAO.BASE_SELECT + "WHERE s.student_id = ?", UserDAO::mapUser, studentId);
        return (Undergraduate) u;
    }

    /** Students enrolled in a course in the current academic year. */
    public List<Undergraduate> findByCourse(int courseId) throws DatabaseException {
        return cast(queryList(UserDAO.BASE_SELECT
                        + "JOIN enrollments e ON e.student_id = s.student_id "
                        + "WHERE e.course_id = ? AND e.academic_year = ? AND e.status <> 'WITHDRAWN' "
                        + "ORDER BY s.tg_no",
                UserDAO::mapUser, courseId, AppConfig.CURRENT_ACADEMIC_YEAR));
    }

    public List<String> findBatches() throws DatabaseException {
        return queryList("SELECT DISTINCT batch FROM undergraduates ORDER BY batch", rs -> rs.getString(1));
    }

    private static List<Undergraduate> cast(List<User> users) {
        List<Undergraduate> list = new ArrayList<>();
        for (User u : users) {
            list.add((Undergraduate) u);
        }
        return list;
    }
}

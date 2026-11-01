package lk.ac.ruh.fot.ams.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.enums.AssessmentType;

/**
 * [COMMON PART] READ-ONLY assessment + marks queries used for results.
 * Entering marks is done by M2's LecturerMarksDAO.
 */
public class AssessmentDAO extends BaseDAO {

    public List<Assessment> findByCourse(int courseId) throws DatabaseException {
        return queryList("SELECT * FROM assessments WHERE course_id = ? "
                        + "ORDER BY FIELD(assessment_type,'CA','ASSIGNMENT','MID','FINAL'), assessment_id",
                AssessmentDAO::mapAssessment, courseId);
    }

    /** @return studentId -> (assessmentId -> mark) for one course. */
    public Map<Integer, Map<Integer, Double>> findMarksByCourse(int courseId) throws DatabaseException {
        Map<Integer, Map<Integer, Double>> map = new HashMap<>();
        queryList("SELECT mk.student_id, mk.assessment_id, mk.marks FROM marks mk "
                + "JOIN assessments a ON a.assessment_id = mk.assessment_id WHERE a.course_id = ?", rs -> {
                    map.computeIfAbsent(rs.getInt(1), k -> new HashMap<>()).put(rs.getInt(2), rs.getDouble(3));
                    return null;
                }, courseId);
        return map;
    }

    /** @return assessmentId -> mark for one student in one course. */
    public Map<Integer, Double> findMarks(int studentId, int courseId) throws DatabaseException {
        Map<Integer, Double> map = new HashMap<>();
        queryList("SELECT mk.assessment_id, mk.marks FROM marks mk "
                + "JOIN assessments a ON a.assessment_id = mk.assessment_id "
                + "WHERE mk.student_id = ? AND a.course_id = ?", rs -> {
                    map.put(rs.getInt(1), rs.getDouble(2));
                    return null;
                }, studentId, courseId);
        return map;
    }

    public static Assessment mapAssessment(ResultSet rs) throws SQLException {
        Assessment a = new Assessment();
        a.setAssessmentId(rs.getInt("assessment_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setName(rs.getString("assessment_name"));
        a.setType(AssessmentType.valueOf(rs.getString("assessment_type")));
        a.setMaxMarks(rs.getInt("max_marks"));
        a.setPercentage(rs.getDouble("percentage"));
        return a;
    }
}

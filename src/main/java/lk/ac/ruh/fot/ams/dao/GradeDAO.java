package lk.ac.ruh.fot.ams.dao;

import java.util.HashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.DatabaseException;

public class GradeDAO extends BaseDAO {

    /** A stored grade row. */
    public static class StoredGrade {
        public int courseId;
        public Double totalMarks;
        public String grade;
        public double gradePoint;
        public String remarks;
    }

    /** @return courseId -> stored grade for one student. */
    public Map<Integer, StoredGrade> findByStudent(int studentId) throws DatabaseException {
        Map<Integer, StoredGrade> map = new HashMap<>();
        queryList("SELECT course_id, total_marks, grade, grade_point, remarks FROM grades WHERE student_id = ?", rs -> {
            StoredGrade g = new StoredGrade();
            g.courseId = rs.getInt("course_id");
            g.totalMarks = getDoubleOrNull(rs, "total_marks");
            g.grade = rs.getString("grade");
            g.gradePoint = rs.getDouble("grade_point");
            g.remarks = rs.getString("remarks");
            map.put(g.courseId, g);
            return null;
        }, studentId);
        return map;
    }
}

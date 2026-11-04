package lk.ac.ruh.fot.ams.lecturer.dao;

import java.util.HashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;


public class LecturerMarksDAO extends BaseDAO {

    private static final String UPSERT =
            "INSERT INTO marks (assessment_id, student_id, marks, entered_by, entered_at) VALUES (?, ?, ?, ?, NOW()) "
            + "ON DUPLICATE KEY UPDATE marks = VALUES(marks), entered_by = VALUES(entered_by), entered_at = NOW()";

    private static final String DELETE = "DELETE FROM marks WHERE assessment_id = ? AND student_id = ?";


    public Map<Integer, Double> findMarks(int assessmentId) throws DatabaseException {
        Map<Integer, Double> map = new HashMap<>();
        queryList("SELECT student_id, marks FROM marks WHERE assessment_id = ?", rs -> {
            map.put(rs.getInt(1), rs.getDouble(2));
            return null;
        }, assessmentId);
        return map;
    }


    public boolean teaches(int lecturerId, int courseId) throws DatabaseException {
        return queryNumber("SELECT COUNT(*) FROM course_lecturers WHERE lecturer_id = ? AND course_id = ?",
                lecturerId, courseId) > 0;
    }


    public int saveMarks(int assessmentId, Map<Integer, Double> marks, int enteredByUserId) throws DatabaseException {
        int[] written = {0};
        inTransaction(c -> {
            for (Map.Entry<Integer, Double> e : marks.entrySet()) {
                if (e.getValue() == null) {
                    written[0] += update(c, DELETE, assessmentId, e.getKey());
                } else {
                    update(c, UPSERT, assessmentId, e.getKey(), e.getValue(), enteredByUserId);
                    written[0]++;
                }
            }
        });
        return written[0];
    }


    public void saveMark(int assessmentId, int studentId, double mark, int enteredByUserId) throws DatabaseException {
        executeUpdate(UPSERT, assessmentId, studentId, mark, enteredByUserId);
    }


    public void clearMark(int assessmentId, int studentId) throws DatabaseException {
        executeUpdate(DELETE, assessmentId, studentId);
    }


    public double averageCaPercent(int courseId) throws DatabaseException {
        return queryNumber("SELECT AVG(x.ca) FROM (SELECT mk.student_id, "
                        + " SUM(mk.marks / a.max_marks * a.percentage) * 100 / "
                        + "  (SELECT SUM(percentage) FROM assessments WHERE course_id = ? AND assessment_type <> 'FINAL') AS ca "
                        + " FROM marks mk JOIN assessments a ON a.assessment_id = mk.assessment_id "
                        + " WHERE a.course_id = ? AND a.assessment_type <> 'FINAL' GROUP BY mk.student_id) x",
                courseId, courseId);
    }
}

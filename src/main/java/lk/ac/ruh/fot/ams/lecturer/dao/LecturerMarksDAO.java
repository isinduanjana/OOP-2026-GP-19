package lk.ac.ruh.fot.ams.lecturer.dao;

import java.util.HashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;

/**
 * [M2 - LECTURER MODULE] Writes marks of an assessment ("Can upload marks for all kind of exams").
 * The common AssessmentDAO is read-only; entering marks is M2's job.
 *
 * OOP: Inheritance (extends the common BaseDAO -> gets queryList(), queryNumber(),
 * inTransaction() and the static update() helper), Encapsulation (SQL kept private),
 * Exception handling (SQLException is turned into DatabaseException by BaseDAO).
 */
public class LecturerMarksDAO extends BaseDAO {

    private static final String UPSERT =
            "INSERT INTO marks (assessment_id, student_id, marks, entered_by, entered_at) VALUES (?, ?, ?, ?, NOW()) "
            + "ON DUPLICATE KEY UPDATE marks = VALUES(marks), entered_by = VALUES(entered_by), entered_at = NOW()";

    private static final String DELETE = "DELETE FROM marks WHERE assessment_id = ? AND student_id = ?";

    /** @return studentId -> mark for one assessment. */
    public Map<Integer, Double> findMarks(int assessmentId) throws DatabaseException {
        Map<Integer, Double> map = new HashMap<>();
        queryList("SELECT student_id, marks FROM marks WHERE assessment_id = ?", rs -> {
            map.put(rs.getInt(1), rs.getDouble(2));
            return null;
        }, assessmentId);
        return map;
    }

    /** True when the lecturer is assigned to the course (security check before saving). */
    public boolean teaches(int lecturerId, int courseId) throws DatabaseException {
        return queryNumber("SELECT COUNT(*) FROM course_lecturers WHERE lecturer_id = ? AND course_id = ?",
                lecturerId, courseId) > 0;
    }

    /**
     * Saves all changed marks of one assessment in ONE transaction.
     * @param marks studentId -> mark (null value = clear that student's mark)
     * @return number of rows written
     */
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

    /** Upserts a single mark (used by the smoke test / quick edits). */
    public void saveMark(int assessmentId, int studentId, double mark, int enteredByUserId) throws DatabaseException {
        executeUpdate(UPSERT, assessmentId, studentId, mark, enteredByUserId);
    }

    /** Removes one student's mark (the "Clear mark" button). */
    public void clearMark(int assessmentId, int studentId) throws DatabaseException {
        executeUpdate(DELETE, assessmentId, studentId);
    }

    /** Average CA percentage of a course (CA marks / CA weight), used on the dashboard. */
    public double averageCaPercent(int courseId) throws DatabaseException {
        return queryNumber("SELECT AVG(x.ca) FROM (SELECT mk.student_id, "
                        + " SUM(mk.marks / a.max_marks * a.percentage) * 100 / "
                        + "  (SELECT SUM(percentage) FROM assessments WHERE course_id = ? AND assessment_type <> 'FINAL') AS ca "
                        + " FROM marks mk JOIN assessments a ON a.assessment_id = mk.assessment_id "
                        + " WHERE a.course_id = ? AND a.assessment_type <> 'FINAL' GROUP BY mk.student_id) x",
                courseId, courseId);
    }
}

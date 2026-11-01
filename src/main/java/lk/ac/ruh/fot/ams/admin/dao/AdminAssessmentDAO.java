package lk.ac.ruh.fot.ams.admin.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.admin.WeightLimitExceededException;
import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Assessment;

/**
 * [M1 - ADMIN MODULE] Maintains the evaluation criteria of a course
 * (quizzes, assignments, mid and final exam with their weights).
 * Lecturers (M2) enter marks for these assessments; results (common ResultService) use the weights.
 *
 * OOP: Inheritance (BaseDAO), Generics/Abstraction (CrudDAO&lt;Assessment&gt;),
 *      custom exception WeightLimitExceededException.
 */
public class AdminAssessmentDAO extends BaseDAO implements CrudDAO<Assessment> {

    private static final String ORDER = " ORDER BY FIELD(assessment_type,'CA','ASSIGNMENT','MID','FINAL'), assessment_id";

    @Override
    public List<Assessment> findAll() throws DatabaseException {
        return queryList("SELECT * FROM assessments" + ORDER, AssessmentDAO::mapAssessment);
    }

    @Override
    public Assessment findById(int id) throws DatabaseException {
        return queryOne("SELECT * FROM assessments WHERE assessment_id = ?", AssessmentDAO::mapAssessment, id);
    }

    public List<Assessment> findByCourse(int courseId) throws DatabaseException {
        return queryList("SELECT * FROM assessments WHERE course_id = ?" + ORDER, AssessmentDAO::mapAssessment, courseId);
    }

    @Override
    public int insert(Assessment a) throws DatabaseException {
        return executeInsert("INSERT INTO assessments (course_id, assessment_name, assessment_type, max_marks, percentage) "
                + "VALUES (?, ?, ?, 100, ?)", a.getCourseId(), a.getName(), a.getType(), a.getPercentage());
    }

    @Override
    public void update(Assessment a) throws DatabaseException {
        executeUpdate("UPDATE assessments SET assessment_name = ?, assessment_type = ?, percentage = ? WHERE assessment_id = ?",
                a.getName(), a.getType(), a.getPercentage(), a.getAssessmentId());
    }

    @Override
    public void delete(int id) throws DatabaseException {
        executeUpdate("DELETE FROM assessments WHERE assessment_id = ?", id);
    }

    /** Insert or update after checking that the course total stays at or below 100%. */
    public void save(Assessment a) throws AppException {
        double others = queryNumber("SELECT COALESCE(SUM(percentage), 0) FROM assessments "
                + "WHERE course_id = ? AND assessment_id <> ?", a.getCourseId(), a.getAssessmentId());
        if (others + a.getPercentage() > 100.0001) {
            throw new WeightLimitExceededException(others, a.getPercentage());
        }
        if (a.getAssessmentId() == 0) {
            a.setAssessmentId(insert(a));
        } else {
            update(a);
        }
    }

    /** Number of marks already entered for an assessment (deleting it removes them). */
    public int countMarks(int assessmentId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM marks WHERE assessment_id = ?", assessmentId);
    }
}

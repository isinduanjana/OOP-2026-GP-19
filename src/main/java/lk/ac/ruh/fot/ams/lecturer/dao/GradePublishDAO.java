package lk.ac.ruh.fot.ams.lecturer.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.report.CourseResult;


public class GradePublishDAO extends BaseDAO {

    private static final String UPSERT =
            "INSERT INTO grades (student_id, course_id, academic_year, total_marks, grade, grade_point, remarks) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE academic_year = VALUES(academic_year), "
            + "total_marks = VALUES(total_marks), grade = VALUES(grade), grade_point = VALUES(grade_point), "
            + "remarks = VALUES(remarks)";


    public int publish(List<CourseResult> results) throws DatabaseException {
        int[] count = {0};
        inTransaction(c -> {
            for (CourseResult r : results) {
                if (!r.hasGrade() || r.getSource() == CourseResult.Source.PENDING) continue;
                String remarks = r.getRemarks();
                if (remarks != null && remarks.length() > 50) remarks = remarks.substring(0, 50);
                update(c, UPSERT, r.getStudentId(), r.getCourse().getCourseId(), AppConfig.CURRENT_ACADEMIC_YEAR,
                        r.getTotalMarks(), r.getGradeLetter(), r.getGradePoint(), remarks);
                count[0]++;
            }
        });
        return count[0];
    }


    public int countPublished(int courseId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM grades WHERE course_id = ? AND academic_year = ?",
                courseId, AppConfig.CURRENT_ACADEMIC_YEAR);
    }
}

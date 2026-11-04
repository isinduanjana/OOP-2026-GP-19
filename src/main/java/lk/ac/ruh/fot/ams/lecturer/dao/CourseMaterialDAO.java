package lk.ac.ruh.fot.ams.lecturer.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.CourseMaterial;


public class CourseMaterialDAO extends BaseDAO implements CrudDAO<CourseMaterial> {

    private static final String SELECT =
            "SELECT m.*, c.course_code, p.full_name AS lecturer_name FROM course_materials m "
            + "JOIN courses c ON c.course_id = m.course_id "
            + "LEFT JOIN lecturers l ON l.lecturer_id = m.lecturer_id "
            + "LEFT JOIN user_profiles p ON p.user_id = l.user_id ";

    @Override
    public List<CourseMaterial> findAll() throws DatabaseException {
        return queryList(SELECT + "ORDER BY m.uploaded_at DESC", CourseDAO::mapMaterial);
    }

    @Override
    public CourseMaterial findById(int id) throws DatabaseException {
        return queryOne(SELECT + "WHERE m.material_id = ?", CourseDAO::mapMaterial, id);
    }

    public List<CourseMaterial> findByCourse(int courseId) throws DatabaseException {
        return queryList(SELECT + "WHERE m.course_id = ? ORDER BY m.uploaded_at DESC", CourseDAO::mapMaterial, courseId);
    }


    public int countForLecturer(int lecturerId) throws DatabaseException {
        return (int) queryNumber("SELECT COUNT(*) FROM course_materials WHERE course_id IN "
                + "(SELECT course_id FROM course_lecturers WHERE lecturer_id = ?)", lecturerId);
    }

    @Override
    public int insert(CourseMaterial m) throws DatabaseException {
        return executeInsert("INSERT INTO course_materials (course_id, lecturer_id, title, file_path, uploaded_at) "
                + "VALUES (?, ?, ?, ?, NOW())", m.getCourseId(), m.getLecturerId(), m.getTitle(), m.getFilePath());
    }


    @Override
    public void update(CourseMaterial m) throws DatabaseException {
        executeUpdate("UPDATE course_materials SET title = ?, "
                        + "uploaded_at = IF(file_path <=> ?, uploaded_at, NOW()), file_path = ? WHERE material_id = ?",
                m.getTitle(), m.getFilePath(), m.getFilePath(), m.getMaterialId());
    }

    @Override
    public void delete(int id) throws DatabaseException {
        executeUpdate("DELETE FROM course_materials WHERE material_id = ?", id);
    }
}

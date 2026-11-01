package lk.ac.ruh.fot.ams.admin.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.admin.DuplicateRecordException;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Course;

/**
 * [M1 - ADMIN MODULE] Create / update / delete course units and assign lecturers.
 *
 * Connection to common: reading re-uses CourseDAO.BASE_SELECT + CourseDAO.mapCourse, so the
 * admin sees exactly the same Course objects (with lecturer names) as the other modules.
 *
 * OOP: Inheritance (extends BaseDAO), Abstraction/Generics (implements CrudDAO&lt;Course&gt;),
 *      Exception handling (DuplicateRecordException, transaction with rollback).
 */
public class AdminCourseDAO extends BaseDAO implements CrudDAO<Course> {

    @Override
    public List<Course> findAll() throws DatabaseException {
        return queryList(CourseDAO.BASE_SELECT + "ORDER BY c.level, c.semester, c.course_code", CourseDAO::mapCourse);
    }

    @Override
    public Course findById(int courseId) throws DatabaseException {
        return queryOne(CourseDAO.BASE_SELECT + "WHERE c.course_id = ?", CourseDAO::mapCourse, courseId);
    }

    @Override
    public int insert(Course c) throws DatabaseException {
        return executeInsert("INSERT INTO courses (course_code, course_name, credits, theory_hours, practical_hours, "
                        + "description, department_id, level, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getTheoryHours(), c.getPracticalHours(),
                c.getDescription(), c.getDepartmentId(), c.getLevel(), c.getSemester());
    }

    @Override
    public void update(Course c) throws DatabaseException {
        executeUpdate("UPDATE courses SET course_code = ?, course_name = ?, credits = ?, theory_hours = ?, "
                        + "practical_hours = ?, description = ?, department_id = ?, level = ?, semester = ? "
                        + "WHERE course_id = ?",
                c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getTheoryHours(), c.getPracticalHours(),
                c.getDescription(), c.getDepartmentId(), c.getLevel(), c.getSemester(), c.getCourseId());
    }

    /** Enrolments, assessments, marks, sessions, timetable slots are removed by ON DELETE CASCADE. */
    @Override
    public void delete(int courseId) throws DatabaseException {
        executeUpdate("DELETE FROM courses WHERE course_id = ?", courseId);
    }

    /**
     * Saves the course AND its lecturer list in one transaction.
     * @return the course_id
     */
    public int save(Course c, List<Integer> lecturerIds) throws AppException {
        if (queryNumber("SELECT COUNT(*) FROM courses WHERE course_code = ? AND course_id <> ?",
                c.getCourseCode(), c.getCourseId()) > 0) {
            throw new DuplicateRecordException("Course code", c.getCourseCode());
        }
        final int[] id = {c.getCourseId()};
        inTransaction(con -> {
            if (id[0] == 0) {
                id[0] = insert(con, "INSERT INTO courses (course_code, course_name, credits, theory_hours, "
                                + "practical_hours, description, department_id, level, semester) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getTheoryHours(),
                        c.getPracticalHours(), c.getDescription(), c.getDepartmentId(), c.getLevel(), c.getSemester());
            } else {
                update(con, "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, theory_hours = ?, "
                                + "practical_hours = ?, description = ?, department_id = ?, level = ?, semester = ? "
                                + "WHERE course_id = ?",
                        c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getTheoryHours(),
                        c.getPracticalHours(), c.getDescription(), c.getDepartmentId(), c.getLevel(),
                        c.getSemester(), c.getCourseId());
                update(con, "DELETE FROM course_lecturers WHERE course_id = ?", c.getCourseId());
            }
            for (int lecturerId : lecturerIds) {
                insert(con, "INSERT INTO course_lecturers (course_id, lecturer_id) VALUES (?, ?)", id[0], lecturerId);
            }
        });
        c.setCourseId(id[0]);
        return id[0];
    }

    /** lecturer_id values assigned to a course. */
    public List<Integer> findLecturerIds(int courseId) throws DatabaseException {
        return queryList("SELECT lecturer_id FROM course_lecturers WHERE course_id = ?", rs -> rs.getInt(1), courseId);
    }

    /** How much data would be lost if the course were deleted: {enrolments, sessions, marks, timetable slots}. */
    public int[] countDependents(int courseId) throws DatabaseException {
        return new int[]{
                (int) queryNumber("SELECT COUNT(*) FROM enrollments WHERE course_id = ?", courseId),
                (int) queryNumber("SELECT COUNT(*) FROM attendance_sessions WHERE course_id = ?", courseId),
                (int) queryNumber("SELECT COUNT(*) FROM marks m JOIN assessments a ON a.assessment_id = m.assessment_id "
                        + "WHERE a.course_id = ?", courseId),
                (int) queryNumber("SELECT COUNT(*) FROM timetables WHERE course_id = ?", courseId)};
    }
}

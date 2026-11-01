package lk.ac.ruh.fot.ams.admin.dao;

import java.util.List;

import lk.ac.ruh.fot.ams.admin.TimetableClashException;
import lk.ac.ruh.fot.ams.dao.BaseDAO;
import lk.ac.ruh.fot.ams.dao.CrudDAO;
import lk.ac.ruh.fot.ams.dao.TimetableDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.enums.Component;

/**
 * [M1 - ADMIN MODULE] Maintains the weekly timetable. Every role's timetable page
 * (common TimetablePanel) reads what is saved here.
 *
 * OOP: Inheritance (BaseDAO), Abstraction/Generics (CrudDAO&lt;TimetableEntry&gt;),
 *      Exception handling: validate() throws ValidationException / TimetableClashException.
 */
public class AdminTimetableDAO extends BaseDAO implements CrudDAO<TimetableEntry> {

    private static final String ORDER =
            " ORDER BY FIELD(t.day,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'), t.start_time, t.room";

    @Override
    public List<TimetableEntry> findAll() throws DatabaseException {
        return queryList(TimetableDAO.BASE_SELECT + ORDER, TimetableDAO::mapEntry);
    }

    public List<TimetableEntry> findByDepartment(int departmentId) throws DatabaseException {
        return queryList(TimetableDAO.BASE_SELECT + "WHERE t.department_id = ?" + ORDER, TimetableDAO::mapEntry,
                departmentId);
    }

    @Override
    public TimetableEntry findById(int id) throws DatabaseException {
        return queryOne(TimetableDAO.BASE_SELECT + "WHERE t.timetable_id = ?", TimetableDAO::mapEntry, id);
    }

    @Override
    public int insert(TimetableEntry e) throws DatabaseException {
        return executeInsert("INSERT INTO timetables (department_id, course_id, day, start_time, end_time, room, component) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                e.getDepartmentId(), e.getCourseId(), e.getDay(), e.getStartTime(), e.getEndTime(), e.getRoom(),
                e.getComponent());
    }

    @Override
    public void update(TimetableEntry e) throws DatabaseException {
        executeUpdate("UPDATE timetables SET department_id = ?, course_id = ?, day = ?, start_time = ?, end_time = ?, "
                        + "room = ?, component = ? WHERE timetable_id = ?",
                e.getDepartmentId(), e.getCourseId(), e.getDay(), e.getStartTime(), e.getEndTime(), e.getRoom(),
                e.getComponent(), e.getTimetableId());
    }

    @Override
    public void delete(int id) throws DatabaseException {
        executeUpdate("DELETE FROM timetables WHERE timetable_id = ?", id);
    }

    /** Rooms already used in the timetable (suggestions for the room combo). */
    public List<String> findRooms() throws DatabaseException {
        return queryList("SELECT DISTINCT room FROM timetables WHERE room IS NOT NULL ORDER BY room", rs -> rs.getString(1));
    }

    /** Validates (times, component, clashes) and then inserts or updates. */
    public void save(TimetableEntry e, Course course) throws AppException {
        validate(e, course);
        if (e.getTimetableId() == 0) {
            e.setTimetableId(insert(e));
        } else {
            update(e);
        }
    }

    /**
     * Business rules of a timetable slot.
     * @throws ValidationException      wrong times / component the course does not have
     * @throws TimetableClashException  room already booked, or same course already scheduled at that time
     */
    public void validate(TimetableEntry e, Course course) throws AppException {
        if (e.getStartTime() == null || e.getEndTime() == null || !e.getEndTime().isAfter(e.getStartTime())) {
            throw new ValidationException("End time", "End time must be later than the start time.");
        }
        if (e.getComponent() == Component.THEORY && !course.hasTheory()) {
            throw new ValidationException("Component", course.getCourseCode() + " has no theory component "
                    + "(theory hours = 0). Choose Practical or change the course.");
        }
        if (e.getComponent() == Component.PRACTICAL && !course.hasPractical()) {
            throw new ValidationException("Component", course.getCourseCode() + " has no practical component "
                    + "(practical hours = 0). Choose Theory or change the course.");
        }
        // overlap rule: existing.start < new.end AND existing.end > new.start
        TimetableEntry roomClash = queryOne(TimetableDAO.BASE_SELECT
                        + "WHERE t.day = ? AND UPPER(t.room) = UPPER(?) AND t.start_time < ? AND t.end_time > ? "
                        + "AND t.timetable_id <> ? LIMIT 1",
                TimetableDAO::mapEntry, e.getDay(), e.getRoom(), e.getEndTime(), e.getStartTime(), e.getTimetableId());
        if (roomClash != null) {
            throw new TimetableClashException("Room " + e.getRoom() + " is already booked at this time.", roomClash);
        }
        TimetableEntry courseClash = queryOne(TimetableDAO.BASE_SELECT
                        + "WHERE t.day = ? AND t.course_id = ? AND t.start_time < ? AND t.end_time > ? "
                        + "AND t.timetable_id <> ? LIMIT 1",
                TimetableDAO::mapEntry, e.getDay(), e.getCourseId(), e.getEndTime(), e.getStartTime(), e.getTimetableId());
        if (courseClash != null) {
            throw new TimetableClashException(course.getCourseCode() + " already has a class at this time.", courseClash);
        }
    }
}

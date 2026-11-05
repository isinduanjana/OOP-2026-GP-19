package lk.ac.ruh.fot.ams.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.dao.AttendanceDAO;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.report.AttendanceRecord;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.ComponentAttendance;


public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    /** Attendance of every enrolled student of a course. */
    public List<AttendanceSummary> getBatchSummary(Course course) throws DatabaseException {
        Map<Integer, Map<Component, ComponentAttendance>> stats = attendanceDAO.findCourseStats(course.getCourseId());
        List<AttendanceSummary> list = new ArrayList<>();
        for (Undergraduate s : studentDAO.findByCourse(course.getCourseId())) {
            list.add(build(s, course, stats));
        }
        return list;
    }

    /** Attendance of one student in one course. */
    public AttendanceSummary getStudentSummary(Undergraduate student, Course course) throws DatabaseException {
        return build(student, course, attendanceDAO.findCourseStats(course.getCourseId()));
    }

    /** Attendance of one student in all current courses. */
    public List<AttendanceSummary> getStudentSummaries(Undergraduate student) throws DatabaseException {
        List<AttendanceSummary> list = new ArrayList<>();
        for (Course c : courseDAO.findByStudent(student.getStudentId())) {
            list.add(getStudentSummary(student, c));
        }
        return list;
    }

    /** Day-by-day attendance rows. */
    public List<AttendanceRecord> getRecords(int studentId, int courseId) throws DatabaseException {
        return attendanceDAO.findStudentRecords(studentId, courseId);
    }

    /** Map studentId -> summary, used by ResultService for eligibility. */
    public Map<Integer, AttendanceSummary> getSummaryMap(Course course) throws DatabaseException {
        Map<Integer, AttendanceSummary> map = new HashMap<>();
        for (AttendanceSummary s : getBatchSummary(course)) {
            map.put(s.getStudentId(), s);
        }
        return map;
    }

    private AttendanceSummary build(Undergraduate s, Course course,
                                    Map<Integer, Map<Component, ComponentAttendance>> stats) {
        AttendanceSummary sum = new AttendanceSummary();
        sum.setStudentId(s.getStudentId());
        sum.setTgNo(s.getTgNo());
        sum.setStudentName(s.getFullName());
        sum.setStudentType(s.getStudentType());
        sum.setCourse(course);
        Map<Component, ComponentAttendance> mine = stats.getOrDefault(s.getStudentId(), new HashMap<>());
        Map<Component, ComponentAttendance> empty = stats.getOrDefault(-1, new HashMap<>());
        if (course.hasTheory()) {
            sum.setTheory(pick(Component.THEORY, mine, empty));
        }
        if (course.hasPractical()) {
            sum.setPractical(pick(Component.PRACTICAL, mine, empty));
        }
        return sum;
    }

    private ComponentAttendance pick(Component c, Map<Component, ComponentAttendance> mine,
                                     Map<Component, ComponentAttendance> empty) {
        if (mine.containsKey(c)) return mine.get(c);
        ComponentAttendance blank = new ComponentAttendance(c);
        ComponentAttendance held = empty.get(c);
        if (held != null) {
            blank.setSessionsHeld(held.getSessionsHeld());
            blank.setHoursPerSession(held.getHoursPerSession());
        }
        return blank;
    }
}

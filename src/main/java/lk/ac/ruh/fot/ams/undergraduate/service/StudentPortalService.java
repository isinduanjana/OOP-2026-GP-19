package lk.ac.ruh.fot.ams.undergraduate.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.NoticeDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.model.report.Transcript;
import lk.ac.ruh.fot.ams.service.AttendanceService;
import lk.ac.ruh.fot.ams.service.ResultService;
import lk.ac.ruh.fot.ams.undergraduate.dao.StudentPortalDAO;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;

/**
 * [M4 - UNDERGRADUATE MODULE] Collects the data of the Student Home page
 * from the COMMON services (ResultService, AttendanceService, NoticeDAO,
 * CourseDAO) and our own StudentPortalDAO, and packs it in a StudentSnapshot.
 *
 * OOP: Encapsulation (the DAO / service objects are private), composition
 * (re-uses common objects instead of copying their logic), exception handling
 * (all methods declare AppException - the GUI shows it with safe(...)).
 */
public class StudentPortalService {

    private static final int NOTICES_ON_HOME = 3;

    private final StudentPortalDAO portalDAO = new StudentPortalDAO();
    private final ResultService resultService = new ResultService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final CourseDAO courseDAO = new CourseDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();

    /** Loads everything for the home page of one student. */
    public StudentSnapshot loadSnapshot(Undergraduate student) throws AppException {
        // fails with StudentDataNotFoundException if the student was removed
        Undergraduate fresh = portalDAO.requireStudent(student.getStudentId());
        student.setLevel(fresh.getLevel());
        student.setSemester(fresh.getSemester());
        student.setStudentType(fresh.getStudentType());
        student.setBatch(fresh.getBatch());

        Transcript transcript = resultService.getTranscript(student);
        List<CourseResult> current = currentResults(student, transcript);

        List<Notice> notices = noticeDAO.findActiveForRole(student.getRole());
        if (notices.size() > NOTICES_ON_HOME) notices = notices.subList(0, NOTICES_ON_HOME);

        String day = classDay(LocalDate.now());
        String dayLabel = isWeekend(LocalDate.now()) ? "Monday (next class day)" : "Today, " + capital(day);

        return new StudentSnapshot(student, transcript, current,
                attendanceService.getStudentSummaries(student), notices,
                portalDAO.findClassesOnDay(student.getStudentId(), day), dayLabel,
                portalDAO.countMedicalsByStatus(student.getStudentId()),
                portalDAO.countEnrolledCredits(student.getStudentId()));
    }

    /**
     * Results of the courses followed THIS academic year, taken from the
     * transcript (so they are not calculated twice).
     */
    public List<CourseResult> currentResults(Undergraduate student, Transcript transcript) throws AppException {
        Set<Integer> currentIds = new HashSet<>();
        for (Course c : courseDAO.findByStudent(student.getStudentId())) currentIds.add(c.getCourseId());
        List<CourseResult> list = new ArrayList<>();
        for (SemesterResult s : transcript.getSemesters()) {
            for (CourseResult r : s.getResults()) {
                if (currentIds.contains(r.getCourse().getCourseId())) list.add(r);
            }
        }
        return list;
    }

    /** "MONDAY".."FRIDAY" - on Saturday/Sunday we show Monday's classes. */
    public static String classDay(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return isWeekend(date) ? DayOfWeek.MONDAY.name() : d.name();
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    public static String capital(String s) {
        return s == null || s.isEmpty() ? "" : s.charAt(0) + s.substring(1).toLowerCase();
    }
}

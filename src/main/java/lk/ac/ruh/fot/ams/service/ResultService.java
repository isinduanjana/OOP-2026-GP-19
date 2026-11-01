package lk.ac.ruh.fot.ams.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lk.ac.ruh.fot.ams.dao.AssessmentDAO;
import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.GradeDAO;
import lk.ac.ruh.fot.ams.dao.StudentDAO;
import lk.ac.ruh.fot.ams.exception.DatabaseException;
import lk.ac.ruh.fot.ams.model.Assessment;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.enums.AssessmentType;
import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.model.report.Transcript;

/**
 * [COMMON PART] All marks / eligibility / grade / GPA calculations.
 *
 *  CA marks      = sum of (mark/100 x weight) for Quiz, Assignment, Mid
 *  CA %          = CA marks / total CA weight x 100   -> eligible if >= 40%
 *  Final mark    = CA marks + (final exam/100 x final weight)   (out of 100)
 *  Eligibility   = CA >= 40%  AND  attendance (with approved medical) >= 80%
 *  Grade         = GradeScale (UGC Circular 12-2024); not eligible -> E;
 *                  REPEAT students are capped at C.
 *  SGPA / CGPA   = sum(grade point x credits) / sum(credits)
 */
public class ResultService {

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final GradeDAO gradeDAO = new GradeDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    /** Results of every enrolled student for one course (whole batch). */
    public List<CourseResult> getBatchResults(Course course) throws DatabaseException {
        List<Assessment> assessments = assessmentDAO.findByCourse(course.getCourseId());
        Map<Integer, Map<Integer, Double>> marks = assessmentDAO.findMarksByCourse(course.getCourseId());
        Map<Integer, AttendanceSummary> attendance = attendanceService.getSummaryMap(course);
        List<CourseResult> list = new ArrayList<>();
        for (Undergraduate s : studentDAO.findByCourse(course.getCourseId())) {
            list.add(calculate(s, course, assessments, marks.getOrDefault(s.getStudentId(), new HashMap<>()),
                    attendance.get(s.getStudentId())));
        }
        return list;
    }

    /** Result of one student for one course. */
    public CourseResult getResult(Undergraduate s, Course course) throws DatabaseException {
        List<Assessment> assessments = assessmentDAO.findByCourse(course.getCourseId());
        Map<Integer, Double> marks = assessmentDAO.findMarks(s.getStudentId(), course.getCourseId());
        AttendanceSummary att = attendanceService.getStudentSummary(s, course);
        return calculate(s, course, assessments, marks, att);
    }

    /** Every course the student has taken, grouped by semester, with SGPA and CGPA. */
    public Transcript getTranscript(Undergraduate s) throws DatabaseException {
        Transcript transcript = new Transcript(s);
        Map<Integer, GradeDAO.StoredGrade> stored = gradeDAO.findByStudent(s.getStudentId());
        Map<String, SemesterResult> semesters = new TreeMap<>();
        for (Course c : courseDAO.findAllTakenByStudent(s.getStudentId())) {
            CourseResult r = getResult(s, c);
            boolean noLiveMarks = r.getRawMarks().isEmpty();
            GradeDAO.StoredGrade g = stored.get(c.getCourseId());
            if (noLiveMarks && g != null) {          // previous semester -> use stored grade
                r.setSource(CourseResult.Source.PUBLISHED);
                r.setTotalMarks(g.totalMarks);
                r.setGrade(GradeScale.fromLetter(g.grade));
                r.setRemarks(g.remarks == null ? "" : g.remarks);
                r.setAttendanceEligible(true);
                r.setCaWeight(0);
            }
            String key = c.getLevel() + "-" + c.getSemester();
            semesters.computeIfAbsent(key, k -> new SemesterResult(c.getLevel(), c.getSemester()))
                    .getResults().add(r);
        }
        transcript.getSemesters().addAll(semesters.values());
        return transcript;
    }

    /** Transcripts of every undergraduate (whole batch GPA view). */
    public List<Transcript> getAllTranscripts() throws DatabaseException {
        List<Transcript> list = new ArrayList<>();
        for (Undergraduate s : studentDAO.findAll()) {
            list.add(getTranscript(s));
        }
        return list;
    }

    /** The core calculation for one student / one course. */
    public CourseResult calculate(Undergraduate s, Course course, List<Assessment> assessments,
                                  Map<Integer, Double> marks, AttendanceSummary attendance) {
        CourseResult r = new CourseResult();
        r.setStudentId(s.getStudentId());
        r.setTgNo(s.getTgNo());
        r.setStudentName(s.getFullName());
        r.setStudentType(s.getStudentType());
        r.setCourse(course);

        double caObtained = 0;
        double caWeight = 0;
        double finalWeight = 0;
        Double finalRaw = null;
        for (Assessment a : assessments) {
            Double m = marks.get(a.getAssessmentId());
            if (m != null) {
                r.getRawMarks().put(a.getName(), m);
            }
            if (a.getType() == AssessmentType.FINAL) {
                finalWeight += a.getPercentage();
                if (m != null) finalRaw = m;
            } else {
                caWeight += a.getPercentage();
                if (m != null) caObtained += a.contribution(m);
            }
        }
        r.setCaObtained(caObtained);
        r.setCaWeight(caWeight);
        r.setFinalWeight(finalWeight);
        r.setFinalExamRaw(finalRaw);

        if (attendance != null && (attendance.getTheory() != null || attendance.getPractical() != null)) {
            r.setAttendancePercent(attendance.getCombinedPercentage(true));
            boolean anyHeld = (attendance.getTheory() != null && attendance.getTheory().getSessionsHeld() > 0)
                    || (attendance.getPractical() != null && attendance.getPractical().getSessionsHeld() > 0);
            r.setAttendanceEligible(!anyHeld || attendance.isEligible());
        }

        if (r.getRawMarks().isEmpty()) {
            r.setSource(CourseResult.Source.PENDING);
            r.setRemarks("No marks yet");
            return r;
        }
        if (!r.isEligible()) {
            r.setTotalMarks(caObtained + r.getFinalContribution());
            r.setGrade(GradeScale.E);
            r.setRemarks(r.getEligibilityText());
            return r;
        }
        if (finalRaw == null) {
            r.setRemarks("Final exam pending");
            return r;
        }
        double total = Math.round((caObtained + r.getFinalContribution()) * 100.0) / 100.0;
        r.setTotalMarks(total);
        GradeScale grade = GradeScale.fromMarks(total);
        if (s.isRepeat() && grade.getGradePoint() > GradeScale.repeatCap().getGradePoint()) {
            grade = GradeScale.repeatCap();
            r.setRemarks("Repeat - max grade C");
        } else {
            r.setRemarks(grade.isPass() ? "Pass" : "Repeat required");
        }
        r.setGrade(grade);
        return r;
    }
}

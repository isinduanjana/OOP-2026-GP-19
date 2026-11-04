package lk.ac.ruh.fot.ams.undergraduate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lk.ac.ruh.fot.ams.model.Notice;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.model.report.AttendanceSummary;
import lk.ac.ruh.fot.ams.model.report.CourseResult;
import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.model.report.Transcript;


public class StudentSnapshot {

    private final Undergraduate student;
    private final Transcript transcript;
    private final List<CourseResult> currentResults;
    private final List<AttendanceSummary> attendance;
    private final List<Notice> latestNotices;
    private final List<TimetableEntry> classes;
    private final String classesDayLabel;
    private final Map<MedicalStatus, Integer> medicalCounts;
    private final int enrolledCredits;

    public StudentSnapshot(Undergraduate student, Transcript transcript, List<CourseResult> currentResults,
                           List<AttendanceSummary> attendance, List<Notice> latestNotices,
                           List<TimetableEntry> classes, String classesDayLabel,
                           Map<MedicalStatus, Integer> medicalCounts, int enrolledCredits) {
        this.student = student;
        this.transcript = transcript;
        this.currentResults = Collections.unmodifiableList(new ArrayList<>(currentResults));
        this.attendance = Collections.unmodifiableList(new ArrayList<>(attendance));
        this.latestNotices = Collections.unmodifiableList(new ArrayList<>(latestNotices));
        this.classes = Collections.unmodifiableList(new ArrayList<>(classes));
        this.classesDayLabel = classesDayLabel;
        this.medicalCounts = new EnumMap<>(medicalCounts);
        this.enrolledCredits = enrolledCredits;
    }

    // ------------------------------------------------------------ calculated values
    /** CGPA over every graded course (common Transcript does the maths). */
    public double getCgpa() {
        return transcript.getCgpa();
    }

    public SemesterResult getCurrentSemester() {
        int level = student.getLevel();
        int sem = student.getSemester();
        if (!currentResults.isEmpty()) {
            level = currentResults.get(0).getCourse().getLevel();
            sem = currentResults.get(0).getCourse().getSemester();
        }
        for (SemesterResult s : transcript.getSemesters()) {
            if (s.getLevel() == level && s.getSemester() == sem) return s;
        }
        return null;
    }

    /** True when at least one course of the current semester already has a grade. */
    public boolean hasCurrentSgpa() {
        SemesterResult s = getCurrentSemester();
        return s != null && s.getGradedCredits() > 0;
    }

    public double getCurrentSgpa() {
        SemesterResult s = getCurrentSemester();
        return s == null ? 0 : s.getSgpa();
    }

    /** Average combined attendance (approved medicals counted) of the current courses. */
    public double getAverageAttendance() {
        double sum = 0;
        int n = 0;
        for (AttendanceSummary a : attendance) {
            if (sessionsHeld(a) > 0) {
                sum += a.getCombinedPercentage(true);
                n++;
            }
        }
        return n == 0 ? Double.NaN : sum / n;
    }

    /** Courses with attendance >= 80%. */
    public int getAttendanceEligibleCount() {
        int n = 0;
        for (AttendanceSummary a : attendance) if (a.isEligible()) n++;
        return n;
    }

    /** Courses where the student may sit the final exam (CA >= 40% AND attendance >= 80%). */
    public int getExamEligibleCount() {
        int n = 0;
        for (CourseResult r : currentResults) if (r.isEligible()) n++;
        return n;
    }

    /** Attendance summary of one current course (or null). */
    public AttendanceSummary getAttendanceFor(int courseId) {
        for (AttendanceSummary a : attendance) {
            if (a.getCourse().getCourseId() == courseId) return a;
        }
        return null;
    }

    public static int sessionsHeld(AttendanceSummary a) {
        return (a.getTheory() == null ? 0 : a.getTheory().getSessionsHeld())
                + (a.getPractical() == null ? 0 : a.getPractical().getSessionsHeld());
    }

    // ------------------------------------------------------------ getters
    public Undergraduate getStudent() { return student; }
    public Transcript getTranscript() { return transcript; }
    public List<CourseResult> getCurrentResults() { return currentResults; }
    public List<AttendanceSummary> getAttendance() { return attendance; }
    public List<Notice> getLatestNotices() { return latestNotices; }
    public List<TimetableEntry> getClasses() { return classes; }
    public String getClassesDayLabel() { return classesDayLabel; }
    public int getEnrolledCredits() { return enrolledCredits; }

    public int getMedicalCount(MedicalStatus status) {
        return medicalCounts.getOrDefault(status, 0);
    }
}

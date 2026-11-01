package lk.ac.ruh.fot.ams.model.report;

import java.util.LinkedHashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [COMMON PART] The complete result of one student for one course:
 * CA marks, final exam, total, attendance, eligibility, grade and grade point.
 * Built by ResultService.
 */
public class CourseResult {

    /** Where the result came from. */
    public enum Source { LIVE, PUBLISHED, PENDING }

    private int studentId;
    private String tgNo;
    private String studentName;
    private StudentType studentType;
    private Course course;

    private final Map<String, Double> rawMarks = new LinkedHashMap<>(); // assessment name -> mark /100
    private double caObtained;      // CA contribution to the final 100
    private double caWeight;        // total weight of CA (e.g. 30)
    private Double finalExamRaw;    // final exam mark /100 (null = not entered)
    private double finalWeight;     // weight of final exam (e.g. 70)
    private Double totalMarks;      // out of 100 (null = pending)
    private double attendancePercent = Double.NaN;
    private boolean attendanceEligible = true;
    private GradeScale grade;
    private String remarks = "";
    private Source source = Source.LIVE;

    /** CA as a percentage of the CA part (eligibility needs >= 40%). */
    public double getCaPercentage() {
        return caWeight == 0 ? 0 : caObtained * 100.0 / caWeight;
    }

    public boolean isCaEligible() {
        return getCaPercentage() + 1e-9 >= AppConfig.MIN_CA_PERCENT;
    }

    /** Final-exam eligibility = attendance >= 80% AND CA >= 40%. */
    public boolean isEligible() {
        return isCaEligible() && attendanceEligible;
    }

    public boolean hasGrade() {
        return grade != null;
    }

    public double getGradePoint() {
        return grade == null ? 0 : grade.getGradePoint();
    }

    public String getGradeLetter() {
        return grade == null ? "-" : grade.getLetter();
    }

    public double getFinalContribution() {
        return finalExamRaw == null ? 0 : finalExamRaw / 100.0 * finalWeight;
    }

    public String getEligibilityText() {
        if (isEligible()) return "Eligible";
        if (!isCaEligible() && !attendanceEligible) return "Not eligible (CA & attendance)";
        return !isCaEligible() ? "Not eligible (CA)" : "Not eligible (attendance)";
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = tgNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Map<String, Double> getRawMarks() { return rawMarks; }

    public double getCaObtained() { return caObtained; }
    public void setCaObtained(double caObtained) { this.caObtained = caObtained; }

    public double getCaWeight() { return caWeight; }
    public void setCaWeight(double caWeight) { this.caWeight = caWeight; }

    public Double getFinalExamRaw() { return finalExamRaw; }
    public void setFinalExamRaw(Double finalExamRaw) { this.finalExamRaw = finalExamRaw; }

    public double getFinalWeight() { return finalWeight; }
    public void setFinalWeight(double finalWeight) { this.finalWeight = finalWeight; }

    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }

    public double getAttendancePercent() { return attendancePercent; }
    public void setAttendancePercent(double attendancePercent) { this.attendancePercent = attendancePercent; }

    public boolean isAttendanceEligible() { return attendanceEligible; }
    public void setAttendanceEligible(boolean attendanceEligible) { this.attendanceEligible = attendanceEligible; }

    public GradeScale getGrade() { return grade; }
    public void setGrade(GradeScale grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}

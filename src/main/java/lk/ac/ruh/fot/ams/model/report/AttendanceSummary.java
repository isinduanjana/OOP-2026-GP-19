package lk.ac.ruh.fot.ams.model.report;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [COMMON PART] Attendance of one student in one course: theory, practical
 * and combined (combined is weighted by hours - handout: 1 session = 2 hours).
 */
public class AttendanceSummary {

    private int studentId;
    private String tgNo;
    private String studentName;
    private StudentType studentType;
    private Course course;
    private ComponentAttendance theory;      // null if course has no theory
    private ComponentAttendance practical;   // null if course has no practical

    /** Percentage for the selected view: THEORY, PRACTICAL or null = combined. */
    public double getPercentage(Component component, boolean withMedical) {
        ComponentAttendance c = component == Component.THEORY ? theory
                : component == Component.PRACTICAL ? practical : null;
        if (component != null) {
            if (c == null) return Double.NaN;
            return withMedical ? c.getPercentageWithMedical() : c.getPercentage();
        }
        return getCombinedPercentage(withMedical);
    }

    /** Combined (theory + practical) percentage weighted by hours. */
    public double getCombinedPercentage(boolean withMedical) {
        int total = 0;
        int attended = 0;
        for (ComponentAttendance c : new ComponentAttendance[]{theory, practical}) {
            if (c != null) {
                total += c.getTotalHours();
                attended += withMedical ? c.getAttendedHoursWithMedical() : c.getAttendedHours();
            }
        }
        return total == 0 ? 0 : attended * 100.0 / total;
    }

    public int getApprovedMedicalCount() {
        return (theory == null ? 0 : theory.getMedicalApproved())
                + (practical == null ? 0 : practical.getMedicalApproved());
    }

    public int getPendingMedicalCount() {
        return (theory == null ? 0 : theory.getMedicalPending())
                + (practical == null ? 0 : practical.getMedicalPending());
    }

    /** Eligible when combined attendance (approved medicals counted) >= 80%. */
    public boolean isEligible() {
        return getCombinedPercentage(true) + 1e-9 >= AppConfig.MIN_ATTENDANCE_PERCENT;
    }

    /** Which of the handout's five scenarios this record falls into. */
    public String getCategory() {
        double raw = getCombinedPercentage(false);
        double withMed = getCombinedPercentage(true);
        boolean hasMedical = getApprovedMedicalCount() > 0;
        double min = AppConfig.MIN_ATTENDANCE_PERCENT;
        if (hasMedical) {
            return withMed + 1e-9 >= min ? "≥ 80% with medical" : "< 80% with medical";
        }
        if (Math.abs(raw - min) < 1e-6) return "Exactly 80%";
        return raw > min ? "Above 80%" : "< 80% without medical";
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

    public ComponentAttendance getTheory() { return theory; }
    public void setTheory(ComponentAttendance theory) { this.theory = theory; }

    public ComponentAttendance getPractical() { return practical; }
    public void setPractical(ComponentAttendance practical) { this.practical = practical; }
}

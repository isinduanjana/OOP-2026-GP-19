package lk.ac.ruh.fot.ams.admin.model;

import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [M1 - ADMIN MODULE] One row of the enrollments table joined with the student's
 * TG number and name (shown in the "Enrollments" dialog of Course Management).
 *
 * OOP: Encapsulation (private fields + getters/setters), plain model object.
 */
public class Enrollment {

    private int enrollmentId;
    private int studentId;
    private int courseId;
    private String tgNo;
    private String studentName;
    private StudentType studentType;
    private String academicYear;
    private int semester;
    private String status;

    public boolean isWithdrawn() {
        return "WITHDRAWN".equals(status);
    }

    /** "Enrolled", "Withdrawn", "Completed". */
    public String getStatusLabel() {
        return status == null ? "-" : status.charAt(0) + status.substring(1).toLowerCase();
    }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = tgNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

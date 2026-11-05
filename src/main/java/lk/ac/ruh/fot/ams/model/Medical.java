package lk.ac.ruh.fot.ams.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/** [COMMON PART] A medical certificate submitted by an undergraduate. */
public class Medical {

    private int medicalId;
    private int studentId;
    private String tgNo;
    private String studentName;
    private Integer courseId;        // null = all courses on that day
    private String courseCode;
    private LocalDate medicalDate; // compatibility: same as fromDate
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime submittedAt;
    private String reason;
    private String documentPath;
    private MedicalStatus status = MedicalStatus.PENDING;
    private Integer approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;

    public boolean isApproved() {
        return status == MedicalStatus.APPROVED;
    }

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = tgNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public LocalDate getMedicalDate() { return medicalDate; }
    public void setMedicalDate(LocalDate medicalDate) { this.medicalDate = medicalDate; }

    public LocalDate getFromDate() { return fromDate != null ? fromDate : medicalDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; this.medicalDate = fromDate; }

    public LocalDate getToDate() { return toDate != null ? toDate : medicalDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public MedicalStatus getStatus() { return status; }
    public void setStatus(MedicalStatus status) { this.status = status; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}

package lk.ac.ruh.fot.ams.model.report;

import java.time.LocalDate;

import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/** [COMMON PART] One row of a student's day-by-day attendance. */
public class AttendanceRecord {

    private int attendanceId;
    private int sessionId;
    private int studentId;
    private LocalDate sessionDate;
    private Component component;
    private int sessionNumber;
    private String status;              // PRESENT / ABSENT
    private Integer medicalId;
    private MedicalStatus medicalStatus; // null if no medical

    public boolean isPresent() {
        return "PRESENT".equals(status);
    }

    /** "Present", "Absent", "Absent (Medical - APPROVED)". */
    public String getDisplayStatus() {
        if (isPresent()) return "Present";
        return medicalStatus == null ? "Absent" : "Absent (Medical - " + medicalStatus + ")";
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMedicalId() { return medicalId; }
    public void setMedicalId(Integer medicalId) { this.medicalId = medicalId; }

    public MedicalStatus getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(MedicalStatus medicalStatus) { this.medicalStatus = medicalStatus; }
}

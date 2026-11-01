package lk.ac.ruh.fot.ams.model;

import java.time.LocalDate;

import lk.ac.ruh.fot.ams.model.enums.Component;

/** [COMMON PART] One lecture / practical session that attendance is taken for. */
public class AttendanceSession {

    private int sessionId;
    private int courseId;
    private String courseCode;
    private LocalDate sessionDate;
    private Component component;
    private int sessionNumber;
    private int totalSessions = 15;
    private int hours = 2;
    private int presentCount;
    private int absentCount;

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    @Override
    public String toString() {
        return component + " #" + sessionNumber + " (" + sessionDate + ")";
    }
}

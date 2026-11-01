package lk.ac.ruh.fot.ams.model.report;

import lk.ac.ruh.fot.ams.model.enums.Component;

/**
 * [COMMON PART] Attendance numbers of ONE student for ONE component
 * (theory or practical) of a course.
 */
public class ComponentAttendance {

    private final Component component;
    private int sessionsHeld;
    private int present;
    private int medicalApproved;     // absences covered by an APPROVED medical
    private int medicalPending;      // absences with a PENDING / REJECTED medical
    private int hoursPerSession = 2;

    public ComponentAttendance(Component component) {
        this.component = component;
    }

    public int getAbsent() {
        return Math.max(0, sessionsHeld - present);
    }

    /** Plain attendance = present / held. */
    public double getPercentage() {
        return sessionsHeld == 0 ? 0 : present * 100.0 / sessionsHeld;
    }

    /** Attendance counting approved medicals as attended (used for eligibility). */
    public double getPercentageWithMedical() {
        return sessionsHeld == 0 ? 0 : (present + medicalApproved) * 100.0 / sessionsHeld;
    }

    public int getTotalHours()             { return sessionsHeld * hoursPerSession; }
    public int getAttendedHours()          { return present * hoursPerSession; }
    public int getAttendedHoursWithMedical() { return (present + medicalApproved) * hoursPerSession; }

    public Component getComponent() { return component; }

    public int getSessionsHeld() { return sessionsHeld; }
    public void setSessionsHeld(int sessionsHeld) { this.sessionsHeld = sessionsHeld; }

    public int getPresent() { return present; }
    public void setPresent(int present) { this.present = present; }

    public int getMedicalApproved() { return medicalApproved; }
    public void setMedicalApproved(int medicalApproved) { this.medicalApproved = medicalApproved; }

    public int getMedicalPending() { return medicalPending; }
    public void setMedicalPending(int medicalPending) { this.medicalPending = medicalPending; }

    public int getHoursPerSession() { return hoursPerSession; }
    public void setHoursPerSession(int hoursPerSession) { this.hoursPerSession = hoursPerSession; }
}

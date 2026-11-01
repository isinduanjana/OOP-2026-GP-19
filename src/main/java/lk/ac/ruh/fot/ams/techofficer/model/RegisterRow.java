package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [M3 - TECHNICAL OFFICER MODULE] One line of the attendance register of a
 * session: an enrolled student + what is saved for them (if anything) + the
 * medical they submitted for that day (if any).
 *
 * OOP: Encapsulation (private fields, getters/setters, small helper methods).
 */
public class RegisterRow {

    private int studentId;
    private String tgNo;
    private String studentName;
    private StudentType studentType;
    private Boolean present;               // null = not marked yet for this session
    private MedicalStatus linkedMedical;   // medical already linked to the saved absence
    private MedicalStatus dayMedical;      // best medical of that student for that day/course

    /** True when this student already has a saved row for the session. */
    public boolean isMarked() {
        return present != null;
    }

    /** Medical status to show: the linked one, otherwise the one found for that day. */
    public MedicalStatus getMedicalToShow() {
        return linkedMedical != null ? linkedMedical : dayMedical;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = tgNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public Boolean getPresent() { return present; }
    public void setPresent(Boolean present) { this.present = present; }

    public MedicalStatus getLinkedMedical() { return linkedMedical; }
    public void setLinkedMedical(MedicalStatus linkedMedical) { this.linkedMedical = linkedMedical; }

    public MedicalStatus getDayMedical() { return dayMedical; }
    public void setDayMedical(MedicalStatus dayMedical) { this.dayMedical = dayMedical; }
}

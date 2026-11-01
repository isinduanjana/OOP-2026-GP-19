package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Approves a medical. After approval the absent
 * sessions of that day count towards the 80% attendance (handled by the common
 * AttendanceDAO / AttendanceSummary through attendance.medical_id).
 *
 * OOP: implements the MedicalDecision interface (polymorphism).
 */
public class ApproveDecision implements MedicalDecision {

    @Override
    public MedicalStatus getResultStatus() {
        return MedicalStatus.APPROVED;
    }

    @Override
    public String getActionName() {
        return "Approve";
    }

    @Override
    public String getIconName() {
        return "check";
    }

    @Override
    public boolean isAllowedFor(Medical medical) {
        return medical.getStatus() != MedicalStatus.APPROVED;
    }

    @Override
    public boolean recordsDecisionMaker() {
        return true;
    }

    @Override
    public String confirmMessage(Medical m) {
        return MedicalDecision.super.confirmMessage(m)
                + "\nThe student's absences on that day will be counted as covered by a medical.";
    }
}

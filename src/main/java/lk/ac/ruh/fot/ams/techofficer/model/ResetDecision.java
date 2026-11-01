package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Puts a medical back to PENDING (to undo a wrong
 * approve / reject). Clears approved_by and approved_at.
 *
 * OOP: implements the MedicalDecision interface (polymorphism).
 */
public class ResetDecision implements MedicalDecision {

    @Override
    public MedicalStatus getResultStatus() {
        return MedicalStatus.PENDING;
    }

    @Override
    public String getActionName() {
        return "Mark pending";
    }

    @Override
    public String getIconName() {
        return "refresh";
    }

    @Override
    public boolean isAllowedFor(Medical medical) {
        return medical.getStatus() != MedicalStatus.PENDING;
    }

    @Override
    public boolean recordsDecisionMaker() {
        return false;
    }

    @Override
    public String confirmMessage(Medical m) {
        return "Move the medical of " + m.getTgNo() + " for " + m.getMedicalDate() + " back to PENDING?";
    }
}

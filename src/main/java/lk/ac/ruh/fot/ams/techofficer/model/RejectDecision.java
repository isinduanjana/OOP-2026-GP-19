package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Rejects a medical. The absence stays linked to
 * the medical (so everyone can see a medical WAS submitted) but it does not
 * count towards attendance.
 *
 * OOP: implements the MedicalDecision interface (polymorphism).
 */
public class RejectDecision implements MedicalDecision {

    @Override
    public MedicalStatus getResultStatus() {
        return MedicalStatus.REJECTED;
    }

    @Override
    public String getActionName() {
        return "Reject";
    }

    @Override
    public String getIconName() {
        return "trash";
    }

    @Override
    public boolean isAllowedFor(Medical medical) {
        return medical.getStatus() != MedicalStatus.REJECTED;
    }

    @Override
    public boolean recordsDecisionMaker() {
        return true;
    }
}

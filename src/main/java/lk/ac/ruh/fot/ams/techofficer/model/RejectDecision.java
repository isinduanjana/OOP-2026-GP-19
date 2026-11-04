package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;


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

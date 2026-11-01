package lk.ac.ruh.fot.ams.techofficer.model;

import lk.ac.ruh.fot.ams.model.Medical;
import lk.ac.ruh.fot.ams.model.enums.MedicalStatus;

/**
 * [M3 - TECHNICAL OFFICER MODULE] A decision a TO can take on a medical
 * (Strategy pattern). Implemented by ApproveDecision, RejectDecision and
 * ResetDecision.
 *
 * The screens build their Approve / Reject / Mark pending buttons from a list of
 * MedicalDecision objects and TOMedicalDAO.applyDecision(...) works with ANY
 * decision - it never asks "which one is it?".
 *
 * OOP: Abstraction (interface) + Polymorphism (each class answers differently).
 */
public interface MedicalDecision {

    /** Status the medical gets after this decision. */
    MedicalStatus getResultStatus();

    /** Button text, e.g. "Approve". */
    String getActionName();

    /** Icon name from the common Icons class. */
    String getIconName();

    /** False when the decision makes no sense (e.g. approving an approved medical). */
    boolean isAllowedFor(Medical medical);

    /** True = save who decided and when (approved_by / approved_at). */
    boolean recordsDecisionMaker();

    /** Question shown before applying the decision. */
    default String confirmMessage(Medical m) {
        return getActionName() + " the medical of " + m.getTgNo() + " (" + m.getStudentName() + ") for "
                + m.getMedicalDate() + "?";
    }
}

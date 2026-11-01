package lk.ac.ruh.fot.ams.admin;

import lk.ac.ruh.fot.ams.exception.ValidationException;

/**
 * [M1 - ADMIN MODULE] Thrown when a value that must be unique already exists
 * (course code, TG number, employee number ...).
 *
 * OOP: Custom exception + Inheritance. It extends ValidationException from the
 * common part, so BasePanel.safe() and every "catch (AppException e)" already
 * know how to show it - no extra code needed in the screens.
 */
public class DuplicateRecordException extends ValidationException {

    private final String duplicateValue;

    public DuplicateRecordException(String fieldName, String duplicateValue) {
        super(fieldName, fieldName + " '" + duplicateValue + "' is already used by another record.");
        this.duplicateValue = duplicateValue;
    }

    /** With a custom message. */
    public DuplicateRecordException(String fieldName, String duplicateValue, String message) {
        super(fieldName, message);
        this.duplicateValue = duplicateValue;
    }

    public String getDuplicateValue() {
        return duplicateValue;
    }
}

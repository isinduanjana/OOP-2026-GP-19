package lk.ac.ruh.fot.ams.admin;

import lk.ac.ruh.fot.ams.exception.ValidationException;


public class DuplicateRecordException extends ValidationException {

    private final String duplicateValue;

    public DuplicateRecordException(String fieldName, String duplicateValue) {
        super(fieldName, fieldName + " '" + duplicateValue + "' is already used by another record.");
        this.duplicateValue = duplicateValue;
    }

    public DuplicateRecordException(String fieldName, String duplicateValue, String message) {
        super(fieldName, message);
        this.duplicateValue = duplicateValue;
    }

    public String getDuplicateValue() {
        return duplicateValue;
    }
}

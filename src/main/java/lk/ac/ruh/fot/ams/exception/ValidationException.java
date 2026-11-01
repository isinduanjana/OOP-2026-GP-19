package lk.ac.ruh.fot.ams.exception;

/**
 * [COMMON PART] Thrown when user input on a form is invalid
 * (empty field, bad email, marks not between 0-100 ...).
 */
public class ValidationException extends AppException {

    private final String fieldName;

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public ValidationException(String message) {
        this(null, message);
    }

    public String getFieldName() {
        return fieldName;
    }
}

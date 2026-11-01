package lk.ac.ruh.fot.ams.exception;

/**
 * [COMMON PART] Root of the application's own (checked) exception hierarchy.
 *
 * OOP: Inheritance - DatabaseException, ValidationException and
 * AuthenticationException all extend this class, so the GUI can catch
 * "any application problem" with a single catch (AppException e).
 */
public class AppException extends Exception {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Message that is safe to show to the user in a dialog. */
    public String getUserMessage() {
        return getMessage();
    }
}

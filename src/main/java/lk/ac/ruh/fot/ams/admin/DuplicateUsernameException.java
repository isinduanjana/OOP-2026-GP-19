package lk.ac.ruh.fot.ams.admin;

/**
 * [M1 - ADMIN MODULE] Thrown when the admin tries to create (or rename) an
 * account with a username that is already taken.
 *
 * OOP: multi-level inheritance of exceptions
 *      AppException -> ValidationException -> DuplicateRecordException -> DuplicateUsernameException
 */
public class DuplicateUsernameException extends DuplicateRecordException {

    public DuplicateUsernameException(String username) {
        super("Username", username, "The username '" + username + "' is already taken. Please choose another one.");
    }
}

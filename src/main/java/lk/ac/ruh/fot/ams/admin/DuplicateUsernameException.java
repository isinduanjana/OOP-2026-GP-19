package lk.ac.ruh.fot.ams.admin;


public class DuplicateUsernameException extends DuplicateRecordException {

    public DuplicateUsernameException(String username) {
        super("Username", username, "The username '" + username + "' is already taken. Please choose another one.");
    }
}

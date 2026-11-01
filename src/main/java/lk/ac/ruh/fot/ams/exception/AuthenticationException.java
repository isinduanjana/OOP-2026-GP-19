package lk.ac.ruh.fot.ams.exception;

/** [COMMON PART] Thrown when login fails (wrong password, inactive account ...). */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message);
    }
}

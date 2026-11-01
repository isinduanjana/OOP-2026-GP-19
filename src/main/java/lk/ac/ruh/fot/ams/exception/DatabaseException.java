package lk.ac.ruh.fot.ams.exception;

import java.sql.SQLException;

/**
 * [COMMON PART] Wraps any SQLException thrown by the DAO layer.
 * OOP: Inheritance + Polymorphism (overrides getUserMessage()).
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getUserMessage() {
        Throwable c = getCause();
        if (c instanceof SQLException sql) {
            // 1062 = duplicate key, 1451/1452 = foreign key problems (MySQL error codes)
            switch (sql.getErrorCode()) {
                case 1062: return getMessage() + "\nThis record already exists (duplicate value).";
                case 1451: return getMessage() + "\nThis record is used by other records and cannot be removed.";
                case 1452: return getMessage() + "\nA related record does not exist.";
                default:   return getMessage() + "\n" + sql.getMessage();
            }
        }
        return super.getUserMessage();
    }
}

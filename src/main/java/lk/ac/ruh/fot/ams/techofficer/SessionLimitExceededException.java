package lk.ac.ruh.fot.ams.techofficer;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.enums.Component;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Thrown when the TO tries to create more than
 * 15 sessions (AppConfig.SESSIONS_PER_COMPONENT) for one course component, or
 * uses a session number outside 1..15.
 *
 * OOP: Inheritance (custom exception extends the common ValidationException,
 * so BasePanel.safe(...) shows it like any other input error) + Encapsulation.
 */
public class SessionLimitExceededException extends ValidationException {

    private final String courseCode;
    private final Component component;

    public SessionLimitExceededException(String courseCode, Component component) {
        super("Session number", courseCode + " " + component + " already has the maximum of "
                + AppConfig.SESSIONS_PER_COMPONENT + " sessions.\n"
                + "Handout rule: theory and practical each have " + AppConfig.SESSIONS_PER_COMPONENT
                + " sessions. Edit or delete an existing session instead.");
        this.courseCode = courseCode;
        this.component = component;
    }

    /** Used when the given session number itself is out of range. */
    public SessionLimitExceededException(int sessionNumber) {
        super("Session number", "Session number " + sessionNumber + " is not allowed. It must be between 1 and "
                + AppConfig.SESSIONS_PER_COMPONENT + ".");
        this.courseCode = null;
        this.component = null;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public Component getComponent() {
        return component;
    }
}

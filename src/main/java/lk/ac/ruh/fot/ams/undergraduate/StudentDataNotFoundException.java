package lk.ac.ruh.fot.ams.undergraduate;

import lk.ac.ruh.fot.ams.exception.AppException;

/**
 * [M4 - UNDERGRADUATE MODULE] Thrown when the logged-in student's row in the
 * "undergraduates" table cannot be found (e.g. the admin deleted it while the
 * student was still logged in).
 *
 * OOP: Inheritance (extends the common AppException, so every page's safe(...)
 * helper from BasePanel catches it and shows a friendly dialog),
 * Polymorphism (overrides getUserMessage()), Encapsulation (private studentId).
 */
public class StudentDataNotFoundException extends AppException {

    private final int studentId;

    public StudentDataNotFoundException(int studentId) {
        super("Student record #" + studentId + " was not found in the database.");
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }

    /** Shown in the error dialog instead of the technical message. */
    @Override
    public String getUserMessage() {
        return "Your student record could not be found (student ID " + studentId + ").\n"
                + "It may have been removed by the administrator. Please contact the faculty office.";
    }
}

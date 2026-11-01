package lk.ac.ruh.fot.ams.lecturer;

import lk.ac.ruh.fot.ams.exception.ValidationException;

/**
 * [M2 - LECTURER MODULE] Thrown when a lecturer types a mark that is not between 0 and 100
 * (handout: "all marks are out of 100").
 *
 * OOP: Inheritance (extends the common ValidationException -> AppException, so the
 * common BasePanel.safe(...) helper catches it and shows a dialog),
 * Encapsulation (private fields with getters), Polymorphism (overrides getUserMessage()).
 */
public class MarksOutOfRangeException extends ValidationException {

    public static final double MIN = 0;
    public static final double MAX = 100;

    private final String studentTg;
    private final double enteredValue;

    public MarksOutOfRangeException(String studentTg, double enteredValue) {
        super("Mark", "Mark " + trim(enteredValue) + " for " + studentTg + " is out of range.");
        this.studentTg = studentTg;
        this.enteredValue = enteredValue;
    }

    public String getStudentTg() {
        return studentTg;
    }

    public double getEnteredValue() {
        return enteredValue;
    }

    /** Friendlier text for the dialog (overrides AppException.getUserMessage). */
    @Override
    public String getUserMessage() {
        return "Invalid mark for " + studentTg + ": " + trim(enteredValue)
                + "\nAll marks must be between " + (int) MIN + " and " + (int) MAX + " (marks are out of 100).";
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}

package lk.ac.ruh.fot.ams.lecturer;

import lk.ac.ruh.fot.ams.exception.ValidationException;


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


    @Override
    public String getUserMessage() {
        return "Invalid mark for " + studentTg + ": " + trim(enteredValue)
                + "\nAll marks must be between " + (int) MIN + " and " + (int) MAX + " (marks are out of 100).";
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}

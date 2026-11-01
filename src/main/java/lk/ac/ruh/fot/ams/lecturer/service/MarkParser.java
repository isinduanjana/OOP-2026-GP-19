package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.lecturer.MarksOutOfRangeException;

/**
 * [M2 - LECTURER MODULE] Turns the text typed into the marks table into a number.
 *   ""      -> null (mark not entered)
 *   "72.5"  -> 72.5
 *   "abc"   -> ValidationException
 *   "120"   -> MarksOutOfRangeException (our own custom exception)
 *
 * OOP: Exception handling (throws checked exceptions of the common hierarchy),
 * Encapsulation (private constructor - static utility).
 */
public final class MarkParser {

    private MarkParser() {
    }

    /** @return the mark (0-100) or null when the text is empty. */
    public static Double parse(String raw, String studentTg) throws ValidationException {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equals("-")) {
            return null;
        }
        double value;
        try {
            value = Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Mark", "'" + raw.trim() + "' for " + studentTg + " is not a number.");
        }
        if (Double.isNaN(value) || value < MarksOutOfRangeException.MIN || value > MarksOutOfRangeException.MAX) {
            throw new MarksOutOfRangeException(studentTg, value);
        }
        return Math.round(value * 100.0) / 100.0;   // DECIMAL(5,2) in the marks table
    }

    /** Shows 72.0 as "72" and 72.5 as "72.5" in the table. */
    public static String format(Double mark) {
        if (mark == null) return "";
        return mark == Math.rint(mark) ? String.valueOf(mark.longValue()) : String.valueOf(mark);
    }
}

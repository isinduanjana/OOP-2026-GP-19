package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.lecturer.MarksOutOfRangeException;


public final class MarkParser {

    private MarkParser() {
    }


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


    public static String format(Double mark) {
        if (mark == null) return "";
        return mark == Math.rint(mark) ? String.valueOf(mark.longValue()) : String.valueOf(mark);
    }
}

package lk.ac.ruh.fot.ams.admin;

import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.TimetableEntry;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] Thrown when a new / edited timetable slot overlaps with an
 * existing slot in the same room (or the same course) on the same day.
 *
 * OOP: Custom exception that also carries data (the clashing entry) -> encapsulation.
 */
public class TimetableClashException extends ValidationException {

    private final transient TimetableEntry clashWith;

    public TimetableClashException(String reason, TimetableEntry clashWith) {
        super("Room", reason + "\n\nClashes with: " + clashWith.getCourseCode() + " (" + clashWith.getComponent() + ")  "
                + capital(clashWith.getDay()) + " " + Format.time(clashWith.getStartTime()) + " - "
                + Format.time(clashWith.getEndTime()) + "  in " + clashWith.getRoom()
                + "\n\nPlease choose another time or room.");
        this.clashWith = clashWith;
    }

    public TimetableEntry getClashWith() {
        return clashWith;
    }

    private static String capital(String s) {
        return s == null || s.isEmpty() ? "" : s.charAt(0) + s.substring(1).toLowerCase();
    }
}

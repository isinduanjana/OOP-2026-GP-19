package lk.ac.ruh.fot.ams.techofficer;

import java.time.LocalDate;

import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Thrown when a medical already exists for the
 * same student, date and course (or "all courses" of that day).
 *
 * OOP: Inheritance (extends the common ValidationException) - the GUI catches it
 * through the AppException parent type, which is polymorphism in exception handling.
 */
public class DuplicateMedicalException extends ValidationException {

    private final int existingMedicalId;

    public DuplicateMedicalException(String tgNo, LocalDate date, String courseLabel, int existingMedicalId) {
        super("Medical", "A medical for " + tgNo + " on " + Format.date(date) + " (" + courseLabel + ")"
                + " is already recorded (medical #" + existingMedicalId + ").\n"
                + "Edit the existing medical instead of adding a new one.");
        this.existingMedicalId = existingMedicalId;
    }

    public int getExistingMedicalId() {
        return existingMedicalId;
    }
}

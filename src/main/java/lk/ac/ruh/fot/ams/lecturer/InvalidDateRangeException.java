package lk.ac.ruh.fot.ams.lecturer;

import java.time.LocalDate;

import lk.ac.ruh.fot.ams.exception.ValidationException;

/**
 * [M2 - LECTURER MODULE] Thrown when a course material's "To" date is before
 * its "From" date.
 *
 * OOP: Inheritance (extends the common ValidationException -> AppException),
 * Encapsulation (keeps the two dates), Polymorphism (overrides getUserMessage()).
 */
public class InvalidDateRangeException extends ValidationException {

    private final LocalDate from;
    private final LocalDate to;

    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super("Date range", "The 'To' date must be the same as or after the 'From' date.");
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }

    @Override
    public String getUserMessage() {
        return getMessage() + "\nFrom: " + from + "\nTo:   " + to;
    }
}

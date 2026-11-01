package lk.ac.ruh.fot.ams.admin;

import lk.ac.ruh.fot.ams.exception.ValidationException;

/**
 * [M1 - ADMIN MODULE] Thrown when the weights (%) of a course's assessments
 * would add up to more than 100%.
 *
 * OOP: Custom exception extending the common ValidationException.
 */
public class WeightLimitExceededException extends ValidationException {

    public WeightLimitExceededException(double alreadyUsed, double requested) {
        super("Weight", String.format("The assessments of this course already use %.0f%%.%n"
                + "Adding %.0f%% would make the total %.0f%% (maximum is 100%%).",
                alreadyUsed, requested, alreadyUsed + requested));
    }
}

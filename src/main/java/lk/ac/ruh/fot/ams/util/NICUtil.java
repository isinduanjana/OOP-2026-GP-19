package lk.ac.ruh.fot.ams.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.DateTimeException;
import java.util.regex.Pattern;

public class NICUtil {

    private static final Pattern OLD_NIC =
            Pattern.compile("^\\d{9}[VvXx]$");

    private static final Pattern NEW_NIC =
            Pattern.compile("^\\d{12}$");

    // Validate NIC format
    public static boolean isValidNIC(String nic) {

        if (nic == null) {
            return false;
        }

        nic = nic.trim();

        return OLD_NIC.matcher(nic).matches()
                || NEW_NIC.matcher(nic).matches();
    }

    // Extract Date of Birth
    public static LocalDate getDateOfBirth(String nic) {

        if (!isValidNIC(nic)) {
            throw new IllegalArgumentException("Invalid NIC number");
        }

        nic = nic.trim().toUpperCase();

        int year;
        int day;

        if (nic.length() == 10) {

            // Old NIC
            int shortYear = Integer.parseInt(
                    nic.substring(0, 2));

            year = (shortYear <= 26)
                    ? 2000 + shortYear
                    : 1900 + shortYear;

            day = Integer.parseInt(
                    nic.substring(2, 5));

        } else {

            // New NIC
            year = Integer.parseInt(
                    nic.substring(0, 4));

            day = Integer.parseInt(
                    nic.substring(4, 7));
        }

        // Female NIC identification
        if (day > 500) {
            day -= 500;
        }

        if (day < 1 || day > 366) {
            throw new IllegalArgumentException(
                    "Invalid date in NIC");
        }

        LocalDate dob;

        try {
            dob = LocalDate.of(year, 1, 1)
                    .plusDays(day - 1);

        } catch (DateTimeException e) {

            throw new IllegalArgumentException(
                    "Invalid date of birth");
        }

        // Reject impossible future dates
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Future date of birth is not allowed");
        }

        return dob;
    }

    // Calculate Age
    public static int calculateAge(LocalDate dob) {

        if (dob == null || dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Invalid date of birth");
        }

        return Period.between(
                dob, LocalDate.now()).getYears();
    }
}
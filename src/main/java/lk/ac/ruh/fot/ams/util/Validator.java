package lk.ac.ruh.fot.ams.util;

import java.util.regex.Pattern;

import lk.ac.ruh.fot.ams.exception.ValidationException;

/**
 * [COMMON PART] Input checks used by all forms. Each method throws a
 * ValidationException with a friendly message when the input is wrong.
 */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{9,12}$");
    private static final Pattern TG_NO = Pattern.compile("^TG/\\d{4}/\\d{3,4}$", Pattern.CASE_INSENSITIVE);

    private Validator() {
    }

    public static String required(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field, field + " is required.");
        }
        return value.trim();
    }

    public static String email(String value) throws ValidationException {
        String v = required(value, "Email");
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidationException("Email", "Please enter a valid email address.");
        }
        return v;
    }

    /** Optional email: empty is allowed. */
    public static String optionalEmail(String value) throws ValidationException {
        return value == null || value.isBlank() ? null : email(value);
    }

    public static String phone(String value) throws ValidationException {
        String v = required(value, "Phone").replace(" ", "").replace("-", "");
        if (!PHONE.matcher(v).matches()) {
            throw new ValidationException("Phone", "Phone number must contain 9-12 digits (e.g. 0712345678).");
        }
        return v;
    }

    public static String optionalPhone(String value) throws ValidationException {
        return value == null || value.isBlank() ? null : phone(value);
    }

    public static String tgNumber(String value) throws ValidationException {
        String v = required(value, "TG number").toUpperCase();
        if (!TG_NO.matcher(v).matches()) {
            throw new ValidationException("TG number", "TG number must look like TG/2022/1234.");
        }
        return v;
    }

    public static int integer(String value, String field, int min, int max) throws ValidationException {
        String v = required(value, field);
        try {
            int n = Integer.parseInt(v);
            if (n < min || n > max) {
                throw new ValidationException(field, field + " must be between " + min + " and " + max + ".");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " must be a whole number.");
        }
    }

    /** All marks are out of 100 (handout). */
    public static double marks(String value, String field) throws ValidationException {
        String v = required(value, field);
        try {
            double d = Double.parseDouble(v);
            if (d < 0 || d > 100) {
                throw new ValidationException(field, field + ": marks must be between 0 and 100.");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + ": '" + v + "' is not a number.");
        }
    }

    public static String password(String value) throws ValidationException {
        String v = required(value, "Password");
        if (v.length() < 4) {
            throw new ValidationException("Password", "Password must have at least 4 characters.");
        }
        return v;
    }
}

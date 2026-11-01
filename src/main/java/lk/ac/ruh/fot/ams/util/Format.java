package lk.ac.ruh.fot.ams.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** [COMMON PART] Formatting helpers for numbers and dates shown in the GUI. */
public final class Format {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter NICE_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    private Format() {
    }

    public static String percent(double v) {
        return Double.isNaN(v) ? "-" : String.format("%.1f%%", v);
    }

    public static String num(double v) {
        return Double.isNaN(v) ? "-" : String.format("%.2f", v);
    }

    public static String num(Double v) {
        return v == null ? "-" : num(v.doubleValue());
    }

    public static String gpa(double v) {
        return String.format("%.2f", v);
    }

    public static String date(LocalDate d) {
        return d == null ? "-" : d.format(DATE);
    }

    public static String dateTime(LocalDateTime d) {
        return d == null ? "-" : d.format(DATE_TIME);
    }

    public static String time(LocalTime t) {
        return t == null ? "-" : t.format(TIME);
    }

    public static String today() {
        return LocalDate.now().format(NICE_DATE);
    }

    public static String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}

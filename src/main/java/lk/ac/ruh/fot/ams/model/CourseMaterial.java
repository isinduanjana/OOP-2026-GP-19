package lk.ac.ruh.fot.ams.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [COMMON PART] A file/material a lecturer added to a course.
 * coversFrom / coversTo = the period of the course the material belongs to
 * (e.g. week 1: 2026-07-06 to 2026-07-10). Both may be null for old rows.
 */
public class CourseMaterial {

    private static final DateTimeFormatter RANGE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private int materialId;
    private int courseId;
    private String courseCode;
    private Integer lecturerId;
    private String lecturerName;
    private String title;
    private String filePath;
    private LocalDate coversFrom;
    private LocalDate coversTo;
    private LocalDateTime uploadedAt;

    /** "2026-07-06 → 2026-07-10", "From 2026-07-06", or "-" when no dates are set. */
    public String getCoverageLabel() {
        if (coversFrom == null && coversTo == null) return "-";
        if (coversTo == null) return "From " + coversFrom.format(RANGE);
        if (coversFrom == null) return "Until " + coversTo.format(RANGE);
        if (coversFrom.equals(coversTo)) return coversFrom.format(RANGE);
        return coversFrom.format(RANGE) + " → " + coversTo.format(RANGE);
    }

    /** Short form for narrow table columns: "06 Jul – 10 Jul" (year added if the range crosses a year). */
    public String getCoverageShort() {
        if (coversFrom == null || coversTo == null) return getCoverageLabel();
        DateTimeFormatter dm = DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.ENGLISH);
        DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd MMM yy", java.util.Locale.ENGLISH);
        if (coversFrom.equals(coversTo)) return coversFrom.format(dm);
        DateTimeFormatter f = coversFrom.getYear() == coversTo.getYear() ? dm : dmy;
        return coversFrom.format(f) + " – " + coversTo.format(f);
    }

    /** True when the given day falls inside the material's date range. */
    public boolean covers(LocalDate day) {
        return day != null && coversFrom != null && coversTo != null
                && !day.isBefore(coversFrom) && !day.isAfter(coversTo);
    }

    public LocalDate getCoversFrom() { return coversFrom; }
    public void setCoversFrom(LocalDate coversFrom) { this.coversFrom = coversFrom; }

    public LocalDate getCoversTo() { return coversTo; }
    public void setCoversTo(LocalDate coversTo) { this.coversTo = coversTo; }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public Integer getLecturerId() { return lecturerId; }
    public void setLecturerId(Integer lecturerId) { this.lecturerId = lecturerId; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

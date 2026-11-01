package lk.ac.ruh.fot.ams.model;

import java.time.LocalTime;

import lk.ac.ruh.fot.ams.model.enums.Component;

/** [COMMON PART] One slot in the weekly timetable. */
public class TimetableEntry {

    public static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

    private int timetableId;
    private int departmentId;
    private String departmentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private Component component = Component.THEORY;
    private int level;
    private int semester;

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Component getComponent() { return component; }
    public void setComponent(Component component) { this.component = component; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    /** Index of the day (0 = Monday) or -1. */
    public int getDayIndex() {
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day)) return i;
        }
        return -1;
    }
}

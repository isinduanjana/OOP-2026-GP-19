package lk.ac.ruh.fot.ams.model;

/**
 * [COMMON PART] A course module (e.g. ICT2132).
 * theoryHours / practicalHours are the hours of ONE session of that component
 * (handout: 1 session = 2 hours). 0 means the course has no such component.
 */
public class Course {

    private int courseId;
    private String courseCode;
    private String courseName;
    private int credits;
    private int theoryHours;
    private int practicalHours;
    private String description;
    private Integer departmentId;
    private String departmentName;
    private int level;
    private int semester;
    private String lecturerNames;   // filled by some queries (comma separated)

    public boolean hasTheory()    { return theoryHours > 0; }
    public boolean hasPractical() { return practicalHours > 0; }

    /** "Theory + Practical", "Theory", "Practical". */
    public String getComponentLabel() {
        if (hasTheory() && hasPractical()) return "Theory + Practical";
        return hasTheory() ? "Theory" : (hasPractical() ? "Practical" : "-");
    }

    public String getLevelSemesterLabel() {
        return "L" + level + " S" + semester;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getTheoryHours() { return theoryHours; }
    public void setTheoryHours(int theoryHours) { this.theoryHours = theoryHours; }

    public int getPracticalHours() { return practicalHours; }
    public void setPracticalHours(int practicalHours) { this.practicalHours = practicalHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getLecturerNames() { return lecturerNames; }
    public void setLecturerNames(String lecturerNames) { this.lecturerNames = lecturerNames; }

    /** Shown in combo boxes. */
    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Course c && c.courseId == courseId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(courseId);
    }
}

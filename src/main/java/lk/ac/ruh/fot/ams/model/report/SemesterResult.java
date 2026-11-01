package lk.ac.ruh.fot.ams.model.report;

import java.util.ArrayList;
import java.util.List;

/**
 * [COMMON PART] All course results of one student in one semester + SGPA.
 * SGPA = sum(grade point x credits) / sum(credits)  (graded courses only)
 */
public class SemesterResult {

    private final int level;
    private final int semester;
    private final List<CourseResult> results = new ArrayList<>();

    public SemesterResult(int level, int semester) {
        this.level = level;
        this.semester = semester;
    }

    public double getSgpa() {
        double points = 0;
        int credits = getGradedCredits();
        for (CourseResult r : results) {
            if (r.hasGrade()) {
                points += r.getGradePoint() * r.getCourse().getCredits();
            }
        }
        return credits == 0 ? 0 : points / credits;
    }

    public int getGradedCredits() {
        int credits = 0;
        for (CourseResult r : results) {
            if (r.hasGrade()) credits += r.getCourse().getCredits();
        }
        return credits;
    }

    public double getWeightedPoints() {
        double points = 0;
        for (CourseResult r : results) {
            if (r.hasGrade()) points += r.getGradePoint() * r.getCourse().getCredits();
        }
        return points;
    }

    public String getLabel() {
        return "Level " + level + " - Semester " + semester;
    }

    public int getLevel() { return level; }
    public int getSemester() { return semester; }
    public List<CourseResult> getResults() { return results; }
}

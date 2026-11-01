package lk.ac.ruh.fot.ams.model.report;

import java.util.ArrayList;
import java.util.List;

import lk.ac.ruh.fot.ams.model.Undergraduate;

/**
 * [COMMON PART] Full academic record of a student: every semester and CGPA.
 * CGPA = sum over all semesters(grade point x credits) / all graded credits
 */
public class Transcript {

    private final Undergraduate student;
    private final List<SemesterResult> semesters = new ArrayList<>();

    public Transcript(Undergraduate student) {
        this.student = student;
    }

    public double getCgpa() {
        double points = 0;
        int credits = 0;
        for (SemesterResult s : semesters) {
            points += s.getWeightedPoints();
            credits += s.getGradedCredits();
        }
        return credits == 0 ? 0 : points / credits;
    }

    public int getTotalCredits() {
        int credits = 0;
        for (SemesterResult s : semesters) credits += s.getGradedCredits();
        return credits;
    }

    /** SGPA of the given semester (0 if no results). */
    public double getSgpa(int level, int semester) {
        for (SemesterResult s : semesters) {
            if (s.getLevel() == level && s.getSemester() == semester) return s.getSgpa();
        }
        return 0;
    }

    /** Class / standing based on CGPA (common Sri Lankan honours rules). */
    public String getStanding() {
        double c = getCgpa();
        if (c >= 3.70) return "First Class";
        if (c >= 3.30) return "Second Class (Upper)";
        if (c >= 3.00) return "Second Class (Lower)";
        if (c >= 2.00) return "Pass";
        return "Below Pass";
    }

    public Undergraduate getStudent() { return student; }
    public List<SemesterResult> getSemesters() { return semesters; }
}

package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import java.awt.Color;

import lk.ac.ruh.fot.ams.model.report.SemesterResult;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] Home tile with the SGPA of the current semester
 * (or "In progress" while final exams are not marked yet).
 *
 * OOP: Inheritance (extends HomeWidget), Polymorphism (own update/summaryLine).
 */
public class SgpaWidget extends HomeWidget {

    private String summary = "";

    public SgpaWidget(Color accent) {
        super("Current SGPA", accent);
    }

    @Override
    public void update(StudentSnapshot s) {
        SemesterResult sem = s.getCurrentSemester();
        String label = sem == null ? "Current semester" : "L" + sem.getLevel() + " S" + sem.getSemester();
        if (s.hasCurrentSgpa()) {
            double sgpa = s.getCurrentSgpa();
            showRing(sgpa, 4.0, Format.gpa(sgpa), label, Theme.INFO);
            showStatus(sem.getGradedCredits() + " graded credits", Theme.INFO);
            summary = "SGPA " + Format.gpa(sgpa) + " in " + label;
        } else {
            showRing(0, 4.0, "-", label, Theme.INFO);
            showStatus("Results in progress", Theme.WARNING);
            summary = label + " results are not finalised yet";
        }
    }

    @Override
    public String summaryLine() {
        return summary;
    }
}

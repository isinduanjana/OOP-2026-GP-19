package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import java.awt.Color;

import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] Home tile showing the CGPA out of 4.0 and the
 * class / standing (First Class, Second Upper ...) from the common Transcript.
 *
 * OOP: Inheritance (extends HomeWidget), Polymorphism (own update/summaryLine).
 */
public class GpaWidget extends HomeWidget {

    private String summary = "";

    public GpaWidget(Color accent) {
        super("Cumulative GPA", accent);
    }

    @Override
    public void update(StudentSnapshot s) {
        double cgpa = s.getCgpa();
        boolean graded = s.getTranscript().getTotalCredits() > 0;
        showRing(cgpa, 4.0, graded ? Format.gpa(cgpa) : "-", "out of 4.00", accent);
        String standing = graded ? s.getTranscript().getStanding() : "No grades yet";
        Color c = !graded ? Theme.TEXT_MUTED : cgpa >= 3.30 ? Theme.SUCCESS : cgpa >= 2.00 ? Theme.INFO : Theme.DANGER;
        showStatus(standing, c);
        summary = graded ? "CGPA " + Format.gpa(cgpa) + " (" + standing + ")" : "No graded courses yet";
    }

    @Override
    public String summaryLine() {
        return summary;
    }
}

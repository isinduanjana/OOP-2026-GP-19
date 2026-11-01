package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import java.awt.Color;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] Home tile with the average attendance of the
 * current courses (approved medicals counted, 80% rule from AppConfig).
 *
 * OOP: Inheritance (extends HomeWidget), Polymorphism (own update/summaryLine).
 */
public class AttendanceWidget extends HomeWidget {

    private String summary = "";

    public AttendanceWidget(Color accent) {
        super("Overall attendance", accent);
    }

    @Override
    public void update(StudentSnapshot s) {
        double avg = s.getAverageAttendance();
        boolean ok = !Double.isNaN(avg) && avg + 1e-9 >= AppConfig.MIN_ATTENDANCE_PERCENT;
        showRing(avg, 100, Double.isNaN(avg) ? "-" : Math.round(avg) + "%", "average, with medical", okColor(ok));
        int eligible = s.getAttendanceEligibleCount();
        int total = s.getAttendance().size();
        showStatus(eligible + " of " + total + " courses ≥ 80%", okColor(eligible == total));
        summary = "Average attendance " + Format.percent(avg);
    }

    @Override
    public String summaryLine() {
        return summary;
    }
}

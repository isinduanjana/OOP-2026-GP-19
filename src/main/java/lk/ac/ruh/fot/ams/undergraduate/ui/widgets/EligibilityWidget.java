package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import java.awt.Color;

import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;

/**
 * [M4 - UNDERGRADUATE MODULE] Home tile: in how many current courses the
 * student may sit the final exam (CA >= 40% AND attendance >= 80%, as
 * calculated by the common ResultService), e.g. "5/6 eligible".
 *
 * OOP: Inheritance (extends HomeWidget), Polymorphism (own update/summaryLine).
 */
public class EligibilityWidget extends HomeWidget {

    private String summary = "";

    public EligibilityWidget(Color accent) {
        super("Exam eligibility", accent);
    }

    @Override
    public void update(StudentSnapshot s) {
        int total = s.getCurrentResults().size();
        int eligible = s.getExamEligibleCount();
        boolean all = total > 0 && eligible == total;
        Color c = total == 0 ? Theme.TEXT_MUTED : all ? Theme.SUCCESS : eligible == 0 ? Theme.DANGER : Theme.WARNING;
        showRing(eligible, Math.max(1, total), eligible + "/" + total, "CA ≥ 40% and attendance ≥ 80%", c);
        if (total == 0) {
            showStatus("No current courses", c);
        } else if (all) {
            showStatus("Eligible for all exams", c);
        } else {
            showStatus((total - eligible) + " course(s) at risk", c);
        }
        summary = eligible + "/" + total + " courses exam-eligible";
    }

    @Override
    public String summaryLine() {
        return summary;
    }
}

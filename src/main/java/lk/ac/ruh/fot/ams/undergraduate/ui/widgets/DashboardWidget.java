package lk.ac.ruh.fot.ams.undergraduate.ui.widgets;

import lk.ac.ruh.fot.ams.undergraduate.model.StudentSnapshot;

/**
 * [M4 - UNDERGRADUATE MODULE] Contract of every small tile on the Student
 * Home page. The home page keeps a List&lt;DashboardWidget&gt; and simply calls
 * update(snapshot) / summaryLine() on each one - it does not know which
 * concrete widget it is talking to.
 *
 * OOP: Abstraction (interface = "what" without "how"), Polymorphism
 * (GpaWidget, SgpaWidget, AttendanceWidget, EligibilityWidget each answer
 * these calls in their own way).
 */
public interface DashboardWidget {

    /** Redraws the widget from freshly loaded data. */
    void update(StudentSnapshot snapshot);

    /** One short sentence for the "at a glance" line, e.g. "CGPA 3.42 (Second Class Upper)". */
    String summaryLine();
}

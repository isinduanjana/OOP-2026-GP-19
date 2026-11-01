package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.Color;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BasePanel;

/**
 * [M4 - UNDERGRADUATE MODULE] Abstract parent of the student's own pages
 * (Home, My Courses, My Attendance, My Medicals, Results & GPA).
 *
 *   BasePanel (common)  ->  StudentViewPanel (M4)  ->  StudentCoursesPanel, StudentAttendancePanel ...
 *
 * OOP:
 *  - Inheritance  : gets user, accent, safe(...) and accentButton(...) from BasePanel.
 *  - Encapsulation: the logged-in User is cast to Undergraduate ONCE here and kept
 *                   in a private field; subclasses read it through getStudent().
 *  - Abstraction  : every subclass MUST say how it loads its data (loadStudentData()).
 *  - Template method + polymorphism: refresh() is final - it always runs the
 *                   subclass's loadStudentData() inside safe(...), so database errors
 *                   are shown in a friendly dialog the same way on every page.
 */
public abstract class StudentViewPanel extends BasePanel {

    private final Undergraduate student;

    protected StudentViewPanel(User user, Color accent) {
        super(user, accent);
        if (!(user instanceof Undergraduate u)) {
            throw new IllegalArgumentException("Student pages need an Undergraduate user, got " + user.getRole());
        }
        this.student = u;
    }

    /** The logged-in student (read-only access for subclasses). */
    protected final Undergraduate getStudent() {
        return student;
    }

    /** Shortcut used by most queries. */
    protected final int getStudentId() {
        return student.getStudentId();
    }

    /**
     * Smallest height this page looks good at. On smaller windows the dashboard's
     * scroll pane shows a scroll bar instead of squeezing the cards
     * (BasePanel only fits the page to the window when it is tall enough).
     */
    private int minPageHeight;

    protected final void setMinimumPageHeight(int height) {
        this.minPageHeight = height;
    }

    @Override
    public java.awt.Dimension getMinimumSize() {
        java.awt.Dimension d = super.getMinimumSize();
        return new java.awt.Dimension(d.width, Math.max(d.height, minPageHeight));
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension d = super.getPreferredSize();
        return new java.awt.Dimension(d.width, Math.max(d.height, minPageHeight));
    }

    /** Loads this page's data from the database (implemented by each page). */
    protected abstract void loadStudentData() throws AppException;

    /** Called by the dashboard each time the page is shown (template method). */
    @Override
    public final void refresh() {
        safe(this::loadStudentData);
    }
}

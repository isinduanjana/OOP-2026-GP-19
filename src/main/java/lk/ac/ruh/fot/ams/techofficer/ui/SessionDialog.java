package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.AttendanceSession;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.enums.Component;
import lk.ac.ruh.fot.ams.techofficer.SessionLimitExceededException;
import lk.ac.ruh.fot.ams.techofficer.dao.TOAttendanceDAO;
import lk.ac.ruh.fot.ams.ui.components.UI;

/**
 * [M3 - TECHNICAL OFFICER MODULE] "New session" / "Edit session" dialog.
 * Fields: session number (1..15, default = next free number), date (default
 * today), hours (default 2 = AppConfig.HOURS_PER_SESSION).
 *
 * OOP: Inheritance - extends the abstract RecordEntryDialog and fills in its
 * steps (buildForm / readForm / save). Polymorphism - one class handles both
 * "create" and "edit" depending on the object it was given.
 */
public class SessionDialog extends RecordEntryDialog<AttendanceSession> {

    private final TOAttendanceDAO dao;
    private final Course course;
    private final Component component;
    private final AttendanceSession existing;   // null = new session
    private final int suggestedNumber;

    private JSpinner numberSpinner;
    private JSpinner dateSpinner;
    private JSpinner hoursSpinner;

    /** Dialog for a NEW session. */
    public SessionDialog(java.awt.Component parent, Color accent, TOAttendanceDAO dao, Course course,
                         Component component, int suggestedNumber) {
        super(parent, "New attendance session", accent);
        this.dao = dao;
        this.course = course;
        this.component = component;
        this.existing = null;
        this.suggestedNumber = suggestedNumber;
    }

    /** Dialog to EDIT the date / hours of an existing session. */
    public SessionDialog(java.awt.Component parent, Color accent, TOAttendanceDAO dao, Course course,
                         AttendanceSession existing) {
        super(parent, "Edit session", accent);
        this.dao = dao;
        this.course = course;
        this.component = existing.getComponent();
        this.existing = existing;
        this.suggestedNumber = existing.getSessionNumber();
    }

    @Override
    protected String getHeading() {
        return existing == null ? "New " + component.toString().toLowerCase() + " session"
                : "Edit " + component.toString().toLowerCase() + " session #" + existing.getSessionNumber();
    }

    @Override
    protected String getIconName() {
        return existing == null ? "plus" : "calendar";
    }

    @Override
    protected String getSaveText() {
        return existing == null ? "Create session" : "Save changes";
    }

    @Override
    protected JComponent buildForm() {
        numberSpinner = new JSpinner(new SpinnerNumberModel(suggestedNumber, 1, AppConfig.SESSIONS_PER_COMPONENT, 1));
        numberSpinner.setEnabled(existing == null);       // number of an existing session is fixed
        dateSpinner = dateSpinner(existing == null ? null : existing.getSessionDate());
        hoursSpinner = new JSpinner(new SpinnerNumberModel(
                existing == null ? AppConfig.HOURS_PER_SESSION : existing.getHours(), 1, 8, 1));
        for (JSpinner s : new JSpinner[]{numberSpinner, dateSpinner, hoursSpinner}) {
            s.setPreferredSize(new Dimension(220, 34));
        }
        JLabel courseLbl = UI.label(course.getCourseCode() + " - " + course.getCourseName());
        JLabel compLbl = UI.label(component.toString());
        JPanel form = UI.form(new String[]{"Course", "Component", "Session number", "Date", "Hours"},
                courseLbl, compLbl, numberSpinner, dateSpinner, hoursSpinner);

        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.setOpaque(false);
        box.add(form, BorderLayout.CENTER);
        box.add(UI.muted("Each component has " + AppConfig.SESSIONS_PER_COMPONENT + " sessions; 1 session = "
                + AppConfig.HOURS_PER_SESSION + " hours. Future dates are not allowed."), BorderLayout.SOUTH);
        return box;
    }

    @Override
    protected AttendanceSession readForm() throws ValidationException {
        int number = (Integer) numberSpinner.getValue();
        if (number < 1 || number > AppConfig.SESSIONS_PER_COMPONENT) {
            throw new SessionLimitExceededException(number);
        }
        AttendanceSession s = existing != null ? existing : new AttendanceSession();
        s.setCourseId(course.getCourseId());
        s.setCourseCode(course.getCourseCode());
        s.setComponent(component);
        s.setSessionNumber(number);
        s.setSessionDate(readDate(dateSpinner));
        s.setHours((Integer) hoursSpinner.getValue());
        s.setTotalSessions(AppConfig.SESSIONS_PER_COMPONENT);
        return s;
    }

    @Override
    protected void save(AttendanceSession s) throws AppException {
        if (existing == null) {
            s.setSessionId(dao.createSession(course, s));
        } else {
            dao.updateSession(s);
        }
    }
}

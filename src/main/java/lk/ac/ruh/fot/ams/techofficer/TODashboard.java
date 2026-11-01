package lk.ac.ruh.fot.ams.techofficer;

import java.awt.Color;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.TimetableDAO;
import lk.ac.ruh.fot.ams.model.TechnicalOfficer;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.techofficer.ui.MarkAttendancePanel;
import lk.ac.ruh.fot.ams.techofficer.ui.MedicalsPanel;
import lk.ac.ruh.fot.ams.techofficer.ui.StudentAttendancePanel;
import lk.ac.ruh.fot.ams.techofficer.ui.TOHomePanel;
import lk.ac.ruh.fot.ams.techofficer.ui.TOProfilePanel;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.shared.AttendanceReportPanel;
import lk.ac.ruh.fot.ams.ui.shared.NoticeBoardPanel;
import lk.ac.ruh.fot.ams.ui.shared.TimetablePanel;

/**
 * [M3 - TECHNICAL OFFICER MODULE] Main window of the Technical Officer.
 *
 * Connection to common: the common DashboardFactory creates this class BY NAME
 * (reflection) when a TO logs in and calls init(); BaseDashboard.init() then
 * calls registerPages() below. Sidebar, header, page switching and logout all
 * come from the common BaseDashboard.
 *
 * Handout requirements covered:
 *   - update profile except username/password   -> My Profile (TOProfilePanel)
 *   - add & maintain attendance                  -> Mark Attendance, Attendance Report, Student Attendance
 *   - add & maintain medical details             -> Medicals
 *   - see notices                                -> Notices
 *   - see timetables of their department         -> Timetable
 *
 * OOP: Inheritance (extends BaseDashboard), Polymorphism (overrides the three
 * abstract methods; every page is a BasePanel), Encapsulation.
 */
public class TODashboard extends BaseDashboard {

    private final TechnicalOfficer officer;
    private final CourseDAO courseDAO = new CourseDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    public TODashboard(User user) {
        super(user);
        this.officer = (TechnicalOfficer) user;    // DashboardFactory only sends TO users here
    }

    @Override
    protected String getRoleTitle() {
        return "Technical Officer Portal";
    }

    @Override
    protected Color getAccentColor() {
        return Theme.TO_ACCENT;
    }

    @Override
    protected void registerPages() {
        Color a = getAccentColor();

        addSection("Main");
        addPage("Dashboard", "home", new TOHomePanel(officer, a));

        addSection("Attendance");
        addPage("Mark Attendance", "clipboard", new MarkAttendancePanel(officer, a));
        addPage("Medicals", "medical", new MedicalsPanel(officer, a));
        addPage("Attendance Report", "chart",
                new AttendanceReportPanel(officer, a, () -> courseDAO.findCurrentSemester()));
        addPage("Student Attendance", "user", new StudentAttendancePanel(officer, a));

        addSection("Information");
        String dept = officer.getDepartmentName();
        addPage("Timetable", "calendar", new TimetablePanel(officer, a,
                dept == null ? "Timetable of all departments" : "Timetable of " + dept,
                // a TO without a department sees every timetable
                () -> officer.getDepartmentId() == null ? timetableDAO.findAll()
                        : timetableDAO.findByDepartment(officer.getDepartmentId())));
        addPage("Notices", "bell", new NoticeBoardPanel(officer, a));

        addSection("Account");
        addPage("My Profile", "user", new TOProfilePanel(officer, a));
    }
}

package lk.ac.ruh.fot.ams.undergraduate;

import java.awt.Color;

import lk.ac.ruh.fot.ams.dao.TimetableDAO;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.shared.NoticeBoardPanel;
import lk.ac.ruh.fot.ams.ui.shared.TimetablePanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentAttendancePanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentCoursesPanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentHomePanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentMedicalsPanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentProfilePanel;
import lk.ac.ruh.fot.ams.undergraduate.ui.StudentResultsPanel;

public class UndergraduateDashboard extends BaseDashboard {

    private final Undergraduate student;
    private final TimetableDAO timetableDAO = new TimetableDAO();

    public UndergraduateDashboard(User user) {
        super(user);
        this.student = (Undergraduate) user;   // DashboardFactory only opens this for UNDERGRADUATE users
    }

    @Override
    protected String getRoleTitle() {
        return "Student Portal";
    }

    @Override
    protected Color getAccentColor() {
        return Theme.STUDENT_ACCENT;
    }

    @Override
    protected void registerPages() {
        Color a = getAccentColor();
        addSection("Main");
        addPage("Home", "home", new StudentHomePanel(user, a));
        addPage("My Courses", "book", new StudentCoursesPanel(user, a));
        addSection("Academic records");
        addPage("My Attendance", "check", new StudentAttendancePanel(user, a));
        addPage("My Medicals", "medical", new StudentMedicalsPanel(user, a));
        addPage("Results & GPA", "award", new StudentResultsPanel(user, a));
        addSection("Information");
        addPage("Timetable", "calendar", new TimetablePanel(user, a, "My weekly timetable",
                () -> timetableDAO.findByStudent(student.getStudentId())));
        addPage("Notices", "bell", new NoticeBoardPanel(user, a));
        addSection("Account");
        addPage("My Profile", "user", new StudentProfilePanel(user, a));
    }
}

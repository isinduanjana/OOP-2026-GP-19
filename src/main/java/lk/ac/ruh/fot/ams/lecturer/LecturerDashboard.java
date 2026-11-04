package lk.ac.ruh.fot.ams.lecturer;

import java.awt.Color;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.dao.TimetableDAO;
import lk.ac.ruh.fot.ams.lecturer.ui.CaEligibilityPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.GpaPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.LecturerHomePanel;
import lk.ac.ruh.fot.ams.lecturer.ui.LecturerProfilePanel;
import lk.ac.ruh.fot.ams.lecturer.ui.MarksEntryPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.MedicalRecordsPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.MyCoursesPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.ResultsGradesPanel;
import lk.ac.ruh.fot.ams.lecturer.ui.UndergraduatesPanel;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.Theme;
import lk.ac.ruh.fot.ams.ui.shared.AttendanceReportPanel;
import lk.ac.ruh.fot.ams.ui.shared.NoticeBoardPanel;
import lk.ac.ruh.fot.ams.ui.shared.TimetablePanel;


public class LecturerDashboard extends BaseDashboard {

    private final CourseDAO courseDAO = new CourseDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    public LecturerDashboard(User user) {
        super(user);
    }

    @Override
    protected String getRoleTitle() {
        return "Lecturer Portal";
    }

    @Override
    protected Color getAccentColor() {
        return Theme.LECTURER_ACCENT;
    }

    @Override
    protected void registerPages() {
        Color a = getAccentColor();
        int lecturerId = ((Lecturer) user).getLecturerId();   // logged-in user is a Lecturer

        addSection("Main");
        addPage("Dashboard", "home", new LecturerHomePanel(user, a));
        addPage("My Courses", "book", new MyCoursesPanel(user, a));
        addPage("Marks Entry", "clipboard", new MarksEntryPanel(user, a));

        addSection("Undergraduates");
        addPage("Undergraduates", "users", new UndergraduatesPanel(user, a));
        addPage("CA & Eligibility", "check", new CaEligibilityPanel(user, a));
        addPage("Results & Grades", "grade", new ResultsGradesPanel(user, a));
        addPage("GPA (SGPA & CGPA)", "award", new GpaPanel(user, a));
        // common shared pages - only the data source (my courses) is given by M2
        addPage("Attendance", "calendar", new AttendanceReportPanel(user, a, () -> courseDAO.findByLecturer(lecturerId)));
        addPage("Medical Records", "medical", new MedicalRecordsPanel(user, a));

        addSection("General");
        addPage("Timetable", "calendar", new TimetablePanel(user, a, "My teaching timetable",
                () -> timetableDAO.findByLecturer(lecturerId)));
        addPage("Notices", "bell", new NoticeBoardPanel(user, a));
        addPage("My Profile", "user", new LecturerProfilePanel(user, a));
    }
}

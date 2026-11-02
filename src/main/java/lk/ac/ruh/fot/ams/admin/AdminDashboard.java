package lk.ac.ruh.fot.ams.admin;

import java.awt.Color;

import lk.ac.ruh.fot.ams.admin.ui.AdminHomePanel;
import lk.ac.ruh.fot.ams.admin.ui.AdminProfilePanel;
import lk.ac.ruh.fot.ams.admin.ui.CourseManagementPanel;
import lk.ac.ruh.fot.ams.admin.ui.CrudPanel;
import lk.ac.ruh.fot.ams.admin.ui.NoticeManagementPanel;
import lk.ac.ruh.fot.ams.admin.ui.TimetableManagementPanel;
import lk.ac.ruh.fot.ams.admin.ui.UserManagementPanel;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.BaseDashboard;
import lk.ac.ruh.fot.ams.ui.Theme;


public class AdminDashboard extends BaseDashboard {

    public static final String PAGE_HOME = "Dashboard";
    public static final String PAGE_USERS = "Users";
    public static final String PAGE_COURSES = "Courses";
    public static final String PAGE_NOTICES = "Notices";
    public static final String PAGE_TIMETABLE = "Timetables";
    public static final String PAGE_PROFILE = "My Profile";

    // created in registerPages() (BaseDashboard.init() calls it after the constructor)
    private UserManagementPanel usersPage;
    private CourseManagementPanel coursesPage;
    private NoticeManagementPanel noticesPage;
    private TimetableManagementPanel timetablePage;

    public AdminDashboard(User user) {
        super(user);
    }

    @Override
    protected String getRoleTitle() {
        return "Administrator Portal";
    }

    @Override
    protected Color getAccentColor() {
        return Theme.ADMIN_ACCENT;
    }

    @Override
    protected void registerPages() {
        Color accent = getAccentColor();
        usersPage = new UserManagementPanel(user, accent);
        coursesPage = new CourseManagementPanel(user, accent);
        noticesPage = new NoticeManagementPanel(user, accent);
        timetablePage = new TimetableManagementPanel(user, accent);

        addSection("Main");
        addPage(PAGE_HOME, "home", new AdminHomePanel(user, accent, new AdminHomePanel.QuickActions() {
            @Override public void open(String page)     { showPage(page); }
            @Override public void addUser()             { openAndAdd(PAGE_USERS, usersPage); }
            @Override public void addCourse()           { openAndAdd(PAGE_COURSES, coursesPage); }
            @Override public void addNotice()           { openAndAdd(PAGE_NOTICES, noticesPage); }
            @Override public void addTimetableSlot()    { openAndAdd(PAGE_TIMETABLE, timetablePage); }
        }));

        addSection("Manage");
        // polymorphism: every management page is "a CrudPanel" - same code registers all four
        String[] names = {PAGE_USERS, PAGE_COURSES, PAGE_NOTICES, PAGE_TIMETABLE};
        String[] icons = {"users", "book", "bell", "calendar"};
        CrudPanel<?>[] pages = {usersPage, coursesPage, noticesPage, timetablePage};
        for (int i = 0; i < pages.length; i++) {
            addPage(names[i], icons[i], pages[i]);
        }

        addSection("Account");
        addPage(PAGE_PROFILE, "user", new AdminProfilePanel(user, accent));
    }

    private void openAndAdd(String page, CrudPanel<?> panel) {
        showPage(page);
        panel.openAddDialog();
    }
}

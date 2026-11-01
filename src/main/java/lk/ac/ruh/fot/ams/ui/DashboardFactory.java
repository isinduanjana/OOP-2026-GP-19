package lk.ac.ruh.fot.ams.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;

/**
 * [COMMON PART] Opens the correct dashboard for the logged-in user.
 *
 * HOW THE MEMBER PARTS CONNECT HERE:
 *   The common part does NOT import the member classes directly. It loads
 *   them by name (reflection). So:
 *     - Common alone compiles and runs (login works, missing modules show a message)
 *     - when M1.zip is extracted, lk.ac.ruh.fot.ams.admin.AdminDashboard exists
 *       and admins can log in; the same for M2, M3, M4.
 *
 * OOP: Factory pattern + Polymorphism (every dashboard is a BaseDashboard).
 */
public final class DashboardFactory {

    private static final Map<Role, String> DASHBOARDS = new EnumMap<>(Role.class);
    private static final Map<Role, String> MODULES = new EnumMap<>(Role.class);

    static {
        DASHBOARDS.put(Role.ADMIN, "lk.ac.ruh.fot.ams.admin.AdminDashboard");                 // M1
        DASHBOARDS.put(Role.LECTURER, "lk.ac.ruh.fot.ams.lecturer.LecturerDashboard");        // M2
        DASHBOARDS.put(Role.TO, "lk.ac.ruh.fot.ams.techofficer.TODashboard");                 // M3
        DASHBOARDS.put(Role.UNDERGRADUATE, "lk.ac.ruh.fot.ams.undergraduate.UndergraduateDashboard"); // M4
        MODULES.put(Role.ADMIN, "M1 - Admin module");
        MODULES.put(Role.LECTURER, "M2 - Lecturer module");
        MODULES.put(Role.TO, "M3 - Technical Officer module");
        MODULES.put(Role.UNDERGRADUATE, "M4 - Undergraduate module");
    }

    private DashboardFactory() {
    }

    /** Creates and builds the dashboard of the user's role. */
    public static BaseDashboard create(User user) throws AppException {
        String className = DASHBOARDS.get(user.getRole());
        try {
            Class<?> cls = Class.forName(className);
            Constructor<?> ctor = cls.getConstructor(User.class);
            BaseDashboard dashboard = (BaseDashboard) ctor.newInstance(user);
            dashboard.init();
            return dashboard;
        } catch (ClassNotFoundException e) {
            throw new AppException("The " + MODULES.get(user.getRole()) + " has not been added to the project yet.\n"
                    + "Extract that member's zip into the project folder and rebuild.");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) cause.printStackTrace();
            throw new AppException("Could not open the dashboard: " + (cause == null ? e : cause.getMessage()), e);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new AppException("Could not open the dashboard: " + e.getMessage(), e);
        }
    }
}

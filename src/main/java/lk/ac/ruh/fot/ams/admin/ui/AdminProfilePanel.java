package lk.ac.ruh.fot.ams.admin.ui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Admin;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.shared.AbstractProfilePanel;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M1 - ADMIN MODULE] "My Profile" page of the administrator.
 * Everything (form, picture, password, edit rules) comes from the common
 * AbstractProfilePanel; this class only adds the admin-specific read-only details.
 *
 * OOP: Inheritance + Abstraction (implements the abstract getRoleDetails()).
 *      Which fields are editable is decided polymorphically by Admin.canEdit().
 */
public class AdminProfilePanel extends AbstractProfilePanel {

    public AdminProfilePanel(User user, Color accent) {
        super(user, accent);
    }

    @Override
    protected Map<String, String> getRoleDetails() throws AppException {
        User fresh = userDAO.findById(user.getUserId());        // userDAO is inherited (protected)
        User u = fresh == null ? user : fresh;
        Map<String, String> map = new LinkedHashMap<>();
        if (u instanceof Admin a) {
            map.put("Admin ID", "ADM-" + String.format("%03d", a.getAdminId()));
            map.put("Employee number", Format.nullToDash(a.getEmployeeNo()));
        }
        map.put("Department", Format.nullToDash(u.getDepartmentName()));
        map.put("Role", u.getRole().getDisplayName());
        map.put("Account status", u.isActive() ? "Active" : "Inactive");
        map.put("Account created", Format.dateTime(u.getCreatedAt()));
        map.put("Permissions", "All user profiles, courses, notices and timetables");
        return map;
    }
}

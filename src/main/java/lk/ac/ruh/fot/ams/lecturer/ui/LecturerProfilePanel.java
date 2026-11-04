package lk.ac.ruh.fot.ams.lecturer.ui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lk.ac.ruh.fot.ams.dao.CourseDAO;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Course;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M2 - LECTURER MODULE] "My Profile" page - "Can update their profile except username and password".
 * Everything (form, picture, saving, locking) is inherited from the common AbstractProfilePanel,
 * which asks user.canEdit(field); Lecturer.canEdit returns false for USERNAME and PASSWORD,
 * so those are locked automatically. Here we only add the lecturer-specific details.
 *
 * OOP: Inheritance (extends the abstract common AbstractProfilePanel),
 * Abstraction / Polymorphism (implements the abstract getRoleDetails()).
 */
public class LecturerProfilePanel extends lk.ac.ruh.fot.ams.ui.shared.AbstractProfilePanel {

    private final CourseDAO courseDAO = new CourseDAO();

    public LecturerProfilePanel(User user, Color accent) {
        super(user, accent);
    }

    @Override
    protected Map<String, String> getRoleDetails() throws AppException {
        Lecturer l = (Lecturer) user;
        List<Course> courses = courseDAO.findByLecturer(l.getLecturerId());
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Employee number", Format.nullToDash(l.getEmployeeNo()));
        map.put("Lecturer ID", String.valueOf(l.getLecturerId()));
        map.put("Department", Format.nullToDash(l.getDepartmentName()));
        map.put("Account status", l.isActive() ? "Active" : "Inactive");
        map.put("Courses taught", courses.isEmpty() ? "-"
                : courses.stream().map(Course::getCourseCode).collect(Collectors.joining(", ")));
        map.put("Total credits", String.valueOf(courses.stream().mapToInt(Course::getCredits).sum()));
        map.put("Username", l.getUsername() + "  (locked)");
        map.put("Password", "Only the admin can reset it");
        return map;
    }
}

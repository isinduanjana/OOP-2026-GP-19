package lk.ac.ruh.fot.ams.undergraduate.ui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.ui.shared.AbstractProfilePanel;
import lk.ac.ruh.fot.ams.undergraduate.dao.StudentPortalDAO;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M4 - UNDERGRADUATE MODULE] "My Profile" for students.
 *
 * The common AbstractProfilePanel builds the whole form and asks
 * user.canEdit(field) for every field. Undergraduate.canEdit(...) only allows
 * email, phone, address and profile picture - so the handout rule "students
 * can update only contact details and profile picture" is enforced
 * automatically (polymorphism). This class only adds the read-only student details.
 *
 * OOP: Inheritance (extends the common abstract AbstractProfilePanel),
 * Abstraction (implements the abstract getRoleDetails()), exception handling
 * (StudentDataNotFoundException from StudentPortalDAO).
 */
public class StudentProfilePanel extends AbstractProfilePanel {

    private final StudentPortalDAO portalDAO = new StudentPortalDAO();

    public StudentProfilePanel(User user, Color accent) {
        super(user, accent);
    }

    @Override
    public String getSubtitle() {
        return "You can change your contact details and profile picture - other details are managed by the faculty";
    }

    @Override
    protected Map<String, String> getRoleDetails() throws AppException {
        Undergraduate s = portalDAO.requireStudent(((Undergraduate) user).getStudentId());
        Map<String, String> map = new LinkedHashMap<>();
        map.put("TG number", s.getTgNo());
        map.put("Batch", s.getBatch());
        map.put("Level", "Level " + s.getLevel());
        map.put("Semester", "Semester " + s.getSemester());
        map.put("Student type", String.valueOf(s.getStudentType()));
        map.put("Department", Format.nullToDash(s.getDepartmentName()));
        map.put("Enrolled courses", portalDAO.countEnrolledCourses(s.getStudentId()) + " course(s) in "
                + AppConfig.CURRENT_ACADEMIC_YEAR);
        map.put("Enrolled credits", portalDAO.countEnrolledCredits(s.getStudentId()) + " credits");
        map.put("Account status", s.isActive() ? "Active" : "Inactive");
        map.put("Member since", s.getCreatedAt() == null ? "-" : Format.date(s.getCreatedAt().toLocalDate()));
        return map;
    }
}

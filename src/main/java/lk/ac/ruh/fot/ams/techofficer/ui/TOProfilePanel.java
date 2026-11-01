package lk.ac.ruh.fot.ams.techofficer.ui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import lk.ac.ruh.fot.ams.exception.AppException;
import lk.ac.ruh.fot.ams.model.TechnicalOfficer;
import lk.ac.ruh.fot.ams.techofficer.dao.TOAttendanceDAO;
import lk.ac.ruh.fot.ams.techofficer.dao.TOMedicalDAO;
import lk.ac.ruh.fot.ams.util.Format;

/**
 * [M3 - TECHNICAL OFFICER MODULE] "My Profile" of a technical officer.
 * The common AbstractProfilePanel already builds the form and locks username +
 * password (because TechnicalOfficer.canEdit(...) says so - handout rule
 * "update their profile except username and password"). This class only adds
 * the TO-specific read-only details.
 *
 * OOP: Inheritance (extends the abstract common class), Abstraction (implements
 * its abstract method getRoleDetails()), Polymorphism (common code calls it).
 */
public class TOProfilePanel extends lk.ac.ruh.fot.ams.ui.shared.AbstractProfilePanel {

    private final TechnicalOfficer officer;
    private final TOAttendanceDAO attendanceDAO = new TOAttendanceDAO();
    private final TOMedicalDAO medicalDAO = new TOMedicalDAO();

    public TOProfilePanel(TechnicalOfficer officer, Color accent) {
        super(officer, accent);
        this.officer = officer;
    }

    @Override
    protected Map<String, String> getRoleDetails() throws AppException {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Employee no", officer.getEmployeeNo());
        m.put("Technical officer ID", "TO-" + officer.getToId());
        m.put("Department", Format.nullToDash(officer.getDepartmentName()));
        m.put("Account status", officer.getStatus());
        m.put("Sessions recorded (department)", String.valueOf(attendanceDAO.countSessionsOfDepartment(officer.getDepartmentId())));
        m.put("Medicals handled by you", String.valueOf(medicalDAO.countHandledBy(officer.getUserId())));
        m.put("Member since", officer.getCreatedAt() == null ? "-" : Format.date(officer.getCreatedAt().toLocalDate()));
        return m;
    }
}

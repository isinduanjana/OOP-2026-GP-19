package lk.ac.ruh.fot.ams.model;

/**
 * [COMMON PART] Abstract middle class for staff members (Admin, Lecturer, TO)
 * who all have an employee number.
 *
 * OOP: multi-level inheritance  User -> Staff -> Lecturer
 */
public abstract class Staff extends User {

    private String employeeNo;

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    @Override
    public String getIdentityNumber() {
        return employeeNo;
    }
}

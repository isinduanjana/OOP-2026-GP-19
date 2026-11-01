package lk.ac.ruh.fot.ams.admin.model;

import java.util.regex.Pattern;

import lk.ac.ruh.fot.ams.config.AppConfig;
import lk.ac.ruh.fot.ams.exception.ValidationException;
import lk.ac.ruh.fot.ams.model.Admin;
import lk.ac.ruh.fot.ams.model.Lecturer;
import lk.ac.ruh.fot.ams.model.Staff;
import lk.ac.ruh.fot.ams.model.TechnicalOfficer;
import lk.ac.ruh.fot.ams.model.Undergraduate;
import lk.ac.ruh.fot.ams.model.User;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;
import lk.ac.ruh.fot.ams.util.Validator;

/**
 * [M1 - ADMIN MODULE] Holds what the admin typed in the "Add / Edit user" dialog,
 * checks it, and turns it into the correct User subclass.
 *
 * OOP:
 *  - Encapsulation : every field is private; values are trimmed in the setters and
 *                    can only be read back through getters.
 *  - Validation    : validate() throws ValidationException (common) with a friendly message.
 *  - Polymorphism  : toUser() is a small factory that returns an Admin, Lecturer,
 *                    TechnicalOfficer or Undergraduate - all used later simply as "User".
 */
public class UserForm {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern EMPLOYEE_NO = Pattern.compile("^[A-Z0-9/_-]{3,20}$");
    private static final Pattern BATCH = Pattern.compile("^\\d{4}$");

    private final int userId;           // 0 = new user
    private Role role = Role.UNDERGRADUATE;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String status = "ACTIVE";
    // staff
    private String employeeNo;
    // undergraduate
    private String tgNo;
    private String batch;
    private int level = AppConfig.CURRENT_LEVEL;
    private int semester = AppConfig.CURRENT_SEMESTER;
    private StudentType studentType = StudentType.NORMAL;
    private boolean enrollInCurrentCourses = true;
    // both
    private Integer departmentId;

    /** Form for a NEW user. */
    public UserForm() {
        this.userId = 0;
    }

    /** Form pre-filled from an existing user (edit mode). */
    public UserForm(User existing) {
        this.userId = existing.getUserId();
        this.role = existing.getRole();
        this.username = existing.getUsername();
        this.fullName = existing.getFullName();
        this.email = existing.getEmail();
        this.phone = existing.getPhone();
        this.address = existing.getAddress();
        this.status = existing.getStatus();
        this.departmentId = existing.getDepartmentId();
        if (existing instanceof Staff s) {                 // pattern matching on the subclass
            this.employeeNo = s.getEmployeeNo();
        } else if (existing instanceof Undergraduate u) {
            this.tgNo = u.getTgNo();
            this.batch = u.getBatch();
            this.level = u.getLevel();
            this.semester = u.getSemester();
            this.studentType = u.getStudentType();
            this.enrollInCurrentCourses = false;
        }
    }

    public boolean isNew() {
        return userId == 0;
    }

    /** Checks every field; throws on the first problem found. */
    public void validate() throws ValidationException {
        username = Validator.required(username, "Username");
        if (!USERNAME.matcher(username).matches()) {
            throw new ValidationException("Username",
                    "Username must be 3-30 characters: letters, digits, dot, dash or underscore (no spaces).");
        }
        if (isNew()) {
            password = Validator.password(password);
        }
        fullName = Validator.required(fullName, "Full name");
        if (fullName.length() > 100) {
            throw new ValidationException("Full name", "Full name can have at most 100 characters.");
        }
        email = Validator.email(email);
        phone = Validator.optionalPhone(phone);
        if (role == null) {
            throw new ValidationException("Role", "Please select a role.");
        }
        if (role == Role.UNDERGRADUATE) {
            tgNo = Validator.tgNumber(tgNo);
            if (batch == null || batch.isBlank()) {
                batch = tgNo.substring(3, 7);                // TG/2022/1234 -> 2022
            }
            if (!BATCH.matcher(batch.trim()).matches()) {
                throw new ValidationException("Batch", "Batch must be a year such as 2022.");
            }
            batch = batch.trim();
            if (level < 1 || level > 4) throw new ValidationException("Level", "Level must be between 1 and 4.");
            if (semester < 1 || semester > 2) throw new ValidationException("Semester", "Semester must be 1 or 2.");
            if (departmentId == null) throw new ValidationException("Department", "Please select the student's department.");
        } else {
            employeeNo = Validator.required(employeeNo, "Employee number").toUpperCase();
            if (!EMPLOYEE_NO.matcher(employeeNo).matches()) {
                throw new ValidationException("Employee number",
                        "Employee number may contain capital letters, digits and '/', e.g. EMP/LEC/007.");
            }
            if (role != Role.ADMIN && departmentId == null) {
                throw new ValidationException("Department", "Please select a department for this " + role + ".");
            }
        }
    }

    /** Factory method: builds the right subclass from the (validated) form. */
    public User toUser() {
        User u = switch (role) {
            case ADMIN -> new Admin();
            case LECTURER -> new Lecturer();
            case TO -> new TechnicalOfficer();
            case UNDERGRADUATE -> {
                Undergraduate s = new Undergraduate();
                s.setTgNo(tgNo);
                s.setBatch(batch);
                s.setLevel(level);
                s.setSemester(semester);
                s.setStudentType(studentType);
                yield s;
            }
        };
        if (u instanceof Staff staff) {
            staff.setEmployeeNo(employeeNo);
        }
        u.setUserId(userId);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(address);
        u.setStatus(status);
        u.setDepartmentId(departmentId);
        return u;
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    // ------------------------------------------------------------- getters / setters
    public int getUserId() { return userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = clean(username); }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = clean(fullName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = clean(email); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = clean(phone); }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = clean(address); }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = clean(employeeNo); }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = clean(tgNo); }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = clean(batch); }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public boolean isEnrollInCurrentCourses() { return enrollInCurrentCourses; }
    public void setEnrollInCurrentCourses(boolean b) { this.enrollInCurrentCourses = b; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
}

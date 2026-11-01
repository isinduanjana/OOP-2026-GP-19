package lk.ac.ruh.fot.ams.model;

import java.time.LocalDateTime;

import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.model.enums.Role;

/**
 * [COMMON PART] Abstract base class for every person who logs in.
 *
 * OOP:
 *  - Abstraction : User cannot be created directly ("abstract"); it defines
 *                  what every user must provide (getRole, getIdentityNumber, canEdit).
 *  - Inheritance : Admin, Lecturer, TechnicalOfficer, Undergraduate extend User.
 *  - Polymorphism: canEdit(field) behaves differently for each subclass, so the
 *                  shared profile screen enables/disables fields automatically.
 *  - Encapsulation: all fields are private, accessed through getters/setters.
 */
public abstract class User {

    private int userId;
    private String username;
    private String status = "ACTIVE";
    private LocalDateTime createdAt;

    // ---- profile (user_profiles table)
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;

    // ---- department (stored in the role table)
    private Integer departmentId;
    private String departmentName;

    protected User() {
    }

    /** Which role this user plays. */
    public abstract Role getRole();

    /** Employee number for staff, TG number for undergraduates. */
    public abstract String getIdentityNumber();

    /** Profile-edit rules required by the handout (different for each role). */
    public abstract boolean canEdit(ProfileField field);

    /** Short text shown under the name in the dashboard side bar. */
    public String getDisplaySubtitle() {
        return getRole().getDisplayName() + "  •  " + getIdentityNumber();
    }

    /** Two-letter initials for the avatar circle. */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return username == null ? "?" : username.substring(0, 1).toUpperCase();
        }
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    // ------------------------------------------------------------- getters / setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @Override
    public String toString() {
        return fullName + " (" + getIdentityNumber() + ")";
    }
}

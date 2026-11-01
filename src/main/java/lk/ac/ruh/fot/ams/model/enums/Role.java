package lk.ac.ruh.fot.ams.model.enums;

/**
 * [COMMON PART] The four user roles. The DB value is stored in users.role.
 * Each role is handled by one group member:
 *   ADMIN -> M1, LECTURER -> M2, TO -> M3, UNDERGRADUATE -> M4
 */
public enum Role {
    ADMIN("Administrator"),
    LECTURER("Lecturer"),
    TO("Technical Officer"),
    UNDERGRADUATE("Undergraduate");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

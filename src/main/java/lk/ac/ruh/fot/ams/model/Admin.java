package lk.ac.ruh.fot.ams.model;

import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.model.enums.Role;

/** [COMMON PART - used mainly by M1] The system administrator. */
public class Admin extends Staff {

    private int adminId;

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    /** Admin maintains all profiles, so can edit everything on his own profile. */
    @Override
    public boolean canEdit(ProfileField field) {
        return field != ProfileField.USERNAME;
    }
}

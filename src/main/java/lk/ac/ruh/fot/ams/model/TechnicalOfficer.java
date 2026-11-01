package lk.ac.ruh.fot.ams.model;

import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.model.enums.Role;

/** [COMMON PART - used mainly by M3] A technical officer. */
public class TechnicalOfficer extends Staff {

    private int toId;

    public int getToId() { return toId; }
    public void setToId(int toId) { this.toId = toId; }

    @Override
    public Role getRole() {
        return Role.TO;
    }

    /** Handout: "Can update their profile except username and password". */
    @Override
    public boolean canEdit(ProfileField field) {
        return field != ProfileField.USERNAME && field != ProfileField.PASSWORD;
    }
}

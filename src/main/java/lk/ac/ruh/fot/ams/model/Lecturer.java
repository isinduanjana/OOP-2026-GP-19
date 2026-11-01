package lk.ac.ruh.fot.ams.model;

import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.model.enums.Role;

/** [COMMON PART - used mainly by M2] A lecturer. */
public class Lecturer extends Staff {

    private int lecturerId;

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    @Override
    public Role getRole() {
        return Role.LECTURER;
    }

    /** Handout: "Can update their profile except username and password". */
    @Override
    public boolean canEdit(ProfileField field) {
        return field != ProfileField.USERNAME && field != ProfileField.PASSWORD;
    }
}

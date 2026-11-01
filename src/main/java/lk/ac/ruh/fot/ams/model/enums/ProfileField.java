package lk.ac.ruh.fot.ams.model.enums;

/**
 * [COMMON PART] Profile fields. Each User subclass decides which of these
 * the logged-in person may edit (see User.canEdit - polymorphism).
 */
public enum ProfileField {
    USERNAME, PASSWORD, FULL_NAME, EMAIL, PHONE, ADDRESS, PROFILE_PICTURE
}

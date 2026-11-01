package lk.ac.ruh.fot.ams.model;

import lk.ac.ruh.fot.ams.model.enums.ProfileField;
import lk.ac.ruh.fot.ams.model.enums.Role;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/** [COMMON PART - used mainly by M4] An undergraduate student. */
public class Undergraduate extends User {

    private int studentId;
    private String tgNo;
    private String batch;
    private int level;
    private int semester;
    private StudentType studentType = StudentType.NORMAL;

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTgNo() { return tgNo; }
    public void setTgNo(String tgNo) { this.tgNo = tgNo; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public StudentType getStudentType() { return studentType; }
    public void setStudentType(StudentType studentType) { this.studentType = studentType; }

    public boolean isRepeat() {
        return studentType == StudentType.REPEAT;
    }

    @Override
    public Role getRole() {
        return Role.UNDERGRADUATE;
    }

    @Override
    public String getIdentityNumber() {
        return tgNo;
    }

    /** Handout: "Can update only contact details and profile picture". */
    @Override
    public boolean canEdit(ProfileField field) {
        return field == ProfileField.EMAIL || field == ProfileField.PHONE
                || field == ProfileField.ADDRESS || field == ProfileField.PROFILE_PICTURE;
    }

    @Override
    public String getDisplaySubtitle() {
        return tgNo + "  •  Level " + level + " Sem " + semester;
    }
}

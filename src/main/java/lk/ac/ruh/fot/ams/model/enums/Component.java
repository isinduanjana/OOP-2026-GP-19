package lk.ac.ruh.fot.ams.model.enums;

/** [COMMON PART] Course component for attendance and timetables. */
public enum Component {
    THEORY("Theory"),
    PRACTICAL("Practical");

    private final String label;

    Component(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

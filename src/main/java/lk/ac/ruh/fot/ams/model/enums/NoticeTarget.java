package lk.ac.ruh.fot.ams.model.enums;

/** [COMMON PART] Who a notice is meant for. */
public enum NoticeTarget {
    ALL("Everyone"),
    LECTURER("Lecturers"),
    TO("Technical Officers"),
    UNDERGRADUATE("Undergraduates");

    private final String label;

    NoticeTarget(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

package lk.ac.ruh.fot.ams.model.enums;

/** [COMMON PART] Undergraduate category (handout asks for repeat and batch-missed students). */
public enum StudentType {
    NORMAL("Normal"),
    REPEAT("Repeat"),
    BATCH_MISSED("Batch Missed");

    private final String label;

    StudentType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

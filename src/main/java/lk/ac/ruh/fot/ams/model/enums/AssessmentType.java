package lk.ac.ruh.fot.ams.model.enums;

/**
 * [COMMON PART] Assessment types. Everything except FINAL belongs to the
 * Continuous Assessment (CA) part used for eligibility.
 */
public enum AssessmentType {
    CA("Quiz / CA"),
    ASSIGNMENT("Assignment"),
    MID("Mid Exam"),
    FINAL("Final Exam");

    private final String label;

    AssessmentType(String label) {
        this.label = label;
    }

    public boolean isContinuousAssessment() {
        return this != FINAL;
    }

    @Override
    public String toString() {
        return label;
    }
}

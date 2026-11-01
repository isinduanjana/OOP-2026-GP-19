package lk.ac.ruh.fot.ams.model.enums;

/**
 * [COMMON PART] Grading scale used for all results
 * (UGC Commission Circular No. 12-2024 - Sri Lankan state universities).
 *
 * If your faculty's copy of the circular shows different boundaries,
 * edit ONLY the numbers below - every screen uses this enum.
 */
public enum GradeScale {
    A_PLUS ("A+", 85, 4.00),
    A      ("A",  75, 4.00),
    A_MINUS("A-", 70, 3.70),
    B_PLUS ("B+", 65, 3.30),
    B      ("B",  60, 3.00),
    B_MINUS("B-", 55, 2.70),
    C_PLUS ("C+", 50, 2.30),
    C      ("C",  45, 2.00),
    C_MINUS("C-", 40, 1.70),
    D_PLUS ("D+", 35, 1.30),
    D      ("D",  30, 1.00),
    E      ("E",   0, 0.00);

    private final String letter;
    private final int minMark;
    private final double gradePoint;

    GradeScale(String letter, int minMark, double gradePoint) {
        this.letter = letter;
        this.minMark = minMark;
        this.gradePoint = gradePoint;
    }

    public String getLetter()     { return letter; }
    public int getMinMark()       { return minMark; }
    public double getGradePoint() { return gradePoint; }

    /** Converts a final mark (0-100) to a grade. */
    public static GradeScale fromMarks(double marks) {
        for (GradeScale g : values()) {      // values() are in descending order
            if (marks >= g.minMark) {
                return g;
            }
        }
        return E;
    }

    /** Converts a stored letter ("B+") back to the enum. */
    public static GradeScale fromLetter(String letter) {
        for (GradeScale g : values()) {
            if (g.letter.equalsIgnoreCase(letter)) {
                return g;
            }
        }
        return E;
    }

    /** Maximum grade a REPEAT student can get for a repeated course. */
    public static GradeScale repeatCap() {
        return C;
    }

    /** True when this grade is a pass (C or above). */
    public boolean isPass() {
        return gradePoint >= C.gradePoint;
    }

    @Override
    public String toString() {
        return letter;
    }
}

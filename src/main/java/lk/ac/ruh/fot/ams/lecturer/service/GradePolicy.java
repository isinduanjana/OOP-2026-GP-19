package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.enums.StudentType;

/**
 * [M2 - LECTURER MODULE] Strategy that explains / applies the grading rule for a student.
 * The marks -> grade table itself is the common GradeScale enum (UGC Circular 12-2024);
 * a policy only decides what happens on top of it (e.g. repeat students capped at C).
 *
 *   GradePolicy p = GradePolicy.forStudent(result.getStudentType());   // Standard or Repeat
 *   p.grade(68.5)   -> B+ (standard)  or  C (repeat)
 *
 * OOP: Abstraction (interface), Polymorphism (two implementations chosen at run time:
 * StandardGradePolicy and RepeatGradePolicy), default + static interface methods.
 */
public interface GradePolicy {

    /** Short name shown in the "Grading rule" column. */
    String getName();

    /** One-sentence explanation shown in the individual view. */
    String describe();

    /** Adjusts the grade read from the UGC scale (each policy does it its own way). */
    GradeScale adjust(GradeScale scaleGrade);

    /** Template: marks -> UGC grade -> policy adjustment. */
    default GradeScale grade(double totalMarks) {
        return adjust(GradeScale.fromMarks(totalMarks));
    }

    /** Factory: picks the right policy for the student type. */
    static GradePolicy forStudent(StudentType type) {
        return type == StudentType.REPEAT ? new RepeatGradePolicy() : new StandardGradePolicy();
    }
}

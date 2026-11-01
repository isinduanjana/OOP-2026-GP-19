package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.model.enums.GradeScale;

/**
 * [M2 - LECTURER MODULE] Repeat students: same UGC scale, but the grade of a repeated
 * course is capped at GradeScale.repeatCap() (C) - the same rule the common
 * ResultService applies when it calculates results.
 *
 * OOP: Polymorphism - a second implementation of GradePolicy with different behaviour.
 */
public class RepeatGradePolicy implements GradePolicy {

    @Override
    public String getName() {
        return "Repeat (max " + GradeScale.repeatCap().getLetter() + ")";
    }

    @Override
    public String describe() {
        return "Repeat student: UGC scale, but the highest grade for a repeated course is "
                + GradeScale.repeatCap().getLetter() + " (" + GradeScale.repeatCap().getGradePoint() + ").";
    }

    @Override
    public GradeScale adjust(GradeScale scaleGrade) {
        GradeScale cap = GradeScale.repeatCap();
        return scaleGrade.getGradePoint() > cap.getGradePoint() ? cap : scaleGrade;
    }
}

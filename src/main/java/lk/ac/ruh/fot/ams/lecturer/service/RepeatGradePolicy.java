package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.model.enums.GradeScale;


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

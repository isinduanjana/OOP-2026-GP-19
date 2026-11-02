package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.model.enums.GradeScale;


public class StandardGradePolicy implements GradePolicy {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public String describe() {
        return "Standard UGC scale: the grade follows the final mark (A+ from 85, pass from C = 45).";
    }

    @Override
    public GradeScale adjust(GradeScale scaleGrade) {
        return scaleGrade;           // no change
    }
}

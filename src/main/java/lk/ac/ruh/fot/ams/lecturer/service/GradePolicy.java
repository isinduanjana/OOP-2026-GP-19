package lk.ac.ruh.fot.ams.lecturer.service;

import lk.ac.ruh.fot.ams.model.enums.GradeScale;
import lk.ac.ruh.fot.ams.model.enums.StudentType;


public interface GradePolicy {


    String getName();


    String describe();


    GradeScale adjust(GradeScale scaleGrade);


    default GradeScale grade(double totalMarks) {
        return adjust(GradeScale.fromMarks(totalMarks));
    }


    static GradePolicy forStudent(StudentType type) {
        return type == StudentType.REPEAT ? new RepeatGradePolicy() : new StandardGradePolicy();
    }
}

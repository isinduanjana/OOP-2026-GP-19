package lk.ac.ruh.fot.ams.model;

import lk.ac.ruh.fot.ams.model.enums.AssessmentType;

/**
 * [COMMON PART] One assessment of a course (Quiz 1, Mid, Final ...).
 * Marks are always entered out of 100 (handout); "percentage" is the weight
 * of the assessment in the final mark.
 */
public class Assessment {

    private int assessmentId;
    private int courseId;
    private String name;
    private AssessmentType type;
    private int maxMarks = 100;
    private double percentage;

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AssessmentType getType() { return type; }
    public void setType(AssessmentType type) { this.type = type; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    /** Contribution of a raw mark to the final 100. */
    public double contribution(double rawMark) {
        return rawMark / maxMarks * percentage;
    }

    @Override
    public String toString() {
        return name + " (" + type + ", " + (int) percentage + "%)";
    }
}

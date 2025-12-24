package com.university.backend.dto;

public class CourseDTO {
    private String courseCode;
    private String courseName;
    private int creditHours;
    private String semester;
    private String majorId;      // The missing piece
    private String prerequisite; // The other missing piece

    public CourseDTO(String courseCode, String courseName, int creditHours, String semester, String majorId, String prerequisite) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.semester = semester;
        this.majorId = majorId;
        this.prerequisite = prerequisite;
    }

    // Getters
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getCreditHours() { return creditHours; }
    public String getSemester() { return semester; }
    public String getMajorId() { return majorId; }
    public String getPrerequisite() { return prerequisite; }
}
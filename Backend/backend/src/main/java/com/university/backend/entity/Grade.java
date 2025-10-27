package com.universitymanagement.model.academic;

import jakarta.persistence.*;
import com.universitymanagement.model.BaseEntity;
import com.universitymanagement.model.user.Student;
import com.universitymanagement.model.course.Course;

@Entity
public class Grade extends BaseEntity {

    @ManyToOne
    private Student student;

    @ManyToOne
    private Course course;

    private String gradeValue; // e.g., A, B+, etc.
    private double numericScore;

    public Grade() {}

    public Grade(Student student, Course course, String gradeValue, double numericScore) {
        this.student = student;
        this.course = course;
        this.gradeValue = gradeValue;
        this.numericScore = numericScore;
    }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getGradeValue() { return gradeValue; }
    public void setGradeValue(String gradeValue) { this.gradeValue = gradeValue; }

    public double getNumericScore() { return numericScore; }
    public void setNumericScore(double numericScore) { this.numericScore = numericScore; }
}

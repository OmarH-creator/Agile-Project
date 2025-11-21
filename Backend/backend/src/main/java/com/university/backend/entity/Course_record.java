package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "course_records")
public class Course_record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private double grade;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private String semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;


    // Default constructor (required by JPA)
    public Course_record() {
    }

    public Course_record(String courseName, double grade, int credits , String semester) {
        this.courseName = courseName;
        this.grade = grade;
        this.credits = credits;
        this.semester = semester;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getSemester() { return semester; }

    public void setSemester(String semester) { this.semester = semester; }

    @Override
    public String toString() {
        return "CourseRecord{" +
                "courseName='" + courseName + '\'' +
                ", grade=" + grade +
                ", credits=" + credits +
                ", semester='" + semester + '\'' +
                '}';
    }
}
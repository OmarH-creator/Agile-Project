package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professors")
public class Professor {

    @Id
    @Column(unique = true, nullable = false, length = 255)
    private String professorId; // Primary Key

    @Column(nullable = false)
    private String professorName;

    @Column(nullable = false)
    private Integer Payment;

    @Column(unique = true, nullable = false)
    private String professorEmail;

    private String professorDepartment;

    @ElementCollection
    @CollectionTable(name = "professor_courses",
            joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "course_name")
    private List<String> professorCourses = new ArrayList<>();

    // Default constructor (required by JPA)
    public Professor() {
    }

    // Constructor with parameters
    public Professor(String professorId, String professorName, String professorEmail, String professorDepartment) {
        this.professorId = professorId;
        this.professorName = professorName;
        this.professorEmail = professorEmail;
        this.professorDepartment = professorDepartment;
    }

    public void assignCourse(String courseName) {
        this.professorCourses.add(courseName);
    }

    // Getters and Setters

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getProfessorEmail() {
        return professorEmail;
    }

    public void setProfessorEmail(String professorEmail) {
        this.professorEmail = professorEmail;
    }

    public String getProfessorDepartment() {
        return professorDepartment;
    }

    public void setProfessorDepartment(String professorDepartment) {
        this.professorDepartment = professorDepartment;
    }

    public List<String> getProfessorCourses() {
        return professorCourses;
    }


    public void setProfessorCourses(List<String> professorCourses) {
        this.professorCourses = professorCourses;
    }

    public Integer getPayment() {
        return Payment;
    }

    public void setPayment(Integer payment) {
        this.Payment = payment;
    }
}

// bamoot fe ma3amee3ak ya besheer ma3moo3 ma3moo3
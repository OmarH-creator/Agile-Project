package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professors")
public class Professor {

    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Technical PK

    @Id
    @Column(unique = true, nullable = false)
    private String professorId; // Business Key

    @Column(nullable = false)
    private String professorName;

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

    public Professor(String id, String name, String email, String department) {
        this.professorId = id;
        this.professorName = name;
        this.professorEmail = email;
        this.professorDepartment = department;
    }

    public void assignCourse(String courseName) {
        this.professorCourses.add(courseName);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
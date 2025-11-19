package com.university.backend.entity;

import java.util.ArrayList;
import java.util.List;

public class Professor {
    private String ProfessorId;
    private String ProfessorName;
    private String ProfessorEmail;
    private String ProfessorDepartment;
    private List<String> ProfessorCourses;

    public Professor() {
    }

    public Professor (String ID, String name, String email, String department) {
        this.ProfessorId = ID;
        this.ProfessorName = name;
        this.ProfessorEmail = email;
        this.ProfessorDepartment = department;
        this.ProfessorCourses = new ArrayList<>();
    }

    public void assignCourse(String courseName) {
        this.ProfessorCourses.add(courseName);
    }

    //getter to see courses later
    public List<String> getProfessorCourses() {
        return ProfessorCourses;
    }

    public String getProfessorId() {
        return ProfessorId;
    }
    public void setProfessorId(String professorId) {
        ProfessorId = professorId;
    }

    public String getProfessorName() {
        return ProfessorName;
    }

    public void setProfessorName(String professorName) {
        ProfessorName = professorName;
    }

    public String getProfessorEmail() {
        return ProfessorEmail;
    }

    public void setProfessorEmail(String professorEmail) {
        ProfessorEmail = professorEmail;
    }
}
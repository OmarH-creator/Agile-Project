package com.universitymanagement.model.user;

import jakarta.persistence.*;
import java.util.List;
import com.universitymanagement.model.BaseEntity;
import com.universitymanagement.model.course.Course;
import com.universitymanagement.model.academic.Grade;
import com.universitymanagement.model.academic.TranscriptRequest;

@Entity
public class Student extends BaseEntity{

    private String FirstName;
    private String LastName;
    private String studentID;
    private String email;
    private String department;

    @ManyToMany
    private List<Course> enrolledCourses;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Grade> grades;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<TranscriptRequest> transcriptRequests;

    //Constructors
    public Student() {
        //super();
    }

    public Student(String FirstName, String LastName, String studentID, String email, String department) {
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.studentID = studentID;
        this.email = email;
        this.department = department;
    }

    public void requestTranscript(){
        TranscriptRequest request = new TranscriptRequest(this);
        transcriptRequests.add(request);
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<Course> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }

    public List<TranscriptRequest> getTranscriptRequests() {
        return transcriptRequests;
    }

    public void setTranscriptRequests(List<TranscriptRequest> transcriptRequests) {
        this.transcriptRequests = transcriptRequests;
    }
}
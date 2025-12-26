package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {


  @Id
  @Column(name = "course_id", unique = true, nullable = false)
  private String courseCode;

  @Column(name = "course_name", nullable = false)
  private String courseName;

  @Column(name = "credit_hrs", nullable = false)
  private int creditHours;

  @Column(name = "semester", nullable = false)
  private String semester;

  @Transient
  private java.util.List<String> prerequisites = new java.util.ArrayList<>();

  @Transient
  private String professorEmail;

  // Default constructor (required by JPA)
  public Course() {
  }

  public Course(String courseCode, String courseName, int creditHours, String semester) {
    this.courseCode = courseCode;
    this.courseName = courseName;
    this.creditHours = creditHours;
    this.semester = semester;

  }

  @Override
  public String toString() {
    return "Course {" +
            ", code='" + courseCode + '\'' +
            ", name='" + courseName + '\'' +
            ", credits=" + creditHours +
            ", semester=" + semester +
            '}';
  }

  // Getters and Setters

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public int getCreditHours() {
    return creditHours;
  }

  public void setCreditHours(int creditHours) {
    this.creditHours = creditHours;
  }

  public String getSemester() {return semester;}

  public void setSemester(String semester) { this.semester = semester; }

  public java.util.List<String> getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(java.util.List<String> prerequisites) {
    this.prerequisites = prerequisites;
  }

  public String getProfessorEmail() {
    return professorEmail;
  }

  public void setProfessorEmail(String professorEmail) {
    this.professorEmail = professorEmail;
  }
}
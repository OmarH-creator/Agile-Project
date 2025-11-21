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

  // Default constructor (required by JPA)
  public Course() {
  }

  public Course(String courseCode, String courseName, int creditHours) {
    this.courseCode = courseCode;
    this.courseName = courseName;
    this.creditHours = creditHours;
  }

  @Override
  public String toString() {
    return "Course {" +
            ", code='" + courseCode + '\'' +
            ", name='" + courseName + '\'' +
            ", credits=" + creditHours +
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
}
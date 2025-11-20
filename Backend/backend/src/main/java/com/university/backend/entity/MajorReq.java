package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "major_requirements")
public class MajorReq {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Links to the Major Entity
  @ManyToOne
  @JoinColumn(name = "major_id", nullable = false)
  private Major major;

  // Links to the Course Entity using the unique "course_id" column (courseCode)
  @ManyToOne
  @JoinColumn(name = "course_code", referencedColumnName = "course_id", nullable = false)
  private Course course;

  // Default constructor
  public MajorReq() {
  }

  public MajorReq(Major major, Course course) {
    this.major = major;
    this.course = course;
  }

  @Override
  public String toString() {
    return "MajorRequirement {" +
            "id=" + id +
            ", major=" + (major != null ? major.getMajorName() : "null") +
            ", course=" + (course != null ? course.getCourseCode() : "null") +
            '}';
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Major getMajor() {
    return major;
  }

  public void setMajor(Major major) {
    this.major = major;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }
}
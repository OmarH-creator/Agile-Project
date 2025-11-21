package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "course_prerequisites")
public class CoursePrerequisite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // The course that has the requirement (e.g., "Advanced Java")
  @ManyToOne
  @JoinColumn(name = "course_code", referencedColumnName = "course_id", nullable = false)
  private Course course;

  // The course that IS the requirement (e.g., "Intro to Java")
  @ManyToOne
  @JoinColumn(name = "prerequisite_code", referencedColumnName = "course_id", nullable = false)
  private Course prerequisite;

  // Default constructor (required by JPA)
  public CoursePrerequisite() {
  }

  public CoursePrerequisite(Course course, Course prerequisite) {
    this.course = course;
    this.prerequisite = prerequisite;
  }

  @Override
  public String toString() {
    return "CoursePrerequisite {" +
            "id=" + id +
            ", course=" + (course != null ? course.getCourseCode() : "null") +
            ", prerequisite=" + (prerequisite != null ? prerequisite.getCourseCode() : "null") +
            '}';
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  public Course getPrerequisite() {
    return prerequisite;
  }

  public void setPrerequisite(Course prerequisite) {
    this.prerequisite = prerequisite;
  }
}
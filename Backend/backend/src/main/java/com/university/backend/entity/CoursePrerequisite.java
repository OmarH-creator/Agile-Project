package com.university.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "course_prerequisites")
@Check(constraints = "course_code <> prerequisite_code")
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
    validatePrerequisite(course, prerequisite);
    this.course = course;
    this.prerequisite = prerequisite;

  }
  private void validatePrerequisite(Course course, Course prerequisite) {
    if (course != null && prerequisite != null &&
            course.getCourseCode().equals(prerequisite.getCourseCode())) {
      throw new IllegalArgumentException("A course cannot be a prerequisite for itself.");
    }
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
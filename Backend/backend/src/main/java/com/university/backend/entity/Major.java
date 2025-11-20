package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "majors")
public class Major {

  @Id
  @Column(name = "major_id", nullable = false, unique = true, length = 255)
  private String majorId;

  @Column(name = "major_name", nullable = false, unique = true)
  private String majorName;

  // Default constructor (required by JPA)
  public Major() {
  }

  public Major(String majorId, String majorName) {
    this.majorId = majorId;
    this.majorName = majorName;
  }

  @Override
  public String toString() {
    return "Major {" +
            "id=" + majorId +
            ", name='" + majorName + '\'' +
            '}';
  }

  // Getters and Setters

  public String getMajorId() {
    return majorId;
  }

  public void setMajorId(String majorId) {
    this.majorId = majorId;
  }

  public String getMajorName() {
    return majorName;
  }

  public void setMajorName(String majorName) {
    this.majorName = majorName;
  }
}
package com.sample;


public class Course {

  private String courseCode;
  private String courseName;
  private long creditHours;


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


  public long getCreditHours() {
    return creditHours;
  }

  public void setCreditHours(long creditHours) {
    this.creditHours = creditHours;
  }

}

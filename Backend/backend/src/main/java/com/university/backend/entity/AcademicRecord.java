package com.sample;


public class AcademicRecord {

  private long studentCode;
  private String courseCode;
  private String semester;
  private String grade;


  public long getStudentCode() {
    return studentCode;
  }

  public void setStudentCode(long studentCode) {
    this.studentCode = studentCode;
  }


  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }


  public String getSemester() {
    return semester;
  }

  public void setSemester(String semester) {
    this.semester = semester;
  }


  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

}

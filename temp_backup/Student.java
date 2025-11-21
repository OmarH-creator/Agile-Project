package com.sample;


public class Student {

  private long studentId;
  private String name;
  private String e_Mail;
  private double cGpa;
  private String phoneNumber;
  private String militaryStatus;
  private String address;
  private long majorId;
  private String nationalId;
  private java.sql.Date birthDate;
  private long gradYear;
  private long completedHours;
  private long fees;


  public long getStudentId() {
    return studentId;
  }

  public void setStudentId(long studentId) {
    this.studentId = studentId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getE_Mail() {
    return e_Mail;
  }

  public void setE_Mail(String e_Mail) {
    this.e_Mail = e_Mail;
  }


  public double getCGpa() {
    return cGpa;
  }

  public void setCGpa(double cGpa) {
    this.cGpa = cGpa;
  }


  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }


  public String getMilitaryStatus() {
    return militaryStatus;
  }

  public void setMilitaryStatus(String militaryStatus) {
    this.militaryStatus = militaryStatus;
  }


  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }


  public long getMajorId() {
    return majorId;
  }

  public void setMajorId(long majorId) {
    this.majorId = majorId;
  }


  public String getNationalId() {
    return nationalId;
  }

  public void setNationalId(String nationalId) {
    this.nationalId = nationalId;
  }


  public java.sql.Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(java.sql.Date birthDate) {
    this.birthDate = birthDate;
  }


  public long getGradYear() {
    return gradYear;
  }

  public void setGradYear(long gradYear) {
    this.gradYear = gradYear;
  }


  public long getCompletedHours() {
    return completedHours;
  }

  public void setCompletedHours(long completedHours) {
    this.completedHours = completedHours;
  }


  public long getFees() {
    return fees;
  }

  public void setFees(long fees) {
    this.fees = fees;
  }

}

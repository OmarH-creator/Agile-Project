package com.sample;


public class Reserved {

  private long reservationId;
  private long hallId;
  private java.sql.Date date;
  private java.sql.Time startTime;
  private java.sql.Time endTime;
  private String purposeOfUse;
  private long staffId;


  public long getReservationId() {
    return reservationId;
  }

  public void setReservationId(long reservationId) {
    this.reservationId = reservationId;
  }


  public long getHallId() {
    return hallId;
  }

  public void setHallId(long hallId) {
    this.hallId = hallId;
  }


  public java.sql.Date getDate() {
    return date;
  }

  public void setDate(java.sql.Date date) {
    this.date = date;
  }


  public java.sql.Time getStartTime() {
    return startTime;
  }

  public void setStartTime(java.sql.Time startTime) {
    this.startTime = startTime;
  }


  public java.sql.Time getEndTime() {
    return endTime;
  }

  public void setEndTime(java.sql.Time endTime) {
    this.endTime = endTime;
  }


  public String getPurposeOfUse() {
    return purposeOfUse;
  }

  public void setPurposeOfUse(String purposeOfUse) {
    this.purposeOfUse = purposeOfUse;
  }


  public long getStaffId() {
    return staffId;
  }

  public void setStaffId(long staffId) {
    this.staffId = staffId;
  }

}

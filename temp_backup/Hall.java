package com.sample;


public class Hall {

  private long id;
  private long capacity;
  private String location;
  private String type;
  private String maintenanceReport;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }


  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getMaintenanceReport() {
    return maintenanceReport;
  }

  public void setMaintenanceReport(String maintenanceReport) {
    this.maintenanceReport = maintenanceReport;
  }

}

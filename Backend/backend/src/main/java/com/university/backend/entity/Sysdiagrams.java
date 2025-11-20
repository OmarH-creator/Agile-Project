package com.sample;


public class Sysdiagrams {

  private String name;
  private long principalId;
  private long diagramId;
  private long version;
  private String definition;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getPrincipalId() {
    return principalId;
  }

  public void setPrincipalId(long principalId) {
    this.principalId = principalId;
  }


  public long getDiagramId() {
    return diagramId;
  }

  public void setDiagramId(long diagramId) {
    this.diagramId = diagramId;
  }


  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }


  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

}

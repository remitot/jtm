package org.jepria.tomcat.manager.web.jdbc;

public class ModRequestDto {
  private String action;
  private String location;
  private ConnectionDto data;
  
  public ModRequestDto() {
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public ConnectionDto getData() {
    return data;
  }

  public void setData(ConnectionDto data) {
    this.data = data;
  }
  
}

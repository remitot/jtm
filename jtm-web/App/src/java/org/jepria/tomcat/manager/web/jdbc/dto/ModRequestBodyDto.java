package org.jepria.tomcat.manager.web.jdbc.dto;

public class ModRequestBodyDto {
  private String action;
  
  /**
   * Reference to the {@link #data#location} for external {@link ConnectionDto} identification (e.g. in modification requests)
   */
  private String location;
  
  private ConnectionDto data;
  
  public ModRequestBodyDto() {
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
package org.jepria.httpd.apache.manager.web.modjk.dto;

public class ModRequestBodyDto {
  private String action;
  
  /**
   * Reference to the {@link #data#location} for external {@link ModjkDto} identification (e.g. in modification requests)
   */
  private String location;
  private ModjkDto data;
  
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

  public ModjkDto getData() {
    return data;
  }

  public void setData(ModjkDto data) {
    this.data = data;
  }
  
}

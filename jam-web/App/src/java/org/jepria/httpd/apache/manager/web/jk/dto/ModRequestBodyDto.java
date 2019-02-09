package org.jepria.httpd.apache.manager.web.jk.dto;

public class ModRequestBodyDto {
  private String action;
  
  /**
   * Reference to the {@link #data#location} for external {@link JkDto} identification (e.g. in modification requests)
   */
  private String location;
  private JkDto data;
  
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

  public JkDto getData() {
    return data;
  }

  public void setData(JkDto data) {
    this.data = data;
  }
  
}

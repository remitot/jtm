package org.jepria.tomcat.manager.web.jdbc.dto;

public class ModRequestBodyDto {
  private String action;
  
  /**
   * Reference to the {@link #data#id} for external {@link ConnectionDto} identification (e.g. in modification requests)
   */
  private String id;
  
  private ConnectionDto data;
  
  public ModRequestBodyDto() {
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ConnectionDto getData() {
    return data;
  }

  public void setData(ConnectionDto data) {
    this.data = data;
  }
  
}

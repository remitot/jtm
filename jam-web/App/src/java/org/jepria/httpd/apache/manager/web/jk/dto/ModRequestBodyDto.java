package org.jepria.httpd.apache.manager.web.jk.dto;

public class ModRequestBodyDto {
  private String action;
  
  /**
   * Reference to the {@link #data#id} for external {@link JkDto} identification (e.g. in modification requests)
   */
  private String id;
  private JkDto data;
  
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

  public JkDto getData() {
    return data;
  }

  public void setData(JkDto data) {
    this.data = data;
  }
  
}

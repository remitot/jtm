package org.jepria.httpd.apache.manager.web.jk.dto;

public class BindingModDto {
  
  private String id;
  private Boolean active;
  private String application;
  /**
   * Host with http port: "tomcat-server:8080"
   */
  private String instance;
  
  public BindingModDto() {}
  
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Boolean getActive() {
    return active;
  }
  public void setActive(Boolean active) {
    this.active = active;
  }
  public String getApplication() {
    return application;
  }
  public void setApplication(String application) {
    this.application = application;
  }
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }
}

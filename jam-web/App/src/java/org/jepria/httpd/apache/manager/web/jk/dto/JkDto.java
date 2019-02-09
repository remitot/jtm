package org.jepria.httpd.apache.manager.web.jk.dto;

public class JkDto {
  
  /**
   * Location of the Jk binding in a configuration file
   */
  private String location;
  
  private Boolean active;
  
  private String application;
  private String worker;
  private String instance;
  
  public JkDto() {
  }
  
  
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
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
  public String getWorker() {
    return worker;
  }
  public void setWorker(String worker) {
    this.worker = worker;
  }
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }
}

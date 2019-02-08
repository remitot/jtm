package org.jepria.httpd.apache.manager.web.modjk.dto;

public class ModjkDto {
  
  /**
   * Location of the modjk binding in a configuration file
   */
  private String location;
  
  private Boolean active;
  
  private String application;
  private String instance;
  
  public ModjkDto() {
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
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }
  
  
  
}

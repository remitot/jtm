package org.jepria.ahttpd.manager.web.modjk.dto;

public class ModjkDto {
  
  /**
   * Location of the modjk binding in a configuration file
   */
  private String location;
  private String appname;
  private String instance;
  
  public ModjkDto() {
  }
  
  
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public String getAppname() {
    return appname;
  }
  public void setAppname(String appname) {
    this.appname = appname;
  }
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }
  
  
  
}

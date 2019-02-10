package org.jepria.httpd.apache.manager.web.jk.dto;

public class JkDto {
  
  /**
   * Location of the Jk binding in a configuration file
   */
  private String location;
  private Boolean active;
  private String application;
  private String host;
  /**
   * Field normally passed from server to client only
   */
  private String ajpPort;
  /**
   * Field normally passed from client to server only
   */
  private String httpPort;
  /**
   * Field normally passed from server to client only
   */
  private String getHttpPortLink;
  
  public JkDto() {}
  
  
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
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }
  public String getAjpPort() {
    return ajpPort;
  }
  public void setAjpPort(String ajpPort) {
    this.ajpPort = ajpPort;
  }
  public String getHttpPort() {
    return httpPort;
  }
  public void setHttpPort(String httpPort) {
    this.httpPort = httpPort;
  }
  public String getGetHttpPortLink() {
    return getHttpPortLink;
  }
  public void setGetHttpPortLink(String getHttpPortLink) {
    this.getHttpPortLink = getHttpPortLink;
  }
}

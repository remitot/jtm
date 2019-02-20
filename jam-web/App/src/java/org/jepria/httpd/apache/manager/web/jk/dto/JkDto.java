package org.jepria.httpd.apache.manager.web.jk.dto;

public class JkDto {
  
  private String id;
  private Boolean active;
  private String application;
  private String host;
  /**
   * Field normally passed from server to client only
   */
  private String ajpPort;
  /**
   * Field normally passed from server to client only
   */
  private String getHttpPortLink;
  /**
   * Field normally passed from client to server only (host with HTTP port: "tomcat-server:8080")
   */
  private String instance;
  
  public JkDto() {}
  
  
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
  public String getGetHttpPortLink() {
    return getHttpPortLink;
  }
  public void setGetHttpPortLink(String getHttpPortLink) {
    this.getHttpPortLink = getHttpPortLink;
  }
  public String getInstance() {
    return instance;
  }
  public void setInstance(String instance) {
    this.instance = instance;
  }
}

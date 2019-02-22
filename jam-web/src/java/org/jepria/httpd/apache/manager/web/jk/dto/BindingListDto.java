package org.jepria.httpd.apache.manager.web.jk.dto;

public class BindingListDto {
  
  private String id;
  private Boolean active;
  private String application;
  private String host;
  private Integer ajpPort;
  private String getHttpPortLink;
  
  public BindingListDto() {}
  
  
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
  public Integer getAjpPort() {
    return ajpPort;
  }
  public void setAjpPort(Integer ajpPort) {
    this.ajpPort = ajpPort;
  }
  public String getGetHttpPortLink() {
    return getHttpPortLink;
  }
  public void setGetHttpPortLink(String getHttpPortLink) {
    this.getHttpPortLink = getHttpPortLink;
  }
}

package org.jepria.httpd.apache.manager.web.jk.dto;

public class AjpRequestDto {
  private String host;
  private int port;
  private String uri;
  
  public AjpRequestDto() {}

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
     
  
}

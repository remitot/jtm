package org.jepria.httpd.apache.manager.web.jk.dto;

public class AjpResponseDto {
  private int status;
  private String statusMessage;
  private String responseBody;
  
  public AjpResponseDto() {}
  
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public String getStatusMessage() {
    return statusMessage;
  }
  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }
  public String getResponseBody() {
    return responseBody;
  }
  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }
  
  
}

package org.jepria.tomcat.manager.web.jdbc;

public class ModRequestDto {
  
  private String modRequestId;
  private ModRequestBodyDto modRequestBody;
  
  public ModRequestDto() {}

  public String getModRequestId() {
    return modRequestId;
  }

  public void setModRequestId(String modRequestId) {
    this.modRequestId = modRequestId;
  }

  public ModRequestBodyDto getModRequestBody() {
    return modRequestBody;
  }

  public void setModRequestBody(ModRequestBodyDto modRequestBody) {
    this.modRequestBody = modRequestBody;
  }
  
  
}

package org.jepria.tomcat.manager.web.port;

public class PortDto {
  private String type;
  private Integer number;
  
  public PortDto() {}

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }
  
  
}

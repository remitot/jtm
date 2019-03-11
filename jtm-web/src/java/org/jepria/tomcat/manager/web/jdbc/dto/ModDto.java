package org.jepria.tomcat.manager.web.jdbc.dto;

import java.util.List;

public class ModDto {
  private String action;
  private List<ItemModRequestDto> data;
  
  public ModDto() {
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public List<ItemModRequestDto> getData() {
    return data;
  }

  public void setData(List<ItemModRequestDto> data) {
    this.data = data;
  }
  
  
  
  
}

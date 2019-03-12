package org.jepria.tomcat.manager.web.dto;

import com.google.gson.JsonElement;

public class PostDto {
  private String action;
  private JsonElement data;
  
  public PostDto() {
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public JsonElement getData() {
    return data;
  }

  public void setData(JsonElement data) {
    this.data = data;
  }
}

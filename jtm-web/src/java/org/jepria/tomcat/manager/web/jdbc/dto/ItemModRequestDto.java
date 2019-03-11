package org.jepria.tomcat.manager.web.jdbc.dto;

import java.util.Map;

/**
 * Class representing a single data item modification request
 */
public class ItemModRequestDto {
  
  private String id;
  private String action;
  /**
   * {@code Map<fieldName, fieldValue>}, contains modified fields only
   */
  private Map<String, String> data;
  
  public ItemModRequestDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Map<String, String> getData() {
    return data;
  }

  public void setData(Map<String, String> data) {
    this.data = data;
  }
  
  
  
}

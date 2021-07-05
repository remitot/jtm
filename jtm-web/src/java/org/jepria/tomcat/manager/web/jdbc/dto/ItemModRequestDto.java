package org.jepria.tomcat.manager.web.jdbc.dto;

/**
 * Class representing a single record modification request
 */
public class ItemModRequestDto {

  private String id;
  private String action;
  /**
   * Contains modified fields only
   */
  private ConnectionDto data;

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

  public ConnectionDto getData() {
    return data;
  }

  public void setData(ConnectionDto data) {
    this.data = data;
  }
}

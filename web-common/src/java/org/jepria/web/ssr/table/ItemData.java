package org.jepria.web.ssr.table;

import java.util.HashMap;

public class ItemData extends HashMap<String, Field>{
  private static final long serialVersionUID = 1L;
  
  private String id;
  
  public ItemData() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}

package org.jepria.web.ssr.fields;

import java.util.HashMap;

// why it is a map? to generally get fields by HTML {@code name} attribute values
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

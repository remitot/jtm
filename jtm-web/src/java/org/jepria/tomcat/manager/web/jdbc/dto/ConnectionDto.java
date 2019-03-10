package org.jepria.tomcat.manager.web.jdbc.dto;

import java.util.HashMap;

public class ConnectionDto extends HashMap<String, String> {
  private static final long serialVersionUID = 1L;
  
  // not a field
  private boolean dataModifiable;
  
  public ConnectionDto() {}
  
  public boolean isDataModifiable() {
    return dataModifiable;
  }

  public void setDataModifiable(boolean dataModifiable) {
    this.dataModifiable = dataModifiable;
  }
}

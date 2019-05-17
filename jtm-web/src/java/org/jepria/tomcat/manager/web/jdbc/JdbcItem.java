package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;

/*package*/class JdbcItem extends ItemData {
  private static final long serialVersionUID = 1L;
  
  public boolean dataModifiable = true;
  
  public JdbcItem() {
    put("active", new Field("active"));
    put("name", new Field("name"));
    put("server", new Field("server"));
    put("db", new Field("db"));
    put("user", new Field("user"));
    put("password", new Field("password"));
  }
  
  public Field active() {
    return get("active");
  }
  public Field name() {
    return get("name");
  }
  public Field server() {
    return get("server");
  }
  public Field db() {
    return get("db");
  }
  public Field user() {
    return get("user");
  }
  public Field password() {
    return get("password");
  }
  
}

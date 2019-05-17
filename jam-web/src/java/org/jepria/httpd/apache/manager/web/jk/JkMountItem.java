package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;

/*package*/class JkMountItem extends ItemData {
  private static final long serialVersionUID = 1L;
  
  public JkMountItem() {
    put("active", new Field("active"));
    put("application", new Field("application"));
    put("details", new Field("details"));
  }
  
  public Field active() {
    return get("active");
  }
  public Field application() {
    return get("application");
  }
  public Field details() {
    return get("details");
  }
}

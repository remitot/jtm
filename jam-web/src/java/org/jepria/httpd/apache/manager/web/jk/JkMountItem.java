package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.table.Field;
import org.jepria.web.ssr.table.ItemData;

/*package*/class JkMountItem extends ItemData {
  private static final long serialVersionUID = 1L;
  
  public JkMountItem() {
    put("active", new Field("active"));
    put("application", new Field("application"));
  }
  
  public Field active() {
    return get("active");
  }
  public Field application() {
    return get("application");
  }
}

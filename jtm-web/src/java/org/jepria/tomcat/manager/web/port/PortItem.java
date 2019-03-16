package org.jepria.tomcat.manager.web.port;

import org.jepria.web.ssr.table.Field;
import org.jepria.web.ssr.table.ItemData;

/*package*/class PortItem extends ItemData {
  private static final long serialVersionUID = 1L;
  
  public PortItem() {
    put("type", new Field("type"));
    put("port", new Field("port"));
  }
  
  public Field type() {
    return get("type");
  }
  public Field port() {
    return get("port");
  }
}

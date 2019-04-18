package org.jepria.tomcat.manager.web.log;

import org.jepria.web.ssr.table.Field;
import org.jepria.web.ssr.table.ItemData;

/*package*/class LogItem extends ItemData {
  
  private static final long serialVersionUID = 1L;

  public LogItem() {
    put("name", new Field("name"));
    put("lastmod", new Field("lastmod"));
    put("download", new Field("download"));
    put("open", new Field("open"));
    put("monitor", new Field("monitor"));
  }
  
  public Field name() {
    return get("name");
  }
  
  public Field lastmod() {
    return get("lastmod");
  }
  
  public Field download() {
    return get("download");
  }
  
  public Field open() {
    return get("open");
  }
  
  public Field monitor() {
    return get("monitor");
  }
  
}

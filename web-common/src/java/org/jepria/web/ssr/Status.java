package org.jepria.web.ssr;

public class Status {
  public static enum Type {
    NONE,
    SUCCESS,
    INFO,
    ERROR
  }
  
  public final Type type;
  public final String statusHTML;
  
  public Status(Type type, String statusHTML) {
    this.type = type;
    this.statusHTML = statusHTML;
  }
}

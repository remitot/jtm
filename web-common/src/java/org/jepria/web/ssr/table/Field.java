package org.jepria.web.ssr.table;

public class Field {
  
  public Field(String name) {
    this.name = name;
  }
  
  public final String name;
  public String value = null;
  public String valueOriginal = null;
  public boolean readonly = false;
  public boolean invalid = false;
  public String invalidMessage = null;
}

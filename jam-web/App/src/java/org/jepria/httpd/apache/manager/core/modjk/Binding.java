package org.jepria.httpd.apache.manager.core.modjk;

public interface Binding {
  
  boolean isActive();
  void setActive(boolean active);

  String getAppname();
  void setAppname(String appname);
  
  String getInstance();
  void setInstance(String instance);
}

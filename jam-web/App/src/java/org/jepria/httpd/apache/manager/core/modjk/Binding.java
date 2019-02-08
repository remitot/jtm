package org.jepria.httpd.apache.manager.core.modjk;

public interface Binding {
  
  boolean isActive();
  void setActive(boolean active);

  String getApplication();
  void setApplication(String application);
  
  String getInstance();
  void setInstance(String instance);
}

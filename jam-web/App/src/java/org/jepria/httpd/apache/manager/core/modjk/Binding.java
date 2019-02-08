package org.jepria.httpd.apache.manager.core.modjk;

public interface Binding {
  boolean isActive();
  
  /**
   * Action for an inactive connection became active
   */
  void onActivate();
  
  /**
   * Action for an active connection became inactive
   */
  void onDeactivate();
  
  String getAppname();
  void setAppname(String appname);
  String getInstance();
  void setInstance(String instance);
}

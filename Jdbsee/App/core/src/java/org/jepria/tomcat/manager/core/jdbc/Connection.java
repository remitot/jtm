package org.jepria.tomcat.manager.core.jdbc;

public interface Connection {
  
  boolean isActive();
  void setActive(boolean active);

  String getName();
  void setName(String connectionName);

  String getServer();
  void setServer(String server);

  String getDb();
  void setDb(String db);

  String getUser();
  void setUser(String user);

  String getPassword();
  void setPassword(String password);
}

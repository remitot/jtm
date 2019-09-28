package org.jepria.tomcat.manager.core.jdbc;

public interface Connection {
  
  /**
   * Whether or not this Resource's "data" fields (server, db, user, password) are modifiable
   * and the resource is deletable.
   * The "non-data" fields (active and name) are always modifiable.
   * An attempt to modify data fields or delete the resource
   * if this method returns {@code false} will cause errors.
   */
  boolean isDataModifiable();
  
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

package org.jepria.tomcat.manager.web.jdbc.dto;

import java.util.HashMap;

public class ConnectionDto {
  
  private String active;
  private String id;
  private String name;
  private String server;
  private String db;
  private String user;
  private String password;
  
  // not a field
  private boolean dataModifiable;
  
  public ConnectionDto() {}
  
  public boolean isDataModifiable() {
    return dataModifiable;
  }

  public void setDataModifiable(boolean dataModifiable) {
    this.dataModifiable = dataModifiable;
  }

  public String getActive() {
    return active;
  }

  public void setActive(String active) {
    this.active = active;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

package org.jepria.tomcat.manager.web.jdbc.dto;

import org.jepria.web.Dto;

public class ConnectionDto extends Dto {
  
  private Boolean dataModifiable;
  private Boolean active;
  
  private String name;
  private String server;
  private String db;
  private String user;
  private String password;
  
  
  public ConnectionDto() {
  }

  public Boolean getDataModifiable() {
    return dataModifiable;
  }

  public void setDataModifiable(Boolean dataModifiable) {
    this.dataModifiable = dataModifiable;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
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

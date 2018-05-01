package org.jepria.tomcat.manager.core;

public class LocationNotExistException extends Exception {
  
  private static final long serialVersionUID = 1L;

  public LocationNotExistException(String message) {
    super(message);
  }
  
}

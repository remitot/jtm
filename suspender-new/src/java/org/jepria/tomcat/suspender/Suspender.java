package org.jepria.tomcat.suspender;

public interface Suspender {
  /**
   * @param application
   * @return {@code true} if the application existed suspended and has been successfully unsuspended, 
   * {@code false} if there has been no such application existed suspended 
   * (e.g. an application existed already unsuspended) 
   */
  boolean unsuspend(String application);
  /**
   * @param application
   * @return {@code true} if the application existed unsuspended and has been successfully suspended, 
   * {@code false} if there has been no such application existed unsuspended 
   * (e.g. an application existed already suspended) 
   */
  boolean suspend(String application);
}

package org.jepria.web;

public class Dto {
  
  private String id;

  public Dto() {}
  
  public final String getId() {
    return id;
  }

  public final void setId(String id) {
    this.id = id;
  }
  
  public void overlay(Dto another) {
    throw new UnsupportedOperationException("The method must be overridden by the applicational Dto classes");
  }
}

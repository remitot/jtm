package org.jepria.tomcat.suspender.core;

public class SuspenderFactory {
  public static Suspender get() {
    return new SuspenderImpl();
  }
}

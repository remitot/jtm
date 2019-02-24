package org.jepria.tomcat.suspender;

public class SuspenderFactory {
  public static Suspender get() {
    return new SuspenderImpl();
  }
}

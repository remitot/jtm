package org.jepria.tomcat.suspender;

import java.nio.file.Paths;

public class WebappsFactory {
  public static Webapps get() {
    // TODO
    return new WebappsImpl(Paths.get("/opt/tomcat/webapps"));
  }
}

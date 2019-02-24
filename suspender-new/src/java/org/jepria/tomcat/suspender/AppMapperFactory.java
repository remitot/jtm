package org.jepria.tomcat.suspender;

import java.nio.file.Paths;

public class AppMapperFactory {
  public static AppMapper get() {
    // TODO
    return new AppMapperImpl(Paths.get("/opt/tomcat/webapps"));
  }
}

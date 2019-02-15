package org.jepria.httpd.apache.manager.web;

import java.io.InputStream;
import java.io.OutputStream;

public interface Environment {
  InputStream getMod_jk_confInputStream();
  InputStream getWorkers_propertiesInputStream();
  OutputStream getMod_jk_confOutputStream();
  OutputStream getWorkers_propertiesOutputStream();
}

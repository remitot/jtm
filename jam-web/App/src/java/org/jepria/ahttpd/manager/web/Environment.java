package org.jepria.ahttpd.manager.web;

import java.io.InputStream;
import java.io.OutputStream;

public interface Environment {
  InputStream getModjkConfInputStream();
  InputStream getWorkerPropertiesInputStream();
  OutputStream getModjkConfOutputStream();
  OutputStream getWorkerPropertiesOutputStream();
}

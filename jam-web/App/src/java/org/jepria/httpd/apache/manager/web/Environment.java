package org.jepria.httpd.apache.manager.web;

import java.io.InputStream;
import java.io.OutputStream;

public interface Environment {
  /**
   * @return new input stream for the mod_jk.conf configuration file
   * (normally at APACHE_HOME/conf/jk/mod_jk.conf)
   */
  InputStream getMod_jk_confInputStream();
  /**
   * @return new input stream for the workers.properties configuration file
   * (normally at APACHE_HOME/conf/jk/workers.properties)
   */
  InputStream getWorkers_propertiesInputStream();
  /**
   * @return new output stream for the mod_jk.conf configuration file 
   * (normally at APACHE_HOME/conf/jk/mod_jk.conf)
   */
  OutputStream getMod_jk_confOutputStream();
  /**
   * @return new output stream for the workers.properties configuration file 
   * (normally at APACHE_HOME/conf/jk/workers.properties)
   */
  OutputStream getWorkers_propertiesOutputStream();
  
  /**
   * Retrieve an application configuration property.
   * 1. Look up the property defined in {@code java:comp/env/name} JNDI entry. If the entry is defined, return the value.
   * 2. Look up the property in the custom {@code app-conf.properties} file, if such file is defined in 
   * {@code java:comp/env/org.jepria.tomcat.manager.web.conf.file} JNDI entry. If the property is defined in that file, return the value.
   * 3. Look up the property in internal {@code app-conf-default.properties} file. If the property is defined, return the value. 
   * 4. return {@code null}.
   */
  String getProperty(String name);
}

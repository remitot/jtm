package org.jepria.httpd.apache.manager.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface Environment {
  
  /**
   * @return Path for the mod_jk.conf configuration file
   * (normally at APACHE_HOME/conf/jk/mod_jk.conf)
   */
  Path getMod_jk_confFile();
  
  default InputStream getMod_jk_confInputStream() {
    try {
      return new FileInputStream(getMod_jk_confFile().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  /**
   * @return Path for the workers.properties configuration file
   * (normally at APACHE_HOME/conf/jk/workers.properties)
   */
  Path getWorkers_propertiesFile();
  
  default InputStream getWorkers_propertiesInputStream() {
    try {
      return new FileInputStream(getWorkers_propertiesFile().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }

  default OutputStream getMod_jk_confOutputStream() {
    try {
      return new FileOutputStream(getMod_jk_confFile().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  default OutputStream getWorkers_propertiesOutputStream() {
    try {
      return new FileOutputStream(getWorkers_propertiesFile().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  /**
   * Retrieve an application configuration property.
   * 1. Look up the property defined in {@code java:comp/env/name} JNDI entry. If the entry is defined, return the value.
   * 2. Look up the property in the custom {@code app-conf.properties} file, if such file is defined in 
   * {@code java:comp/env/org.jepria.tomcat.manager.web.conf.file} JNDI entry. If the property is defined in that file, return the value.
   * 3. Look up the property in internal {@code app-conf-default.properties} file. If the property is defined, return the value. 
   * 4. return {@code null}.
   */
  String getProperty(String name);
  
  /**
   * @return new {@link Path} representing the {@code conf} directory
   * (normally at APACHE_HOME/conf). Normally the file is an existing readable directory.
   */
  default Path getConfDirectory() {
    return getHomeDirectory().resolve("conf");
  }
  
  /**
   * @return Path known as APACHE_HOME. Normally the file is an existing readable directory.
   */
  Path getHomeDirectory();
}

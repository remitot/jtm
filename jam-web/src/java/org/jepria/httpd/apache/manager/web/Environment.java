package org.jepria.httpd.apache.manager.web;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Environment {
  
  /**
   * @return Path for the mod_jk.conf configuration file
   * (normally at APACHE_HOME/conf/jk/mod_jk.conf)
   */
  default Path getMod_jk_confFile() {
    return getConfDirectory().resolve("jk").resolve("mod_jk.conf");
    // TODO?
//    if (path == null || !Files.isRegularFile(path)) {
//      throw new RuntimeException("Misconfiguration exception: could not initialize mod_jk.conf file: [" + path + "] is not a file");
//    }
  }
  
  default InputStream getMod_jk_confInputStream() {
    try {
      return new FileInputStream(getMod_jk_confFile().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  /**
   * @return Path for the workers.properties configuration file
   * (normally parsed from the JkWorkersFile directive in the mod_jk.conf file)
   */
  default Path getWorkers_propertiesFile() {

    Path path = null;

    try (Scanner sc = new Scanner(getMod_jk_confFile())) {

      // JkWorkersFile directive syntax: the value may be quoted or non-quoted, absolute or relative
      final Pattern p = Pattern.compile("\\s*JkWorkersFile\\s+\"?(.+?)\"?\\s*");

      boolean jkWorkersFileFound = false;
      while (sc.hasNextLine()) {

        String line = sc.nextLine();
        Matcher m = p.matcher(line);
        if (m.matches()) {
          jkWorkersFileFound = true;

          Path workersFile = Paths.get(m.group(1));

          if (workersFile.isAbsolute()) {
            path = workersFile;
          } else {
            // the path must be relative to the apache home
            // TODO find out how exactly does Apache parse the JkWorkersFile relative path
            path = getHomeDirectory().resolve(workersFile);
            if (path == null || !Files.isRegularFile(path)) {
              throw new RuntimeException("Misconfiguration exception: could not initialize workers.properties file: [" + path + "] is not a file");
            }
          }

          if (path == null || !Files.isRegularFile(path)) {
            throw new RuntimeException("Misconfiguration exception: "
                    + "the directive [" + line + "] in the file [" + path + "] "
                    + "does not represent a file");
          }

          break;
        }
      }
      if (!jkWorkersFileFound) {
        throw new RuntimeException("Misconfiguration exception: "
                + "the file [" + getMod_jk_confFile() + "] contains no 'JkWorkersFile' directive");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return path;
  }
  
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

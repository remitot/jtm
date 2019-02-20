package org.jepria.httpd.apache.manager.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final Path mod_jk_conf;
  private Path workers_properties;// initialized dynamically from mod_jk.conf
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  public BasicEnvironment(HttpServletRequest request) {
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
    
    final String confDirEnv = getProperty("org.jepria.httpd.apache.manager.web.apacheConf");
    if (confDirEnv == null) {
      throw new RuntimeException("Misconfiguration exception: "
          + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheConf\" is not defined");
    }
    
    mod_jk_conf = Paths.get(confDirEnv).resolve("jk").resolve("mod_jk.conf");
    if (!Files.isRegularFile(mod_jk_conf)) {
      throw new RuntimeException("Misconfiguration exception: [" + mod_jk_conf + "] is not a file");
    }
    
    
    // init workers.properties
    workers_properties = null;
    try (Scanner sc = new Scanner(mod_jk_conf)) {
      final Pattern p = Pattern.compile("\\s*JkWorkersFile\\s+\"(.+)\"\\s*");
      while (sc.hasNext()) {
        String line = sc.nextLine();
        Matcher m = p.matcher(line);
        if (m.matches()) {
          final String workersFileDef = m.group(1);
          Path workersFile = Paths.get(workersFileDef);// normally begins with 'conf/'
          Path workersFileRel = workersFile.getName(0).relativize(workersFile);
          workers_properties = Paths.get(confDirEnv).resolve(workersFileRel);
          if (!Files.isRegularFile(workers_properties)) {
            throw new RuntimeException("Misconfiguration exception: "
                + "the directive [" + line + "] in the file [" + mod_jk_conf + "] "
                    + "does not represent a file");
          }
          break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    if (workers_properties == null) {
      throw new RuntimeException("Misconfiguration exception: "
          + "the file [" + mod_jk_conf + "] contains no 'JkWorkersFile' directive");
    }
    
  }
  
  @Override
  public OutputStream getMod_jk_confOutputStream() {
    try {
      return new FileOutputStream(mod_jk_conf.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getMod_jk_confInputStream() {
    try {
      return new FileInputStream(mod_jk_conf.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public OutputStream getWorkers_propertiesOutputStream() {
    try {
      return new FileOutputStream(workers_properties.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getWorkers_propertiesInputStream() {
    try {
      return new FileInputStream(workers_properties.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
}
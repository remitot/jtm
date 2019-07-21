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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic production environment
 */
public class BasicEnvironment implements Environment {
  
  private final Supplier<Path> mod_jk_conf = new Supplier<Path>() {
    
    private Path path;
    
    @Override
    public Path get() {
      if (path == null) {
        init();
      }
      return path;
    }
    
    private void init() {
      final String confDirEnv = getProperty("org.jepria.httpd.apache.manager.web.apacheConf");
      if (confDirEnv == null) {
        throw new RuntimeException("Misconfiguration exception: "
            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheConf\" is not defined");
      }
      
      path = Paths.get(confDirEnv).resolve("jk").resolve("mod_jk.conf");
      if (path == null) {
        throw new IllegalStateException("The Path is not expected to be null");
      }
      if (!Files.isRegularFile(path)) {
        throw new RuntimeException("Misconfiguration exception: [" + mod_jk_conf + "] is not a file");
      }
    }
  };
  
  private final Supplier<Path> workers_properties = new Supplier<Path>() {
    
    private Path path;
    
    @Override
    public Path get() {
      if (path == null) {
        init();
      }
      return path;
    }
    
    private void init() {
      path = null;
      try (Scanner sc = new Scanner(mod_jk_conf.get())) {

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
              throw new UnsupportedOperationException("Relative JkWorkersFile path is not supported, specify the absolute path");
            }
            if (path == null) {
              throw new IllegalStateException("The Path is not expected to be null");
            }
            if (!Files.isRegularFile(path)) {
              throw new RuntimeException("Misconfiguration exception: "
                  + "the directive [" + line + "] in the file [" + mod_jk_conf.get() + "] "
                      + "does not represent a file");
            }
            
            break;
          }
        }
        if (!jkWorkersFileFound) {
          throw new RuntimeException("Misconfiguration exception: "
              + "the file [" + mod_jk_conf + "] contains no 'JkWorkersFile' directive");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  };
  
  private final EnvironmentPropertyFactory envPropertyFactory;
  
  public BasicEnvironment(HttpServletRequest request) {
    envPropertyFactory = new EnvironmentPropertyFactory(new File(request.getServletContext().getRealPath("/WEB-INF/app-conf-default.properties")));
  }
  
  protected Path getMod_jk_confFile() {
    return mod_jk_conf.get();
  }
  
  @Override
  public OutputStream getMod_jk_confOutputStream() {
    try {
      return new FileOutputStream(mod_jk_conf.get().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getMod_jk_confInputStream() {
    try {
      return new FileInputStream(mod_jk_conf.get().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public OutputStream getWorkers_propertiesOutputStream() {
    try {
      return new FileOutputStream(workers_properties.get().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public InputStream getWorkers_propertiesInputStream() {
    try {
      return new FileInputStream(workers_properties.get().toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);//TODO?
    }
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
}
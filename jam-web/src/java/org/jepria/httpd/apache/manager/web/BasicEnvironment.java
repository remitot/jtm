package org.jepria.httpd.apache.manager.web;

import java.io.File;
import java.io.IOException;
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
  
  private final Supplier<Path> apacheHome = new Supplier<Path>() {
    
    private Path path = null;
    
    @Override
    public Path get() {
      if (path == null) {
        init();
      }
      return path;
    }
    
    private void init() {
      final String apacheHomeEnv = getProperty("org.jepria.httpd.apache.manager.web.apacheHome");
      if (apacheHomeEnv == null) {
        throw new RuntimeException("Misconfiguration exception: "
            + "mandatory configuration property \"org.jepria.httpd.apache.manager.web.apacheHome\" is not defined");
      }
      
      path = Paths.get(apacheHomeEnv);
      
      if (path == null || !Files.isDirectory(path)) {
        throw new RuntimeException("Misconfiguration exception: "
            + "the configuration property \"org.jepria.httpd.apache.manager.web.apacheHome\" does not represent a directory: " 
            + "[" + path + "]");
      }
    }
  };
  
  private final Supplier<Path> mod_jk_conf = new Supplier<Path>() {
    
    private Path path = null;
    
    @Override
    public Path get() {
      if (path == null) {
        init();
      }
      return path;
    }
    
    private void init() {
      path = getConfDirectory().resolve("jk/mod_jk.conf");
      if (path == null || !Files.isRegularFile(path)) {
        throw new RuntimeException("Misconfiguration exception: could not initialize mod_jk.conf file: [" + path + "] is not a file");
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
              // the path must be relative to the apache home
              // TODO find out how exactly does Apache parse the JkWorkersFile relative path
              path = apacheHome.get().resolve(workersFile);
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
              + "the file [" + mod_jk_conf.get() + "] contains no 'JkWorkersFile' directive");
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
  
  @Override
  public Path getMod_jk_confFile() {
    return mod_jk_conf.get();
  }
  
  @Override
  public String getProperty(String name) {
    return envPropertyFactory.getProperty(name);
  }
  
  @Override
  public Path getHomeDirectory() {
    return apacheHome.get();
  }
  
  @Override
  public Path getWorkers_propertiesFile() {
    return workers_properties.get();
  }
}
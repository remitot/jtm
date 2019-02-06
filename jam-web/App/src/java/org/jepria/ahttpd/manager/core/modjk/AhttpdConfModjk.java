package org.jepria.ahttpd.manager.core.modjk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jepria.ahttpd.manager.core.TransactionException;

public class AhttpdConfModjk {
  
  protected final List<StringBuilder> modjkConfLines;
  
  protected final List<String> workerPropertiesLines;
  
  ///////////////// Methods are analogous to TomcatConfJdbc ///////////////
  
  public AhttpdConfModjk(InputStream modjkConfInputStream,
      InputStream workerPropertiesInputStream) throws TransactionException {
    
    try (InputStream modjkConfInputStream0 = modjkConfInputStream;
        InputStream workerPropertiesInputStream0 = workerPropertiesInputStream) {
      
      modjkConfLines = new ArrayList<>();
      try (Scanner sc = new Scanner(modjkConfInputStream0)) {
        while (sc.hasNextLine()) {
          modjkConfLines.add(new StringBuilder(sc.nextLine()));
        }
      }
      
      
      workerPropertiesLines = new ArrayList<>();
      try (Scanner sc = new Scanner(workerPropertiesInputStream0)) {
        while (sc.hasNextLine()) {
          workerPropertiesLines.add(sc.nextLine());
        }
      }
      
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
  
  /**
   * Lazily initialized map of bindings
   */
  private Map<String, Binding> baseBindings = null;
  
  /**
   * @return unmodifiable Map&lt;Location, Connection&gt;
   */
  @SuppressWarnings("unchecked")
  public Map<String, Binding> getBindings() {
    return (Map<String, Binding>)(Map<String, ?>)getBaseBindings();
  }
  
  /**
   * @return unmodifiable Map&lt;Location, BaseConnection&gt;
   */
  protected Map<String, Binding> getBaseBindings() {
    if (baseBindings == null) {
      initBaseBindings();
    }
    
    return baseBindings;
  }
  
  private class JkMount {
    public JkMount(boolean commented, String workerName, int lineNumber, StringBuilder line) {
      this.commented = commented;
      this.workerName = workerName;
      this.lineNumber = lineNumber;
      this.line = line;
    }
    
    public final boolean commented;
    
    public final String workerName;
    /**
     * The number of line in a file
     */
    public final int lineNumber;
    /**
     * The line itself
     */
    public final StringBuilder line;
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #baseBindings} map
   */
  private void initBaseBindings() {
    
    Map<String, Binding> baseBindings0 = new HashMap<>();
    

    // collect JkMounts to '/Application'
    Map<String, JkMount> appRootJkMounts = new HashMap<>();
    // collect JkMounts to '/Application/*'
    Map<String, JkMount> appAsterJkMounts = new HashMap<>();
    
    
    for (int i = 0; i < modjkConfLines.size(); i++) {
      final StringBuilder line = modjkConfLines.get(i);
      
      Matcher m = Pattern.compile("\\s*(#*)\\s*JkMount\\s+([^\\s]+)\\s+([^\\s]+)\\s*").matcher(line);
      if (m.matches()) {
        final boolean commented = m.group(1).length() > 0;
        final String urlPattern = m.group(2);
        final String workerName = m.group(3);
        
        if (urlPattern.startsWith("/")) {
          if (urlPattern.endsWith("/*")) {
            appAsterJkMounts.put(urlPattern.substring(1, urlPattern.length() - 2), new JkMount(commented, workerName, i, line));
          } else {
            appRootJkMounts.put(urlPattern.substring(1), new JkMount(commented, workerName, i, line));
          }
        }
      }
    }
    
    
    // filter only JkMounts with both application root and application asterisk mounted to the same worker
    Set<String> rootAndAsterApps = new HashSet<>(appRootJkMounts.keySet());
    rootAndAsterApps.retainAll(appAsterJkMounts.keySet());
    for (String rootAndAsterApp: rootAndAsterApps) {
      JkMount rootMount = appRootJkMounts.get(rootAndAsterApp);
      JkMount asterMount = appAsterJkMounts.get(rootAndAsterApp);
      if (rootMount.workerName.equals(asterMount.workerName) && rootMount.commented == asterMount.commented) {
        
        String location = rootMount.lineNumber + "_" + asterMount.lineNumber;
        
        baseBindings0.put(location, new Binding() {
          @Override
          public void setInstance(String instance) {
          }
          @Override
          public void setAppname(String appname) {
          }
          @Override
          public void onDeactivate() {
          }
          @Override
          public void onActivate() {
          }
          @Override
          public boolean isActive() {
            return true;
          }
          @Override
          public String getInstance() {
            return location + ": " + rootMount.workerName;
          }
          @Override
          public String getAppname() {
            return rootAndAsterApp;
          }
        });
      }
    }
    
    this.baseBindings = Collections.unmodifiableMap(baseBindings0);
  }
  
  public void delete(String location) {
    
  }
  
  public Binding create() {
    return null;
  }
  
  public void save(OutputStream modjkConfOutputStream,
      OutputStream workerPropertiesOutputStream) {
    
  }
}

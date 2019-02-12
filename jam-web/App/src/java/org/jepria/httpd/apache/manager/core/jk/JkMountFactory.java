package org.jepria.httpd.apache.manager.core.jk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JkMountFactory {

  private static class JkMountImpl implements JkMount {
    private final JkMountDirective rootMountDirective;
    private final  JkMountDirective asterMountDirective;
    
    public JkMountImpl(JkMountDirective rootMountDirective, JkMountDirective asterMountDirective) {
      this.rootMountDirective = rootMountDirective;
      this.asterMountDirective = asterMountDirective;
    }

    @Override
    public String getLocation() {
      return rootMountDirective.line.lineNumber() + "-" + asterMountDirective.line.lineNumber();
    }
    
    @Override
    public boolean isActive() {
      return !rootMountDirective.commented && !asterMountDirective.commented;
    }
    
    @Override
    public void setActive(boolean active) {
      
    }
    
    @Override
    public String getApplication() {
      // TODO better place to validate all three directive's application equality? what if not equal?
      if (!rootMountDirective.application.equals(asterMountDirective.application)) {
        throw new IllegalStateException("Expected all the same applications, but actual: [" 
            + rootMountDirective.application + "][" + asterMountDirective.application + "]");
      }
      return rootMountDirective.application;
    }
    
    @Override
    public void setApplication(String application) {
      rootMountDirective.application =
          asterMountDirective.application = application;
    }

    @Override
    public String workerName() {
      // TODO better place to validate all three directive's workerName equality? what if not equal?
      if (!rootMountDirective.workerName.equals(asterMountDirective.workerName)) {
        throw new IllegalStateException("Expected all the same workerNames, but actual: [" 
            + rootMountDirective.workerName + "][" + asterMountDirective.workerName + "]");
      }
      return rootMountDirective.workerName;
    }
    
    @Override
    public void setWorkerName(String workerName) {
      rootMountDirective.workerName =
          asterMountDirective.workerName = workerName;
    }
    
  }
  
  public static final Pattern JK_MOUNT_PATTERN = Pattern.compile("\\s*(#*)\\s*JkMount\\s+([^\\s]+)\\s+([^\\s]+)\\s*");
  
  public static List<JkMount> parse(Iterator<TextLineReference> lineIterator) {
    List<JkMount> ret = new ArrayList<>();
    
    if (lineIterator != null) {
      // collect root mounts ('/Application'), with application names as keys
      Map<String, JkMountDirective> rootMounts = new HashMap<>();
      // collect asterisk mounts ('/Application/*'), with application names as keys 
      Map<String, JkMountDirective> asterMounts = new HashMap<>();
      
      while (lineIterator.hasNext()) {
        final TextLineReference line = lineIterator.next();
        
        Matcher m = JK_MOUNT_PATTERN.matcher(line);
        if (m.matches()) {
          final boolean commented = m.group(1).length() > 0;
          final String urlPattern = m.group(2);
          final String workerName = m.group(3);
          
          final Map<String, JkMountDirective> targetMap;
          
          if (urlPattern.startsWith("/")) {
            final String application;
            if (urlPattern.endsWith("/*")) {
              application = urlPattern.substring(1, urlPattern.length() - 2);
              targetMap = asterMounts;
            } else {
              application = urlPattern.substring(1);
              targetMap = rootMounts;
            }
          
            final JkMountDirective mountd = new JkMountDirective(commented, workerName, application, line);
            
            targetMap.put(application, mountd);
          }
        }
      }
      
      
      // assemble JkMounts from JkMountDirectives with both root and asterisk mounted to the same application and worker
      Set<String> applications = new HashSet<>(rootMounts.keySet());
      applications.retainAll(asterMounts.keySet());
      
      for (String application: applications) {
        JkMountDirective rootMountd = rootMounts.get(application);
        JkMountDirective asterMountd = asterMounts.get(application);
        
        if (rootMountd.workerName.equals(asterMountd.workerName)) {
          
          JkMount mount = new JkMountImpl(rootMountd, asterMountd);
          
          ret.add(mount);
        }
      }
    }
    
    return ret;
  }
  
  /**
   * Class representing a single JkMount directive
   */
  private static class JkMountDirective {
    
    public boolean commented;

    /**
     * The worker name (value of the mount)
     */
    public String workerName;
    
    public String application;
    
    /**
     * The line with mount itself
     */
    public final TextLineReference line;
    
    public JkMountDirective(boolean commented, String workerName, String application, TextLineReference line) {
      this.commented = commented;
      this.workerName = workerName;
      this.application = application;
      this.line = line;
    }
  }
  
}

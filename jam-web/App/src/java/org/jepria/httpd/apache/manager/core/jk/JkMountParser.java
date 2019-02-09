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

public class JkMountParser {

  private static class JkMountImpl implements JkMount {
    private final boolean commented;
    private final String application;
    private final String workerName;
    private final TextLineReference rootMountLine;
    private final TextLineReference asteriskMountLine;
    
    public JkMountImpl(boolean commented, String application, String workerName, 
        TextLineReference rootMountLine, TextLineReference asteriskMountLine) {
      this.commented = commented;
      this.application = application;
      this.workerName = workerName;
      this.rootMountLine = rootMountLine;
      this.asteriskMountLine = asteriskMountLine;
    }

    @Override
    public boolean isCommented() {
      return commented;
    }
    
    @Override
    public String application() {
      return application;
    }

    @Override
    public TextLineReference rootMountLine() {
      return rootMountLine;
    }

    @Override
    public TextLineReference asteriskMountLine() {
      // TODO Auto-generated method stub
      return asteriskMountLine;
    }

    @Override
    public String workerName() {
      return workerName;
    }
    
    
  }
  
  private static final Pattern JK_MOUNT_PATTERN = Pattern.compile("\\s*(#*)\\s*JkMount\\s+([^\\s]+)\\s+([^\\s]+)\\s*");
  
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
          
            final JkMountDirective mountd = new JkMountDirective(commented, workerName, line);
            
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
          
          JkMount mount = new JkMountImpl(
              rootMountd.commented || asterMountd.commented,
              application,
              rootMountd.workerName,
              rootMountd.line,
              asterMountd.line);
          
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
    
    public final boolean commented;

    /**
     * The worker name (value of the mount)
     */
    public final String workerName;
    
    /**
     * The line with mount itself
     */
    public final TextLineReference line;
    
    public JkMountDirective(boolean commented, String workerName, TextLineReference line) {
      this.commented = commented;
      this.workerName = workerName;
      this.line = line;
    }
  }
  
}

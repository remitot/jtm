package org.jepria.httpd.apache.manager.core.jk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
      return rootMountDirective.getLine().lineNumber() + "-" + asterMountDirective.getLine().lineNumber();
    }
    
    @Override
    public boolean isActive() {
      return !rootMountDirective.isCommented() && !asterMountDirective.isCommented();
    }
    
    @Override
    public void setActive(boolean active) {
      rootMountDirective.setCommented(!active);
      asterMountDirective.setCommented(!active);
    }
    
    @Override
    public String getApplication() {
      // TODO better place to validate all three directive's application equality? what if not equal?
      if (!rootMountDirective.getApplication().equals(asterMountDirective.getApplication())) {
        throw new IllegalStateException("Expected all the same applications, but actual: [" 
            + rootMountDirective.getApplication() + "][" + asterMountDirective.getApplication() + "]");
      }
      return rootMountDirective.getApplication();
    }
    
    @Override
    public void setApplication(String application) {
      rootMountDirective.setApplication(application);
      asterMountDirective.setApplication(application);
    }

    @Override
    public String workerName() {
      // TODO better place to validate all three directive's workerName equality? what if not equal?
      if (!rootMountDirective.getWorkerName().equals(asterMountDirective.getWorkerName())) {
        throw new IllegalStateException("Expected all the same workerNames, but actual: [" 
            + rootMountDirective.getWorkerName() + "][" + asterMountDirective.getWorkerName() + "]");
      }
      return rootMountDirective.getWorkerName();
    }
    
    @Override
    public void setWorkerName(String workerName) {
      rootMountDirective.setWorkerName(workerName);
      asterMountDirective.setWorkerName(workerName);
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
        
        tryParseJkMountDirective(line, 
            m -> rootMounts.put(m.getApplication(), m),
            m -> asterMounts.put(m.getApplication(), m));
      }
      
      // assemble JkMounts from JkMountDirectives with both root and asterisk mounted to the same application and worker
      Set<String> applications = new HashSet<>(rootMounts.keySet());
      applications.retainAll(asterMounts.keySet());
      
      for (String application: applications) {
        JkMountDirective rootMountd = rootMounts.get(application);
        JkMountDirective asterMountd = asterMounts.get(application);
        
        if (rootMountd.getWorkerName().equals(asterMountd.getWorkerName())) {
          
          JkMount mount = new JkMountImpl(rootMountd, asterMountd);
          
          ret.add(mount);
        }
      }
    }
    
    return ret;
  }
  
  /**
   * Interface representing a single JkMount directive
   */
  private static interface JkMountDirective {
    
    boolean isCommented();
    void setCommented(boolean commented);

    /**
     * The worker name (value of the mount)
     */
    String getWorkerName();
    void setWorkerName(String workerName);
    
    String getApplication();
    void setApplication(String application);
    
    /**
     * Service method.
     */
    TextLineReference getLine();
  }
  
  private static class JkMountDirectiveImpl implements JkMountDirective {
    private boolean commented;
    private String application;
    private boolean asterMount;
    private String workerName;
    private final TextLineReference line;
    
    public JkMountDirectiveImpl(boolean commented, String application, boolean asterMount, String workerName, TextLineReference line) {
      this.commented = commented;
      this.application = application;
      this.asterMount = asterMount;
      this.workerName = workerName;
      this.line = line;
    }

    @Override
    public boolean isCommented() {
      return commented;
    }

    @Override
    public void setCommented(boolean commented) {
      this.commented = commented;
      rebuild();
    }

    @Override
    public String getWorkerName() {
      return workerName;
    }

    @Override
    public void setWorkerName(String workerName) {
      this.workerName = workerName;
      rebuild();
    }

    @Override
    public String getApplication() {
      return application;
    }

    @Override
    public void setApplication(String application) {
      this.application = application;
      rebuild();
    }
    
    private void rebuild() {
      StringBuilder content = new StringBuilder();
      if (commented) {
        content.append("# ");
      }
      content.append("JkMount /").append(application);
      if (asterMount) {
        content.append("/*");
      }
      content.append(' ').append(workerName);
      
      line.setContent(content);
    }
    
    @Override
    public TextLineReference getLine() {
      return line;
    }
  }
  
  /**
   * Does nothing if failed to parse the line into a JkMountDirective, 
   * otherwise tells one of the consumers to consume the parsed object.
   * @param line
   * @param rootMountConsumer
   * @param asterMountConsumer
   */
  private static void tryParseJkMountDirective(TextLineReference line, 
      Consumer<JkMountDirective> rootMountConsumer, Consumer<JkMountDirective> asterMountConsumer) {
    
    Matcher m = JK_MOUNT_PATTERN.matcher(line);
    if (m.matches()) {
      final boolean commented = m.group(1).length() > 0;
      final String urlPattern = m.group(2);
      final String workerName = m.group(3);
      
      final boolean asterMount;
      
      if (urlPattern.startsWith("/")) {
        final String application;
        if (urlPattern.endsWith("/*")) {
          application = urlPattern.substring(1, urlPattern.length() - 2);
          asterMount = true;
        } else {
          application = urlPattern.substring(1);
          asterMount = false;
        }
      
        final JkMountDirective mountd = new JkMountDirectiveImpl(commented, application, asterMount, workerName, line);
        
        if (asterMount) {
          asterMountConsumer.accept(mountd);
        } else {
          rootMountConsumer.accept(mountd);
        }
      }
    }
  }
}

package org.jepria.httpd.apache.manager.core.jk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JkMountFactory {

  private static class JkMountImpl implements JkMount {
    private final JkMountDirective rootMountDirective;
    private final JkMountDirective asterMountDirective;
    
    public JkMountImpl(JkMountDirective rootMountDirective, JkMountDirective asterMountDirective) {
      this.rootMountDirective = rootMountDirective;
      this.asterMountDirective = asterMountDirective;
    }

    /**
     * @return id for this JkMount. The id <i>may</i> be further used in URL, so it must be URL-safe 
     */
    public String getId() {
      int r = rootMountDirective.getLine().lineNumber();
      int a = asterMountDirective.getLine().lineNumber();
      return "$R" + r + "+A" + a; // means "located at Root-mount-at-line-r + Asterisk-mount-at-line-a"
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
    
    @Override
    public void delete() {
      rootMountDirective.delete();
      asterMountDirective.delete();
    }
    
  }
  
  public static final Pattern JK_MOUNT_PATTERN = Pattern.compile("\\s*(#*)\\s*JkMount\\s+([^\\s]+)\\s+([^\\s]+)\\s*");
  
  /**
   * 
   * @param lines of the {@code mod_jk.conf} file
   * @return or else empty collection. Every {@link JkMount} element in the collection has unique {@link JkMount#getApplication()} field value
   */
  public static Map<String, JkMount> parse(Iterable<TextLineReference> lines) {
    // TODO maintain order of insertion same as the JkMount directives are 
    // declared in conf files using LinkedHashMap?
    Map<String, JkMount> ret = new HashMap<>();
    
    if (lines != null) {
      // collect root mounts ('/Application'), with application names as keys
      Map<String, JkMountDirective> rootMounts = new HashMap<>();
      // collect asterisk mounts ('/Application/*'), with application names as keys 
      Map<String, JkMountDirective> asterMounts = new HashMap<>();

      for (TextLineReference line: lines) {
        // Apache actually uses the last declared directive (among the same application names)
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
          
          JkMountImpl mount = new JkMountImpl(rootMountd, asterMountd);
          String mountId = mount.getId();
          
          ret.put(mountId, mount);
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
    
    void delete();
    
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
    
    public JkMountDirectiveImpl(boolean commented, String application, boolean asterMount, String workerName, TextLineReference line, boolean rebuild) {
      this.commented = commented;
      this.application = application;
      this.asterMount = asterMount;
      this.workerName = workerName;
      this.line = line;
      
      if (rebuild) {
        rebuild();
      }
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
      content.append("JkMount ");
      if (application != null) {
        content.append('/').append(application);
        if (asterMount) {
          content.append("/*");
        }
      }
      content.append(' ');
      if (workerName != null) {
        content.append(workerName);
      }
      
      line.setContent(content);
    }
    
    @Override
    public TextLineReference getLine() {
      return line;
    }
    
    @Override
    public void delete() {
      line.delete();
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
      
        final JkMountDirective mountd = new JkMountDirectiveImpl(commented, application, asterMount, workerName, line, false);
        
        if (asterMount) {
          asterMountConsumer.accept(mountd);
        } else {
          rootMountConsumer.accept(mountd);
        }
      }
    }
  }
  
  /**
   * Creates a new (empty) non-commented JkMount with the name specified
   * @param name
   * @param rootMountLine will be reset
   * @param asterMountLine will be reset
   * @return
   */
  public static JkMount create(TextLineReference rootMountLine,
      TextLineReference asterMountLine) {
    
    JkMountDirective rootMountDirective = new JkMountDirectiveImpl(false, null, false, null, rootMountLine, true);
    JkMountDirective asterMountDirective = new JkMountDirectiveImpl(false, null, true, null, asterMountLine, true);
    
    return new JkMountImpl(rootMountDirective, asterMountDirective);
  }
}

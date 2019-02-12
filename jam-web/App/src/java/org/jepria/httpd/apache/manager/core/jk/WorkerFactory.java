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

public class WorkerFactory {
  
  private static class WorkerImpl implements Worker {
    
    private final WorkerProperty typeWorkerProperty;
    private final WorkerProperty hostWorkerProperty;
    private final WorkerProperty portWorkerProperty;
    
    public WorkerImpl(WorkerProperty typeWorkerProperty, WorkerProperty hostWorkerProperty,
        WorkerProperty portWorkerProperty) {
      this.typeWorkerProperty = typeWorkerProperty;
      this.hostWorkerProperty = hostWorkerProperty;
      this.portWorkerProperty = portWorkerProperty;
    }

    @Override
    public String getLocation() {
      return typeWorkerProperty.getLine().lineNumber() + "-" 
          + hostWorkerProperty.getLine().lineNumber() + "-" 
          + portWorkerProperty.getLine().lineNumber();
    }
    
    @Override
    public boolean isActive() {
      return !typeWorkerProperty.isCommented() && !hostWorkerProperty.isCommented() && !portWorkerProperty.isCommented();
    }
    
    @Override
    public void setActive(boolean active) {
      typeWorkerProperty.setCommented(!active);
      hostWorkerProperty.setCommented(!active);
      portWorkerProperty.setCommented(!active);
    }

    @Override
    public String getName() {
      // TODO better place to validate all three properties's name equality? what if not equal?
      if (!typeWorkerProperty.getWorkerName().equals(hostWorkerProperty.getWorkerName()) 
          || !typeWorkerProperty.getWorkerName().equals(portWorkerProperty.getWorkerName())) {
        throw new IllegalStateException("Expected all the same worker names, but actual: [" 
            + typeWorkerProperty.getWorkerName() + "][" + hostWorkerProperty.getWorkerName() + "][" + portWorkerProperty.getWorkerName() + "]");
      }
      return typeWorkerProperty.getWorkerName();
    }
    
    @Override
    public void setName(String workerName) {
      typeWorkerProperty.setWorkerName(workerName); 
      hostWorkerProperty.setWorkerName(workerName); 
      portWorkerProperty.setWorkerName(workerName);
    }

    @Override
    public String getType() {
      return typeWorkerProperty.getValue();
    }

    @Override
    public String getHost() {
      return hostWorkerProperty.getValue();
    }
    
    @Override
    public void setHost(String host) {
      hostWorkerProperty.setValue(host);
    }

    @Override
    public String getPort() {
      return portWorkerProperty.getValue();
    }
    
    @Override
    public void setPort(String port) {
      portWorkerProperty.setValue(port);
    }
  }
  
  
  public static final Pattern PROPERTY_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.([^=\\s]+)\\s*\\=\\s*([^\\s]+)\\s*");
  
  public static List<Worker> parse(Iterator<TextLineReference> lineIterator) {
    List<Worker> ret = new ArrayList<>();
    
    if (lineIterator != null) {
      // collect worker.name.type properties, with worker names as keys
      Map<String, WorkerProperty> typeProperties = new HashMap<>();
      // collect worker.name.host properties, with worker names as keys
      Map<String, WorkerProperty> hostProperties = new HashMap<>();
      // collect worker.name.port properties, with worker names as keys
      Map<String, WorkerProperty> portProperties = new HashMap<>();
      
      while (lineIterator.hasNext()) {
        final TextLineReference line = lineIterator.next();
        
        tryParseWorkerProperty(line, 
            m -> typeProperties.put(m.getWorkerName(), m),
            m -> hostProperties.put(m.getWorkerName(), m),
            m -> portProperties.put(m.getWorkerName(), m));
      }
      
      
      // assemble Workers from WorkerProperties with all three properties with the same worker name
      Set<String> names = new HashSet<>(typeProperties.keySet());
      names.retainAll(hostProperties.keySet());
      names.retainAll(portProperties.keySet());
      
      for (String name: names) {
        WorkerProperty typeWorkerp = typeProperties.get(name);
        WorkerProperty hostWorkerp = hostProperties.get(name);
        WorkerProperty portWorkerp = portProperties.get(name);
        
        Worker worker = new WorkerImpl(typeWorkerp, hostWorkerp, portWorkerp);
        
        ret.add(worker);
      }
    }
    
    return ret;
  }
  
  /**
   * Interface representing a single worker property
   */
  private static interface WorkerProperty {
    boolean isCommented();
    void setCommented(boolean commented);

    String getWorkerName();
    void setWorkerName(String workerName);
    
    /**
     * The property value
     */
    String getValue();
    void setValue(String value);
    
    /**
     * Service method.
     */
    TextLineReference getLine();
  }
  
  private static class WorkerPropertyImpl implements WorkerProperty {
    private boolean commented;
    private String workerName;
    private String workerType;
    private String value;
    private final TextLineReference line;
    
    public WorkerPropertyImpl(boolean commented, String workerName, String workerType, String value, TextLineReference line) {
      this.commented = commented;
      this.workerName = workerName;
      this.workerType = workerType;
      this.value = value;
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
    public String getValue() {
      return value;
    }

    @Override
    public void setValue(String value) {
      this.value = value;
      rebuild();
    }
    
    private void rebuild() {
      StringBuilder content = new StringBuilder();
      if (commented) {
        content.append("# ");
      }
      content.append("worker.").append(workerName).append('.').append(workerType).append('=').append(value);
      
      line.setContent(content);
    }
    
    @Override
    public TextLineReference getLine() {
      return line;
    }
  }
  
  /**
   * Does nothing if failed to parse the line into a WorkerProperty, 
   * otherwise tells one of the consumers to consume the parsed object.
   * @param line
   * @param rootMountConsumer
   * @param asterMountConsumer
   */
  private static void tryParseWorkerProperty(TextLineReference line, 
      Consumer<WorkerProperty> typePropertyConsumer,
      Consumer<WorkerProperty> hostPropertyConsumer,
      Consumer<WorkerProperty> portPropertyConsumer) {
    
    Matcher m = PROPERTY_PATTERN.matcher(line);
    if (m.matches()) {
      final boolean commented = m.group(1).length() > 0;
      final String workerName = m.group(2);
      final String workerType = m.group(3);
      final String value = m.group(4);
      
      final WorkerProperty workerProperty;
      final Consumer<WorkerProperty> targetConsumer;
      
      switch (workerType) {
      case "type": {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line);
        targetConsumer = typePropertyConsumer;
        break;
      }
      case "host": {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line);
        targetConsumer = hostPropertyConsumer;
        break;
      }
      case "port": {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line);
        targetConsumer = portPropertyConsumer;
        break;
      }
      default: {
        workerProperty = null;
        targetConsumer = null;
      }
      }
      
      if (workerProperty != null && targetConsumer != null) {
        targetConsumer.accept(workerProperty);
      }
    }
  }
}

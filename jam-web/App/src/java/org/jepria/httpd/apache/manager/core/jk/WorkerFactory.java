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
      return typeWorkerProperty.line.lineNumber() + "-" + hostWorkerProperty.line.lineNumber() + "-" + portWorkerProperty.line.lineNumber();
    }
    
    @Override
    public boolean isActive() {
      return !typeWorkerProperty.commented && !hostWorkerProperty.commented && !portWorkerProperty.commented;
    }
    
    @Override
    public void setActive(boolean active) {
      typeWorkerProperty.commented = 
          hostWorkerProperty.commented = 
          portWorkerProperty.commented = !active;
    }

    @Override
    public String getName() {
      // TODO better place to validate all three properties's name equality? what if not equal?
      if (!typeWorkerProperty.workerName.equals(hostWorkerProperty.workerName) || !typeWorkerProperty.workerName.equals(portWorkerProperty.workerName)) {
        throw new IllegalStateException("Expected all the same worker names, but actual: [" 
            + typeWorkerProperty.workerName + "][" + hostWorkerProperty.workerName + "][" + portWorkerProperty.workerName + "]");
      }
      return typeWorkerProperty.workerName;
    }
    
    @Override
    public void setName(String name) {
      typeWorkerProperty.workerName = 
          hostWorkerProperty.workerName = 
          portWorkerProperty.workerName = name;
    }

    @Override
    public String type() {
      return typeWorkerProperty.value;
    }

    @Override
    public String host() {
      return hostWorkerProperty.value;
    }
    
    @Override
    public void setHost(String host) {
      hostWorkerProperty.value = host;
    }

    @Override
    public String port() {
      return portWorkerProperty.value;
    }
    
    @Override
    public void setPort(String port) {
      portWorkerProperty.value = port;
    }
  }
  
  
  public static final Pattern TYPE_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.type\\=([^\\s]+)\\s*");
  public static final Pattern HOST_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.host\\=([^\\s]+)\\s*");
  public static final Pattern PORT_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.port\\=([^\\s]+)\\s*"); 
  
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
        
        final Map<String, WorkerProperty> targetMap;
        Matcher m;
        
        m = TYPE_PATTERN.matcher(line);
        if (m.matches()) {
          targetMap = typeProperties;
        } else {
          m = HOST_PATTERN.matcher(line);
          if (m.matches()) {
            targetMap = hostProperties;
          } else {
            m = PORT_PATTERN.matcher(line);
            if (m.matches()) {
              targetMap = portProperties;
            } else {
              m = null;
              targetMap = null;
            }
          }
        }
        
        if (targetMap != null && m != null && m.matches()) {
          final boolean commented = m.group(1).length() > 0;
          final String name = m.group(2);
          final String value = m.group(3);
          
          final WorkerProperty workerp = new WorkerProperty(commented, name, value, line);
          
          targetMap.put(name, workerp);
        }
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
   * Class representing a single worker property
   */
  private static class WorkerProperty {
    
    public boolean commented;

    public String workerName;
    
    /**
     * The property value
     */
    public String value;
    
    /**
     * The line with property itself
     */
    public TextLineReference line;
    
    public WorkerProperty(boolean commented, String workerName, String value, TextLineReference line) {
      this.commented = commented;
      this.workerName = workerName;
      this.value = value;
      this.line = line;
    }
  }
}

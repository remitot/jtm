package org.jepria.httpd.apache.manager.core.modjk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkerParser {
  
  /**
   * Class representing a worker with three worker properties: 
   * worker.name.type, worker.name.host and worker.name.port
   */
  public static class Worker {
    public final boolean commented;

    /**
     * The common worker name for all three properties
     */
    public final String name;
    
    public final String type;
    /**
     * The line with worker.name.type property itself
     */
    public final TextLineReference typeLine;
    
    public final String host;
    /**
     * The line with worker.name.host property itself
     */
    public final TextLineReference hostLine;
    
    public final String port;
    /**
     * The line with worker.name.port property itself
     */
    public final TextLineReference portLine;
    
    
    public Worker(boolean commented, String name, String type, TextLineReference typeLine, 
        String host, TextLineReference hostLine, String port, TextLineReference portLine) {
      this.commented = commented;
      this.name = name;
      this.type = type;
      this.typeLine = typeLine;
      this.host = host;
      this.hostLine = hostLine;
      this.port = port;
      this.portLine = portLine;
    }
  }
  
  
  private static final Pattern TYPE_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.type\\=([^\\s]+)\\s*");
  private static final Pattern HOST_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.host\\=([^\\s]+)\\s*");
  private static final Pattern PORT_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.port\\=([^\\s]+)\\s*"); 
  
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
          
          final WorkerProperty workerp = new WorkerProperty(commented, value, line);
          
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
        
        Worker worker = new Worker(
            typeWorkerp.commented || hostWorkerp.commented || portWorkerp.commented,
            name,
            typeWorkerp.value,
            typeWorkerp.line, 
            hostWorkerp.value,
            hostWorkerp.line,
            portWorkerp.value,
            portWorkerp.line);
        
        ret.add(worker);
      }
    }
    
    return ret;
  }
  
  /**
   * Class representing a single worker property
   */
  private static class WorkerProperty {
    
    public final boolean commented;

    /**
     * The property value
     */
    public final String value;
    
    /**
     * The line with property itself
     */
    public final TextLineReference line;
    
    public WorkerProperty(boolean commented, String value, TextLineReference line) {
      this.commented = commented;
      this.value = value;
      this.line = line;
    }
  }
}

package org.jepria.ahttpd.manager.core.modjk;

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
     * The number of line with worker.name.type property in the containing file
     */
    public final int typeLineNumber;
    /**
     * The line with worker.name.type property itself
     */
    public final StringBuilder typeLine;
    
    public final String host;
    /**
     * The number of line with worker.name.host property in the containing file
     */
    public final int hostLineNumber;
    /**
     * The line with worker.name.host property itself
     */
    public final StringBuilder hostLine;
    
    public final String port;
    /**
     * The number of line with worker.name.port property in the containing file
     */
    public final int portLineNumber;
    /**
     * The line with worker.name.port property itself
     */
    public final StringBuilder portLine;
    
    
    public Worker(boolean commented, String name, String type, int typeLineNumber, StringBuilder typeLine, String host,
        int hostLineNumber, StringBuilder hostLine, String port, int portLineNumber, StringBuilder portLine) {
      this.commented = commented;
      this.name = name;
      this.type = type;
      this.typeLineNumber = typeLineNumber;
      this.typeLine = typeLine;
      this.host = host;
      this.hostLineNumber = hostLineNumber;
      this.hostLine = hostLine;
      this.port = port;
      this.portLineNumber = portLineNumber;
      this.portLine = portLine;
    }
  }
  
  
  private static final Pattern TYPE_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.type\\=([^\\s]+)\\s*");
  private static final Pattern HOST_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.host\\=([^\\s]+)\\s*");
  private static final Pattern PORT_PATTERN = Pattern.compile("\\s*(#*)\\s*worker\\.([^\\.]+)\\.port\\=([^\\s]+)\\s*"); 
  
  public static List<Worker> parse(Iterator<StringBuilder> lineIterator) {
    List<Worker> ret = new ArrayList<>();
    
    if (lineIterator != null) {
      // collect worker.name.type properties, with worker names as keys
      Map<String, WorkerProperty> typeProperties = new HashMap<>();
      // collect worker.name.host properties, with worker names as keys
      Map<String, WorkerProperty> hostProperties = new HashMap<>();
      // collect worker.name.port properties, with worker names as keys
      Map<String, WorkerProperty> portProperties = new HashMap<>();
      
      int lineNumber = 0;
      while (lineIterator.hasNext()) {
        final StringBuilder line = lineIterator.next();
        lineNumber++;
        
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
          
          final WorkerProperty workerp = new WorkerProperty(commented, value, lineNumber, line);
          
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
            typeWorkerp.lineNumber,
            typeWorkerp.line, 
            hostWorkerp.value,
            hostWorkerp.lineNumber,
            hostWorkerp.line,
            portWorkerp.value,
            portWorkerp.lineNumber,
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
     * The number of line in the containing file
     */
    public final int lineNumber;
    /**
     * The line with property itself
     */
    public final StringBuilder line;
    
    public WorkerProperty(boolean commented, String value, int lineNumber, StringBuilder line) {
      this.commented = commented;
      this.value = value;
      this.lineNumber = lineNumber;
      this.line = line;
    }
  }
}

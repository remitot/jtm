package org.jepria.httpd.apache.manager.core.jk;

import java.util.AbstractList;
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
  
  public static final Pattern WORKER_LIST_PATTERN = Pattern.compile("\\s*worker.list\\s*=(.+)");
  
  /**
   * Finds {@code worker.list} property line and parses its value
   * @param lines of the {@code workers.properties} file
   * @return or else empty list
   */
  public static List<String> parseWorkerNames(Iterator<TextLineReference> lines) {
    if (lines != null) {
      while (lines.hasNext()) {
        final TextLineReference line = lines.next();
        
        Matcher m = WORKER_LIST_PATTERN.matcher(line);
        if (m.matches()) {
          List<String> list = new ArrayList<>();
          
          String workerList = m.group(1);
          String[] split = workerList.split("\\s*,\\s*");
          if (split != null) {
            for (String worker: split) {
              if (worker != null && !"".equals(worker)) {
                list.add(worker);
              }
            }
          }
          
          return new WorkerNameList(list, line, false);
        }
      }
    }
    return null;
  }
  
  /**
   * Controlled modifications
   */
  private static class WorkerNameList extends AbstractList<String> {
    private final List<String> list;
    private final TextLineReference line;
    
    public WorkerNameList(List<String> list, TextLineReference line, boolean rebuild) {
      this.list = list;
      this.line = line;
      
      if (rebuild) {
        rebuild();
      }
    }
    
    @Override
    public void add(int index, String element) {
      list.add(index, element);
      rebuild();
    }
    @Override
    public String remove(int index) {
      String ret = list.remove(index);
      rebuild();
      return ret;
    }
    @Override
    public String get(int index) {
      return list.get(index);
    }
    @Override
    public int size() {
      return list.size();
    }
    
    private void rebuild() {
      StringBuilder content = new StringBuilder();
      content.append("worker.list=");
      if (list != null) {
        boolean first = true;
        for (String worker: list) {
          if (!first) {
            content.append(',');
          } else {
            first = false;
          }
          content.append(worker);
        }
      }
      
      line.setContent(content);
    }
  }
  
  /**
   * 
   * @param lines of the {@code workers.properties} file
   * @return or else empty list
   */
  public static List<Worker> parse(Iterator<TextLineReference> lines) {
    List<Worker> ret = new ArrayList<>();
    
    if (lines != null) {
      // collect worker.name.type properties, with worker names as keys
      Map<String, WorkerProperty> typeProperties = new HashMap<>();
      // collect worker.name.host properties, with worker names as keys
      Map<String, WorkerProperty> hostProperties = new HashMap<>();
      // collect worker.name.port properties, with worker names as keys
      Map<String, WorkerProperty> portProperties = new HashMap<>();
      
      while (lines.hasNext()) {
        final TextLineReference line = lines.next();
        
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
    /**
     * final: must not be changed normally
     */
    private final String workerName;
    private String workerType;
    private String value;
    private final TextLineReference line;
    
    public WorkerPropertyImpl(boolean commented, String workerName, String workerType, String value, TextLineReference line, boolean rebuild) {
      this.commented = commented;
      this.workerName = workerName;
      this.workerType = workerType;
      this.value = value;
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
      content.append("worker.");
      if (workerName != null) {
        content.append(workerName);
      }
      content.append('.');
      if (workerType != null) {
        content.append(workerType);
      }
      content.append('=');
      if (value != null) {
        content.append(value);
      }
      
      line.setContent(content);
    }
    
    @Override
    public TextLineReference getLine() {
      return line;
    }
  }
  
  /**
   * {@code worker.name.type} value
   */
  public static final String AJP_13_TYPE = "ajp13";// TODO extract?
  
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
      
      if ("type".equals(workerType) && AJP_13_TYPE.equals(value)) {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line, false);
        targetConsumer = typePropertyConsumer;
      } else if ("host".equals(workerType)) {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line, false);
        targetConsumer = hostPropertyConsumer;
      } else if ("port".equals(workerType)) {
        workerProperty = new WorkerPropertyImpl(commented, workerName, workerType, value, line, false);
        targetConsumer = portPropertyConsumer;
      } else {
        workerProperty = null;
        targetConsumer = null;
      }
      
      if (workerProperty != null && targetConsumer != null) {
        targetConsumer.accept(workerProperty);
      }
    }
  }
  
  /**
   * Creates a new (empty) worker with the name specified
   * @param name
   * @param typeWorkerPropertyLine will be reset  
   * @param hostWorkerPropertyLine will be reset
   * @param portWorkerPropertyLine will be reset
   * @return
   */
  public static Worker create(String name,
      TextLineReference typeWorkerPropertyLine,
      TextLineReference hostWorkerPropertyLine,
      TextLineReference portWorkerPropertyLine) {
    
    WorkerProperty typeWorkerProperty = new WorkerPropertyImpl(false, name, "type", AJP_13_TYPE, typeWorkerPropertyLine, true);
    WorkerProperty hostWorkerProperty = new WorkerPropertyImpl(false, name, "host", null, hostWorkerPropertyLine, true);
    WorkerProperty portWorkerProperty = new WorkerPropertyImpl(false, name, "port", null, portWorkerPropertyLine, true); 
    
    return new WorkerImpl(typeWorkerProperty, hostWorkerProperty, portWorkerProperty);
  }
}

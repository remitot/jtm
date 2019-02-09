package org.jepria.httpd.apache.manager.core.jk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.jepria.httpd.apache.manager.core.TransactionException;

public class ApacheConfJk {
  
  protected final List<TextLineReference> mod_jk_confLines;
  
  protected final List<TextLineReference> workers_propertiesLines;
  
  ///////////////// Methods are analogous to TomcatConfJdbc ///////////////
  
  public ApacheConfJk(InputStream mod_jk_confInputStream,
      InputStream workers_propertiesInputStream) throws TransactionException {
    
    try (InputStream mod_jk_confInputStream0 = mod_jk_confInputStream;
        InputStream workers_propertiesInputStream0 = workers_propertiesInputStream) {
      
      mod_jk_confLines = new ArrayList<>();
      try (Scanner sc = new Scanner(mod_jk_confInputStream0)) {
        int lineNumber = 0;
        while (sc.hasNextLine()) {
          lineNumber++;
          mod_jk_confLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
        }
      }
      
      
      workers_propertiesLines = new ArrayList<>();
      try (Scanner sc = new Scanner(workers_propertiesInputStream0)) {
        int lineNumber = 0;
        while (sc.hasNextLine()) {
          lineNumber++;
          workers_propertiesLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
        }
      }
      
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
  
  private static class TextLineReferenceImpl implements TextLineReference {
    private final int lineNumber;
    private CharSequence content;
    
    public TextLineReferenceImpl(int lineNumber, String content) {
      this.lineNumber = lineNumber;
      this.content = content;
    }
    
    @Override
    public CharSequence getContent() {
      return content;
    }
    
    @Override
    public int lineNumber() {
      return lineNumber;
    }
    
    @Override
    public void setContent(CharSequence content) {
      this.content = content;
    }
    
  }
   
  /**
   * Lazily initialized map of bindings
   */
  private Map<String, Binding> bindings = null;
  
  /**
   * @return unmodifiable Map&lt;Location, Binding&gt;
   */
  public Map<String, Binding> getBindings() {
    if (bindings == null) {
      initBindings();
    }
    
    return bindings;
  }
  
  
  /**
   * Lazily initialized set of worker names
   */
  private Set<String> workerNames = null;
  
  /**
   * @return unmodifiable Set of worker names
   */
  public Set<String> getWorkerNames() {
    if (workerNames == null) {
      initWorkerNames();
    }
    
    return workerNames;
  }
  
  /**
   * Lazily initialized list of Workers
   */
  private List<Worker> workers = null;
  
  /**
   * @return unmodifiable List of Workers
   */
  protected List<Worker> getWorkers() {
    if (workers == null) {
      initWorkers();
    }
    
    return workers;
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #bindings} map
   */
  private void initBindings() {
    
    List<JkMount> jkMounts = JkMountParser.parse(mod_jk_confLines.iterator());

    Map<String, Binding> bindings0 = new HashMap<>();
    for (JkMount jkMount: jkMounts) {
      String workerName = jkMount.workerName();
      
      Worker worker = null;
      for (Worker worker0: getWorkers()) {
        if (worker0.name().equals(workerName)) {
          worker = worker0;
          break;
        }
      }
      
      if (worker != null) {
        String location = "mod_jk.conf-" + jkMount.rootMountLine().lineNumber() + "-" + jkMount.asteriskMountLine().lineNumber() 
            + "__workers.properties-" + worker.typePropertyLine().lineNumber() + "-" + worker.hostPropertyLine().lineNumber() + "-" + worker.portPropertyLine().lineNumber();
        
        Binding binding = new BindingImpl(
            !jkMount.isCommented() && !worker.isCommented(),
            jkMount.application(),
            worker.name(),
            worker.host() + ":" + worker.port());
        
        bindings0.put(location, binding);
      }
    }
    
    
    this.bindings = Collections.unmodifiableMap(bindings0);
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initWorkers() {
    this.workers = Collections.unmodifiableList(WorkerParser.parse(workers_propertiesLines.iterator()));
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #workerNames} set
   */
  private void initWorkerNames() {
    // TODO define the order of items same as the order of the Apache's real workers priority (if re-declared twice)
    this.workerNames = Collections.unmodifiableSet(getWorkers().stream().map(worker -> worker.name()).collect(Collectors.toSet()));
  }
  
  private static class BindingImpl implements Binding {
    private final boolean active;
    private final String application;
    private final String worker;
    private final String instance;
    
    public BindingImpl(boolean active, String application, String worker, String instance) {
      this.active = active;
      this.application = application;
      this.worker = worker;
      this.instance = instance;
    }

    @Override
    public boolean isActive() {
      return active;
    }

    @Override
    public void setActive(boolean active) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public String getApplication() {
      return application;
    }

    @Override
    public void setApplication(String application) {
      // TODO Auto-generated method stub
      
    }
    
    @Override
    public void setWorker(String worker) {
      // TODO Auto-generated method stub
      
    }
    
    @Override
    public String getWorker() {
      return worker;
    }

    @Override
    public String getInstance() {
      return instance;
    }

    @Override
    public void setInstance(String instance) {
      // TODO Auto-generated method stub
    }
    
  }
  
  
  public void delete(String location) {
    // TODO
  }
  
  public Binding create() {
    return null;
 // TODO
  }
  
  public void save(OutputStream mod_jk_confOutputStream,
      OutputStream workers_propertiesOutputStream) {
 // TODO
  }
}

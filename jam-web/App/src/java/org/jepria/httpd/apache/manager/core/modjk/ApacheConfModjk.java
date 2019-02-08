package org.jepria.httpd.apache.manager.core.modjk;

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

public class ApacheConfModjk {
  
  protected final List<TextLineReference> modjkConfLines;
  
  protected final List<TextLineReference> workerPropertiesLines;
  
  ///////////////// Methods are analogous to TomcatConfJdbc ///////////////
  
  public ApacheConfModjk(InputStream modjkConfInputStream,
      InputStream workerPropertiesInputStream) throws TransactionException {
    
    try (InputStream modjkConfInputStream0 = modjkConfInputStream;
        InputStream workerPropertiesInputStream0 = workerPropertiesInputStream) {
      
      modjkConfLines = new ArrayList<>();
      try (Scanner sc = new Scanner(modjkConfInputStream0)) {
        int lineNumber = 0;
        while (sc.hasNextLine()) {
          lineNumber++;
          modjkConfLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
        }
      }
      
      
      workerPropertiesLines = new ArrayList<>();
      try (Scanner sc = new Scanner(workerPropertiesInputStream0)) {
        int lineNumber = 0;
        while (sc.hasNextLine()) {
          lineNumber++;
          workerPropertiesLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
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
    
    List<JkMount> jkMounts = JkMountParser.parse(modjkConfLines.iterator());

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
    this.workers = Collections.unmodifiableList(WorkerParser.parse(workerPropertiesLines.iterator()));
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
  
  public void save(OutputStream modjkConfOutputStream,
      OutputStream workerPropertiesOutputStream) {
 // TODO
  }
}

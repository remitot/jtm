package org.jepria.httpd.apache.manager.core.jk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jepria.httpd.apache.manager.core.ApacheConfBase;

public class ApacheConfJk extends ApacheConfBase {
  
  public ApacheConfJk(Supplier<InputStream> mod_jk_confInput,
      Supplier<InputStream> workers_propertiesInput) {
    super(mod_jk_confInput, workers_propertiesInput);
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
    
    List<JkMount> jkMounts = JkMountFactory.parse(getMod_jk_confLines().iterator());

    Map<String, Binding> bindings0 = new HashMap<>();
    for (JkMount jkMount: jkMounts) {
      String workerName = jkMount.workerName();
      
      Worker worker = null;
      for (Worker worker0: getWorkers()) {
        if (worker0.getName().equals(workerName)) {
          worker = worker0;
          break;
        }
      }
      
      if (worker != null) {
        String location = "mod_jk.conf-" + jkMount.getLocation() + "__workers.properties-" + worker.getLocation();
        
        Binding binding = new BindingImpl(jkMount, worker, this);
        
        bindings0.put(location, binding);
      }
    }
    
    
    this.bindings = Collections.unmodifiableMap(bindings0);
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initWorkers() {
    this.workers = Collections.unmodifiableList(WorkerFactory.parse(
        getWorkers_propertiesLines().iterator()));
  }
  
  public void delete(String location) {
    // TODO
  }
  
  public Binding create() {
    return null;
 // TODO
  }
  
  protected Worker createWorker(String name) {
    
    int lineNumber = getWorkers_propertiesLines().size() + 1;
    
    getWorkers_propertiesLines().add(new TextLineReferenceImpl(lineNumber++, ""));// empty line
    TextLineReference typeWorkerPropertyLine = new TextLineReferenceImpl(lineNumber++, "");
    getWorkers_propertiesLines().add(typeWorkerPropertyLine);
    TextLineReference hostWorkerPropertyLine = new TextLineReferenceImpl(lineNumber++, "");
    getWorkers_propertiesLines().add(hostWorkerPropertyLine);
    TextLineReference portWorkerPropertyLine = new TextLineReferenceImpl(lineNumber++, "");
    getWorkers_propertiesLines().add(portWorkerPropertyLine);
    
    // add the new worker name into the worker.list
    List<String> workerNames = WorkerFactory.parseWorkerNames(getWorkers_propertiesLines().iterator());
    if (!workerNames.contains(name)) {
      workerNames.add(name);
    }
    
    return WorkerFactory.create(name, typeWorkerPropertyLine, hostWorkerPropertyLine, portWorkerPropertyLine);
  }
  
  public void save(OutputStream mod_jk_confOutputStream,
      OutputStream workers_propertiesOutputStream) {
    saveMod_jk_conf(mod_jk_confOutputStream);
    saveWorkers_properties(workers_propertiesOutputStream);
  }
}

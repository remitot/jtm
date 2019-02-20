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
   * Lazily initialized map of BaseBindings
   */
  private Map<String, BaseBinding> baseBindings = null;
  
  /**
   * @return unmodifiable Map&lt;BindingId, BaseBinding&gt;
   */
  protected Map<String, BaseBinding> getBaseBindings() {
    if (baseBindings == null) {
      initBaseBindings();
    }
    
    return baseBindings;
  }
  
  /**
   * @return unmodifiable Map&lt;BindingId, Binding&gt;
   */
  @SuppressWarnings("unchecked")
  public Map<String, Binding> getBindings() {
    return (Map<String, Binding>)(Map<String, ?>)getBaseBindings();
  }
  
  
  /**
   * Lazily initialized list of Workers
   */
  private List<Worker> workers = null;
  
  /**
   * @return unmodifiable List of Workers
   */
  public List<Worker> getWorkers() {
    if (workers == null) {
      initWorkers();
    }
    
    return Collections.unmodifiableList(workers);
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #baseBindings} map
   */
  private void initBaseBindings() {
    
    List<JkMount> jkMounts = JkMountFactory.parse(getMod_jk_confLines().iterator());

    Map<String, BaseBinding> bindings = new HashMap<>();
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
        String id = jkMount.getId() + "+" + worker.getId();
        
        BaseBinding binding = new BindingImpl(jkMount, worker, this);
        
        bindings.put(id, binding);
      }
    }
    
    
    this.baseBindings = Collections.unmodifiableMap(bindings);
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initWorkers() {
    this.workers = WorkerFactory.parse(getWorkers_propertiesLines().iterator());
  }
  
  /**
   * Delete binding by id
   * @param id
   */
  public void delete(String id) {
    BaseBinding binding = getBaseBindings().get(id);
    
    if (binding == null) {
      throw new IllegalArgumentException("No binding found by id [" + id + "]");
    }
    
    binding.delete();
  }
  
  /**
   * Create new (empty) Binding
   * @return
   */
  public Binding create() {
    
    TextLineReference.addNewLine(getMod_jk_confLines());// empty line
    TextLineReference rootMountLine = TextLineReference.addNewLine(getMod_jk_confLines());
    TextLineReference asterMountLine = TextLineReference.addNewLine(getMod_jk_confLines());
    
    JkMount jkMount = JkMountFactory.create(rootMountLine, asterMountLine);
    
    return new BindingImpl(jkMount, null, this);// TODO it is bad to pass null as Worker because the 'Binding' entity literally MUST include Worker (otherwise 'binding' to what?)
  }
  
  /**
   * Creates a new active worker.
   */
  protected Worker createWorker(String name) {
    
    TextLineReference.addNewLine(getWorkers_propertiesLines());// empty line
    TextLineReference typeWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());
    TextLineReference hostWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());
    TextLineReference portWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());
    
    // add the new worker name into the worker.list
    List<String> workerNames = WorkerFactory.parseWorkerNames(getWorkers_propertiesLines().iterator());
    if (!workerNames.contains(name)) {
      workerNames.add(name);
    }
    
    Worker worker = WorkerFactory.create(name, typeWorkerPropertyLine, hostWorkerPropertyLine, portWorkerPropertyLine);

    
    // add the new worker to the list
    getWorkers(); // initialize if necessary
    
    // TODO unchecked add! 
    // Future getWorkers() external access may rely on the new worker presence,
    // but the new worker is NOT saved to the target file yet (added to the list only instead),
    // and if the save fails, this may lead to inconsistency 
    // (because the new 'present' worker will be actually absent)
    this.workers.add(worker); 
    
    
    return worker;
  }
  
  public void save(OutputStream mod_jk_confOutputStream,
      OutputStream workers_propertiesOutputStream) {
    saveMod_jk_conf(mod_jk_confOutputStream);
    saveWorkers_properties(workers_propertiesOutputStream);
  }
  
  /**
   * Validates new 'application' field of the binding that is about to be created (before the creation) or updated.
   * @param application application of the binding that is about to be created or updated
   * @return {@code true} if the new name is OK; 
   * {@code false} if there is a binding with the same application
   */
  public boolean validateNewBindingApplication(String application) {
    return !getBindings().values().stream().anyMatch(
        binding -> application.equals(binding.getApplication()));
  }
}

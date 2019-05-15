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

  

  /////////////////////////////////////////////////////////////
  
  
  
  /**
   * Lazily initialized collection of Workers
   */
  private Map<String, Worker> workers = null;

  /**
   * @return unmodifiable Map&lt;WorkerId, Worker&gt;. 
   * WorkerIds are synthetic (the values actually reflect 
   * location of nodes in configuration files)
   */
  public Map<String, Worker> getWorkers() {
    return Collections.unmodifiableMap(getBaseWorkers());
  }

  protected Map<String, Worker> getBaseWorkers() {
    if (workers == null) {
      initWorkers();
    }

    return workers;
  }

  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initWorkers() {
    this.workers = WorkerFactory.parse(getWorkers_propertiesLines());
  }
  
  
  
  /////////////////////////////////////////////////////////////

  
  
  /**
   * Lazily initialized collection of JkMounts
   */
  private Map<String, JkMount> mounts = null;

  /**
   * @return unmodifiable Map&lt;JkMountId, JkMount&gt;. 
   * JkMountIds are synthetic (the values actually reflect 
   * location of nodes in configuration files)
   */
  public Map<String, JkMount> getMounts() {
    return Collections.unmodifiableMap(getBaseMounts());
  }

  protected Map<String, JkMount> getBaseMounts() {
    if (mounts == null) {
      initBaseMounts();
    }

    return mounts;
  }

  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initBaseMounts() {
    this.mounts = JkMountFactory.parse(getMod_jk_confLines());
  }
  
  
  
  /////////////////////////////////////////////////////////////
  
  
  
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
   * @return unmodifiable Map&lt;BindingId, Binding&gt;.
   * BindingIds are synthetic (the values actually reflect 
   * location of nodes in configuration files)
   */
  public Map<String, Binding> getBindings() {
    return Collections.unmodifiableMap(getBaseBindings());
  }

  /**
   * Lazily initialize (or re-initialize) {@link #baseBindings} map
   */
  private void initBaseBindings() {
    Map<String, BaseBinding> bindings = new HashMap<>();
    
    for (Map.Entry<String, JkMount> mountEntry: getMounts().entrySet()) {
      String mountId = mountEntry.getKey();
      JkMount jkMount = mountEntry.getValue();
      
      String workerName = jkMount.workerName();

      String workerId = null;
      Worker worker = null;
      for (Map.Entry<String, Worker> workerEntry: getWorkers().entrySet()) {
        String workerId0 = workerEntry.getKey();
        Worker worker0 = workerEntry.getValue();
        
        if (worker0.getName().equals(workerName)) {
          workerId = workerId0;
          worker = worker0;
          break;
        }
      }

      if (worker != null) {
        String bindingId = mountId + "+" + workerId;

        BaseBinding binding = new BindingImpl(jkMount, worker, this);

        bindings.put(bindingId, binding);
      }
    }


    this.baseBindings = bindings;
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
    List<String> workerNames = WorkerFactory.parseWorkerNames(getWorkers_propertiesLines());
    if (!workerNames.contains(name)) {
      workerNames.add(name);
    }

    Worker worker = WorkerFactory.create(name, typeWorkerPropertyLine, hostWorkerPropertyLine, portWorkerPropertyLine);


    // add the new worker to the list

    // TODO unchecked add! 
    // Future getWorkers() external access may rely on the new worker presence,
    // but the new worker is NOT saved to the target file yet (added to the list only instead),
    // and if the save fails, this may lead to inconsistency 
    // (because the new 'present' worker will be actually absent)
//    getBaseWorkers().put(worker); 

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
  public boolean validateNewApplication(String application) {
    return !getBindings().values().stream().anyMatch(
        binding -> application.equals(binding.getApplication()));
  }

  public boolean validateNewWorkerName(String workerName) {
    return !getWorkers().values().stream().anyMatch(worker -> workerName.equals(worker.getName()));
  }
}


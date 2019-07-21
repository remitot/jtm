package org.jepria.httpd.apache.manager.core.jk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
      initMounts();
    }

    return mounts;
  }

  /**
   * Lazily initialize (or re-initialize) {@link #workers} list
   */
  private void initMounts() {
    this.mounts = JkMountFactory.parse(getMod_jk_confLines());
  }
  
  
  
  /////////////////////////////////////////////////////////////
  
  /**
   * Delete binding by mountId
   * @param id
   */
  public void delete(String mountId) {
    JkMount mount = getMounts().get(mountId);

    if (mount == null) {
      throw new IllegalArgumentException("No mount found by id [" + mountId + "]");
    }

    mount.delete();
  }

  /**
   * Create new active mount.
   * The subsequent {@link #getMounts()} invocations will return the original mount collection,
   * not containing the newly created worker.
   * @return
   */
  public JkMount createMount() {
    
    TextLineReference.addNewLine(getMod_jk_confLines());// empty line
    TextLineReference rootMountLine = TextLineReference.addNewLine(getMod_jk_confLines());
    TextLineReference asterMountLine = TextLineReference.addNewLine(getMod_jk_confLines());

    JkMount jkMount = JkMountFactory.create(rootMountLine, asterMountLine);

    return jkMount;
  }

  /**
   * Creates a new active worker.
   * The subsequent {@link #getWorkers()} invocations will return the original worker collection,
   * not containing the newly created worker.
   * @param name not null
   * @param type
   * @throws IllegalArgumentException if the worker with the same name already exists
   */
  public Worker createWorker(String name, String type) {

    Objects.requireNonNull(name);
    
    // validate new worker name
    if (getWorkers().values().stream().anyMatch(worker -> name.equals(worker.getName()))) {
      throw new IllegalArgumentException("The worker with name '" + name + "' already exists");
    }
    
    TextLineReference.addNewLine(getWorkers_propertiesLines());// empty line
    TextLineReference typeWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());
    TextLineReference hostWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());
    TextLineReference portWorkerPropertyLine = TextLineReference.addNewLine(getWorkers_propertiesLines());

    // add the new worker name into the worker.list
    List<String> workerNames = WorkerFactory.parseWorkerNames(getWorkers_propertiesLines());
    if (!workerNames.contains(name)) {
      workerNames.add(name);
    }

    Worker worker = WorkerFactory.create(name, type, typeWorkerPropertyLine, hostWorkerPropertyLine, portWorkerPropertyLine);

    return worker;
  }

  public void save(Supplier<OutputStream> mod_jk_confOutputStream,
      Supplier<OutputStream> workers_propertiesOutputStream) {
    saveMod_jk_conf(mod_jk_confOutputStream);
    saveWorkers_properties(workers_propertiesOutputStream);
  }
}


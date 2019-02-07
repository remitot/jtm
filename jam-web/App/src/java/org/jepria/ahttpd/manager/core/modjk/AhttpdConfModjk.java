package org.jepria.ahttpd.manager.core.modjk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jepria.ahttpd.manager.core.TransactionException;

public class AhttpdConfModjk {
  
  protected final List<StringBuilder> modjkConfLines;
  
  protected final List<StringBuilder> workerPropertiesLines;
  
  ///////////////// Methods are analogous to TomcatConfJdbc ///////////////
  
  public AhttpdConfModjk(InputStream modjkConfInputStream,
      InputStream workerPropertiesInputStream) throws TransactionException {
    
    try (InputStream modjkConfInputStream0 = modjkConfInputStream;
        InputStream workerPropertiesInputStream0 = workerPropertiesInputStream) {
      
      modjkConfLines = new ArrayList<>();
      try (Scanner sc = new Scanner(modjkConfInputStream0)) {
        while (sc.hasNextLine()) {
          modjkConfLines.add(new StringBuilder(sc.nextLine()));
        }
      }
      
      
      workerPropertiesLines = new ArrayList<>();
      try (Scanner sc = new Scanner(workerPropertiesInputStream0)) {
        while (sc.hasNextLine()) {
          workerPropertiesLines.add(new StringBuilder(sc.nextLine()));
        }
      }
      
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
  
  /**
   * Lazily initialized map of bindings
   */
  private Map<String, Binding> baseBindings = null;
  
  /**
   * @return unmodifiable Map&lt;Location, Connection&gt;
   */
  @SuppressWarnings("unchecked")
  public Map<String, Binding> getBindings() {
    return (Map<String, Binding>)(Map<String, ?>)getBaseBindings();
  }
  
  /**
   * @return unmodifiable Map&lt;Location, BaseConnection&gt;
   */
  protected Map<String, Binding> getBaseBindings() {
    if (baseBindings == null) {
      initBindings();
    }
    
    return baseBindings;
  }
  
  
  /**
   * Lazily initialize (or re-initialize) {@link #baseBindings} map
   */
  private void initBindings() {
    
    List<JkMountParser.JkMount> jkMounts = JkMountParser.parse(modjkConfLines.iterator());
    List<WorkerParser.Worker> workers = WorkerParser.parse(workerPropertiesLines.iterator());

    Map<String, Binding> bindings0 = new HashMap<>();
    for (JkMountParser.JkMount jkMount: jkMounts) {
      String workerName = jkMount.workerName;
      
      WorkerParser.Worker worker1 = null;
      for (WorkerParser.Worker worker0: workers) {
        if (worker0.name.equals(workerName)) {
          worker1 = worker0;
          break;
        }
      }
      
      final WorkerParser.Worker worker = worker1;
      
      if (worker != null) {
        String location = jkMount.rootLineNumber + "_" + jkMount.asterLineNumber 
            + "__" + worker.typeLineNumber + "_" + worker.hostLineNumber + "_" + worker.portLineNumber;
        
        Binding binding = new Binding() {

          @Override
          public boolean isActive() {
            return !jkMount.commented && !worker.commented;
          }

          @Override
          public void onActivate() {
            // TODO Auto-generated method stub
            
          }

          @Override
          public void onDeactivate() {
            // TODO Auto-generated method stub
            
          }

          @Override
          public String getAppname() {
            return jkMount.application;
          }

          @Override
          public void setAppname(String appname) {
            // TODO Auto-generated method stub
            
          }

          @Override
          public String getInstance() {
            return worker.host + ":" + worker.port;
          }

          @Override
          public void setInstance(String instance) {
            // TODO Auto-generated method stub
            
          }
          
        };
        
        bindings0.put(location, binding);
      }
    }
    
    
    this.baseBindings = Collections.unmodifiableMap(bindings0);
  }
  
  
  public void delete(String location) {
    
  }
  
  public Binding create() {
    return null;
  }
  
  public void save(OutputStream modjkConfOutputStream,
      OutputStream workerPropertiesOutputStream) {
    
  }
}

package org.jepria.httpd.apache.manager.core.modjk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    
    List<JkMount> jkMounts = JkMountParser.parse(modjkConfLines.iterator());
    List<Worker> workers = WorkerParser.parse(workerPropertiesLines.iterator());

    Map<String, Binding> bindings0 = new HashMap<>();
    for (JkMount jkMount: jkMounts) {
      String workerName = jkMount.workerName();
      
      Worker worker1 = null;
      for (Worker worker0: workers) {
        if (worker0.name().equals(workerName)) {
          worker1 = worker0;
          break;
        }
      }
      
      final Worker worker = worker1;
      
      if (worker != null) {
        String location = "mod_jk.conf-" + jkMount.rootMountLine().lineNumber() + "-" + jkMount.asteriskMountLine().lineNumber() 
            + "__workers.properties-" + worker.typePropertyLine().lineNumber() + "-" + worker.hostPropertyLine().lineNumber() + "-" + worker.portPropertyLine().lineNumber();
        
        Binding binding = new Binding() {

          @Override
          public boolean isActive() {
            return !jkMount.isCommented() && !worker.isCommented();
          }

          @Override
          public void setActive(boolean active) {
            if (!isActive() && active) {
              // on activate
              // TODO
            } else if (isActive() && !active) {
              // on deactivate
              // TODO
            }
          }

          @Override
          public String getAppname() {
            return jkMount.application();
          }

          @Override
          public void setAppname(String appname) {
            // TODO Auto-generated method stub
            
          }

          @Override
          public String getInstance() {
            return worker.host() + ":" + worker.port();
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

package org.jepria.httpd.apache.manager.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import org.jepria.httpd.apache.manager.core.jk.TextLineReference;
import org.jepria.httpd.apache.manager.core.jk.TextLineReference.Impl.OnDeleteHandler;

/**
 * Apache HTTPD configuration based on the following configuration files:
 * {@code jk/mod_jk.conf}, {@code jk/workers.properties}  
 */
public class ApacheConfBase {
  
  //TODO this value is assumed. But how to determine it? 
  private static final String FILE_READ_ENCODING = "UTF-8";
//TODO this value is assumed. But how to determine it? 
  private static final String FILE_WRITE_ENCODING = "UTF-8";
  
  private final Supplier<InputStream> mod_jk_confInput;
  private final Supplier<InputStream> workers_propertiesInput;
  
  /**
   * @param mod_jk_confInput supplier for a lazy initialization
   * @param workers_propertiesInput supplier for a lazy initialization
   */
  //TODO actually BOTH conf files are not necessarily needed (because we can need working 
  // with jk bindings only or with workers only). 
  // So DO NOT require both inputs in constructor. Ideally, having the no-arg constructor
  // and requiring inputs in such methods like getMod_jk_confLines() or getWorkers_propertiesLines()
  public ApacheConfBase(Supplier<InputStream> mod_jk_confInput,
      Supplier<InputStream> workers_propertiesInput) {
    this.mod_jk_confInput = mod_jk_confInput;
    this.workers_propertiesInput = workers_propertiesInput;
  }
  

  
  /**
   * Lazily initialized
   */
  private List<TextLineReference> mod_jk_confLines;
  
  /**
   * @return lazily parsed {@code mod_jk.conf} configuration file 
   */
  public List<TextLineReference> getMod_jk_confLines() {
    if (mod_jk_confLines == null) {
      initMod_jk_confLines();
    }
    return mod_jk_confLines;
  }
  
  /**
   * Parses the file into the list
   */
  private void initMod_jk_confLines() {
    try (Scanner sc = new Scanner(mod_jk_confInput.get(), FILE_READ_ENCODING)) {
      mod_jk_confLines = new ArrayList<>();
      
      final OnDeleteHandler onDeleteHandler = new OnDeleteHandler() {
        @Override
        public void onDelete(TextLineReference line) {
          mod_jk_confLines.remove(line);
        }
      };
      
      int lineNumber = 0;
      while (sc.hasNextLine()) {
        lineNumber++;
        mod_jk_confLines.add(new TextLineReference.Impl(lineNumber, sc.nextLine(), onDeleteHandler));
      }
    }//TODO catch filenotfound, file not readable
  }
  
  
  /**
   * Lazily initialized
   */
  private List<TextLineReference> workers_propertiesLines;
  
  /**
   * @return lazily parsed {@code workers.properties} configuration file 
   */
  public List<TextLineReference> getWorkers_propertiesLines() {
    if (workers_propertiesLines == null) {
      initWorkers_propertiesLines();
    }
    return workers_propertiesLines;
  }
  
  /**
   * Parses the file into the list
   */
  private void initWorkers_propertiesLines() {
    try (Scanner sc = new Scanner(workers_propertiesInput.get(), FILE_READ_ENCODING)) {
      workers_propertiesLines = new ArrayList<>();
      
      final OnDeleteHandler onDeleteHandler = new OnDeleteHandler() {
        @Override
        public void onDelete(TextLineReference line) {
          workers_propertiesLines.remove(line);
        }
      };
      
      int lineNumber = 0;
      while (sc.hasNextLine()) {
        lineNumber++;
        workers_propertiesLines.add(new TextLineReference.Impl(lineNumber, sc.nextLine(), onDeleteHandler));
      }
    }//TODO catch filenotfound, file not readable
  }
  
  public void saveMod_jk_conf(Supplier<OutputStream> mod_jk_confOutputStream) {
    if (mod_jk_confLines != null) {
      try (PrintStream printStream = new PrintStream(mod_jk_confOutputStream.get(), true, FILE_WRITE_ENCODING)) {
        for (TextLineReference line: mod_jk_confLines) { // invoke get() instead of direct access to initialize if necessary 
          printStream.println(line);
        }
      } catch (UnsupportedEncodingException e) {
        // impossible
        throw new RuntimeException(e);
      } // TODO catch filenotwritable
    } else {
      // if not initialized, do nothing (means that nothing changed)
    }
  }
  
  public void saveWorkers_properties(Supplier<OutputStream> workers_propertiesOutputStream) {
    if (workers_propertiesLines != null) {
      try (PrintStream printStream = new PrintStream(workers_propertiesOutputStream.get(), true, FILE_WRITE_ENCODING)) {
        for (TextLineReference line: workers_propertiesLines) { // invoke get() instead of direct access to initialize if necessary
          printStream.println(line);
        }
      } catch (UnsupportedEncodingException e) {
        // impossible
        throw new RuntimeException(e);
      } // TODO catch filenotwritable
    } else {
      // if not initialized, do nothing (means that nothing changed)
    }
  }
}

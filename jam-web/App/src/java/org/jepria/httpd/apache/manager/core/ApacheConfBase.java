package org.jepria.httpd.apache.manager.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import org.jepria.httpd.apache.manager.core.jk.TextLineReference;

/**
 * Apache HTTPD configuration based on the following configuration files:
 * {@code jk/mod_jk.conf}, {@code jk/workers.properties}  
 */
public class ApacheConfBase {
  
  //TODO this value is assumed. But how to determine it? 
  private static final String FILE_READ_ENCODING = "UTF-8";
  
  private final Supplier<InputStream> mod_jk_confInput;
  private final Supplier<InputStream> workers_propertiesInput;
  
  /**
   * @param mod_jk_confInput supplier for a lazy initialization
   * @param workers_propertiesInput supplier for a lazy initialization
   */
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
      int lineNumber = 0;
      while (sc.hasNextLine()) {
        lineNumber++;
        mod_jk_confLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
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
      int lineNumber = 0;
      while (sc.hasNextLine()) {
        lineNumber++;
        workers_propertiesLines.add(new TextLineReferenceImpl(lineNumber, sc.nextLine()));
      }
    }//TODO catch filenotfound, file not readable
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
    
    @Override
    public String toString() {
      return getContent().toString();
    }
  }
}

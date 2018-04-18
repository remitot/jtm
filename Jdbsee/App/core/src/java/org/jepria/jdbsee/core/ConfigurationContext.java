package org.jepria.jdbsee.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface ConfigurationContext {
  
  InputStream getContextXmlInputStream();
  InputStream getServerXmlInputStream();
  OutputStream getContextXmlOutputStream();
  OutputStream getServerXmlOutputStream();
  
  /**
   * 
   * @return {@code true} if to create new connections using {@code Context/ResourceLink+Server/Resource},
   * otherwise {@code false} to create new connections using {@code Context/Resource}
   */
  default boolean useResourceLinkOnCreateConnection() {
    return true;
  }
  
  default boolean deleteDuplicatesOnSave() {
    return true;
  }
  
  public static class Default implements ConfigurationContext {
    
    protected final Path confPath;
    
    public Default(Path confPath) {
      this.confPath = confPath;
    }
    
    @Override
    public OutputStream getServerXmlOutputStream() {
      try {
        return new FileOutputStream(confPath.resolve("server.xml").toFile());
      } catch (FileNotFoundException e) {
        //TODO legal?
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public InputStream getServerXmlInputStream() {
      try {
        return new FileInputStream(confPath.resolve("server.xml").toFile());
      } catch (FileNotFoundException e) {
        //TODO legal?
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public OutputStream getContextXmlOutputStream() {
      try {
        return new FileOutputStream(confPath.resolve("context.xml").toFile());
      } catch (FileNotFoundException e) {
        //TODO legal?
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public InputStream getContextXmlInputStream() {
      try {
        return new FileInputStream(confPath.resolve("context.xml").toFile());
      } catch (FileNotFoundException e) {
        //TODO legal?
        throw new RuntimeException(e);
      }
    }
  }
}

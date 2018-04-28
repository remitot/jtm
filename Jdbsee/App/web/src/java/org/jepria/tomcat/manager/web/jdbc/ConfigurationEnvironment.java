package org.jepria.tomcat.manager.web.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jepria.tomcat.manager.core.jdbc.ConnectionInitialParams;

public class ConfigurationEnvironment {
  
  protected final Path confPath;
  
  public ConfigurationEnvironment(Path confPath) {
    this.confPath = confPath;
  }
  
  public OutputStream getServerXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(confPath.resolve("server.xml").toFile());
  }
  
  public InputStream getServerXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(confPath.resolve("server.xml").toFile());
  }
  
  public OutputStream getContextXmlOutputStream() throws FileNotFoundException {
    return new FileOutputStream(confPath.resolve("context.xml").toFile());
  }
  
  public InputStream getContextXmlInputStream() throws FileNotFoundException {
    return new FileInputStream(confPath.resolve("context.xml").toFile());
  }
  
  public ConnectionInitialParams getConnectionInitialParams() {
    return new ConnectionInitialParams() {
      
      @Override
      public Map<String, String> serverResourceNodeAttributeValues() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("auth", "Container");
        attrs.put("type", "javax.sql.DataSource");
        attrs.put("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
        attrs.put("testWhileIdle", "false");
        attrs.put("testOnBorrow", "true");
        attrs.put("testOnReturn", "false");
        attrs.put("validationQuery", "SELECT 1 FROM DUAL");
        attrs.put("validationInterval", "34000");
        attrs.put("timeBetweenEvictionRunsMillis", "30000");
        attrs.put("maxActive", "100");
        attrs.put("minIdle", "30");
        attrs.put("maxIdle", "70");
        attrs.put("maxWait", "10000");
        attrs.put("initialSize", "30");
        attrs.put("removeAbandonedTimeout", "15");
        attrs.put("removeAbandoned", "true");
        attrs.put("logAbandoned", "false");
        attrs.put("minEvictableIdleTimeMillis", "30000");
        attrs.put("jmxEnabled", "true");
        attrs.put("jdbcInterceptors", 
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        attrs.put("driverClassName", "oracle.jdbc.OracleDriver");
        return attrs;
      }
      
      @Override
      public Map<String, String> contextResourceNodeAttributeValues() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("auth", "Container");
        attrs.put("connectionCachingEnabled", "true");
        attrs.put("factory", "oracle.jdbc.pool.OracleDataSourceFactory");
        attrs.put("type", "oracle.jdbc.pool.OracleDataSource");
        return attrs;
      }
      
      @Override
      public Map<String, String> contextResourceLinkNodeAttributeValues() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("closeMethod", "close");
        attrs.put("type", "javax.sql.DataSource");
        return attrs;
      }
    };
  }
}
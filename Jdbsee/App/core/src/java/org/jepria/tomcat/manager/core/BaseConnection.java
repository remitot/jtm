package org.jepria.tomcat.manager.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*package*/abstract class BaseConnection implements Connection {
  
  /**
   * Delete the node from the endpoint files
   */
  /*package*/abstract void delete();
  
  /**
   * Apply initial settings to the nodes in the endpoint files
   */
  /*package*/abstract void fillDefault(ConnectionInitialParams initialParams);
  
  protected abstract void setUrl(String url);
  protected abstract String getUrl();
  
  protected String server;
  private boolean serverHasBeenSet = false;
  
  protected String db;
  private boolean dbHasBeenSet = false;
  
  // final
  protected final void parseUrl() {
    if (!dbHasBeenSet || !serverHasBeenSet) { 
      String url = getUrl();
      
      if (url != null && !"".equals(url)) {
        Matcher m = Pattern.compile("jdbc:oracle:thin:@//([^/]*)/(.*)").matcher(url);
        if (m.matches()) {
          if (!serverHasBeenSet) {
            server = m.group(1);
            serverHasBeenSet = true;
          }
          if (!dbHasBeenSet) {
            db = m.group(2);
            dbHasBeenSet = true;
          }
        }
      }
    }
  }
  
  // final
  protected final void setUrl() {
    setUrl("jdbc:oracle:thin:@//" + getServer() + "/" + getDb());
  }
  
  // final
  @Override
  public final String getDb() {
    parseUrl();
    return db;
  }
  
  // final
  @Override
  public final void setDb(String db) {
    this.db = db;
    dbHasBeenSet = true;
    setUrl();
  }
  
  // final
  @Override
  public final String getServer() {
    parseUrl();
    return server;
  }
  
  // final
  @Override
  public final void setServer(String server) {
    this.server = server;
    serverHasBeenSet = true;
    setUrl();
  }
}

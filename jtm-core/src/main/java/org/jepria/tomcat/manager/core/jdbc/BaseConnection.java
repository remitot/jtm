package org.jepria.tomcat.manager.core.jdbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*package*/abstract class BaseConnection implements Connection {
  
  /**
   * Delete the node from the endpoint files
   */
  /*package*/abstract void delete();
  
  /**
   * Set initial attributes for the newly created nodes
   */
  /*package*/abstract void setInitialAttrs(ResourceInitialParams initialParams);
  
  /**
   * Sets the 'url' attribute of the node
   * @param url the value assembled from 
   * {@link #setServer(String)} and {@link #setDb(String)} 
   */
  protected abstract void setUrl(String url);
  
  /**
   * @return 'url' attribute of the node to parse 
   * the return values of {@link #getServer()} and {@link #getDb()}   
   */
  protected abstract String getUrl();
  
  protected boolean urlParsed = false;
  
  protected String protocol;
  private boolean protocolHasBeenSet = false;
  
  protected String server;
  private boolean serverHasBeenSet = false;
  
  protected String db;
  private boolean dbHasBeenSet = false;
  

  protected void parseUrl() {
    if (!dbHasBeenSet || !serverHasBeenSet) { 
      final String url = getUrl();
      
      if (url != null && !"".equals(url)) {
        Matcher m = Pattern.compile("(?<protocol>.+)@(//)?(?<server>.+(:\\d+)?)[:/](?<dbOrSid>.+)").matcher(url);
        if (m.matches()) {
          if (!protocolHasBeenSet) {
            protocol = m.group("protocol");
            protocolHasBeenSet = true;
          }
          if (!serverHasBeenSet) {
            server = m.group("server");
            serverHasBeenSet = true;
          }
          if (!dbHasBeenSet) {
            db = m.group("dbOrSid");
            dbHasBeenSet = true;
          }
        } else {
          throw new IllegalArgumentException("The url [" + url + "] does not match the expected regex");
        }
      }
    }
  }
  
  protected String getProtocol() {
    parseUrl();
    return protocol;
  }
  
  protected void setUrl() {
    setUrl(getProtocol() + "@//" + getServer() + "/" + getDb());
  }
  
  /*package*/void setProtocol(String protocol) {
    this.protocol = protocol;
    protocolHasBeenSet = true;
  }
  
  @Override
  public String getServer() {
    parseUrl();
    return server;
  }
 
  @Override
  public String getDb() {
    parseUrl();
    return db;
  }
  
  @Override
  public void setServer(String server) {
    this.server = server;
    serverHasBeenSet = true;
    setUrl();
  }
  
  @Override
  public void setDb(String db) {
    this.db = db;
    dbHasBeenSet = true;
    setUrl();
  }
}

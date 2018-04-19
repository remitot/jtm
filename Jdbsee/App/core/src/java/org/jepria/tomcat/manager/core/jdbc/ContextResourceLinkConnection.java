package org.jepria.tomcat.manager.core.jdbc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*package*/class ContextResourceLinkConnection extends BaseConnection {

  private Element contextResourceLinkNode;
  private Element serverResourceNode;
  
  /*package*/public ContextResourceLinkConnection(
      Node contextResourceLinkNode, Node serverResourceNode, boolean active) {
    this.contextResourceLinkNode = (Element)contextResourceLinkNode;
    this.serverResourceNode = (Element)serverResourceNode;
    this.active = active;
  }
 
  @Override
  protected void onActivate() {
    Node uncommented = NodeUtils.moveNodeFromUnfoldedComments(contextResourceLinkNode);
    contextResourceLinkNode = (Element)uncommented;
  }
  
  @Override
  protected void onDeactivate() {
    Node commented = NodeUtils.moveNodeToUnfoldedComments(contextResourceLinkNode);
    contextResourceLinkNode = (Element)commented;
  }

  @Override
  public String getName() {
    return contextResourceLinkNode.getAttribute("name");
  }

  @Override
  public void setName(String name) {
    contextResourceLinkNode.setAttribute("name", name);
    contextResourceLinkNode.setAttribute("global", name);
    serverResourceNode.setAttribute("name", name);
  }

  @Override
  public void setUrl(String url) {
    serverResourceNode.setAttribute("url", url);
  }

  @Override
  public String getUrl() {
    return serverResourceNode.getAttribute("url");
  }
  
  @Override
  public String getUser() {
    return serverResourceNode.getAttribute("username");
  }

  @Override
  public void setUser(String user) {
    serverResourceNode.setAttribute("username", user);
  }

  @Override
  public String getPassword() {
    return serverResourceNode.getAttribute("password");
  }

  @Override
  public void setPassword(String password) {
    serverResourceNode.setAttribute("password", password);
  }
  
  @Override
  public void delete() {
    contextResourceLinkNode.getParentNode().removeChild(contextResourceLinkNode);
    serverResourceNode.getParentNode().removeChild(serverResourceNode);
  }
  
  @Override
  public void fillDefault() {
    contextResourceLinkNode.setAttribute("closeMethod", "close");
    contextResourceLinkNode.setAttribute("type", "javax.sql.DataSource");
    
    serverResourceNode.setAttribute("auth", "Container");
    serverResourceNode.setAttribute("type", "javax.sql.DataSource");
    serverResourceNode.setAttribute("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
    serverResourceNode.setAttribute("testWhileIdle", "false");
    serverResourceNode.setAttribute("testOnBorrow", "true");
    serverResourceNode.setAttribute("testOnReturn", "false");
    serverResourceNode.setAttribute("validationQuery", "SELECT 1 FROM DUAL");
    serverResourceNode.setAttribute("validationInterval", "34000");
    serverResourceNode.setAttribute("timeBetweenEvictionRunsMillis", "30000");
    serverResourceNode.setAttribute("maxActive", "100");
    serverResourceNode.setAttribute("minIdle", "30");
    serverResourceNode.setAttribute("maxIdle", "70");
    serverResourceNode.setAttribute("maxWait", "10000");
    serverResourceNode.setAttribute("initialSize", "30");
    serverResourceNode.setAttribute("removeAbandonedTimeout", "15");
    serverResourceNode.setAttribute("removeAbandoned", "true");
    serverResourceNode.setAttribute("logAbandoned", "false");
    serverResourceNode.setAttribute("minEvictableIdleTimeMillis", "30000");
    serverResourceNode.setAttribute("jmxEnabled", "true");
    serverResourceNode.setAttribute("jdbcInterceptors", 
        "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    serverResourceNode.setAttribute("driverClassName", "oracle.jdbc.OracleDriver");
  }
}

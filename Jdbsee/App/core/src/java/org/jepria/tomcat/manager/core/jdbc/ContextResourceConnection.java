package org.jepria.tomcat.manager.core.jdbc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*package*/class ContextResourceConnection extends BaseConnection {

  private Element contextResourceNode;
  
  /*package*/public ContextResourceConnection(
      Node contextResourceNode, boolean active) {
    this.contextResourceNode = (Element)contextResourceNode;
    this.active = active;
  }
 

  @Override
  protected void onActivate() {
    Node uncommented = NodeUtils.moveNodeFromUnfoldedComments(contextResourceNode);
    contextResourceNode = (Element)uncommented;
  }
  
  @Override
  protected void onDeactivate() {
    Node commented = NodeUtils.moveNodeToUnfoldedComments(contextResourceNode);
    contextResourceNode = (Element)commented;
  }

  @Override
  public String getName() {
    return contextResourceNode.getAttribute("name");
  }

  @Override
  public void setName(String name) {
    contextResourceNode.setAttribute("name", name);
  }

  @Override
  public String getUrl() {
    return contextResourceNode.getAttribute("url");
  }
  
  @Override
  public void setUrl(String url) {
    contextResourceNode.setAttribute("url", url);
  }

  @Override
  public String getUser() {
    return contextResourceNode.getAttribute("user");
  }

  @Override
  public void setUser(String user) {
    contextResourceNode.setAttribute("user", user);
  }

  @Override
  public String getPassword() {
    return contextResourceNode.getAttribute("password");
  }

  @Override
  public void setPassword(String password) {
    contextResourceNode.setAttribute("password", password);
  }
  
  @Override
  public void delete() {
    contextResourceNode.getParentNode().removeChild(contextResourceNode);
  }
  
  @Override
  public void fillDefault() {
    contextResourceNode.setAttribute("auth", "Container");
    contextResourceNode.setAttribute("connectionCachingEnabled", "true");
    contextResourceNode.setAttribute("factory", "oracle.jdbc.pool.OracleDataSourceFactory");
    contextResourceNode.setAttribute("type", "oracle.jdbc.pool.OracleDataSource");
  }
}

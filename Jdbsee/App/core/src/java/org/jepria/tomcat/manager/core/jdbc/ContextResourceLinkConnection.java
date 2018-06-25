package org.jepria.tomcat.manager.core.jdbc;

import org.jepria.tomcat.manager.core.NodeFoldHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*package*/class ContextResourceLinkConnection extends BaseConnection {

  private Element contextResourceLinkNode;
  private Element serverResourceNode;
  
  private boolean active = true;
  
  /*package*/public ContextResourceLinkConnection(
      Node contextResourceLinkNode, Node serverResourceNode, boolean active) {
    this.contextResourceLinkNode = (Element)contextResourceLinkNode;
    this.serverResourceNode = (Element)serverResourceNode;
    this.active = active;
  }
  
  @Override
  public boolean isActive() {
    return active;
  }
 
  @Override
  public void onActivate() {
    Node uncommented = NodeFoldHelper.moveNodeFromUnfoldedComments(contextResourceLinkNode);
    contextResourceLinkNode = (Element)uncommented;
  }
  
  @Override
  public void onDeactivate() {
    Node commented = NodeFoldHelper.moveNodeToUnfoldedComments(contextResourceLinkNode);
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
  public void fillDefault(ConnectionInitialParams initialParams) {
    initialParams.contextResourceLinkDefaultAttrs().forEach(
        (name, value) -> contextResourceLinkNode.setAttribute(name, value));
    initialParams.serverResourceDefaultAttrs().forEach(
        (name, value) -> serverResourceNode.setAttribute(name, value));
  }
}

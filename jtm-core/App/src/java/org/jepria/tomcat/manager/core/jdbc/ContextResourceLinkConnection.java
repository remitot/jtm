package org.jepria.tomcat.manager.core.jdbc;

import org.jepria.tomcat.manager.core.NodeFoldHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*package*/class ContextResourceLinkConnection extends BaseConnection {

  private Element contextResourceLinkNode;
  private Element serverResourceNode;
  
  private final boolean dataModifiable;
  private boolean active;
  
  /*package*/ContextResourceLinkConnection(
      Node contextResourceLinkNode, Node serverResourceNode,
      boolean dataModifiable) {
    this.contextResourceLinkNode = (Element)contextResourceLinkNode;
    this.serverResourceNode = (Element)serverResourceNode;
    this.dataModifiable = dataModifiable;
    
    // determine the active state
    this.active = !NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceLinkNode)
        && !NodeFoldHelper.isNodeWithinUnfoldedComment(serverResourceNode);
  }
  
  @Override
  public boolean isDataModifiable() {
    return dataModifiable;
  }
  
  @Override
  public boolean isActive() {
    return active;
  }
 
  @Override
  public void setActive(boolean active) {
    if (!this.active && active) {
      // on activate
      if (NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceLinkNode)) {
        Node uncommented = NodeFoldHelper.unwrapNodeFromUnfoldedComment(contextResourceLinkNode);
        contextResourceLinkNode = (Element)uncommented;
      }
      if (NodeFoldHelper.isNodeWithinUnfoldedComment(serverResourceNode)) {
        Node uncommented = NodeFoldHelper.unwrapNodeFromUnfoldedComment(serverResourceNode);
        serverResourceNode = (Element)uncommented;
      }
      
      this.active = active;
      
    } else if (this.active && !active) {
      // on deactivate
      if (!NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceLinkNode)) {
        Node commented = NodeFoldHelper.wrapNodeIntoUnfoldedComment(contextResourceLinkNode);
        contextResourceLinkNode = (Element)commented;
      }
      if (!NodeFoldHelper.isNodeWithinUnfoldedComment(serverResourceNode)) {
        Node commented = NodeFoldHelper.wrapNodeIntoUnfoldedComment(serverResourceNode);
        serverResourceNode = (Element)commented;
      }
      
      this.active = active;
    }
  }

  @Override
  public String getName() {
    return contextResourceLinkNode.getAttribute("name");
  }

  @Override
  public void setName(String name) {
    contextResourceLinkNode.setAttribute("name", name);
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

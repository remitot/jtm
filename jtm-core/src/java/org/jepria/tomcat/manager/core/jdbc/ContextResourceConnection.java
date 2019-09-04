package org.jepria.tomcat.manager.core.jdbc;

import org.jepria.tomcat.manager.core.NodeFoldHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*package*/class ContextResourceConnection extends BaseConnection {

  /**
   * Context/Resource node
   */
  private Element contextResourceNode;
  
  private boolean active;
  
  public ContextResourceConnection(
      Node contextResourceNode) {
    this.contextResourceNode = (Element)contextResourceNode;
    
    // determine the active state
    this.active = !NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceNode);
  }
  
  @Override
  public boolean isDataModifiable() {
    // ContextResource's data is always modifiable
    return true;
  }
  
  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    if (!this.active && active) {
      // on activate
      
      if (NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceNode)) {
        Node uncommented = NodeFoldHelper.unwrapNodeFromUnfoldedComment(contextResourceNode);
        contextResourceNode = (Element)uncommented;
      }
      
      this.active = active;
      
    } else if (this.active && !active) {
      // on deactivate
      
      if (!NodeFoldHelper.isNodeWithinUnfoldedComment(contextResourceNode)) {
        Node commented = NodeFoldHelper.wrapNodeIntoUnfoldedComment(contextResourceNode);
        contextResourceNode = (Element)commented;
      }
      
      this.active = active;
    }
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
  public void setInitialAttrs(ResourceInitialParams initialParams) {
    initialParams.getContextResourceAttrs().forEach(
        (name, value) -> contextResourceNode.setAttribute(name, value));
  }
  
  @Override
  public String getValidationQuery() {
    // TODO does Context/Resource node support validationQuery attribute like Server/Resource does? 
    return null;
  }
}

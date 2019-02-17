package org.jepria.tomcat.manager.core.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jepria.tomcat.manager.core.LocationNotExistException;
import org.jepria.tomcat.manager.core.NodeFoldHelper;
import org.jepria.tomcat.manager.core.NodeUtils;
import org.jepria.tomcat.manager.core.TomcatConfBase;
import org.jepria.tomcat.manager.core.TransactionException;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class represents the configuration of Tomcat server for accessing JDBC resources
 */
public class TomcatConfJdbc extends TomcatConfBase {
  
  
  public TomcatConfJdbc(Supplier<InputStream> context_xmlInput,
      Supplier<InputStream> server_xmlInput) {
    super(context_xmlInput, server_xmlInput);
  }

  /**
   * Lazily initialized map of connections
   */
  private Map<String, BaseConnection> baseConnections = null;
  
  /**
   * @return unmodifiable Map&lt;Location, Connection&gt;
   */
  public Map<String, Connection> getConnections() {
    return Collections.unmodifiableMap(getBaseConnections());
  }
  
  /**
   * @return unmodifiable Map&lt;Location, BaseConnection&gt;
   */
  protected Map<String, BaseConnection> getBaseConnections() {
    if (baseConnections == null) {
      initBaseConnections();
    }
    
    return baseConnections;
  }
  
  /**
   * Lazily initialize (or re-initialize) {@link #baseConnections} map
   */
  private void initBaseConnections() {
    
    Map<String, BaseConnection> baseConnections0 = new HashMap<>();
    
    // context resources
    List<Node> contextResourceNodes = getContextResourceNodes();
    
    for (int i = 0; i < contextResourceNodes.size(); i++) {
      Node contextResourceNode = contextResourceNodes.get(i);
      
      String location = "Context.Resource-" + i;
      
      baseConnections0.put(location, 
          new ContextResourceConnection(
              contextResourceNode, 
              true));
    }
    

    
    // server resources
    final List<Node> serverResourceNodes = getServerResourceNodes();
    
    
    // context ResourceLinks
    List<Node> contextResourceLinkNodes = getContextResourceLinkNodes();
    
    for (int i = 0; i < contextResourceLinkNodes.size(); i++) {
      Node contextResourceLinkNode = contextResourceLinkNodes.get(i);
      int serverResourceIndex = lookupFirstServerResourceIndexForContextResourceLink(serverResourceNodes, contextResourceLinkNode);
      if (serverResourceIndex != -1) {
        Node serverResourceNode = serverResourceNodes.get(serverResourceIndex);
        
        String location = "Context.ResourceLink-" + i + "__Server.Resource-" + serverResourceIndex; 
        
        baseConnections0.put(location, 
            new ContextResourceLinkConnection(
                contextResourceLinkNode, 
                serverResourceNode, 
                true));
      } else {
        // TODO log?
      }
    }
    
    
    unfoldContextComments();
    
    
    // context Resources from unfolded comments
    List<NodesInUnfoldedComment> contextResourceCommentedNodes = getContextResourceCommentedNodes();
    
    for (NodesInUnfoldedComment commentedNodes: contextResourceCommentedNodes) {
      if (commentedNodes.commentIndex == null) {
        throw new IllegalStateException("All UnfoldedComments must be indexed at this point");
      }
      
      for (int i = 0; i < commentedNodes.nodes.size(); i++) {
        Node contextResourceCommentedNode = commentedNodes.nodes.get(i);
        
        String location = "Context.comment-" + commentedNodes.commentIndex + ".Resource-" + i; 
        
        baseConnections0.put(location,
            new ContextResourceConnection(
                contextResourceCommentedNode,
                false));
      }
    }
      
    
    
    // context Resources from unfolded comments
    List<NodesInUnfoldedComment> contextResourceLinksCommentedNodes = getContextResourceLinkCommentedNodes();
    
    for (NodesInUnfoldedComment commentedNodes: contextResourceLinksCommentedNodes) {
      if (commentedNodes.commentIndex == null) {
        throw new IllegalStateException("All UnfoldedComments must be indexed at this point");
      }
      
      for (int i = 0; i < commentedNodes.nodes.size(); i++) {
        Node contextResourceLinkCommentedNode = commentedNodes.nodes.get(i);
        
        int serverResourceIndex = lookupFirstServerResourceIndexForContextResourceLink(serverResourceNodes, contextResourceLinkCommentedNode);
        if (serverResourceIndex != -1) {
          Node serverResourceNode = serverResourceNodes.get(serverResourceIndex);
          
          String location = "Context.comment-" + commentedNodes.commentIndex + ".ResourceLink-" + i + "__Server.Resource-" + serverResourceIndex; 
          
          baseConnections0.put(location, 
              new ContextResourceLinkConnection(
                  contextResourceLinkCommentedNode,
                  serverResourceNode,
                  false));
        } else {
          // TODO log?
        }
      }
    }
      
      
    
    this.baseConnections = baseConnections0;
  }
  
  private List<Node> getContextResourceNodes() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      return NodeUtils.nodeListToList(nodeList);
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private List<Node> getServerResourceNodes() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      return NodeUtils.nodeListToList(nodeList);
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private List<Node> getContextResourceLinkNodes() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/ResourceLink");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      return NodeUtils.nodeListToList(nodeList);
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * List of nodes inside an <code>&lt;UnfoldedComment&gt;</code> node
   */
  private static class NodesInUnfoldedComment {
    /**
     * May be null (means that the node has been commented after the indexes assigned)
     */
    public final Integer commentIndex;
    /**
     * The list of nodes inside the <code>&lt;UnfoldedComment&gt;</code> node
     */
    public final List<Node> nodes;
    
    public NodesInUnfoldedComment(Integer commentIndex, List<Node> nodes) {
      this.commentIndex = commentIndex;
      this.nodes = nodes;
    }
  }
  
  /**
   * @return list of Resource nodes within the UnfoldedComment nodes
   */
  private List<NodesInUnfoldedComment> getContextResourceCommentedNodes() {
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      List<NodesInUnfoldedComment> res = new ArrayList<>();
      for (int k = 0; k < ucNodeList.getLength(); k++) {
        Node ucNode = ucNodeList.item(k);
        
        final Integer ucIndex;
        final String commentIndex = ((Element)ucNode).getAttribute("commentIndex");
        if (commentIndex == null || "".equals(commentIndex)) {
          ucIndex = null;
        } else {
          ucIndex = Integer.parseInt(commentIndex);
        }
        
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
            "Context/UnfoldedComment[" + (k + 1) + "]/Resource");
        NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
        
        if (nodeList.getLength() > 0) {
          List<Node> res1 = NodeUtils.nodeListToList(nodeList);
          res.add(new NodesInUnfoldedComment(ucIndex, res1));
        }
      }
      return res;
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of ResourceLink nodes within the UnfoldedComment nodes
   */
  private List<NodesInUnfoldedComment> getContextResourceLinkCommentedNodes() {
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      List<NodesInUnfoldedComment> res = new ArrayList<>();
      for (int k = 0; k < ucNodeList.getLength(); k++) {
        Node ucNode = ucNodeList.item(k);
        
        final Integer ucIndex;
        final String commentIndex = ((Element)ucNode).getAttribute("commentIndex");
        if (commentIndex == null || "".equals(commentIndex)) {
          ucIndex = null;
        } else {
          ucIndex = Integer.parseInt(commentIndex);
        }
        
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
            "Context/UnfoldedComment[" + (k + 1) + "]/ResourceLink");
        NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
        
        if (nodeList.getLength() > 0) {
          List<Node> res1 = NodeUtils.nodeListToList(nodeList);
          res.add(new NodesInUnfoldedComment(ucIndex, res1));
        }
      }
      return res;
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private static int lookupFirstServerResourceIndexForContextResourceLink(List<Node> serverResourceNodes, Node contextResourceLinkNode) {
    String contextResourceLinkName = ((Element)contextResourceLinkNode).getAttribute("name");
    for (int i = 0; i < serverResourceNodes.size(); i++) {
      if (contextResourceLinkName.equals(((Element)serverResourceNodes.get(i)).getAttribute("name"))) {
        return i;
      }
    }
    return -1;
  }
  
  private void unfoldContextComments() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Context/comment()");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        Comment comment = (Comment)nodeList.item(i);
        
        // try unfold comment (but may fail to parse comment text as a node)
        Node unfolded = NodeFoldHelper.unfoldComment(comment);
        ((Element)unfolded).setAttribute("commentIndex", Integer.toString(i));
        
        // insert unfolded comment node instead of original comment
        unfolded = getContext_xmlDoc().importNode(unfolded, true);
        comment.getParentNode().insertBefore(unfolded, comment);
        comment.getParentNode().removeChild(comment);
      }
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
      
    } catch (IOException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }
  
  private void foldContextComments() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Context/UnfoldedComment");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        
        // try fold comment
        Comment comment = NodeFoldHelper.foldComment(node);
        
        // insert original comment instead of unfolded comment node
        if (comment != null) {
          node.getParentNode().insertBefore(comment, node);
        }
        node.getParentNode().removeChild(node);
      }
      
    } catch (XPathExpressionException e) {
      // impossible: controlled XPath expression
      throw new RuntimeException(e);
    }
  }
  
  public void save(OutputStream contextXmlOutputStream, OutputStream serverXmlOutputStream) {
  
    foldContextComments();
    
    saveContext_xml(contextXmlOutputStream);
    saveServer_xml(serverXmlOutputStream);
  }
  
  protected boolean useResourceLinkOnCreateConnection = true;
  
  /**
   * 
   * @param useResourceLinkOnCreateConnection {@code true} if to create new connections using {@code Context/ResourceLink+Server/Resource},
   * otherwise {@code false} to create new connections using {@code Context/Resource}
   */
  public void setUseResourceLinkOnCreateConnection(boolean useResourceLinkOnCreateConnection) {
    this.useResourceLinkOnCreateConnection = useResourceLinkOnCreateConnection;
  }
  
  /**
   * Creates a new active connection and adds it to the document(s).
   * @param initialParams initial params to apply to the newly created connections, in the endpoint files
   * @return
   * @throws TransactionException 
   */
  public Connection create(ConnectionInitialParams initialParams) throws TransactionException {
    try {
      final BaseConnection baseConnection;
      
      if (useResourceLinkOnCreateConnection) {
        baseConnection = createContextResourceLinkConnection();
      } else {
        baseConnection = createContextResourceConnection();
      }
      
      baseConnection.fillDefault(initialParams);
      
      return baseConnection;
      
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
  
  protected ContextResourceConnection createContextResourceConnection() {
    try {
      Node contextResourceNode = getContext_xmlDoc().createElement("Resource");
      
      final XPathExpression contextResourceRootExpr = XPathFactory.newInstance().newXPath().compile(
          "Context");
      Node contextResourceRoot = (Node)contextResourceRootExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODE);
      contextResourceRoot.appendChild(contextResourceNode);
      
      return new ContextResourceConnection(contextResourceNode, true);
      
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Creates and adds
   * @return
   */
  protected ContextResourceLinkConnection createContextResourceLinkConnection() {
    try {
      // context ResourceLink
      Node contextResourceLinkNode = getContext_xmlDoc().createElement("ResourceLink");
      
      final XPathExpression contextResourceLinkRootExpr = XPathFactory.newInstance().newXPath().compile(
          "Context");
      Node contextResourceLinkRoot = (Node)contextResourceLinkRootExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODE);
      contextResourceLinkRoot.appendChild(contextResourceLinkNode);
    
      
      // server Resource
      Node serverResourceNode = getServer_xmlDoc().createElement("Resource");
      
      final XPathExpression serverResourceRootExpr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources");
      Node serverResourceRoot = (Node)serverResourceRootExpr.evaluate(getServer_xmlDoc(), XPathConstants.NODE);
      serverResourceRoot.appendChild(serverResourceNode);
      
      return new ContextResourceLinkConnection(contextResourceLinkNode, serverResourceNode, true);
      
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void delete(String location) throws TransactionException, LocationNotExistException {
    try {
      BaseConnection connection = getBaseConnections().get(location);
      
      if (connection == null) {
        throw new LocationNotExistException(location);
      }
      
      connection.delete();
      
    } catch (LocationNotExistException e) {
      throw e;
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
}

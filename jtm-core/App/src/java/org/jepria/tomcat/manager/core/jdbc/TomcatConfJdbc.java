package org.jepria.tomcat.manager.core.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.w3c.dom.NamedNodeMap;
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
    
    try {
      // context resources
      try {
        List<Node> contextResourceNodes = getContextResourceNodes();
        
        for (int i = 0; i < contextResourceNodes.size(); i++) {
          Node contextResourceNode = contextResourceNodes.get(i);
          
          String location = "Context.Resource-" + i;
          
          baseConnections0.put(location, 
              new ContextResourceConnection(
                  contextResourceNode, 
                  true));
        }
      } catch (Throwable e) {
        handleThrowable(e);
      }
      

      
      // server resources
      final List<Node> serverResourceNodes = getServerResourceNodes();
      
      
      // context ResourceLinks
      try {
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
      } catch (Throwable e) {
        handleThrowable(e);
      }
      
      
      unfoldContextComments();
      
      
      // context Resources from unfolded comments
      try {
        Map<Integer, List<Node>> contextResourceCommentedNodes = getContextResourceCommentedNodes();
        
        for (Integer commentIndex: contextResourceCommentedNodes.keySet()) {
          List<Node> contextResourceCommentedNodes1 = contextResourceCommentedNodes.get(commentIndex);
          
          for (int i = 0; i < contextResourceCommentedNodes1.size(); i++) {
            Node contextResourceCommentedNode = contextResourceCommentedNodes1.get(i);
            
            String location = "Context.comment-" + commentIndex + ".Resource-" + i; 
            
            baseConnections0.put(location,
                new ContextResourceConnection(
                    contextResourceCommentedNode,
                    false));
          }
        }
      } catch (Throwable e) {
        handleThrowable(e);
      }
        
      
      
      // context Resources from unfolded comments
      try {
        Map<Integer, List<Node>> contextResourceLinksCommentedNodes = getContextResourceLinkCommentedNodes();
        
        for (Integer commentIndex: contextResourceLinksCommentedNodes.keySet()) {
          List<Node> contextResourceLinksCommentedNodes1 = contextResourceLinksCommentedNodes.get(commentIndex);
          
          for (int i = 0; i < contextResourceLinksCommentedNodes1.size(); i++) {
            Node contextResourceLinkCommentedNode = contextResourceLinksCommentedNodes1.get(i);
            
            int serverResourceIndex = lookupFirstServerResourceIndexForContextResourceLink(serverResourceNodes, contextResourceLinkCommentedNode);
            if (serverResourceIndex != -1) {
              Node serverResourceNode = serverResourceNodes.get(serverResourceIndex);
              
              String location = "Context.comment-" + commentIndex + ".ResourceLink-" + i + "__Server.Resource-" + serverResourceIndex; 
              
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
      } catch (Throwable e) {
        handleThrowable(e);
      }
      
    } catch (Throwable e) {
      handleThrowable(e);
    }
    
    this.baseConnections = baseConnections0;
  }
  
  private List<Node> getContextResourceNodes() throws XPathExpressionException {
    final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
        "Context/Resource");
    
    NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    
    return NodeUtils.nodeListToList(nodeList);
  }
  
  private List<Node> getServerResourceNodes() throws XPathExpressionException {
    final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
        "Server/GlobalNamingResources/Resource");
    NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
    
    return NodeUtils.nodeListToList(nodeList);
  }
  
  private List<Node> getContextResourceLinkNodes() throws XPathExpressionException {
    final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
        "Context/ResourceLink");
    NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    
    return NodeUtils.nodeListToList(nodeList);
  }
  
  /**
   * @return Map&lt;index of comment in Context document, List of Resources within the comment&gt;
   * @throws XPathExpressionException
   */
  private Map<Integer, List<Node>> getContextResourceCommentedNodes() throws XPathExpressionException {
    final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
        "Context/UnfoldedComment");
    NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    
    Map<Integer, List<Node>> res = new HashMap<>();
    for (int k = 0; k < ucNodeList.getLength(); k++) {
      Node ucNode = ucNodeList.item(k);
      
      final int ucIndex = Integer.parseInt(((Element)ucNode).getAttribute("commentIndex"));
      
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment[" + (k + 1) + "]/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      if (nodeList.getLength() > 0) {
        List<Node> res1 = NodeUtils.nodeListToList(nodeList);
        res.put(ucIndex, res1);
      }
    }
    return res;
  }
  
  /**
   * @return Map&lt;Index of comment in Context document, List of ResourceLinks within the comment&gt;
   * @throws XPathExpressionException
   */
  private Map<Integer, List<Node>> getContextResourceLinkCommentedNodes() throws XPathExpressionException {
    final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
        "Context/UnfoldedComment");
    NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    
    Map<Integer, List<Node>> res = new HashMap<>();
    for (int k = 0; k < ucNodeList.getLength(); k++) {
      Node ucNode = ucNodeList.item(k);
      
      final int ucIndex = Integer.parseInt(((Element)ucNode).getAttribute("commentIndex"));
      
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment[" + (k + 1) + "]/ResourceLink");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      if (nodeList.getLength() > 0) {
        List<Node> res1 = NodeUtils.nodeListToList(nodeList);
        res.put(ucIndex, res1);
      }
    }
    return res;
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
  
  private void unfoldContextComments() throws XPathExpressionException {
    final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Context/comment()");
    NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    
    for (int i = 0; i < nodeList.getLength(); i++) {
      try {
        Comment comment = (Comment)nodeList.item(i);
        
        // try unfold comment (but may fail to parse comment text as a node)
        Node unfolded = NodeFoldHelper.unfoldComment(comment);
        ((Element)unfolded).setAttribute("commentIndex", Integer.toString(i));
        
        // insert unfolded comment node instead of original comment
        unfolded = getContext_xmlDoc().importNode(unfolded, true);
        comment.getParentNode().insertBefore(unfolded, comment);
        comment.getParentNode().removeChild(comment);
        
      } catch (Throwable e) {
        handleThrowable(e);
      }
    }
  }
  
  private void foldContextComments() throws XPathExpressionException {
    final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Context/UnfoldedComment");
    NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
    for (int i = 0; i < nodeList.getLength(); i++) {
      try {
        Node node = nodeList.item(i);
        
        // try fold comment
        Comment comment = NodeFoldHelper.foldComment(node);
        
        // insert original comment instead of unfolded comment node
        if (comment != null) {
          node.getParentNode().insertBefore(comment, node);
        }
        node.getParentNode().removeChild(node);
        
      } catch (Throwable e) {
        handleThrowable(e);
      }
    }
  }
  
  protected boolean deleteDuplicatesOnSave = true;
  
  public void setDeleteDuplicatesOnSave(boolean deleteDuplicatesOnSave) {
    this.deleteDuplicatesOnSave = deleteDuplicatesOnSave;
  }
  
  public void save(OutputStream contextXmlOutputStream, OutputStream serverXmlOutputStream) {
  
    if (deleteDuplicatesOnSave) {
      deleteDuplicates();
    }
    
    try {
      foldContextComments();
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
    
    saveContext_xml(contextXmlOutputStream);
    saveServer_xml(serverXmlOutputStream);
  }
  
  /**
   * Deletes duplicate context Resources, ResourceLinks and server Resources from the documents
   */
  public void deleteDuplicates() {
    try {
      // context Resources
      List<Node> contextResourceNodes = getContextResourceNodes();
      Map<Integer, List<Node>> contextResourceCommentedNodes = getContextResourceCommentedNodes();
      
      List<Node> allContextResourceNodes = new ArrayList<>();
      allContextResourceNodes.addAll(contextResourceNodes);
      contextResourceCommentedNodes.values().forEach(collection -> allContextResourceNodes.addAll(collection));
      
      deleteDuplicates(allContextResourceNodes);
      
      
      // context ResourceLinks
      List<Node> contextResourceLinkNodes = getContextResourceLinkNodes();
      Map<Integer, List<Node>> contextResourceLinksCommentedNodes = getContextResourceLinkCommentedNodes();
      
      List<Node> allContextResourceLinkNodes = new ArrayList<>();
      allContextResourceLinkNodes.addAll(contextResourceLinkNodes);
      contextResourceLinksCommentedNodes.values().forEach(collection -> allContextResourceLinkNodes.addAll(collection));
      
      deleteDuplicates(allContextResourceLinkNodes);
      
      
      // server Resources
      List<Node> serverResourceNodes = getServerResourceNodes();
      
      deleteDuplicates(serverResourceNodes);
      
      
    } catch (Throwable e) {
      handleThrowable(e);
    }
  }
  
  /**
   * Deletes duplicate nodes, contained in a list, from their DOM parents and from the list
   * @param nodes
   */
  private static void deleteDuplicates(List<Node> nodes) {
    List<Node> nodes1 = new ArrayList<>(nodes);
    List<Node> distinctResourceNodes = nodes1.stream().filter(distinctNodeFilter()).collect(Collectors.toList());
    
    nodes1.removeAll(distinctResourceNodes);
    for (Node node: nodes1) {
      nodes.remove(node);
      node.getParentNode().removeChild(node);
    }
  }
  
  /**
   * @return predicate that used to filter a stream of nodes 
   * (filter remains only such nodes that no pair of them gives true for {@link #nodesEqual})
   */
  private static Predicate<Node> distinctNodeFilter() {
    List<Node> distinctNodes = new ArrayList<>();
    return node -> {
      for (Node node1: distinctNodes) {
        if (nodesEqual(node, node1)) {
          return false;
        }
      }
      distinctNodes.add(node);
      return true;
    };
  }
  
  /**
   * Checks whether two nodes have same nodeName and same attributes.
   * Does not test equality of children or any other node properties
   * @param node1
   * @param node2
   * @return
   */
  private static boolean nodesEqual(Node node1, Node node2) {
    if (!node1.getNodeName().equals(node2.getNodeName())) {
      return false;
    }
    
    // compare attributes
    NamedNodeMap attrs1 = node1.getAttributes();
    NamedNodeMap attrs2 = node2.getAttributes();
    if (attrs1.getLength() != attrs2.getLength()) {
      return false;
    }
    for (int i = 0; i < attrs1.getLength(); i++) {
      Node attr1 = attrs1.item(i);
      String name = attr1.getNodeName();
      Node attr2 = attrs2.getNamedItem(name);
      if (attr2 == null) {
        // because attr1 != null here
        return false;
      }
      String value = attr1.getNodeValue();
      if (!value.equals(attr2.getNodeValue())) {
        return false;
      }
    }
    
    return true;
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
      handleThrowable(e);
      throw new TransactionException(e);
    }
  }
  
  protected ContextResourceConnection createContextResourceConnection() throws XPathExpressionException {
    Node contextResourceNode = getContext_xmlDoc().createElement("Resource");
    
    final XPathExpression contextResourceRootExpr = XPathFactory.newInstance().newXPath().compile(
        "Context");
    Node contextResourceRoot = (Node)contextResourceRootExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODE);
    contextResourceRoot.appendChild(contextResourceNode);
    
    return new ContextResourceConnection(contextResourceNode, true);
  }
  
  /**
   * Creates and adds
   * @return
   * @throws XPathExpressionException 
   */
  protected ContextResourceLinkConnection createContextResourceLinkConnection() throws XPathExpressionException {
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

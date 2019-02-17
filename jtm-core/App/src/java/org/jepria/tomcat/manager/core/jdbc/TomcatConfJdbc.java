package org.jepria.tomcat.manager.core.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jepria.tomcat.manager.core.LocationNotExistException;
import org.jepria.tomcat.manager.core.NodeFoldHelper;
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
    
    Map<String, BaseConnection> baseConnections = new HashMap<>();
    
    // Context/Resource nodes
    final List<NodeWithLocation> contextResources = getContextResources();
    
    // commented Context/Resource nodes
    final List<NodeWithLocation> commentedContextResources = getCommentedContextResources();
    
    
    
    for (NodeWithLocation contextResourceNode: contextResources) {
      final BaseConnection resource = new ContextResourceConnection(contextResourceNode.node, true);
      baseConnections.put(contextResourceNode.location, resource);
    }
    
    for (NodeWithLocation resourceNode: commentedContextResources) {
      final BaseConnection resource = new ContextResourceConnection(resourceNode.node, false);
      baseConnections.put(resourceNode.location, resource);
    }
    
    
    
    
    
    
    // Context/ResourceLink nodes
    final List<NodeWithLocation> contextResourceLinks = getContextResourceLinkNodes();
    // commented Context/ResourceLink nodes
    final List<NodeWithLocation> commentedContextResourceLinks = getCommentedContextResourceLinks();
    
    // Server/Resource nodes
    final List<NodeWithLocation> serverResources = getServerResources();
    // commented Server/Resource nodes
    final List<NodeWithLocation> commentedServerResources = getCommentedServerResources();
    
    
    
    
    // count links from Context/ResourseLink nodes to the same Server/Resource node.
    // If multiple Context/ResourceLink nodes link to the same Server/Resource node,
    // then that Server/Resource node is unmodifiable.
    
    // Map<Server/Resource's location, Context/ResourceLink's link count>
    // Map uses AtomicInteger not for concurrency, but for easier increment only
    final Map<String, AtomicInteger> serverResourceLinkedLocations = new HashMap<>();
    
    {
      for (NodeWithLocation contextResourceLinkNode: contextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        for (NodeWithLocation serverResourceNode: serverResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            // increment count
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              serverResourceLinkedLocations.put(serverResourceLocation, new AtomicInteger(1));
            } else {
              count.incrementAndGet();
            }
          }
        }
        
        for (NodeWithLocation serverResourceNode: commentedServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            // increment count
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              serverResourceLinkedLocations.put(serverResourceLocation, new AtomicInteger(1));
            } else {
              count.incrementAndGet();
            }
          }
        }
      }
      
      for (NodeWithLocation contextResourceLinkNode: commentedContextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        List<NodeWithLocation> allServerResources = new ArrayList<>();
        allServerResources.addAll(serverResources);
        allServerResources.addAll(commentedServerResources);
        
        for (NodeWithLocation serverResourceNode: allServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            // increment count
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              serverResourceLinkedLocations.put(serverResourceLocation, new AtomicInteger(1));
            } else {
              count.incrementAndGet();
            }
          }
        }
      }
    }
    
    
    // assemble Resources from Context/ResourceLink+Server/Resource
    {
      for (NodeWithLocation contextResourceLinkNode: contextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        for (NodeWithLocation serverResourceNode: serverResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            final boolean dataModifiable;
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              dataModifiable = true;
            } else {
              dataModifiable = count.get() == 1;
            }
            
            final BaseConnection resource = new ContextResourceLinkConnection(
                contextResourceLinkNode.node, serverResourceNode.node,
                dataModifiable, true);
            final String location = contextResourceLinkNode.location 
                + "+" + serverResourceLocation;
            baseConnections.put(location, resource);
          }
        }
        
        for (NodeWithLocation serverResourceNode: commentedServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            final boolean dataModifiable;
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              dataModifiable = true;
            } else {
              dataModifiable = count.get() == 1;
            }
            
            final BaseConnection resource = new ContextResourceLinkConnection(
                contextResourceLinkNode.node, serverResourceNode.node,
                dataModifiable, false);
            final String location = contextResourceLinkNode.location 
                + "+" + serverResourceLocation;
            baseConnections.put(location, resource);
          }
        }
      }
      
      for (NodeWithLocation contextResourceLinkNode: commentedContextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        List<NodeWithLocation> allServerResources = new ArrayList<>();
        allServerResources.addAll(serverResources);
        allServerResources.addAll(commentedServerResources);
        
        for (NodeWithLocation serverResourceNode: allServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceLocation = serverResourceNode.location;
            
            final boolean dataModifiable;
            AtomicInteger count = serverResourceLinkedLocations.get(serverResourceLocation);
            if (count == null) {
              dataModifiable = true;
            } else {
              dataModifiable = count.get() == 1;
            }
            
            final BaseConnection resource = new ContextResourceLinkConnection(
                contextResourceLinkNode.node, serverResourceNode.node,
                dataModifiable, true);
            final String location = contextResourceLinkNode.location 
                + "+" + serverResourceLocation;
            baseConnections.put(location, resource);
          }
        }
      }
    }
    
    this.baseConnections = baseConnections;
  }
  
  /**
   * @return list of <b>unique-named</b> (retaining the first only) commented Context/Resource nodes.
   * The list items are filtered by unique name because
   * Tomcat ignores all same-named Context/Resource nodes except for the first
   */
  private List<NodeWithLocation> getContextResources() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        
        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String location = "Context/Resource[" + i + "]";;
          ret.add(new NodeWithLocation((Element)node, location));
          
        } else {
          // TODO warn (log)? Or silently comment the other nodes? 
          // Commenting will solve the problem, because 
          // it is allowed (by JTM) to have multiple same-named commented resources.
        }
      }
      
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of <b>unique-named</b> (retaining the first only) commented Server/Resource nodes.
   * The list items are filtered by unique name because
   * Tomcat ignores all same-named Server/Resource nodes except for the first
   */
  private List<NodeWithLocation> getServerResources() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        
        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String location = "Server/Resource[" + i + "]";;
          ret.add(new NodeWithLocation((Element)node, location));
          
        } else {
          // TODO warn (log)? Or silently comment the other nodes? 
          // Commenting will solve the problem, because 
          // it is allowed (by JTM) to have multiple same-named commented resources.
        }
      }
      
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of <b>unique-named</b> (retaining the first only) commented Context/ResourceLink nodes.
   * The list items are filtered by unique name because
   * Tomcat ignores all same-named Context/ResourceLink nodes except for the first
   */
  private List<NodeWithLocation> getContextResourceLinkNodes() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/ResourceLink");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);

        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String location = "Context/ResourceLink[" + i + "]";;
          ret.add(new NodeWithLocation((Element)node, location));
          
        } else {
          // TODO warn (log)? Or silently comment the other nodes? 
          // Commenting will solve the problem, because 
          // it is allowed (by JTM) to have multiple same-named commented resources.
        }
      }
      
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of <b>all</b> commented Context/Resource nodes.
   * The list items are not filtered by unique name because 
   * JTM allows multiple commented same-named resources  
   */
  private List<NodeWithLocation> getCommentedContextResources() {
    
    if (!contextCommentsUnfolded) {
      unfoldContextComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Context/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all Resource nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Resource");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String location = "Context/comment[" + i + "]/Resource[" + j + "]";
          ret.add(new NodeWithLocation((Element)node, location));
        }
      }
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of <b>all</b> commented Context/ResourceLink nodes.
   * The list items are not filtered by unique name because 
   * JTM allows multiple commented same-named resources  
   */
  private List<NodeWithLocation> getCommentedContextResourceLinks() {
    
    if (!contextCommentsUnfolded) {
      unfoldContextComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Context/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all ResourceLink nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("ResourceLink");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String location = "Context/comment[" + i + "]/ResourceLink[" + j + "]";
          ret.add(new NodeWithLocation((Element)node, location));
        }
      }
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return list of <b>all</b> commented Server/Resource nodes.
   * The list items are not filtered by unique name because 
   * JTM allows multiple commented same-named resources  
   */
  private List<NodeWithLocation> getCommentedServerResources() {
    
    if (!serverGnrCommentsUnfolded) {
      unfoldServerGnrComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithLocation> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Server/GlobalNamingResources/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all Resource nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Resource");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String location = "Server/comment[" + i + "]/Resource[" + j + "]";
          ret.add(new NodeWithLocation((Element)node, location));
        }
      }
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private boolean contextCommentsUnfolded = false;
  private boolean serverGnrCommentsUnfolded = false;
  
  /**
   * Unfolds XML comments within the Context node:
   * from
   * <Context>
   *   <!-- <CommentedNode>content</CommentedNode> -->
   * </Context>
   * to
   * <Context>
   *   <UnfoldedComment><CommentedNode>content</CommentedNode></UnfoldedComment>
   * </Context>
   */
  private void unfoldContextComments() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Context/comment()");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        Comment comment = (Comment)nodeList.item(i);
        
        // try unfold comment (but may fail to parse comment text as a node)
        Node unfolded = NodeFoldHelper.unfoldComment(comment);
        
        // insert unfolded comment node instead of original comment
        unfolded = comment.getOwnerDocument().importNode(unfolded, true);
        comment.getParentNode().insertBefore(unfolded, comment);
        comment.getParentNode().removeChild(comment);
      }
      
      contextCommentsUnfolded = true;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
      
    } catch (IOException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Unfolds XML comments within the Server/GlobalNamingResources node:
   * from
   * <Server>
   *   <GlobalNamingResources>
   *     <!-- <CommentedNode>content</CommentedNode> -->
   *   </GlobalNamingResources>
   * </Server>
   * to
   * <Server>
   *   <GlobalNamingResources>
   *     <UnfoldedComment><CommentedNode>content</CommentedNode></UnfoldedComment>
   *   </GlobalNamingResources>
   * </Server>
   */
  private void unfoldServerGnrComments() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Server/GlobalNamingResources/comment()");
      NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        Comment comment = (Comment)nodeList.item(i);
        
        // try unfold comment (but may fail to parse comment text as a node)
        Node unfolded = NodeFoldHelper.unfoldComment(comment);
        
        // insert unfolded comment node instead of original comment
        unfolded = comment.getOwnerDocument().importNode(unfolded, true);
        comment.getParentNode().insertBefore(unfolded, comment);
        comment.getParentNode().removeChild(comment);
      }
      
      serverGnrCommentsUnfolded = true;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
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
  
  private static class NodeWithLocation {
    public final Node node;
    public final String location;
     
    
    public NodeWithLocation(Node node, String location) {
      this.node = node;
      this.location = location;
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
      
      return new ContextResourceLinkConnection(contextResourceLinkNode, serverResourceNode, true, true);
      
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

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

import org.jepria.tomcat.manager.core.NodeFoldHelper;
import org.jepria.tomcat.manager.core.TomcatConfBase;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class represents the configuration of Tomcat server for accessing JDBC resources
 */
public class TomcatConfJdbc extends TomcatConfBase {
  
  /**
   * Whether to create new resources with Context/Resource or Context/ResourceLink+Server/GlobalNamingResources/Resource nodes
   */
  private final boolean createContextResources;
  
  /**
   * @param context_xmlInput
   * @param server_xmlInput
   * @param createContextResources if {@code true}: create new resources with {@code Context/Resource} nodes, 
   * otherwise: create new resources with {@code Context/ResourceLink+Server/GlobalNamingResources/Resource} nodes
   */
  public TomcatConfJdbc(Supplier<InputStream> context_xmlInput,
      Supplier<InputStream> server_xmlInput, 
      boolean createContextResources) {
    
    super(context_xmlInput, server_xmlInput);
    this.createContextResources = createContextResources;
  }

  /**
   * Lazily initialized map of connections
   */
  private Map<String, BaseConnection> baseConnections = null;
  
  /**
   * @return unmodifiable Map&lt;ResourceId, Resource&gt;
   */
  public Map<String, Connection> getConnections() {
    return Collections.unmodifiableMap(getBaseConnections());
  }
  
  /**
   * @return Map&lt;ResourceId, BaseConnection&gt;
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
    
    // for filtering unique context names
    final Set<String> contextNames = new HashSet<>();
    
    // Context/Resource nodes
    final List<NodeWithId> contextResources = getContextResources();
    // commented Context/Resource nodes
    final List<NodeWithId> commentedContextResources = getCommentedContextResources();
    
    {
      final List<NodeWithId> allContextResources = new ArrayList<>();
      allContextResources.addAll(contextResources);
      allContextResources.addAll(commentedContextResources);
      
      for (NodeWithId contextResourceNode: allContextResources) {
        if (contextNames.add(((Element)contextResourceNode.node).getAttribute("name"))) {// filter unique context names
          final BaseConnection resource = new ContextResourceConnection(contextResourceNode.node);
          baseConnections.put(contextResourceNode.id, resource);
        }
      }
    }
    
    
    
    
    
    
    // Context/ResourceLink nodes
    final List<NodeWithId> contextResourceLinks = getContextResourceLinks();
    // commented Context/ResourceLink nodes
    final List<NodeWithId> commentedContextResourceLinks = getCommentedContextResourceLinks();
    
    // Server/Resource nodes
    final List<NodeWithId> serverResources = getServerResources();
    // commented Server/Resource nodes
    final List<NodeWithId> commentedServerResources = getCommentedServerResources();
    
    
    
    
    // count links from Context/ResourseLink nodes to the same Server/Resource node.
    // If multiple Context/ResourceLink nodes link to the same Server/Resource node,
    // then that Server/Resource node is unmodifiable.
    
    // Map<Server/Resource's id, Context/ResourceLink's link count>
    // Map uses AtomicInteger not for concurrency, but for easier increment only
    final Map<String, AtomicInteger> serverResourceLinkedIds = new HashMap<>();
    
    {
      for (NodeWithId contextResourceLinkNode: contextResourceLinks) {
        if (contextNames.add(((Element)contextResourceLinkNode.node).getAttribute("name"))) {// filter unique context names
          
          final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
          
          for (NodeWithId serverResourceNode: serverResources) {
            final String name = ((Element)serverResourceNode.node).getAttribute("name");
            if (name.equals(global)) {
              final String serverResourceId = serverResourceNode.id;
              
              // increment count
              AtomicInteger count = serverResourceLinkedIds.get(serverResourceId);
              if (count == null) {
                serverResourceLinkedIds.put(serverResourceId, new AtomicInteger(1));
              } else {
                count.incrementAndGet();
              }
            }
          }
          
          for (NodeWithId serverResourceNode: commentedServerResources) {
            final String name = ((Element)serverResourceNode.node).getAttribute("name");
            if (name.equals(global)) {
              final String serverResourceId = serverResourceNode.id;
              
              // increment count
              AtomicInteger count = serverResourceLinkedIds.get(serverResourceId);
              if (count == null) {
                serverResourceLinkedIds.put(serverResourceId, new AtomicInteger(1));
              } else {
                count.incrementAndGet();
              }
            }
          }
        }
      }
      
      for (NodeWithId contextResourceLinkNode: commentedContextResourceLinks) {
        if (contextNames.add(((Element)contextResourceLinkNode.node).getAttribute("name"))) {// filter unique context names
        
          final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
          List<NodeWithId> allServerResources = new ArrayList<>();
          allServerResources.addAll(serverResources);
          allServerResources.addAll(commentedServerResources);
          
          for (NodeWithId serverResourceNode: allServerResources) {
            final String name = ((Element)serverResourceNode.node).getAttribute("name");
            if (name.equals(global)) {
              final String serverResourceId = serverResourceNode.id;
              
              // increment count
              AtomicInteger count = serverResourceLinkedIds.get(serverResourceId);
              if (count == null) {
                serverResourceLinkedIds.put(serverResourceId, new AtomicInteger(1));
              } else {
                count.incrementAndGet();
              }
            }
          }
        }
      }
    }
    
    
    // assemble Resources from Context/ResourceLink+Server/Resource
    {
      for (NodeWithId contextResourceLinkNode: contextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        final List<NodeWithId> allServerResources = new ArrayList<>();
        allServerResources.addAll(serverResources);
        allServerResources.addAll(commentedServerResources);
        
        for (NodeWithId serverResourceNode: allServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceId = serverResourceNode.id;
            
            final boolean dataModifiable;
            AtomicInteger count = serverResourceLinkedIds.get(serverResourceId);
            if (count == null) {
              dataModifiable = true;
            } else {
              dataModifiable = count.get() == 1;
            }
            
            final BaseConnection resource = new ContextResourceLinkConnection(
                contextResourceLinkNode.node, serverResourceNode.node,
                dataModifiable);
            final String id = contextResourceLinkNode.id 
                + "+" + serverResourceId;
            baseConnections.put(id, resource);
          }
        }
      }
      
      for (NodeWithId contextResourceLinkNode: commentedContextResourceLinks) {
        final String global = ((Element)contextResourceLinkNode.node).getAttribute("global");
        
        List<NodeWithId> allServerResources = new ArrayList<>();
        allServerResources.addAll(serverResources);
        allServerResources.addAll(commentedServerResources);
        
        for (NodeWithId serverResourceNode: allServerResources) {
          final String name = ((Element)serverResourceNode.node).getAttribute("name");
          if (name.equals(global)) {
            final String serverResourceId = serverResourceNode.id;
            
            final boolean dataModifiable;
            AtomicInteger count = serverResourceLinkedIds.get(serverResourceId);
            if (count == null) {
              dataModifiable = true;
            } else {
              dataModifiable = count.get() == 1;
            }
            
            final BaseConnection resource = new ContextResourceLinkConnection(
                contextResourceLinkNode.node, serverResourceNode.node,
                dataModifiable);
            final String id = contextResourceLinkNode.id + "+" + serverResourceId;
            baseConnections.put(id, resource);
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
  private List<NodeWithId> getContextResources() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        
        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String id = "$C.R" + i; // means "located at Context/Resource-with-index-i"
          ret.add(new NodeWithId((Element)node, id));
          
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
  private List<NodeWithId> getServerResources() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources/Resource");
      NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        
        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String id = "$S.GNR.R" + i; // means "located at Server/GlobalNamingResources/Resource-with-index-i"
          ret.add(new NodeWithId((Element)node, id));
          
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
  private List<NodeWithId> getContextResourceLinks() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile(
          "Context/ResourceLink");
      NodeList nodeList = (NodeList) expr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      // for filtering duplicate names
      final Set<String> names = new HashSet<>();
      
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);

        final String name = ((Element)node).getAttribute("name");
        if (!names.contains(name)) {
          names.add(name);
          
          final String id = "$C.RL" + i; // means "located at Context/ResourceLink-with-index-i"
          ret.add(new NodeWithId((Element)node, id));
          
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
  private List<NodeWithId> getCommentedContextResources() {
    
    if (!contextCommentsUnfolded) {
      unfoldContextComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Context/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all Resource nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Resource");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String id = "$C.#" + i + ".R" + j; // means "located at Context/comment-with-index-i/Resource-with-index-j"
          ret.add(new NodeWithId((Element)node, id));
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
  private List<NodeWithId> getCommentedContextResourceLinks() {
    
    if (!contextCommentsUnfolded) {
      unfoldContextComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Context/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Context/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all ResourceLink nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("ResourceLink");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String id = "$C.#" + i + ".RL" + j; // means "located at Context/comment-with-index-i/ResourceLink-with-index-j"
          ret.add(new NodeWithId((Element)node, id));
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
  private List<NodeWithId> getCommentedServerResources() {
    
    if (!serverCommentsUnfolded) {
      unfoldServerComments();
    }
    
    try {
      final XPathExpression ucExpr = XPathFactory.newInstance().newXPath().compile(
          "Server/GlobalNamingResources/UnfoldedComment");
      NodeList ucNodeList = (NodeList) ucExpr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
      
      final List<NodeWithId> ret = new ArrayList<>();
      
      for (int i = 0; i < ucNodeList.getLength(); i++) {
        // for each Server/GlobalNamingResources/UnfoldedComment node
        Node ucNode = ucNodeList.item(i);
        
        // find all Resource nodes
        final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Resource");
        NodeList nodeList = (NodeList) expr.evaluate(ucNode, XPathConstants.NODESET);
        
        for (int j = 0; j < nodeList.getLength(); j++) {
          final Node node = nodeList.item(j);
          final String id = "$S.GNR.#" + i + ".R" + j; // means "located at Server/GlobalNamingResources/comment-with-index-i/Resource-with-index-j"
          ret.add(new NodeWithId((Element)node, id));
        }
      }
      return ret;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private boolean contextCommentsUnfolded = false;
  private boolean serverCommentsUnfolded = false;
  
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
  private void unfoldServerComments() {
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
      
      serverCommentsUnfolded = true;
      
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
      
      contextCommentsUnfolded = false;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  private void foldServerComments() {
    try {
      final XPathExpression expr = XPathFactory.newInstance().newXPath().compile("Server/GlobalNamingResources/UnfoldedComment");
      NodeList nodeList = (NodeList) expr.evaluate(getServer_xmlDoc(), XPathConstants.NODESET);
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
      
      serverCommentsUnfolded = false;
      
    } catch (XPathExpressionException e) {
      // impossible: trusted XPath expression
      throw new RuntimeException(e);
    }
  }
  
  /**
   * A {@link Node} with id (based on the node's location in the XML file)
   */
  private static class NodeWithId {
    public final Node node;
    public final String id;
     
    
    public NodeWithId(Node node, String id) {
      this.node = node;
      this.id = id;
    }
  }
  
  
  
  public void save(OutputStream contextXmlOutputStream, OutputStream serverXmlOutputStream) {
  
    if (contextCommentsUnfolded) {
      foldContextComments();
    }
    if (serverCommentsUnfolded) {
      foldServerComments();
    }
    
    saveContext_xml(contextXmlOutputStream);
    saveServer_xml(serverXmlOutputStream);
  }
  
  public enum ValidateNameResult {
    /**
     * The target name neither matches any existing {@code Context/Resource} or {@code Context/ResourceLink} name nor 
     * any {@code Server/Resource} name (if {@link #createContextResources} is set to {@code false})  
     */
    OK,
    /**
     * The target name matches an existing {@code Context/Resource} or {@code Context/ResourceLink} name
     */
    DUPLICATE_NAME,
    /**
     * The target name does not match any existing {@code Context/Resource} or {@code Context/ResourceLink} name,
     * but {@link #createContextResources} is set to {@code false} and the target name matches an existing {@code Server/Resource} name
     */
    DUPLICATE_GLOBAL
  }
  /**
   * Validates 'name' field of the resource that is about to be created (before the creation) or updated.
   * @param name name of the resource that is about to be created or updated
   * @return
   */
  public ValidateNameResult validateNewResourceName(String name) {
    if (!validateNewContextResourceOrResourceLinkName(name)) {
      return ValidateNameResult.DUPLICATE_NAME;
    }
    
    if (!createContextResources) {
      if (!validateNewServerResourceName(name)) {
        return ValidateNameResult.DUPLICATE_GLOBAL;
      }
    }
    
    return ValidateNameResult.OK;
  }
  
  protected boolean validateNewContextResourceOrResourceLinkName(String name) {
    final List<NodeWithId> nodes = new ArrayList<>();
    nodes.addAll(getContextResources());
    nodes.addAll(getCommentedContextResources());
    nodes.addAll(getContextResourceLinks());
    nodes.addAll(getCommentedContextResourceLinks());
    
    return !nodes.stream().anyMatch(
        node -> name.equals(((Element)node.node).getAttribute("name")));
  }
  
  protected boolean validateNewServerResourceName(String name) {
    final List<NodeWithId> nodes = new ArrayList<>();
    nodes.addAll(getServerResources());
    nodes.addAll(getCommentedServerResources());

    return !nodes.stream().anyMatch(
        node -> name.equals(((Element)node.node).getAttribute("name")));
  }
  
  
  /**
   * Creates a new active connection.
   * @param initialParams initial params to apply to the newly created resource
   * @return the created resource
   */
  public Connection create(String name, 
      ResourceInitialParams initialParams) {
    
    final BaseConnection baseConnection;

    if (createContextResources) {
      baseConnection = createContextResourceConnection();
    } else {
      baseConnection = createContextResourceLinkConnection();
    }
    
    baseConnection.setProtocol(initialParams.getJdbcProtocol());
    baseConnection.setInitialAttrs(initialParams);
    
    return baseConnection;
  }
  
  protected ContextResourceConnection createContextResourceConnection() {
    try {
      Node contextResourceNode = getContext_xmlDoc().createElement("Resource");
      
      final XPathExpression contextResourceRootExpr = XPathFactory.newInstance().newXPath().compile(
          "Context");
      Node contextResourceRoot = (Node)contextResourceRootExpr.evaluate(getContext_xmlDoc(), XPathConstants.NODE);
      contextResourceRoot.appendChild(contextResourceNode);
      
      return new ContextResourceConnection(contextResourceNode);
      
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
      
      
      return new ContextResourceLinkCreatedConnection(contextResourceLinkNode, serverResourceNode, true);
      
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }
  
  
  /**
   * Class represents a newly created connection. 
   * Used to initially set the Context/ResourceLink's 'global' and Server/Resource's 'name' attributes
   */
  private class ContextResourceLinkCreatedConnection extends ContextResourceLinkConnection {
    
    private boolean globalHasBeenSet = false;
    
    public ContextResourceLinkCreatedConnection(Node contextResourceLinkNode,
        Node serverResourceNode, boolean dataModifiable) {
      super(contextResourceLinkNode, serverResourceNode, dataModifiable);
    }
    
    @Override
    public void setName(String name) {
      super.setName(name);
      
      if (!globalHasBeenSet) {
        setGlobal(name);
        globalHasBeenSet = true;
      }
    }
    
    protected void setGlobal(String name) {
      contextResourceLinkNode.setAttribute("global", name);
      serverResourceNode.setAttribute("name", name);
    }
  }
  
  /**
   * Delete resource by id
   * @param id resource id
   */
  public void delete(String id) {
      BaseConnection connection = getBaseConnections().get(id);
      
      if (connection == null) {
        throw new IllegalArgumentException("No resource found by id [" + id + "]");
      }
      
      connection.delete();
  }
}

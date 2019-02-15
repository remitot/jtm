package org.jepria.tomcat.manager.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class NodeFoldHelper {
  
  
  /**
   * Folds the UnfoldedComment node into the normal comment tag,
   * e.g. from <pre>&lt;UnfoldedComment&gt;&lt;Node&gt;node-contents&lt;/Node&gt;&lt;/UnfoldedComment&gt;</pre>
   * to <pre>&lt;!-- &lt;Node&gt;node-contents&lt;/Node&gt; --&gt;</pre>
   * 
   * @param the node <b>which itself</b> is an <code>&lt;UnfoldedComment&gt;</code>
   * @return comment or null if the comment is empty or if the given node is not an UnfoldedComment
   */
  public static Comment foldComment(Node node) {
    if ("UnfoldedComment".equals(((Element)node).getTagName())) {
      
      // remove all UnfoldedComment attributes, e.g. commentIndex to easily match by regex
      while (node.getAttributes().getLength() > 0) {
        Node attr = node.getAttributes().item(0);
        node.getAttributes().removeNamedItem(attr.getNodeName());
      }
      
      final String unfoldedCommentAsString = NodeUtils.nodeToString(node);
      final String commentContent;
      
      Matcher m;
      
      m = Pattern.compile("\\s*" + Pattern.quote("<UnfoldedComment>") + "(.*?)" + Pattern.quote("</UnfoldedComment>") + "\\s*", Pattern.DOTALL).matcher(unfoldedCommentAsString);
      if (m.matches()) {
        commentContent = m.group(1);
      } else {
        m = Pattern.compile("\\s*" + Pattern.quote("<UnfoldedComment/>") + "\\s*", Pattern.DOTALL).matcher(unfoldedCommentAsString);
        if (m.matches()) {
          return null;
        } else {
          throw new IllegalStateException("Unable to fold the node [" + unfoldedCommentAsString + "] as an UnfoldedComment");
        }
      }
      

      // trim
      m = Pattern.compile("\\s*", Pattern.DOTALL).matcher(commentContent);
      if (m.matches()) {
        // comment content is empty
        return null;
      }
      
      return node.getOwnerDocument().createComment(commentContent);
      
    } else {
      return null;
    }
  }
  
  /**
   * Tries to parse a comment into a document, may fail
   * @param comment 
   * @return comment text as a document with {@code <UnfoldedComment>} root tag
   * @throws IOException
   */
  public static Node unfoldComment(Comment comment) throws IOException {
    try {
      
      String commentContentAsXml = "<UnfoldedComment>" + comment.getTextContent() + "</UnfoldedComment>";
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
          new ByteArrayInputStream(commentContentAsXml.getBytes("UTF-8"))).getDocumentElement();
      
    } catch (SAXException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Unwraps the node from the unfolded comment,
   * e.g. from <pre>&lt;UnfoldedComment&gt;&lt;Node&gt;node-contents&lt;/Node&gt;&lt;/UnfoldedComment&gt;</pre>
   * to <pre>&lt;Node&gt;node-contents&lt;/Node&gt;</pre>
   *   
   * @param node the node <b>whose parent</b> is an <code>&lt;UnfoldedComment&gt;</code>
   * @return a clone of the given node (just unfolded), or the given node itself, if the node's parent is not an <code>&lt;UnfoldedComment&gt;</code>
   */
  public static Node unwrapNodeFromUnfoldedComment(Node node) {
    Node unfoldedCommentRoot = node.getParentNode();
    
    if (unfoldedCommentRoot == null || !"UnfoldedComment".equals(((Element)unfoldedCommentRoot).getTagName())) {
      return node;
    }
    
    Node nodeClone = node.cloneNode(true);
    nodeClone = node.getOwnerDocument().importNode(nodeClone, true);
    
    Node unfoldedCommentParent = unfoldedCommentRoot.getParentNode();

    unfoldedCommentParent.insertBefore(nodeClone, unfoldedCommentRoot);
    unfoldedCommentRoot.removeChild(node);
    
    return nodeClone;
  }
  
  /**
   * Wraps the node into <code>&lt;UnfoldedComment&gt;</code> tag,
   * e.g. from <pre>&lt;Node&gt;node-contents&lt;/Node&gt;</pre> to
   * <pre>&lt;UnfoldedComment&gt;&lt;Node&gt;node-contents&lt;/Node&gt;&lt;/UnfoldedComment&gt;</pre>  
   * @param node
   * @return
   */
  public static Node wrapNodeIntoUnfoldedComment(Node node) {
    Node nodeClone = node.cloneNode(true);
    nodeClone = node.getOwnerDocument().importNode(nodeClone, true);
    
    Node unfoldedCommentRoot = node.getOwnerDocument().createElement("UnfoldedComment");
    unfoldedCommentRoot.appendChild(nodeClone);
    
    Node unfoldedCommentParent = node.getParentNode();
    
    unfoldedCommentParent.insertBefore(unfoldedCommentRoot, node);
    unfoldedCommentParent.removeChild(node);
    
    return nodeClone;
  }
}

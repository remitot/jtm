package org.jepria.tomcat.manager.core.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/*package*/class NodeUtils {
  
  
  /**
   * @param node
   * @return or null if the comment is empty
   * @throws TransformerException
   * @throws IllegalArgumentException
   * @throws IOException 
   */
  /*package*/static Comment foldComment(Node node) throws TransformerException, IllegalArgumentException, IOException {
    if ("UnfoldedComment".equals(((Element)node).getTagName())) {
      
      // remove all UnfoldedComment attributes, e.g. commentIndex
      while (node.getAttributes().getLength() > 0) {
        Node attr = node.getAttributes().item(0);
        node.getAttributes().removeNamedItem(attr.getNodeName());
      }
      
      String commentContentAsXml = printNodeToString(node);
      Matcher m = Pattern.compile("\\s*<UnfoldedComment>(.*?)</UnfoldedComment>\\s*", Pattern.DOTALL).matcher(commentContentAsXml);
      if (!m.matches()) {
        throw new IllegalArgumentException("The root of the node must be '<UnfoldedComment>' tag");
      }
      String commentContent = m.group(1);
      
      // trim
      m = Pattern.compile("\\s*", Pattern.DOTALL).matcher(commentContent);
      if (m.matches()) {
        // no comment node needed after trimming
        return null;
      }
      
      return node.getOwnerDocument().createComment(commentContent);
    } else {
      // TODO log? return null? throw?
      throw new IllegalArgumentException();
    }
  }
  
  /**
   * Tries to parse a comment into a document, may fail
   * @param comment 
   * @return comment text as a document with {@code <UnfoldedComment>} root tag
   * @throws UnsupportedEncodingException
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  /*package*/static Node unfoldComment(Comment comment) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
    String commentContentAsXml = "<UnfoldedComment>" + comment.getTextContent() + "</UnfoldedComment>";
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
        new ByteArrayInputStream(commentContentAsXml.getBytes("UTF-8"))).getDocumentElement();
  }
  
  /**
   * 
   * @param node
   * @return new node which is the uncommented old
   */
  /*package*/static Node moveNodeFromUnfoldedComments(Node node) {
    Node unfoldedCommentRoot = node.getParentNode();
    
    if (unfoldedCommentRoot == null || !"UnfoldedComment".equals(((Element)unfoldedCommentRoot).getTagName())) {
      throw new IllegalArgumentException("The node must have 'UnfoldedComment' parent");
    }
    
    Node nodeClone = node.cloneNode(true);
    nodeClone = node.getOwnerDocument().importNode(nodeClone, true);
    
    Node unfoldedCommentParent = unfoldedCommentRoot.getParentNode();

    unfoldedCommentParent.insertBefore(nodeClone, unfoldedCommentRoot);
    unfoldedCommentRoot.removeChild(node);
    
    return nodeClone;
  }
  
  /*package*/static Node moveNodeToUnfoldedComments(Node node) {
    Node nodeClone = node.cloneNode(true);
    nodeClone = node.getOwnerDocument().importNode(nodeClone, true);
    
    Node unfoldedCommentRoot = node.getOwnerDocument().createElement("UnfoldedComment");
    unfoldedCommentRoot.appendChild(nodeClone);
    
    Node unfoldedCommentParent = node.getParentNode();
    
    unfoldedCommentParent.insertBefore(unfoldedCommentRoot, node);
    unfoldedCommentParent.removeChild(node);
    
    return nodeClone;
  }
  
  /*package*/static String printNodeToString(Node node) throws TransformerException, IOException {
    if (node == null) {
      return null;
    }
    
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//TODO obtain existing indent amount

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      transformer.transform(new DOMSource(node), new StreamResult(baos));
      
      try {
        return baos.toString("UTF-8");
      } catch (UnsupportedEncodingException e) {
        // impossible
        throw new RuntimeException(e);
      }
    }
  }
}

package org.jepria.tomcat.manager.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeUtils {
  
  public static List<Node> nodeListToList(NodeList nodeList) {
    List<Node> res = new ArrayList<>();

    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        res.add(nodeList.item(i));
      }
    }
    return res;
  }
  
  public static String nodeToString(Node node) {
    try {
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
    } catch (TransformerException | IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Returns the value of the node's attribute, if such attribute exists, or {@code null} if the attribute is missing.
   * <br/>
   * This is because the new w3c specification says to return an empty string for the missing attribute (instead of null),
   * but sometimes it is semantically correct to return {@code null} for missing attribute
   * @param el
   * @param attributeName
   * @return
   */
  public static String getAttributeOrNull(Element el, String attributeName) {
    String value = el.getAttribute(attributeName);
    if ("".equals(value)) {
      return el.hasAttribute(attributeName) ? value : null;
    } else {
      return value;
    }
  }
}


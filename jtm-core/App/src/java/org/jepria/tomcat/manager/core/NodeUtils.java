package org.jepria.tomcat.manager.core;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
  
  /**
   * For debugging purposes
   * @param n
   * @return
   */
  public static String printNode(Node node) {
    try {
      DOMSource domSource = new DOMSource(node);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}

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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeUtils {
  public static String printNodeToString(Node node) throws TransformerException, IOException {
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
  
  public static List<Node> nodeListToList(NodeList nodeList) {
    List<Node> res = new ArrayList<>();

    if (nodeList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        res.add(nodeList.item(i));
      }
    }
    return res;
  }
}

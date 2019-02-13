package org.jepria.tomcat.manager.core;

import java.util.ArrayList;
import java.util.List;

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
}

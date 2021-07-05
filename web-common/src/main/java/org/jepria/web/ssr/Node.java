package org.jepria.web.ssr;

import java.io.IOException;
import java.util.Objects;

public interface Node {
  void render(Appendable sb) throws IOException;
  
  static Node fromHtml(String html) {
    return new Node() {
      @Override
      public void render(Appendable sb) throws IOException {
        if (html != null) {
          sb.append(html);
        }
      }
    };
  }

  /**
   * 
   * @param nodes
   * @return a Node representing a sequence of Nodes (e.g. a node's child nodes as a single Node)  
   */
  static Node fromNodes(Node... nodes) {
    Objects.requireNonNull(nodes);
    
    return new Node() {
      @Override
      public void render(Appendable sb) throws IOException {
        for (Node node: nodes) {
          if (node != null) {
            node.render(sb);
          }
        }
      }
    };
  }

  /**
   * Prints the HTML of this node
   * @return
   * @throws IOException
   */
  default String printHtml() {
    StringBuilder sb = new StringBuilder();
    try {
      render(sb);
    } catch (IOException e) {
      throw new RuntimeException(e); // impossible
    }

    if (sb.length() > 0) {
      return sb.toString();
    } else {
      return null;
    }
  }
}

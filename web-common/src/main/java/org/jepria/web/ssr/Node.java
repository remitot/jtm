package org.jepria.web.ssr;

import java.io.IOException;

public interface Node {
  void render(Appendable sb) throws IOException;
  
  public static Node fromHtml(String html) {
    return new Node() {
      @Override
      public void render(Appendable sb) throws IOException {
        if (html != null) {
          sb.append(html);
        }
      }
    };
  }
}

package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface HtmlPageBuilder {
  void setTitle(String title);
  /**
   * Has the same effect as if {@code content} nodes were subsequently appended to the HTML &lt;body&gt; tag
   * @param content list of {@code body} child nodes 
   */
  void setContent(Iterable<? extends Node> content);
  /**
   * Has the same effect as if {@code content} node was appended to the HTML &lt;body&gt; tag
   * @param content a {@code body} child node 
   */
  void setContent(Node content);
  
  void setBodyAttributes(Map<String, String> attributes);
  /**
   * Subsequent pairs of {@code key1}, {@code value1}, {@code key2}, {@code value2}, ...  
   * @param attributes
   */
  void setBodyAttributes(String...attributes);
  
  Page build();
  
  public static HtmlPageBuilder newInstance(Context context) {
    return new HtmlPageBuilderImpl(context);
  }
  
  public interface Page {
    void print(PrintWriter out) throws IOException;
    void respond(HttpServletResponse response) throws IOException;
  }
  
}

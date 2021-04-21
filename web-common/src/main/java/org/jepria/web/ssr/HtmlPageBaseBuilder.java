package org.jepria.web.ssr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * Builder for base html pages
 */
public interface HtmlPageBaseBuilder {
  void setTitle(String title);
  
  // analogous methods getHead() and getRoot() might be supported, if necessary
  El getBody();
  
  Page build();
  
  public static HtmlPageBaseBuilder newInstance(Context context) {
    return new HtmlPageBaseBuilderImpl(context);
  }
  
  public interface Page {
    void print(PrintWriter out) throws IOException;
    void respond(HttpServletResponse response) throws IOException;
  }
  
}

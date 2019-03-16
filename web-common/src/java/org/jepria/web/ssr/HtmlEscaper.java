package org.jepria.web.ssr;

import java.io.IOException;

public class HtmlEscaper {
  public static void escapeAndWrite(String unescaped, Appendable out) throws IOException {
    if (unescaped != null) {
      for (int i = 0; i < unescaped.length(); i++) {
        char c = unescaped.charAt(i);
        
        if (c == '&') {
          out.append("&amp;");
        } else if (c == '<') {
          out.append("&lt;");
        } else if (c == '>') {
          out.append("&gt;");
        } else if (c == '"') {
          out.append("&quot;");
        } else if (c == '\'') {
          out.append("&#39;");
        } else if (c == ' ') {
          out.append("&nbsp;");
        } else if (c == '\t') {
          out.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        } else {
          out.append(c);
        }
      }
    }
  }
  
  public static String escape(String unescaped) {
    if (unescaped == null) {
      return null;
    }
    
    final StringBuilder sb = new StringBuilder();
    try {
      escapeAndWrite(unescaped, sb);
    } catch (IOException e) {
      // impossible: StringBuilder does not throw IOException
      throw new RuntimeException(e);
    }
    return sb.toString();
  }
}

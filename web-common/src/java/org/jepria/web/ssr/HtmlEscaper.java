package org.jepria.web.ssr;

import java.io.IOException;

public class HtmlEscaper {
  /**
   * Escape and write
   * @param unescaped
   * @param out to write the escaped string
   * @param escapeSpaces whether to replace regular spaces with {@code &nbsp;} or leave unescaped
   * @throws IOException
   */
  public static void escape(String unescaped, Appendable out, boolean escapeSpaces) throws IOException {
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
        } else if (escapeSpaces && c == ' ') {
          out.append("&nbsp;");
        } else if (c == '\t') {
          out.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        } else {
          out.append(c);
        }
      }
    }
  }
  
  /**
   *  
   * @param unescaped
   * @return
   */
  public static String escape(String unescaped) {
    return escape(unescaped, false);
  }
  
  /**
   * 
   * @param unescaped
   * @param escapeSpaces whether to escape regular spaces with {@code &nbsp;}
   * @return
   */
  public static String escape(String unescaped, boolean escapeSpaces) {
    if (unescaped == null) {
      return "&nbsp;";
    }
    
    final StringBuilder sb = new StringBuilder();
    try {
      escape(unescaped, sb, escapeSpaces);
    } catch (IOException e) {
      // impossible: StringBuilder does not throw IOException
      throw new RuntimeException(e);
    }
    return sb.toString();
  }
}

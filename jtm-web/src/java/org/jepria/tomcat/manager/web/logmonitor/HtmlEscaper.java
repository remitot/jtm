package org.jepria.tomcat.manager.web.logmonitor;

import java.io.IOException;
import java.io.Writer;

public class HtmlEscaper {
  public static void escapeAndWrite(String unescaped, Writer writer) throws IOException {
    if (unescaped != null) {
      for (int i = 0; i < unescaped.length(); i++) {
        char c = unescaped.charAt(i);
        
        if (c == '&') {
          writer.write(new char[]{'&','a','m','p',';'}); // &amp;
        } else if (c == '<') {
          writer.write(new char[]{'&','l','t',';'}); // &lt;
        } else if (c == '>') {
          writer.write(new char[]{'&','g','t',';'}); // &gt;
        } else if (c == '"') {
          writer.write(new char[]{'&','q','u','o','t',';'}); // &quot;
        } else if (c == '\'') {
          writer.write(new char[]{'&','#','3','9',';'}); // &#39;
        } else if (c == ' ') {
          writer.write(new char[]{'&','n','b','s','p',';'}); // &nbsp;
        } else if (c == '\t') {
          writer.write(new char[]{
              '&','n','b','s','p',';',
              '&','n','b','s','p',';',
              '&','n','b','s','p',';',
              '&','n','b','s','p',';'}); // &nbsp;&nbsp;&nbsp;&nbsp;
        } else {
          writer.write(c);
        }
      }
    }
  }
}

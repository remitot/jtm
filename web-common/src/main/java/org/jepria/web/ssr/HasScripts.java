package org.jepria.web.ssr;

import java.util.ArrayList;
import java.util.List;

public interface HasScripts {
  
  /**
   * Adds a {@code js} script specific to this element.
   */
  void addScript(Script script);
  
  static class Script {
    /**
     * relative path (same as {@code src} attribute value of a {@code <script>} tag) 
     */
    public final String src;
    /**
     * names of functions in the script to invoke on {@code <body onload>} subsequently
     */
    public final Iterable<String> onloadFuntions;
    
    public Script(String src) {
      this(src, (Iterable<String>)null);
    }
    
    public Script(String src, String...onloadFunctions) {
      this(src, toIterable(onloadFunctions));
    }

    public Script(String src, Iterable<String> onloadFuntions) {
      this.src = src;
      this.onloadFuntions = onloadFuntions;
    }
    
    private static Iterable<String> toIterable(String...strings) {
      if (strings == null) {
        return null;
      }
      List<String> list = new ArrayList<>();
      for (String s: strings) {
        if (s != null) {
          list.add(s);
        }
      }
      return list;
    }
  }
}

package org.jepria.web.ssr.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * A {@link CheckBox} contained within a {@link Table}, provides a table-specific script.
 */
public class TableCheckBox extends CheckBox {
  public TableCheckBox(boolean active) {
    super(active);
  }
  
  @Override
  protected String getScript() throws IOException {
    final String superScript = super.getScript(); 
    
    final String script;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = TableCheckBox.class.getClassLoader(); // fallback
    }
    final String resourceName 
        = TableCheckBox.class.getCanonicalName() // org.jepria.web.ssr.table.TableCheckBox
        .replaceAll("\\.", "/") + ".js"; // org/jepria/web/ssr/table/TableCheckBox.js
    
    try (InputStream in = classLoader.getResourceAsStream(resourceName);
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        script = sc.next();
      } else {
        script = null;
      }
    }
    
    return superScript + "\n\n" + script;
  }
}

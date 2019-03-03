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
    classList.add("table__checkbox");
  }
  
  @Override
  protected void addScript(Scripts scripts) throws IOException {
    super.addScript(scripts);
    
    
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
        scripts.add(sc.next());
      }
    }
  }
}

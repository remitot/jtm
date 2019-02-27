package org.jepria.tomcat.manager.web.jdbc.ssr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class El {
  
  protected final String tag;
  
  protected final Map<String, Optional<String>> attributes = new HashMap<>();
  
  protected final List<El> childs = new ArrayList<>();
  
  public final Set<String> classList = new HashSet<>();
  
  public String innerHTML;
  
  public El(String tag) {
    this.tag = tag;
  }
  
  public void setAttribute(String name, Object value) {
    attributes.put(name, value == null ? Optional.empty() : Optional.of(String.valueOf(value)));
  }

  public void appendChild(El child) {
    childs.add(child);
  }
  
  
  
}

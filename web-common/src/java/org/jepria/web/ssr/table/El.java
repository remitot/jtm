package org.jepria.web.ssr.table;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class El {
  
  public final String tag;
  
  public final Map<String, Optional<String>> attributes = new HashMap<>();
  
  public final List<El> childs = new ArrayList<>();
  
  public final Set<String> classList = new HashSet<>();
  
  private String innerHTML;
  
  public El(String tag) {
    this.tag = tag;
  }
  
  /**
   * Sets attribute with no value (name only)
   * @param name
   */
  public void setAttribute(String name) {
    attributes.put(name, Optional.empty());
  }
  
  /**
   * 
   * @param name
   * @param value {@code null} removes the attribute
   */
  public void setAttribute(String name, Object value) {
    if ("class".equals(name)) {
      classList.clear();
      if (value != null) {
        if (value instanceof String) {
          // parse classnames
          String valueStr = (String)value;
          String[] split = valueStr.split("\\s+");
          for (String className: split) {
            classList.add(className);
          }
        } else {
          // abnormal case: set as regular attribute
          setAttributeRegular(name, value);
        }
      }
    } else {
      setAttributeRegular(name, value);
    }
  }
  
  private void setAttributeRegular(String name, Object value) {
    if (value != null) {
      attributes.put(name, Optional.of(String.valueOf(value)));
    } else {
      attributes.remove(name);
    }
  }

  public void appendChild(El child) {
    childs.add(child);
  }
  
  public void setInnerHTML(String innerHTML) {
    this.innerHTML = innerHTML;
    // setting innerHTML destroys the children
    childs.clear();
  }

  public String getInnerHTML() {
    return innerHTML;
  }
  
  /**
   * 
   * @return {@code true} if to short-close empty tag while {@link #printHtml(Appendable)}: {@code <empty/>};
   * {@code false} if to full-close empty tags on {@link #printHtml(Appendable)}: {@code <empty></empty>}.
   * <br/>
   * <strong>Note:</strong> the Chrome browser fails rendering html if there are short-closed tags present. So, the default value is {@code false}.  
   */
  protected boolean allowShortClose() {
    return false;
  }
  
  /**
   * Prints the entire html DOM tree of this element and its children recursively
   * @param sb
   * @throws IOException
   */
  // TODO escape HTML
  public void printHtml(Appendable sb) throws IOException {
    sb.append('<').append(tag);
    
    if (!classList.isEmpty()) {
      sb.append(' ').append("class=\"");
      boolean first = true;
      for (String className: classList) {
        if (!first) {
          sb.append(' ');
        } else {
          first = false;
        }
        sb.append(className);
      }
      sb.append('"');
    }
    
    if (!attributes.isEmpty()) {
      for (Map.Entry<String, Optional<String>> attribute: attributes.entrySet()) {
        sb.append(' ').append(attribute.getKey());
        if (attribute.getValue().isPresent()) {
          String value = attribute.getValue().get();
          sb.append('=').append('"').append(value).append('"');
        }
      }
    }
    
    if (innerHTML == null && childs.isEmpty() && allowShortClose()) {
      // empty body
      sb.append("/>");
      
    } else {
    
      sb.append('>');
      
      if (innerHTML != null) {
        // innerHTML comes first
        sb.append(innerHTML);
      }
      
      if (!childs.isEmpty()) {// recursively
        for (El child: childs) {
          child.printHtml(sb);
        }
      }
      
      sb.append('<').append('/').append(tag).append('>');
    }
    
  }
  
  /**
   * Prints the entire html DOM tree of this element and its children recursively
   * @return
   * @throws IOException
   */
  public String printHtml() throws IOException {
    StringBuilder sb = new StringBuilder();
    printHtml(sb);
    return sb.toString();
  }
  
  /**
   * Prints all scripts related to this elements and its children recursively
   * @param sb
   * @throws IOException
   */
  public void printScript(Appendable sb) throws IOException {
    Set<String> scripts = new HashSet<>();
    addScript(scripts);
    
    // remove scripts contained in others
    Iterator<String> it = scripts.iterator();
    while (it.hasNext()) {
      String script = it.next();
      if (scripts.stream().anyMatch(anotherScript -> 
          anotherScript != script && anotherScript.contains(script))) {
        it.remove();
      }
    }
    
    // print
    boolean first = true;
    for (String script: scripts) {
      if (!first) {
        sb.append("\n\n\n");// TODO replace with os-dependent newline
      } else {
        first = false;
      }
      sb.append(script);
    }
  }
  
  /**
   * Prints all scripts related to this elements and its children recursively
   * @return
   * @throws IOException
   */
  public String printScript() throws IOException {
    StringBuilder sb = new StringBuilder();
    printScript(sb);
    return sb.toString();
  }
  
  /**
   * Adds all scripts related to this elements and its children recursively to the set
   * @param scripts
   * @throws IOException
   */
  protected void addScript(Set<String> scripts) throws IOException {
    String script = getScript();
    if (script != null) {
      scripts.add(script);
    }
    for (El child: childs) {
      child.addScript(scripts);
    }
  }
  
  /**
   * @return script related to this element only, or else {@code null}
   * @throws IOException
   */
  protected String getScript() throws IOException {
    return null;
  }
}

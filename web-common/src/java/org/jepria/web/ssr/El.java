package org.jepria.web.ssr;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jepria.web.ssr.table.Collection;

public class El implements Node {
  
  public String tagName;
  
  public final Map<String, Optional<String>> attributes = new HashMap<>();
  
  public final List<Node> childs = new ArrayList<>();
  
  public final Set<String> classList = new LinkedHashSet<String>();
  
  private String innerHTML;
  
  public El(String tag) {
    this.tagName = tag;
  }
  
  public El() {
    
  }
  
  public void setEnabled(boolean enabled) {
    
  }
  
  public void setReadonly(boolean readonly) {
    if (readonly) {
      classList.add("readonly");
    } else {
      classList.remove("readonly");
    }
  }
  
  /**
   * 
   * @param className
   * @return {@code this}
   */
  public El addClass(String className) {
    classList.add(className);
    return this;
  }
  
  /**
   * Sets attribute with no value (name only)
   * @param name
   * @return {@code this}
   */
  public El setAttribute(String name) {
    attributes.put(name, Optional.empty());
    return this;
  }
  
  /**
   * 
   * @param name
   * @param value {@code null} removes the attribute
   * @return {@code this}
   */
  public El setAttribute(String name, Object value) {
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
    return this;
  }
  
  /**
   * 
   * @param name
   * @param value
   * @return {@code this}
   */
  private El setAttributeRegular(String name, Object value) {
    if (value != null) {
      attributes.put(name, Optional.of(String.valueOf(value)));
    } else {
      attributes.remove(name);
    }
    return this;
  }

  /**
   * 
   * @param child
   * @return {@code this}
   */
  public El appendChild(Node child) {
    childs.add(child);
    return this;
  }
  
  /**
   * @param innerHTML
   * @return {@code this}
   */
  public El setInnerHTML(String innerHTML) {
    this.innerHTML = innerHTML;
    // setting innerHTML destroys the children
    childs.clear();
    return this;
  }

  public String getInnerHTML() {
    return innerHTML;
  }
  
  /**
   * 
   * @return {@code true} if to short-close empty tag while {@link #render(Appendable)}: {@code <empty/>};
   * {@code false} if to full-close empty tags on {@link #render(Appendable)}: {@code <empty></empty>}.
   * <br/>
   * <strong>Note:</strong> the Chrome browser fails rendering html if there are short-closed tags present. So, the default value is {@code false}.  
   */
  protected boolean allowShortClose() {
    return false;
  }
  
  /**
   * Render the entire HTML DOM tree of this element and its children recursively
   * @param sb
   * @throws IOException
   */
  // TODO escape HTML
  @Override
  public void render(Appendable sb) throws IOException {
    
    Objects.requireNonNull(tagName);
    
    sb.append('<').append(tagName);
    
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
        for (Node child: childs) {
          child.render(sb);
        }
      }
      
      sb.append('<').append('/').append(tagName).append('>');
    }
    
  }
  
  /**
   * Prints the HTML of this node
   * @return
   * @throws IOException
   */
  public String printHtml() throws IOException {
    StringBuilder sb = new StringBuilder();
    render(sb);
    
    if (sb.length() > 0) {
      return sb.toString();
    } else {
      return null;
    }
  }
  
  /**
   * Prints all scripts related to this elements and its children recursively
   * @param sb
   * @throws IOException
   */
  public void printScripts(Appendable sb) throws IOException {
    // LinkedHashSet to maintain (parent,descendant) order of adding
    // (if super.addScript is invoked first in elements)
    Set<String> scripts = new LinkedHashSet<>();
    
    collectScripts(new Collection() {
      @Override
      public void add(String script) {
        scripts.add(script);
      }
    });
    
    // remove items contained in others
    Iterator<String> it = scripts.iterator();
    while (it.hasNext()) {
      String script = it.next();
      if (scripts.stream().anyMatch(anotherItem -> 
          anotherItem != script && anotherItem.contains(script))) {
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
   * Collect all scripts of this element and its children recursively
   * 
   * @param scripts a collection to add a script to, not null
   * @throws IOException 
   */
  // private final: not for overriding or direct invocation
  private final void collectScripts(Collection scripts) throws IOException {
    addScripts(scripts);
    for (Node child: childs) {
      if (child instanceof El) {
        ((El)child).collectScripts(scripts);
      }
    }
  }
  
  /**
   * Adds a script (none, single or multiple), specific to this element or elements class 
   * 
   * @param scripts a collection to add a script to, not null
   * @throws IOException
   */
  protected void addScripts(Collection scripts) throws IOException {
  }
  
  /**
   * Prints all scripts related to this elements and its children recursively
   * @return
   * @throws IOException
   */
  public String printScripts() throws IOException {
    StringBuilder sb = new StringBuilder();
    printScripts(sb);
    
    if (sb.length() > 0) {
      return sb.toString();
    } else {
      return null;
    }
  }
  
  /**
   * Prints all styles related to this elements and its children recursively
   * @param sb
   * @throws IOException
   */
  public void printStyles(Appendable sb) throws IOException {
    // LinkedHashSet to maintain (parent,descendant) order of adding
    // (if super.addScript is invoked first in elements)
    Set<String> styles = new LinkedHashSet<>();
    
    collectStyles(new Collection() {
      @Override
      public void add(String item) {
        styles.add(item);
      }
    });
    
    // remove items contained in others
    Iterator<String> it = styles.iterator();
    while (it.hasNext()) {
      String style = it.next();
      if (styles.stream().anyMatch(anotherItem -> 
          anotherItem != style && anotherItem.contains(style))) {
        it.remove();
      }
    }
    
    // print
    boolean first = true;
    for (String style: styles) {
      if (!first) {
        sb.append("\n\n\n");// TODO replace with os-dependent newline
      } else {
        first = false;
      }
      sb.append(style);
    }
  }
  
  /**
   * Collect all styles of this element and its children recursively
   * 
   * @param styles a collection to add a style to, not null
   * @throws IOException 
   */
  // private final: not for overriding or direct invocation
  private final void collectStyles(Collection styles) throws IOException {
    addStyles(styles);
    for (Node child: childs) {
      if (child instanceof El) {
        ((El)child).collectStyles(styles);
      }
    }
  }
  
  /**
   * Adds a style (none, single or multiple), specific to this element or elements class 
   * 
   * @param styles a collection to add a style to, not null
   * @throws IOException
   */
  protected void addStyles(Collection styles) throws IOException {
  }
  
  /**
   * Prints all styles related to this elements and its children recursively
   * @return
   * @throws IOException
   */
  public String printStyles() throws IOException {
    StringBuilder sb = new StringBuilder();
    printStyles(sb);
    
    if (sb.length() > 0) {
      return sb.toString();
    } else {
      return null;
    }
  }
}

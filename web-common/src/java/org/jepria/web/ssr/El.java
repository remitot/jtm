package org.jepria.web.ssr;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class El implements Node {
  
  public String tagName;
  
  public final Map<String, Optional<String>> attributes = new HashMap<>();
  
  public final List<Node> childs = new ArrayList<>();
  
  public final Set<String> classList = new LinkedHashSet<String>();
  
  private String innerHTML;
  
  public final Context context;
  
  public El(String tag, Context context) {
    this.tagName = tag;
    this.context = context;
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
   * @return {@code this}
   */
  public El removeAttribute(String name) {
    attributes.remove(name);
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
      String valueStr = String.valueOf(value);
      String valueEsc = HtmlEscaper.escape(valueStr, false); // attribute values are always escaped
      attributes.put(name, Optional.of(valueEsc));
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
    return setInnerHTML(innerHTML, false);
  }
  
  /**
   * Set inner HTML of the element
   * @param innerHTML
   * @param escape {@code true} if the innerHTML parameter is not a trusted safe HTML string and needs escaping before setting,
   * {@code false} will set the innerHTML value as-is
   * @return {@code this}
   */
  public El setInnerHTML(String innerHTML, boolean escape) {

    final String innerHTMLEscaped = escape ? HtmlEscaper.escape(innerHTML) : innerHTML;
    
    this.innerHTML = innerHTMLEscaped;
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
  
  public void classListToAttribute() {
    if (!classList.isEmpty()) {
      StringBuilder sb = new StringBuilder();

      boolean first = true;
      for (String className: classList) {
        if (!first) {
          sb.append(' ');
        } else {
          first = false;
        }
        sb.append(className);
      }
      
      classList.clear();
      
      attributes.put("class", Optional.of(sb.toString()));
    }
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
    
    classListToAttribute();
    
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
   * Element's own scripts (not its descendant's)
   */
  private final Set<String> ownScripts = new LinkedHashSet<>();
  
  /**
   * Get all scripts related to this elements and its children recursively
   * @param sb
   */
  public Set<String> getScripts() {
    final Set<String> scripts = new LinkedHashSet<>();
    collectScripts(scripts);
    return scripts;
  }
  
  /**
   * Collect all scripts of this element and its children recursively
   * 
   * @param scripts a collection to add a script to, not null
   */
  // private final: not for overriding or direct invocation
  private final void collectScripts(Set<String> scripts) {
    scripts.addAll(ownScripts);
    for (Node child: childs) {
      if (child instanceof El) {
        ((El)child).collectScripts(scripts);
      }
    }
  }
  
  /**
   * Adds a .js script specific to this element or element class.
   * A script is added by its relative path (same as {@code src} attribute value of a {@code <script>} tag) 
   */
  public void addScript(String script) {
    if (script != null) {
      this.ownScripts.add(script);
    }
  }
  
  
  
  
  
  /**
   * Element's own styles (not its descendant's)
   */
  private final Set<String> ownStyles = new LinkedHashSet<>();
  
  /**
   * Get all styles related to this elements and its children recursively
   * @return
   */
  public Set<String> getStyles() {
    final Set<String> styles = new LinkedHashSet<>();
    collectStyles(styles);
    return styles;
  }
  
  /**
   * Collect all styles of this element and its children recursively
   * 
   * @param styles a collection to add a style to, not null
   */
  // private final: not for overriding or direct invocation
  private final void collectStyles(Set<String> styles) {
    styles.addAll(ownStyles);
    for (Node child: childs) {
      if (child instanceof El) {
        ((El)child).collectStyles(styles);
      }
    }
  }
  
  /**
   * Adds a .css style specific to this element or element class.
   * A style is added by its relative path (same as {@code href} attribute value of a {@code <link rel="stylesheet">} tag) 
   */
  public void addStyle(String style) {
    if (style != null) {
      this.ownStyles.add(style);
    }
  }
}

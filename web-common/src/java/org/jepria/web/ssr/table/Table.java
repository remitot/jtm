package org.jepria.web.ssr.table;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.jepria.web.Dto;
import org.jepria.web.ssr.El;

public abstract class Table<T extends Dto> extends El {
  
  private int tabIndexValue;
  
  public static interface TabIndex {
    /**
     * Assigns the next {@code tabindex} to the element
     * @param el
     */
    void setNext(El el);
  }
  
  public Table() {
    super("div");
    
    // TODO remove, use class and css
    setAttribute("id", "table");
    setAttribute("style", "width: 100%;");
  }
  
  protected boolean isEditable() {
    return true;
  }
  
  /**
   * Clear the table and load new items 
   * (these items will be treated as 'original' against the overlaying provided by {@link #overlay(List)})
   * 
   * @param items
   * @param nextTabIndexOutref reference to store the next proper {@code tabindex} value after load
   */
  public void load(List<T> items) {
    
    // reset
    childs.clear();
    tabIndexValue = 1;
    
    
    TabIndex tabIndex = new TabIndex() {
      @Override
      public void setNext(El el) {
        el.setAttribute("tabindex", tabIndexValue++);
      }
    };
     
    if (items != null && !items.isEmpty()) {
      
      appendChild(createHeader());
      
      for (T item: items) {
        appendChild(createRow(item, tabIndex));
      }
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  /**
   * Creates a table row representing a single item
   * @param item
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  protected abstract El createRow(T item, TabIndex tabIndex);
  
  protected El addField(El cell, String name, String value, String placeholder) {
    El field = createField(name, value, placeholder);
    
    El wrapper = wrapCellPad(field);
    cell.appendChild(wrapper);

    if (isEditable()) {
      addStrike(cell);
    }
      
    return field;
  }
  
  protected El createField(String name, String value, String placeholder) {
    if (isEditable()) {
      return createFieldInput(name, value, placeholder);
    } else {
      return createFieldLabel(value);
    }
  }

  protected El createFieldInput(String name, String value, String placeholder) {
    El field = new El("input");
    field.setAttribute("type", "text");
    field.setAttribute("name", name);
    field.setAttribute("value", value);
    field.setAttribute("placeholder", placeholder);
    
    field.classList.add("field-text");
    field.classList.add("inactivatible");
    field.classList.add("deletable");
    
    return field;
  }

  protected void addStrike(El cell) {
    El strike = new El("div");
    strike.classList.add("strike");
    cell.appendChild(strike);
  }

  protected El createFieldLabel(String value) {
    El field = new El("label");
    field.setInnerHTML(value);
    
    field.classList.add("field-text");
    field.classList.add("inactivatible");
    field.classList.add("deletable");
    
    return field;
  }

  protected El wrapCellPad(El element) {
    El wrapper = new El("div");
    
    El leftDiv = new El("div");
    leftDiv.classList.add("cell-pad-left");
    wrapper.appendChild(leftDiv);
    
    El rightDiv = new El("div");
    rightDiv.classList.add("cell-pad-right");
    wrapper.appendChild(rightDiv);
    
    El midDiv = new El("div");
    midDiv.classList.add("cell-pad-mid");
    wrapper.appendChild(midDiv);
    
    midDiv.appendChild(element);
    
    return wrapper;
  }
  
  protected El addFieldDelete(El cell) {

    El button = new El("input");
    button.classList.add("field-delete");
    
    button.setAttribute("type", "image");
    button.setAttribute("src", "gui/img/delete.png");
    button.setAttribute("title", "Удалить"); // NON-NLS
    
    El wrapper = wrapCellPad(button);  
    cell.appendChild(wrapper);
    
    return button;
  }
  
  protected El createCell(El row, String columnClass) {
    El cell = new El("div");
    cell.classList.add("cell");
    cell.classList.add(columnClass);
    row.appendChild(cell);
    return cell;
  }
  
  protected boolean hasCheckboxes = false;
  
  protected CheckBox addCheckbox(El cell, boolean active, boolean enabled) {
    CheckBox checkbox = new CheckBox(active);
    checkbox.classList.add("table__checkbox");
    
    checkbox.setEnabled(enabled);
    
    checkbox.classList.add("deletable");

    El wrapper = wrapCellPad(checkbox);  
    cell.appendChild(wrapper);
    addStrike(cell);
    
    hasCheckboxes = true;
    return checkbox;
  }
  
  protected abstract El createHeader();
  
  @Override
  protected void addScripts(Collection scripts) throws IOException {
    super.addScripts(scripts);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = Table.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/table/table.js");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        scripts.add(sc.next());
      }
    }
    
    
    if (hasCheckboxes) {
      try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/table/table__checkbox.js");
          Scanner sc = new Scanner(in, "UTF-8")) {
        sc.useDelimiter("\\Z");
        if (sc.hasNext()) {
          scripts.add(sc.next());
        }
      }
    }
  }
  
  @Override
  protected void addStyles(Collection styles) throws IOException {
    super.addStyles(styles);
    
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = Table.class.getClassLoader(); // fallback
    }

    
    try (InputStream in = classLoader.getResourceAsStream("org/jepria/web/ssr/table/table.css");
        Scanner sc = new Scanner(in, "UTF-8")) {
      sc.useDelimiter("\\Z");
      if (sc.hasNext()) {
        styles.add(sc.next());
      }
    }
  }
}

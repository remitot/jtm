package org.jepria.web.ssr.table;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jepria.web.ssr.El;

public abstract class Table<T extends ItemData> extends El {
  
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
   * 
   * @param items the 'original' items
   * @param itemsCreated optional items to render {@code .created} table rows, may be null
   * @param itemsModified optional items ({@code Map<item.id, item>}) to render {@code .modified} table rows, may be null
   * @param itemsDeleted optional item (set of {@link Dto#id}) to render {@code .deleted} table rows, may be null
   */
  public void load(List<T> items, List<T> itemsCreated, Map<String, T> itemsModified, Set<String> itemsDeleted) {
    
    // reset
    childs.clear();
    tabIndexValue = 1;
    
    
    final TabIndex tabIndex = new TabIndex() {
      @Override
      public void setNext(El el) {
        el.setAttribute("tabindex", tabIndexValue++);
      }
    };
    
    
    appendChild(createHeader());
    
    if (items != null) {
      for (T item: items) {
        final El row;
        final String itemId = item.getId();
        if (itemsDeleted != null && itemsDeleted.contains(itemId)) {
          row = createRowDeleted(item, tabIndex);
        } else {
          final T itemModified = itemsModified.get(itemId);
          if (itemModified != null) {
            // merge items
            for (String name: item.keySet()) {
              Field fieldModified = itemModified.get(name);
              if (fieldModified != null) {
                Field field = item.get(name);
                field.value = fieldModified.value;
              }
            }
            row = createRowModified(item, tabIndex);
          } else {
            row = createRow(item, tabIndex);
          }
        }
        
        row.setAttribute("item-id", item.getId());
        
        appendChild(row);
      }
    }
    if (itemsCreated != null) {
      for (T item: itemsCreated) {
        appendChild(createRowCreated(item, tabIndex));
      }
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  /**
   * Creates a table row (in its original: non-modified, non-deleted state) representing a single item
   * @param item data from the server, non-null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  public abstract El createRow(T item, TabIndex tabIndex);
  
  /**
   * Creates a table row (in created state) representing a single item, that has not been saved to the server yet,
   * but having a UI table row created for saving
   * @param item optional data from the UI to fill the created table row with, may be null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  public abstract El createRowCreated(T item, TabIndex tabIndex);
  
  /**
   * Creates a table row (in deleted state) representing a single item, that is still present on the server,
   * and having the corresponding UI table row deleted
   * @param item data from the server, non-null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  public abstract El createRowDeleted(T item, TabIndex tabIndex);
  
  /**
   * Creates a table row (in modified state) representing a single item, that is both present on the server,
   * and having the corresponding UI table row data modified
   * @param itemOriginal original data from the server, non-null
   * @param item optional data from the UI to overlay the original data with, may be null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  public abstract El createRowModified(T item, TabIndex tabIndex);
  
  protected El addField(El cell, Field field, String placeholder) {
    El fieldEl = createField(field.name, field.value, placeholder);
    
    if (field.readonly) {
      setFieldReadonly(fieldEl);
    } else {
      fieldEl.setAttribute("value-original", field.valueOriginal);
    }
    
    El wrapper = wrapCellPad(fieldEl);
    cell.appendChild(wrapper);

    if (isEditable()) {
      addStrike(cell);
    }
      
    return fieldEl;
  }
  
  protected void setFieldReadonly(El field) {
    field.setReadonly(true);
    field.setAttribute("readonly", "true");
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
  
  protected void addFieldDelete(El cell, TabIndex tabIndex) {

    El field = new El("div");
    
    El buttonDelete = new El("input");
    buttonDelete.classList.add("button-delete");
    buttonDelete.classList.add("button-delete_delete");
    buttonDelete.setAttribute("type", "image");
    buttonDelete.setAttribute("src", "gui/img/delete.png");
    buttonDelete.setAttribute("title", "Удалить"); // NON-NLS
    tabIndex.setNext(buttonDelete);
    
    El buttonUndelete = new El("input");
    buttonUndelete.classList.add("button-delete");
    buttonUndelete.classList.add("button-delete_undelete");
    buttonUndelete.setAttribute("type", "image");
    buttonUndelete.setAttribute("src", "gui/img/undelete.png");
    buttonUndelete.setAttribute("title", "Не удалять"); // NON-NLS
    tabIndex.setNext(buttonUndelete);
    
    field.appendChild(buttonDelete);
    field.appendChild(buttonUndelete);
    
    El wrapper = wrapCellPad(field);  
    cell.appendChild(wrapper);
    
    //return field;
  }
  
  protected El createCell(El row, String columnClass) {
    El cell = new El("div");
    cell.classList.add("cell");
    cell.classList.add(columnClass);
    row.appendChild(cell);
    return cell;
  }
  
  protected boolean hasCheckboxes = false;
  
  protected CheckBox addCheckbox(El cell, Field field) {
    
    boolean active = !"false".equals(field.value);
    CheckBox checkbox = new CheckBox(active);
    
    checkbox.setEnabled(!field.readonly);

    if (field.readonly) {
      checkbox.setReadonly(true);
    } else {
      checkbox.setAttribute("value-original", !"false".equals(field.valueOriginal));
    }
    
    checkbox.classList.add("table__checkbox");
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

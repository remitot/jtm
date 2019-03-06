package org.jepria.web.ssr.table;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
   * 
   * @param items the 'original' items
   * @param itemsOverlay optional items to overlay 'original' items with (by {@link Dto#id}) 
   * to render {@code .created} or {@code .modified} table rows, may be null
   * @param itemsDeletedIds optional item ids (refs to {@link Dto#id}) to render {@code .deleted} table rows, may be null
   */
  public void load(List<T> items, List<T> itemsOverlay, Set<String> itemsDeletedIds) {
    
    // reset
    childs.clear();
    tabIndexValue = 1;
    
    
    final TabIndex tabIndex = new TabIndex() {
      @Override
      public void setNext(El el) {
        el.setAttribute("tabindex", tabIndexValue++);
      }
    };
    
    
    // Map<Dto.id, Dto>
    final Map<String, T> itemsModified = new HashMap<>();
    final List<T> itemsCreated = new ArrayList<>();
    if (itemsOverlay != null) {
      for (T item: itemsOverlay) {
        String id = item.getId(); 
        if (id != null) {
          itemsModified.put(id, item);
        } else {
          // TODO safe to treat every non-id dto as created?
          itemsCreated.add(item);
        }
      }
    }
    
    
    appendChild(createHeader());
    
    if (items != null) {
      for (T item: items) {
        final El row;
        final String itemId = item.getId();
        if (itemsDeletedIds != null && itemsDeletedIds.contains(itemId)) {
          row = createRowDeletedInternal(item, tabIndex);
        } else {
          final T itemModified = itemsModified.get(itemId);
          if (itemModified != null) {
            row = createRowModifiedInternal(item, itemModified, tabIndex);
          } else {
            row = createRowInternal(item, tabIndex);
          }
        }
        appendChild(row);
      }
    }
    for (T item: itemsCreated) {
      appendChild(createRowCreated(item, tabIndex));
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  private El createRowInternal(T item, TabIndex tabIndex) {
    El row = createRow(item, tabIndex);
    row.setAttribute("item-id", item.getId());
    return row;
  }
  
  private El createRowDeletedInternal(T item, TabIndex tabIndex) {
    El row = createRowDeleted(item, tabIndex);
    row.setAttribute("item-id", item.getId());
    return row;
  }
  
  private El createRowModifiedInternal(T itemOriginal, T item, TabIndex tabIndex) {
    El row = createRowModified(itemOriginal, item, tabIndex);
    row.setAttribute("item-id", item.getId());
    return row;
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
  public abstract El createRowModified(T itemOriginal, T item, TabIndex tabIndex);
  
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
    buttonUndelete.setAttribute("title", "Удалить"); // NON-NLS
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

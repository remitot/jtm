package org.jepria.web.ssr.table;


import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public abstract class Table<T extends ItemData> extends El {
  
  private int tabIndexValue;
  
  public static interface TabIndex {
    /**
     * Assigns the next {@code tabindex} to the element
     * @param el
     */
    void setNext(El el);
  }
  
  protected final Text text;
  
  public Table(Text text) {
    super("div");
    this.text = text;
    
    // TODO remove id, use class and css
    setAttribute("id", "table");
    setAttribute("style", "width: 100%;");
    
    
    addScript("js/table.js");
    addStyle("css/table.css");
    addStyle("css/common.css"); // for .field-text
  }
  
  protected boolean isEditable() {
    return true;
  }
  
  /**
   * Clear the table and load new items 
   * 
   * @param items basic table items, possibly modified
   * @param itemsCreated optional items to render {@code .created} table rows, may be null
   * @param itemsDeleted optional item (set of {@link Dto#id}) to render {@code .deleted} table rows, may be null
   */
  public void load(List<T> items, List<T> itemsCreated, Set<String> itemsDeleted) {
    
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
      
      boolean evenOddGray = true; // for unmodifiable table
      
      for (T item: items) {
        final String itemId = item.getId();
        
        final El row = createRow(item, tabIndex);
        
        if (!isEditable()) {
          if (evenOddGray) {
            row.addClass("even-odd-gray");
          }
          evenOddGray = !evenOddGray;
        }
        
        if (itemsDeleted != null && itemsDeleted.contains(itemId)) {
          row.classList.add("deleted");
        } else {
          // check any field modified
          if (item.values().stream().anyMatch(
              field -> !Objects.equals(field.value, field.valueOriginal))) {
            row.classList.add("modified");
          }
        }
        
        row.setAttribute("item-id", item.getId());
        
        appendChild(row);
      }
    }
    if (itemsCreated != null) {
      for (T item: itemsCreated) {
        final El row = createRowCreated(item, tabIndex);
        row.setAttribute("item-id", item.getId());
        appendChild(row);
      }
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  /**
   * Create a basic table row (possibly modified) representing a single item
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
  
  protected El addField(El cell, Field field, String placeholder) {
    return addField(cell, field, placeholder, isEditable());
  }
  
  protected El addField(El cell, Field field, String placeholder, boolean fieldEditable) {
    final El fieldEl;
    if (fieldEditable) {
      fieldEl = createFieldInput(field.name, field.value, placeholder);
    } else {
      fieldEl = createFieldLabel(field.value);
    }
    
    return addField(cell, field, fieldEl, placeholder);
  }
  
  protected El addField(El cell, Field field, El fieldEl, String placeholder) {
    
    fieldEl.classList.add("field-text");
    fieldEl.classList.add("field-text_inactivatible");
    fieldEl.classList.add("disableable");
    
    if (field.readonly) {
      fieldEl.setReadonly(true);
    } else {
      fieldEl.setAttribute("value-original", field.valueOriginal);
    }
    
    if (field.invalid) {
      fieldEl.classList.add("invalid");
      if (field.invalidMessage != null) {
        fieldEl.setAttribute("title", field.invalidMessage);
      }
    }
    
    El wrapper = wrapCellPad(fieldEl);
    cell.appendChild(wrapper);

    if (isEditable()) {
      addStrike(cell);
    }
      
    return fieldEl;
  }
  
  protected El createFieldInput(String name, String value, String placeholder) {
    El field = new El("input");
    field.setAttribute("type", "text");
    field.setAttribute("name", name);
    field.setAttribute("value", value);
    field.setAttribute("placeholder", placeholder);
    return field;
  }

  protected void addStrike(El cell) {
    El strike = new El("div");
    strike.classList.add("strike");
    cell.appendChild(strike);
  }

  protected El createFieldLabel(String value) {
    El field = new El("label");
    field.setInnerHTML(value, true);
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
    buttonDelete.setAttribute("src", "img/delete.png");
    buttonDelete.setAttribute("title", text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete"));
    tabIndex.setNext(buttonDelete);
    
    El buttonUndelete = new El("input");
    buttonUndelete.classList.add("button-delete");
    buttonUndelete.classList.add("button-delete_undelete");
    buttonUndelete.setAttribute("type", "image");
    buttonUndelete.setAttribute("src", "img/undelete.png");
    buttonUndelete.setAttribute("title", text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete"));
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
    
    checkbox.classList.add("disableable");

    if (field.invalid) {
      checkbox.classList.add("invalid");
      if (field.invalidMessage != null) {
        checkbox.setAttribute("title", field.invalidMessage);
      }
    }
    
    El wrapper = wrapCellPad(checkbox);  
    cell.appendChild(wrapper);
    
    if (isEditable()) {
      addStrike(cell);
    }
    
    // add text attributes
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.inactive", text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive"));
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.active", text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active"));
    
    
    return checkbox;
  }
  
  protected abstract El createHeader();
}

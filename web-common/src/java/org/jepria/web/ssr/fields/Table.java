package org.jepria.web.ssr.fields;


import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;

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
    
    // TODO remove id, use class and css
    setAttribute("id", "table");
    
    addScript("js/table.js");
    addStyle("css/table.css");
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
    
    
    El header = createHeader();
    if (header != null) {
      appendChild(header);
    }
    
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
        El rowCreated = createRowCreated(item, tabIndex);
        if (rowCreated != null) {
          rowCreated.setAttribute("item-id", item.getId());
          appendChild(rowCreated);
        }
      }
    }
    
    setAttribute("tabindex-next", tabIndexValue);
  }
  
  /**
   * Create a basic table row (possibly modified) representing a single item. 
   * <br/>Implementors should not manually add the element to the table, just return it.
   * @param item data from the server, non-null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  protected abstract El createRow(T item, TabIndex tabIndex);
  
  /**
   * Creates a table row (in created state) representing a single item, that has not been saved to the server yet,
   * but having a UI table row created for saving.
   * <br/>Implementors should not manually add the element to the table, just return it.
   * @param item optional data from the UI to fill the created table row with, may be null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return {@code null} if no rowCreated required for the table (e.g. the table does not support creation of new items)
   */
  protected abstract El createRowCreated(T item, TabIndex tabIndex);
  
  protected El addField(El cell, Field field, String placeholder) {
    return addField(cell, field, placeholder, isEditable());
  }
  
  protected El addField(El cell, El field) {
    
    
    El wrapper = Fields.wrapCellPad(field);
    cell.appendChild(wrapper);
    
    
    field.classList.add("table__field-text_inactivatible");
    field.classList.add("table__field_disableable");
    
    return field;
  }
  
  protected void addStrike(El cell) {
    El strike = new El("div");
    strike.classList.add("strike");
    cell.appendChild(strike);
  }

  protected El addField(El cell, Field field, String placeholder, boolean fieldEditable) {
    
    El fieldEl;
    {
      if (fieldEditable && !field.readonly) {
        fieldEl = new FieldTextInput(field.name, 
            field.value, field.valueOriginal, placeholder,
            field.invalid, field.invalidMessage);
        
      } else {
        
        fieldEl = new FieldTextLabel(HtmlEscaper.escape(field.value));
      }      
      
      
      El wrapper = Fields.wrapCellPad(fieldEl);
      cell.appendChild(wrapper);
    }
    
    
    fieldEl.classList.add("table__field-text_inactivatible");
    fieldEl.classList.add("table__field_disableable");
    
    if (isEditable()) {
      addStrike(cell);
    }
    
    return fieldEl;
  }
  
  /**
   * 
   * @param cell
   * @param tabIndex
   * @param titleDelete text to display as a title of delete button. If {@code null} then no title
   * @param titleUndelete text to display as a title of undelete button. If {@code null} then no title
   */
  protected El addFieldDelete(El cell, TabIndex tabIndex, String titleDelete, String titleUndelete) {

    El field = new El("div");
    
    El buttonDelete = new El("input");
    buttonDelete.classList.add("button-delete");
    buttonDelete.classList.add("button-delete_delete");
    buttonDelete.setAttribute("type", "image");
    buttonDelete.setAttribute("src", "img/delete.png");
    if (titleDelete != null) {
      buttonDelete.setAttribute("title", titleDelete);
    }
    tabIndex.setNext(buttonDelete);
    
    El buttonUndelete = new El("input");
    buttonUndelete.classList.add("button-delete");
    buttonUndelete.classList.add("button-delete_undelete");
    buttonUndelete.setAttribute("type", "image");
    buttonUndelete.setAttribute("src", "img/undelete.png");
    if (titleUndelete != null) {
      buttonUndelete.setAttribute("title", titleUndelete);
    }
    tabIndex.setNext(buttonUndelete);
    
    field.appendChild(buttonDelete);
    field.appendChild(buttonUndelete);
    
    El wrapper = Fields.wrapCellPad(field);  
    cell.appendChild(wrapper);
    
    return field;
  }
  
  protected El createCell(El row, String columnClass) {
    El cell = new El("div");
    cell.classList.add("cell");
    cell.classList.add(columnClass);
    row.appendChild(cell);
    return cell;
  }
  
  /**
   * 
   * @param cell
   * @param field
   * @param titleActive text to display as a title of active checkbox. If {@code null} then empty title
   * @param titleInactive text to display as a title of inactive checkbox. If {@code null} then empty title
   * @return
   */
  protected FieldCheckBox addCheckbox(El cell, Field field, String titleActive, String titleInactive) {
    
    FieldCheckBox checkbox;
    
    {
      boolean active = !"false".equals(field.value);
      Boolean valueOriginal;
      if (!field.readonly && field.valueOriginal != null) {
        valueOriginal = !"false".equals(field.valueOriginal);
      } else {
        valueOriginal = null;
      }
      
      checkbox = new FieldCheckBox(field.name, active, valueOriginal, field.invalid, field.invalidMessage);
      
      checkbox.setEnabled(!field.readonly);
  
      
      // add text attributes
      checkbox.setTitleActive(titleActive);
      checkbox.setTitleInactive(titleInactive);
      
      
      El wrapper = Fields.wrapCellPad(checkbox);
      cell.appendChild(wrapper);
    }
    
    
    checkbox.classList.add("table__checkbox");
    checkbox.classList.add("table__field_disableable");

    
    if (isEditable()) {
      addStrike(cell);
    }
    
    return checkbox;
  }
  
  /**
   * Create a table header.
   * <br/>Implementors should not manually add the element to the table, just return it.
   * @return {@code null} if no header required for the table
   */
  protected abstract El createHeader();
}

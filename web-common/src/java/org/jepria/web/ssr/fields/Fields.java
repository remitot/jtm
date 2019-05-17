package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;

public class Fields {
  
  /**
   * Creates an editable field element ({@code <input>}) from name, value and placeholder
   * @param name
   * @param value
   * @param placeholder
   * @return
   */
  public static El createFieldInput(String name, String value, String placeholder) {
    El field = new El("input");
    field.setAttribute("type", "text");
    field.setAttribute("name", name);
    field.setAttribute("value", value);
    field.setAttribute("placeholder", placeholder);
    return field;
  }
  
  /**
   * Creates a non-editable field element ({@code <label>}) from value
   * @param value
   * @return
   */
  public static El createFieldLabel(String value) {
    El field = new El("label");
    field.setInnerHTML(value, true);
    return field;
  }
  
  /**
   * Creates and adds an editable or non-editable field to the {@code cell}
   * @param cell to add a field to
   * @param field not null
   * @param placeholder for an editable field
   * @param fieldEditable
   * @return
   */
  public static El addField(El cell, Field field, String placeholder, boolean fieldEditable) {
    final El fieldEl;
    if (fieldEditable) {
      fieldEl = createFieldInput(field.name, field.value, placeholder);
    } else {
      fieldEl = createFieldLabel(field.value);
    }
    
    addField(cell, field, fieldEl);
    
    return fieldEl;
  }
  
  /**
   * Adds an element to the cell as a field (with setting styles properly) 
   * @param cell
   * @param fieldEl
   */
  public static void addField(El cell, El fieldEl) {
    fieldEl.classList.add("field-text");
    fieldEl.classList.add("field-text_inactivatible");
    fieldEl.classList.add("disableable");
    
    El wrapper = wrapCellPad(fieldEl);
    cell.appendChild(wrapper);
  }

  /**
   * Adds an element to the cell as a field (with setting styles properly), sets top-level field attributes 
   * ({@code invalid}, {@code value-original}) 
   * @param cell
   * @param field
   * @param fieldEl
   */
  public static void addField(El cell, Field field, El fieldEl) {
    addField(cell, fieldEl);
    
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
  }
  
  public static El wrapCellPad(El element) {
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
  
  /**
   * 
   * @param cell
   * @param field
   * @param titleCheckboxActive text to display as a title of active checkbox. If {@code null} then empty title
   * @param titleCheckboxInactive text to display as a title of inactive checkbox. If {@code null} then empty title
   * @return
   */
  public static CheckBox addCheckbox(El cell, Field field, String titleCheckboxActive, String titleCheckboxInactive) {
    
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
    
    
    // add text attributes
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.active", 
        titleCheckboxActive == null ? "" : titleCheckboxActive);
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.inactive", 
        titleCheckboxInactive == null ? "" : titleCheckboxInactive);
    
    
    
    return checkbox;
  }
}

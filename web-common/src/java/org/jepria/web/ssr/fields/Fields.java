package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;

public class Fields {
  public static El createFieldInput(String name, String value, String placeholder) {
    El field = new El("input");
    field.setAttribute("type", "text");
    field.setAttribute("name", name);
    field.setAttribute("value", value);
    field.setAttribute("placeholder", placeholder);
    return field;
  }
  
  public static El createFieldLabel(String value) {
    El field = new El("label");
    field.setInnerHTML(value, true);
    return field;
  }
  
  /**
   * Creates and adds a field to the {@code cell}
   * @param cell to add a field to
   * @param field
   * @param placeholder
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
    
    addField(cell, field, fieldEl, placeholder);
    
    return fieldEl;
  }
  
  public static void addField(El cell, Field field, El fieldEl, String placeholder) {
    
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

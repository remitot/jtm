package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;

public class Fields {
  
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
    
    if (fieldEditable && !field.readonly) {
      fieldEl = new FieldTextInput(field.name, 
          field.value, field.valueOriginal, placeholder,
          field.invalid, field.invalidMessage);
      
    } else {
      
      fieldEl = new FieldTextLabel(HtmlEscaper.escape(field.value));
    }      
    
    addField(cell, fieldEl);
    
    return fieldEl;
  }
  
  /**
   * Adds an element to the cell as a field (with setting styles properly) 
   * @param cell
   * @param fieldEl
   */
  public static void addField(El cell, El fieldEl) {
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
  public static FieldCheckBox addCheckbox(El cell, Field field, String titleCheckboxActive, String titleCheckboxInactive) {
    
    boolean active = !"false".equals(field.value);
    Boolean valueOriginal;
    if (!field.readonly && field.valueOriginal != null) {
      valueOriginal = !"false".equals(field.valueOriginal);
    } else {
      valueOriginal = null;
    }
    
    FieldCheckBox checkbox = new FieldCheckBox(field.name, active, valueOriginal, field.invalid, field.invalidMessage);
    
    checkbox.setEnabled(!field.readonly);

    // add text attributes
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.active", 
        titleCheckboxActive == null ? "" : titleCheckboxActive);
    checkbox.setAttribute("org.jepria.web.ssr.Table.checkbox_active.title.inactive", 
        titleCheckboxInactive == null ? "" : titleCheckboxInactive);
    
    addField(cell, checkbox);
    
    return checkbox;
  }
}

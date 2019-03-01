package org.jepria.web.ssr.table;


import java.util.List;

public abstract class Table<T> extends El {
  
  public Table() {
    super("div");
    
    // TODO remove, use class and css
    setAttribute("id", "table");
    setAttribute("style", "width: 100%;");
  }
  
  private TabIndex tabIndex;
  
  protected interface TabIndex {
    int next();
  }
  
  private void resetTabIndex() {
    tabIndex = new TabIndex() {
      private int i = 1;
      @Override
      public int next() {
        return i++;
      }
    };
  }
  
  protected boolean isEditable() {
    return true;
  }
  
  public void load(List<T> items) {
    resetTabIndex();
     
    if (items != null && !items.isEmpty()) {
      
      // TODO here are table CONTENTS only (but better to return the whole table from the root <div class="table">)
      appendChild(createHeader());
      
      for (T item: items) {
        appendChild(createRow(item, tabIndex));
      }
    }
    
    appendChild(createRowButtonCreate());
  }
  
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
  
  protected CheckBox addCheckbox(El cell, boolean active, boolean enabled) {
    CheckBox checkbox = new CheckBox(active);
    
    checkbox.setEnabled(enabled);
    
    checkbox.classList.add("deletable");

    El wrapper = wrapCellPad(checkbox);  
    
    cell.appendChild(wrapper);
    
    addStrike(cell);
    
    return checkbox;
  }
  
  protected abstract El createRowCreate(TabIndex tabIndex);
  
  protected abstract El createHeader();
  
  protected El createRowButtonCreate() {
    El row = new El("div");
    row.classList.add("row-button-create");
    
    El cell;
    
    // active
    cell = createCell(row, "column-button-create");
    cell.classList.add("column-left");
    
    El buttonCreate = new El("button");
    buttonCreate.classList.add("row-button-create__button-create");
    buttonCreate.classList.add("big-black-button");
    buttonCreate.setInnerHTML("НОВАЯ ЗАПИСЬ"); // NON-NLS
    
    El wrapper = wrapCellPad(buttonCreate);
    
    cell.appendChild(wrapper);
    
    return row;
  }
}

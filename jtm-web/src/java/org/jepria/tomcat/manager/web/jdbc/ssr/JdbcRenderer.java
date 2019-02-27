package org.jepria.tomcat.manager.web.jdbc.ssr;

import java.util.List;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;

public class JdbcRenderer {
  
  private int tabindex = 1; 
  
  public String renderTable(List<ConnectionDto> resources) {
    
    final StringBuilder sb = new StringBuilder();
    
    if (resources != null) {
      for (ConnectionDto resource: resources) {
        El row = createRow(resource);
      }
    }
    
    return sb.toString();
  }
  
  private El createRow(ConnectionDto listItem) {
    final boolean dataModifiable = !Boolean.FALSE.equals(listItem.getDataModifiable());

    El row = new El("div");
    row.classList.add("row");
    row.setAttribute("item-id", listItem.getId());
    
    El cell;
    El field;
    
    // active
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, listItem.getActive(), true);
    if (!dataModifiable) {
      checkBox.classList.add("readonly");
      checkBox.setEnabled(false);
    } else {
      // TODO bad direct access
      checkBox.input.setAttribute("tabindex", tabindex++);
    }
    
    if (Boolean.FALSE.equals(listItem.getActive())) {
      row.classList.add("inactive");
    }
    
    
    El cellDelete = createCell(row, "column-delete");;
    
    
    El div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-name");
    cell.classList.add("cell-field");
    field = addField(cell, "name", listItem.getName(), null);
    field.setAttribute("value-original", listItem.getName());
    field.setAttribute("tabindex", tabindex++);
    
    cell = createCell(div, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", listItem.getServer(), null);
    field.setAttribute("value-original", listItem.getServer());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabindex++);
    }
    
    cell = createCell(div, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", listItem.getDb(), null);
    field.setAttribute("value-original", listItem.getDb());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabindex++);
    }
    
    cell = createCell(div, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", listItem.getUser(), null);
    field.setAttribute("value-original", listItem.getUser());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabindex++);
    }
    
    cell = createCell(div, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", listItem.getPassword(), null);
    field.setAttribute("value-original", listItem.getPassword());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabindex++);
    }
    
    if (dataModifiable) {
      El deleteButton = addFieldDelete(cellDelete);
      deleteButton.setAttribute("tabindex", tabindex++);
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  
  // from table.js
  private El addField(El cell, String name, String value, String placeholder) {
    El field = createField(name, value, placeholder);
    
    El wrapper = wrapCellPad(field);
    cell.appendChild(wrapper);

    if (isEditable()) {
      addStrike(cell);
    }
      
    return field;
  }
  
  private El createField(String name, String value, String placeholder) {
    if (isEditable()) {
      return createFieldInput(name, value, placeholder);
    } else {
      return createFieldLabel(value);
    }
  }

  private El createFieldInput(String name, String value, String placeholder) {
    El field = new El("input");
    field.setAttribute("type", "text");
    field.setAttribute("name", name);
    field.value = value;
    if (placeholder != null) {
      field.setAttribute("placeholder", placeholder);
    }
    
    field.oninput = function(event){onFieldInput(event.target)};
    field.classList.add("field-text");
    field.classList.add("inactivatible");
    field.classList.add("deletable");
    
    return field;
  }

  private void addStrike(El cell) {
    El strike = new El("div");
    strike.classList.add("strike");
    cell.appendChild(strike);
  }

  private El createFieldLabel(String value) {
    El field = new El("label");
    field.innerHTML = value;
    
    field.classList.add("field-text");
    field.classList.add("inactivatible");
    field.classList.add("deletable");
    
    return field;
  }

  private El wrapCellPad(El element) {
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
  
  private El addFieldDelete(El cell) {

    El delete = new El("input");
    delete.setAttribute("type", "image");
    delete.setAttribute("src", "gui/img/delete.png");
    delete.setAttribute("title", "Удалить"); // NON-NLS
    delete.onclick = function(event){onDeleteButtonClick(event.target);};
    
    El wrapper = wrapCellPad(delete);  
    cell.appendChild(wrapper);
    
    return delete;
  }
  
  private El createCell(El row, String columnClass) {
    El cell = new El("div");
    cell.classList.add("cell");
    cell.classList.add(columnClass);
    row.appendChild(cell);
    return cell;
  }
  
  private CheckBox addCheckbox(El cell, boolean active, boolean enabled) {
    CheckBox checkbox = new CheckBox(active);
    
    // add 'hovered' class for checkbox's onfocus and onmouseover  
    // TODO bad direct access
    checkbox.input.onfocus = function(event){
      var input = event.target;
      input.parentElement.getElementsByClassName("checkmark")[0].classList.add("hovered");
    }
    //TODO bad direct access
    checkbox.input.addEventListener("focusout", function(event) { // .onfocusout not working in some browsers
      var input = event.target;
      input.parentElement.getElementsByClassName("checkmark")[0].classList.remove("hovered");
    });
    //TODO bad direct access
    checkbox.checkmark.onmouseover = function(event) {
      var checkmark = event.target;
      checkmark.classList.add("hovered");
    }
    //TODO bad direct access
    checkbox.checkmark.addEventListener("mouseout", function(event) { // .onmouseout not working in some browsers
      var checkmark = event.target;
      checkmark.classList.remove("hovered");
    });
    
    checkbox.setEnabled(enabled);
    
    checkbox.onclick = function(event){
      onCheckboxInput(event.target);
      checkModifications();
    };
    checkbox.classList.add("deletable");

    El wrapper = wrapCellPad(checkbox);  
    
    cell.appendChild(wrapper);
    
    onCheckboxInput(checkbox.getElementsByTagName("input")[0]);// trigger initial event
    
    addStrike(cell);
    
    return checkbox;
  }
  
  // The very JDBC's method! Not table's!
  private void setFieldReadonly(El field) {
    field.setAttribute("readonly", "true");
    field.classList.add("readonly");
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }
}

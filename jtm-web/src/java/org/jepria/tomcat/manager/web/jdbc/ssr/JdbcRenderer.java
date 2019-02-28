package org.jepria.tomcat.manager.web.jdbc.ssr;

import java.io.IOException;
import java.util.List;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;

public class JdbcRenderer {
  
  private int tabindex = 1; 
  
  public String tableJs() {
    return null;
  }
  
  private boolean isEditable() {
    return true;
  }
  
  public String tableHtml(List<ConnectionDto> resources) throws IOException {
    
    final StringBuilder sb = new StringBuilder();
    
    if (resources != null && !resources.isEmpty()) {
      
      // TODO here are table CONTENTS only (but better to return the whole table from the root <div class="table">)
      El header = createHeader();
      header.print(sb);
      
      for (ConnectionDto resource: resources) {
        El row = createRow(resource);
        row.print(sb);
      }
    }
    
    El rowButtonCreate = createRowButtonCreate();
    rowButtonCreate.print(sb);
    
    return sb.toString();
  }
  
  private El createRow(ConnectionDto listItem) {
    final boolean dataModifiable = !Boolean.FALSE.equals(listItem.getDataModifiable());

    El row = new El("div");
    row.classList.add("row");
    row.setAttribute("item-id", listItem.getId());
    
    El cell, field;
    
    // active
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, listItem.getActive(), true);
    checkBox.setAttribute("value-original", listItem.getActive());
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
    
    El cellDelete = createCell(row, "column-delete");
    
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
    field.setAttribute("value", value);
    field.setAttribute("placeholder", placeholder);
    
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
    field.setInnerHTML(value);
    
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

    El button = new El("input");
    button.classList.add("field-delete");
    
    button.setAttribute("type", "image");
    button.setAttribute("src", "gui/img/delete.png");
    button.setAttribute("title", "Удалить"); // NON-NLS
    
    El wrapper = wrapCellPad(button);  
    cell.appendChild(wrapper);
    
    return button;
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
    
    checkbox.setEnabled(enabled);
    
    checkbox.classList.add("deletable");

    El wrapper = wrapCellPad(checkbox);  
    
    cell.appendChild(wrapper);
    
    addStrike(cell);
    
    return checkbox;
  }
  
  // The very JDBC's method! Not table's!
  private void setFieldReadonly(El field) {
    field.setAttribute("readonly", "true");
    field.classList.add("readonly");
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }
  
  private El createRowCreate() {
    El row = new El("div");
    row.classList.add("row");
    row.classList.add("created");
    
    El cell, field;
    
    // active
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, true, false);
    checkBox.classList.add("readonly");
    checkBox.setEnabled(false);
    
    El cellDelete = createCell(row, "column-delete");
    
    El flexColumns = new El("div");
    flexColumns.classList.add("flexColumns");
    
    cell = createCell(flexColumns, "column-name");
    cell.classList.add("cell-field");
    field = addField(cell, "name", null, "jdbc/MyDataSource");
    field.setAttribute("tabindex", tabindex++);
   
    cell = createCell(flexColumns, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", null, "db-server:1521");
    field.setAttribute("tabindex", tabindex++);
    
    cell = createCell(flexColumns, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", null, "MYDATABASE");
    field.setAttribute("tabindex", tabindex++);
    
    cell = createCell(flexColumns, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", null, "me");
    field.setAttribute("tabindex", tabindex++);
    
    cell = createCell(flexColumns, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", null, "mysecret");
    field.setAttribute("tabindex", tabindex++);
    
    El deleteButton = addFieldDelete(cellDelete);
    deleteButton.setAttribute("tabindex", tabindex++);
    
    row.appendChild(flexColumns);
    
    return row;
  }
  
  private El createHeader() {
    El row = new El("div");
    row.classList.add("header");
    
    El cell, div;
    Label label;
    
    // active
    cell = createCell(row, "column-active");// empty cell
    cell.classList.add("column-left");
    
    cell = createCell(row, "column-delete");// empty cell
    
    div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-name");
    label = new Label("Название"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-server");
    label = new Label("Сервер базы данных"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-db");
    label = new Label("Имя базы"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-user");
    label = new Label("Пользователь базы"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-password");
    label = new Label("Пароль к базе"); // NON-NLS
    cell.appendChild(label);
    
    row.appendChild(div);
    
    return row;
  }
  
  private El createRowButtonCreate() {
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

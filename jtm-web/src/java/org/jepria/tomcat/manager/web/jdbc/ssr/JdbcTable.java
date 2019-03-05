package org.jepria.tomcat.manager.web.jdbc.ssr;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.CheckBox;
import org.jepria.web.ssr.table.Table;

public class JdbcTable extends Table<ConnectionDto> {
  
  @Override
  protected El createRow(ConnectionDto listItem, TabIndex tabIndex) {
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
      tabIndex.setNext(checkBox.input);
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
    tabIndex.setNext(field);
    
    cell = createCell(div, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", listItem.getServer(), null);
    field.setAttribute("value-original", listItem.getServer());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", listItem.getDb(), null);
    field.setAttribute("value-original", listItem.getDb());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", listItem.getUser(), null);
    field.setAttribute("value-original", listItem.getUser());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", listItem.getPassword(), null);
    field.setAttribute("value-original", listItem.getPassword());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    if (dataModifiable) {
      El deleteButton = addFieldDelete(cellDelete);
      tabIndex.setNext(deleteButton);
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  protected void setFieldReadonly(El field) {
    field.setAttribute("readonly", "true");
    field.classList.add("readonly");
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }
  
  /**
   * Creates a new (empty) table row for creating a new item
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  public El createRowCreate(TabIndex tabIndex) {
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
    tabIndex.setNext(field);
   
    cell = createCell(flexColumns, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", null, "db-server:1521");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", null, "MYDATABASE");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", null, "me");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", null, "mysecret");
    tabIndex.setNext(field);
    
    El deleteButton = addFieldDelete(cellDelete);
    tabIndex.setNext(deleteButton);
    
    row.appendChild(flexColumns);
    
    return row;
  }
  
  @Override
  protected El createHeader() {
    El row = new El("div");
    row.classList.add("header");
    
    El cell, div, label;
    
    // active
    cell = createCell(row, "column-active");// empty cell
    cell.classList.add("column-left");
    
    cell = createCell(row, "column-delete");// empty cell
    
    div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-name");
    label = new El("label");
    label.setInnerHTML("Название"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-server");
    label = new El("label");
    label.setInnerHTML("Сервер базы данных"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-db");
    label = new El("label");
    label.setInnerHTML("Имя базы"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-user");
    label = new El("label");
    label.setInnerHTML("Пользователь базы"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-password");
    label = new El("label");
    label.setInnerHTML("Пароль к базе"); // NON-NLS
    cell.appendChild(label);
    
    row.appendChild(div);
    
    return row;
  }
}

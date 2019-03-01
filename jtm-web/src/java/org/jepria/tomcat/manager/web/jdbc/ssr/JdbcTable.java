package org.jepria.tomcat.manager.web.jdbc.ssr;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.ssr.table.CheckBox;
import org.jepria.web.ssr.table.El;
import org.jepria.web.ssr.table.Label;
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
      // TODO bad direct access
      checkBox.input.setAttribute("tabindex", tabIndex.next());
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
    field.setAttribute("tabindex", tabIndex.next());
    
    cell = createCell(div, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", listItem.getServer(), null);
    field.setAttribute("value-original", listItem.getServer());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabIndex.next());
    }
    
    cell = createCell(div, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", listItem.getDb(), null);
    field.setAttribute("value-original", listItem.getDb());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabIndex.next());
    }
    
    cell = createCell(div, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", listItem.getUser(), null);
    field.setAttribute("value-original", listItem.getUser());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabIndex.next());
    }
    
    cell = createCell(div, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", listItem.getPassword(), null);
    field.setAttribute("value-original", listItem.getPassword());
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      field.setAttribute("tabindex", tabIndex.next());
    }
    
    if (dataModifiable) {
      El deleteButton = addFieldDelete(cellDelete);
      deleteButton.setAttribute("tabindex", tabIndex.next());
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  protected void setFieldReadonly(El field) {
    field.setAttribute("readonly", "true");
    field.classList.add("readonly");
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }
  
  @Override
  protected El createRowCreate(TabIndex tabIndex) {
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
    field.setAttribute("tabindex", tabIndex.next());
   
    cell = createCell(flexColumns, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", null, "db-server:1521");
    field.setAttribute("tabindex", tabIndex.next());
    
    cell = createCell(flexColumns, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", null, "MYDATABASE");
    field.setAttribute("tabindex", tabIndex.next());
    
    cell = createCell(flexColumns, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", null, "me");
    field.setAttribute("tabindex", tabIndex.next());
    
    cell = createCell(flexColumns, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", null, "mysecret");
    field.setAttribute("tabindex", tabIndex.next());
    
    El deleteButton = addFieldDelete(cellDelete);
    deleteButton.setAttribute("tabindex", tabIndex.next());
    
    row.appendChild(flexColumns);
    
    return row;
  }
  
  @Override
  protected El createHeader() {
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
}

package org.jepria.tomcat.manager.web.jdbc.ssr;

import java.util.Optional;

import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.CheckBox;
import org.jepria.web.ssr.table.Table;

public class JdbcTable extends Table<ConnectionDto> {
  
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
  
  @Override
  public El createRow(ConnectionDto item, TabIndex tabIndex) {
    return createRowInternal(item, null, tabIndex);
  }
  
  @Override
  public El createRowCreated(ConnectionDto item, TabIndex tabIndex) {
    final Optional<ConnectionDto> itemOpt = Optional.ofNullable(item);
    
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
    field = addField(cell, "name", itemOpt.isPresent() ? itemOpt.get().get("name") : null, "jdbc/MyDataSource");
    tabIndex.setNext(field);
   
    cell = createCell(flexColumns, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", itemOpt.isPresent() ? itemOpt.get().get("server") : null, "db-server:1521");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", itemOpt.isPresent() ? itemOpt.get().get("db") : null, "MYDATABASE");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", itemOpt.isPresent() ? itemOpt.get().get("user") : null, "me");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", itemOpt.isPresent() ? itemOpt.get().get("password") : null, "mysecret");
    tabIndex.setNext(field);
    
    
    addFieldDelete(cellDelete, tabIndex);
    
    
    row.appendChild(flexColumns);
    
    return row;
  }
  
  @Override
  public El createRowModified(ConnectionDto itemOriginal, ConnectionDto item, TabIndex tabIndex) {
    El row = createRowInternal(itemOriginal, item, tabIndex);
    row.classList.add("modified");
    return row;
  }
  
  /**
   * Creates a table row, fills it with original data and (possibly) overlays that data with new data 
   * @param itemOriginal original data from the server, non-null
   * @param item optional data from the UI to overlay the original data with, may be null
   * @param tabIndex table-wide counter for assigning {@code tabindex} attributes to {@code input} elements
   * @return
   */
  private El createRowInternal(ConnectionDto itemOriginal, ConnectionDto item, TabIndex tabIndex) {
    final boolean dataModifiable = !Boolean.FALSE.equals(itemOriginal.getDataModifiable());

    El row = new El("div");
    row.classList.add("row");
    
    El cell, field;
    
    // active
    final boolean active = item != null && item.get("active") != null ? !"false".equals(item.get("active")) : !"false".equals(itemOriginal.get("active"));
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, active, true);
    checkBox.setAttribute("value-original", !"false".equals(itemOriginal.get("active")));
    if (!dataModifiable) {
      checkBox.classList.add("readonly");
      checkBox.setEnabled(false);
    } else {
      tabIndex.setNext(checkBox.input);
    }
    
    if ("false".equals(itemOriginal.get("active"))) {
      row.classList.add("inactive");
    }
    
    El cellDelete = createCell(row, "column-delete");
    
    El div = new El("div");
    div.classList.add("flexColumns");
    
    final String name = item != null && item.get("name") != null ? item.get("name") : itemOriginal.get("name");
    cell = createCell(div, "column-name");
    cell.classList.add("cell-field");
    field = addField(cell, "name", name, null);
    field.setAttribute("value-original", itemOriginal.get("name"));
    tabIndex.setNext(field);
    
    final String server = item != null && item.get("server") != null ? item.get("server") : itemOriginal.get("server");
    cell = createCell(div, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, "server", server, null);
    field.setAttribute("value-original", itemOriginal.get("server"));
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    final String db = item != null && item.get("db") != null ? item.get("db") : itemOriginal.get("db");
    cell = createCell(div, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, "db", db, null);
    field.setAttribute("value-original", itemOriginal.get("db"));
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    final String user = item != null && item.get("user") != null ? item.get("user") : itemOriginal.get("user");
    cell = createCell(div, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, "user", user, null);
    field.setAttribute("value-original", itemOriginal.get("user"));
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    final String password = item != null && item.get("password") != null ? item.get("password") : itemOriginal.get("password");
    cell = createCell(div, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, "password", password, null);
    field.setAttribute("value-original", itemOriginal.get("password"));
    if (!dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    if (dataModifiable) {
      addFieldDelete(cellDelete, tabIndex);
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRowDeleted(ConnectionDto item, TabIndex tabIndex) {
    El row = createRowInternal(item, null, tabIndex);
    row.classList.add("deleted");
    return row;
  }
  
  protected void setFieldReadonly(El field) {
    field.setAttribute("readonly", "true");
    field.classList.add("readonly");
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }

  
}

package org.jepria.tomcat.manager.web.jdbc.ssr;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.CheckBox;
import org.jepria.web.ssr.table.Table;

public class JdbcTable extends Table<JdbcItem> {
  
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
  public El createRow(JdbcItem item, TabIndex tabIndex) {
    return createRowInternal(item, tabIndex);
  }
  
  @Override
  public El createRowCreated(JdbcItem item, TabIndex tabIndex) {

    El row = new El("div");
    row.classList.add("row");
    row.classList.add("created");
    
    El cell, field;
    
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    addCheckbox(cell, item.active());
    
    El cellDelete = createCell(row, "column-delete");
    
    El flexColumns = new El("div");
    flexColumns.classList.add("flexColumns");
    
    cell = createCell(flexColumns, "column-name");
    cell.classList.add("cell-field");
    field = addField(cell, item.name(), "jdbc/MyDataSource");
    tabIndex.setNext(field);
   
    cell = createCell(flexColumns, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, item.server(), "db-server:1521");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, item.db(), "MYDATABASE");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, item.user(), "me");
    tabIndex.setNext(field);
    
    cell = createCell(flexColumns, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, item.password(), "mysecret");
    tabIndex.setNext(field);
    
    
    addFieldDelete(cellDelete, tabIndex);
    
    
    row.appendChild(flexColumns);
    
    return row;
  }
  
  @Override
  public El createRowModified(JdbcItem item, TabIndex tabIndex) {
    El row = createRowInternal(item, tabIndex);
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
  private El createRowInternal(JdbcItem item, TabIndex tabIndex) {
    El row = new El("div");
    row.classList.add("row");
    
    El cell, field;
    
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, item.active());
    if (!item.active().readonly) {
      tabIndex.setNext(checkBox.input);
    }
    
    if ("false".equals(item.active().value)) {
      row.classList.add("inactive");
    }
    
    El cellDelete = createCell(row, "column-delete");
    
    El div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-name");
    cell.classList.add("cell-field");
    field = addField(cell, item.name(), null);
    tabIndex.setNext(field);
    
    cell = createCell(div, "column-server");
    cell.classList.add("cell-field");
    field = addField(cell, item.server(), null);
    if (!item.dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-db");
    cell.classList.add("cell-field");
    field = addField(cell, item.db(), null);
    if (!item.dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-user");
    cell.classList.add("cell-field");
    field = addField(cell, item.user(), null);
    if (!item.dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    cell = createCell(div, "column-password");
    cell.classList.add("cell-field");
    field = addField(cell, item.password(), null);
    if (!item.dataModifiable) {
      setFieldReadonly(field);
    } else {
      tabIndex.setNext(field);
    }
    
    if (item.dataModifiable) {
      addFieldDelete(cellDelete, tabIndex);
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRowDeleted(JdbcItem item, TabIndex tabIndex) {
    El row = createRowInternal(item, tabIndex);
    row.classList.add("deleted");
    return row;
  }
  
  @Override
  protected void setFieldReadonly(El field) {
    super.setFieldReadonly(field);
    field.setAttribute("title", "Поле нередактируемо, поскольку несколько Context/ResourceLink ссылаются на один и тот же Server/Resource в конфигурации Tomcat"); // NON-NLS
  }

  
}

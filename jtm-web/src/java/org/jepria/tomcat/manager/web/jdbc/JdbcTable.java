package org.jepria.tomcat.manager.web.jdbc;

import org.jepria.tomcat.manager.web.jdbc.JdbcTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.CheckBox;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;
import org.jepria.web.ssr.fields.Table;

public class JdbcTable extends Table<Record> {
  
  public static class Record extends ItemData {
    private static final long serialVersionUID = 1L;
    
    public boolean dataModifiable = true;
    
    public Record() {
      put("active", new Field("active"));
      put("name", new Field("name"));
      put("server", new Field("server"));
      put("db", new Field("db"));
      put("user", new Field("user"));
      put("password", new Field("password"));
    }
    
    public Field active() {
      return get("active");
    }
    public Field name() {
      return get("name");
    }
    public Field server() {
      return get("server");
    }
    public Field db() {
      return get("db");
    }
    public Field user() {
      return get("user");
    }
    public Field password() {
      return get("password");
    }
  }
  
  public JdbcTable(Context context) {
    super(context);
    addStyle("css/jdbc/jdbc.css");
  }
  
  @Override
  protected El createHeader() {
    
    Text text = context.getText();
    
    El row = new El("div", context);
    row.classList.add("header");
    
    El cell, div, label;
    
    cell = createCell(row, "column-active");// empty cell
    cell.classList.add("column-left");
    
    cell = createCell(row, "column-delete");// empty cell
    
    div = new El("div", context);
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-name");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_name"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-server");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_server"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-db");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_db"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-user");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_user"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-password");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.jdbc.Table.header.column_password"));
    cell.appendChild(label);
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRow(Record item, TabIndex tabIndex) {
    
    Text text = context.getText();
    
    El row = new El("div", context);
    row.classList.add("row");
    
    {
      El cell = createCell(row, "column-active");
      cell.classList.add("column-left");
      cell.classList.add("cell-field");
      String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
      String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
      CheckBox checkBox = addCheckbox(cell, item.active(), titleCheckboxActive, titleCheckboxInactive);
      if (item.dataModifiable) {
        tabIndex.setNext(checkBox.input);
      } else {
        addFieldUnmodifiableTitle(checkBox);
      }
      
      if ("false".equals(item.active().value)) {
        row.classList.add("inactive");
      }
    }
    
    El cellDelete = createCell(row, "column-delete");
    
    El div = new El("div", row.context);
    div.classList.add("flexColumns");
    
    {
      El cell = createCell(div, "column-name");
      cell.classList.add("cell-field");
      El field = addField(cell, item.name(), null);
      tabIndex.setNext(field);
    }
    
    {
      El cell = createCell(div, "column-server");
      cell.classList.add("cell-field");
      El field = addField(cell, item.server(), null);
      if (item.dataModifiable) {
        tabIndex.setNext(field);
      } else {
        addFieldUnmodifiableTitle(field);
      }
    }
    
    {
      El cell = createCell(div, "column-db");
      cell.classList.add("cell-field");
      El field = addField(cell, item.db(), null);
      if (item.dataModifiable) {
        tabIndex.setNext(field);
      } else {
        addFieldUnmodifiableTitle(field);
      }
    }
    
    {
      El cell = createCell(div, "column-user");
      cell.classList.add("cell-field");
      El field = addField(cell, item.user(), null);
      if (item.dataModifiable) {
        tabIndex.setNext(field);
      } else {
        addFieldUnmodifiableTitle(field);
      }
    }
    
    {
      El cell = createCell(div, "column-password");
      cell.classList.add("cell-field");
      El field = addField(cell, item.password(), null);
      if (item.dataModifiable) {
        tabIndex.setNext(field);
      } else {
        addFieldUnmodifiableTitle(field);
      }
    }
    
    if (item.dataModifiable) {
      String titleDelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete");
      String titleUndelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete");
      addFieldDelete(cellDelete, tabIndex, titleDelete, titleUndelete);
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  protected void addFieldUnmodifiableTitle(El field) {
    field.setAttribute("title", context.getText().getString("org.jepria.tomcat.manager.web.jdbc.field.unmodifiable"));
  }
  
  @Override
  public El createRowCreated(Record item, TabIndex tabIndex) {

    Text text = context.getText();
    
    El row = new El("div", context);
    row.classList.add("row");
    row.classList.add("created");
    
    El cell, field;
    
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
    String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
    addCheckbox(cell, item.active(), titleCheckboxActive, titleCheckboxInactive);
    
    El cellDelete = createCell(row, "column-delete");
    
    El flexColumns = new El("div", row.context);
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
    
    
    String titleDelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete");
    String titleUndelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete");
    addFieldDelete(cellDelete, tabIndex, titleDelete, titleUndelete);
    
    
    row.appendChild(flexColumns);
    
    return row;
  }
}

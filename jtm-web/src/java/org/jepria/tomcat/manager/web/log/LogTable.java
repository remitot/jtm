package org.jepria.tomcat.manager.web.log;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.Table;

public class LogTable extends Table<LogItem> {

  public LogTable(Context context) {
    super(context);
    addStyle("css/log/log.css");
  }
  
  @Override
  public El createRow(LogItem item, Table.TabIndex tabIndex) {
    final El row = new El("div", context);
    row.classList.add("row");
    
    El cell, fieldEl;
    
    El div = new El("div", context);
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-name");
    cell.classList.add("cell-field");
    addField(cell, item.name(), null);
    
    cell = createCell(div, "column-lastmod");
    cell.classList.add("cell-field");
    fieldEl = new El("label", context).setInnerHTML(item.lastmod().value);// create the label manually to avoid escaping 
    addField(cell, item.lastmod(), fieldEl, null);
    
    cell = createCell(div, "column-download");
    cell.classList.add("cell-field");
    fieldEl = new El("label", context).setInnerHTML(item.download().value);// create the label manually to avoid escaping
    addField(cell, item.download(), fieldEl, null);
    
    cell = createCell(div, "column-open");
    cell.classList.add("cell-field");
    fieldEl = new El("label", context).setInnerHTML(item.open().value);// create the label manually to avoid escaping
    addField(cell, item.open(), fieldEl, null);
    
    cell = createCell(div, "column-monitor");
    cell.classList.add("cell-monitor");
    fieldEl = new El("label", context).setInnerHTML(item.monitor().value);// create the label manually to avoid escaping
    addField(cell, item.monitor(), fieldEl, null);
    
    row.appendChild(div);
    
    return row;
  }

  @Override
  public El createRowCreated(LogItem item, Table.TabIndex tabIndex) {
    // the table is unmodifiable and must not allow creating rows
    throw new UnsupportedOperationException();
  }

  @Override
  protected El createHeader() {
    final El row = new El("div", context);
    row.classList.add("header");
    
    El div, cell, label;
    
    div = new El("div", context);
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-name");
    label = new El("label", context);
    label.setInnerHTML("Файл"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-lastmod");
    label = new El("label", context);
    label.setInnerHTML("Последняя запись"); // NON-NLS
    cell.appendChild(label);
    
    createCell(div, "column-download");
    
    createCell(div, "column-open");
    
    createCell(div, "column-monitor");
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  protected boolean isEditable() {
    return false;
  }

}

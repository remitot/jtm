package org.jepria.tomcat.manager.web.port;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.table.Collection;
import org.jepria.web.ssr.table.Table;

public class PortTable extends Table<PortItem> {
  
  @Override
  protected El createHeader() {
    El row = new El("div");
    row.classList.add("header");
    
    El cell, div, label;
    
    div = new El("div");// empty cell
    div.classList.add("flexColumns");
    div.classList.add("column-left");
      
    cell = createCell(div, "column-type");
    label = new El("label");
    label.setInnerHTML("Тип"); // NON-NLS
    cell.appendChild(label);
    
    cell = createCell(div, "column-port");
    label = new El("label");
    label.setInnerHTML("Порт"); // NON-NLS
    cell.appendChild(label);
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRow(PortItem item, TabIndex tabIndex) {
    El row = new El("div");
    row.classList.add("row");
    
    El cell, div;
    
    div = new El("div");// empty cell
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-type");
    cell.classList.add("cell-field");
    addField(cell, item.type(), null);
    
    cell = createCell(div, "column-port");
    cell.classList.add("cell-field");
    addField(cell, item.port(), null);
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRowCreated(PortItem item, TabIndex tabIndex) {
    // the table is unmodifiable and must not allow creating rows
    throw new UnsupportedOperationException();
  }
  
  @Override
  protected boolean isEditable() {
    return false;
  }
  
  @Override
  protected void addStyles(Collection styles) {
    super.addStyles(styles);
    styles.add("css/port/port.css");
  }
}

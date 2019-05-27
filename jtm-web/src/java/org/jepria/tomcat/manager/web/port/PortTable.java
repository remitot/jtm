package org.jepria.tomcat.manager.web.port;

import org.jepria.tomcat.manager.web.port.PortTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;
import org.jepria.web.ssr.fields.Table;

public class PortTable extends Table<Record> {
  
  public static class Record extends ItemData {
    private static final long serialVersionUID = 1L;
    
    public Record() {
      put("type", new Field("type"));
      put("number", new Field("number"));
    }
    
    public Field type() {
      return get("type");
    }
    public Field number() {
      return get("number");
    }
  }

  
  public PortTable(Context context) {
    super(context);
    addStyle("css/port/port.css");
  }
  
  @Override
  protected El createHeader() {
    
    Text text = context.getText();
    
    El row = new El("div", context);
    row.classList.add("header");
    
    El cell, div, label;
    
    div = new El("div", row.context);// empty cell
    div.classList.add("flexColumns");
    div.classList.add("column-left");
      
    cell = createCell(div, "column-type");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.port.Table.header.column_type"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-port");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.port.Table.header.column_port"));
    cell.appendChild(label);
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRow(Record item, TabIndex tabIndex) {
    El row = new El("div", context);
    row.classList.add("row");
    
    El cell, div;
    
    div = new El("div", row.context);// empty cell
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-type");
    cell.classList.add("cell-field");
    addField(cell, item.type(), null);
    
    cell = createCell(div, "column-number");
    cell.classList.add("cell-field");
    addField(cell, item.number(), null);
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRowCreated(Record item, TabIndex tabIndex) {
    // the table is unmodifiable and must not allow creating rows
    throw new UnsupportedOperationException();
  }
  
  @Override
  protected boolean isEditable() {
    return false;
  }
}

package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.table.CheckBox;
import org.jepria.web.ssr.table.Table;

public class JkMountTable extends Table<JkMountItem> {

  public JkMountTable(Text text) {
    super(text);
    addStyle("css/jk/jk.css");
  }
  
  @Override
  public El createRow(JkMountItem item, Table.TabIndex tabIndex) {
    El row = new El("div");
    row.classList.add("row");
    
    El cell, field;
    
    cell = createCell(row, "column-active");
    cell.classList.add("column-left");
    cell.classList.add("cell-field");
    CheckBox checkBox = addCheckbox(cell, item.active());
    tabIndex.setNext(checkBox.input);
    
    if ("false".equals(item.active().value)) {
      row.classList.add("inactive");
    }
    
    El cellDelete = createCell(row, "column-delete");
    
    El div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-application");
    cell.classList.add("cell-field");
    field = addField(cell, item.application(), null);
    tabIndex.setNext(field);
    
    
    cell = createCell(div, "column-details");
    cell.classList.add("cell-field");
    El a = new El("img").setAttribute("src", "img/delete.png").setAttribute("style", "width:24px;");
    El wrapper = wrapCellPad(a);
    cell.appendChild(wrapper);
    tabIndex.setNext(field);
    
    
    addFieldDelete(cellDelete, tabIndex);
    
    row.appendChild(div);
    
    return row;
  }

  @Override
  public El createRowCreated(JkMountItem item, Table.TabIndex tabIndex) {
    throw new UnsupportedOperationException();// TODO
  }

  @Override
  protected El createHeader() {
    El row = new El("div");
    row.classList.add("header");
    
    El cell, div, label;
    
    cell = createCell(row, "column-active");// empty cell
    cell.classList.add("column-left");
    
    cell = createCell(row, "column-delete");// empty cell
    
    div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-application");
    label = new El("label");
    label.setInnerHTML("apple");// TODO non-nls
    cell.appendChild(label);
    
    createCell(div, "column-details");
    
    row.appendChild(div);
    
    return row;
  }
}

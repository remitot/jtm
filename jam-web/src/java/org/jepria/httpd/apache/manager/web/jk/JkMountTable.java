package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.CheckBox;
import org.jepria.web.ssr.fields.Table;

public class JkMountTable extends Table<JkMountItem> {

  protected final Text text;
  
  public JkMountTable(Text text) {
    this.text = text;
    addClass("table");
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
    String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
    String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
    CheckBox checkBox = addCheckbox(cell, item.active(), titleCheckboxActive, titleCheckboxInactive);
    tabIndex.setNext(checkBox.input);
    
    if ("false".equals(item.active().value)) {
      row.classList.add("inactive");
    }
    
    El cellDelete = createCell(row, "column-delete");
    
    El div = new El("div");
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-application");
    cell.classList.add("cell-field");
    field = addField(cell, item.application(), null, false);
    tabIndex.setNext(field);
    
    
    cell = createCell(div, "column-details");
    cell.classList.add("cell-field");
    String detailsHref = item.details().value;

    
    field = new El("label");
    {
      El a = new El("a").setAttribute("href", detailsHref).setAttribute("title", "detali");// TODO NON-NLS
      El img = new El("img").setAttribute("src", "img/jk/details.png").addClass("button-details");
      a.appendChild(img);
      field.appendChild(a);
    }
    addField(cell, field);
    tabIndex.setNext(field);
    
    
    String titleDelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.delete");
    String titleUndelete = text.getString("org.jepria.web.ssr.table.buttonDelete.title.undelete");
    addFieldDelete(cellDelete, tabIndex, titleDelete, titleUndelete);
    
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

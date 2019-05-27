package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.JkMountTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.CheckBox;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.ItemData;
import org.jepria.web.ssr.fields.Table;

public class JkMountTable extends Table<Record> {

  public static class Record extends ItemData {
    private static final long serialVersionUID = 1L;
    
    public Record() {
      put("active", new Field("active"));
      put("application", new Field("application"));
      put("details", new Field("details"));
    }
    
    public Field active() {
      return get("active");
    }
    public Field application() {
      return get("application");
    }
    /**
     * Context-relative link to detail view
     * @return
     */
    public Field details() {
      return get("details");
    }
  }
  
  public JkMountTable(Context context) {
    super(context);
    addStyle("css/jk/jk.css");
  }
  
  @Override
  public El createRow(Record item, Table.TabIndex tabIndex) {
    El row = new El("div", context);
    row.classList.add("row");
    
    
    {
      Text text = context.getText();
      
      El cell = createCell(row, "column-active");
      cell.classList.add("column-left");
      cell.classList.add("cell-field");
      String titleCheckboxActive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.active");
      String titleCheckboxInactive = text.getString("org.jepria.web.ssr.Table.checkbox_active.title.inactive");
      CheckBox checkBox = addCheckbox(cell, item.active(), titleCheckboxActive, titleCheckboxInactive);
      tabIndex.setNext(checkBox.input);
      
      if ("false".equals(item.active().value)) {
        row.classList.add("inactive");
      }
    }
    
    El div = new El("div", context);
    div.classList.add("flexColumns");
    
    {
      El cell = createCell(div, "column-application");
      cell.classList.add("cell-field");
      El field = addField(cell, item.application(), null, false);
      tabIndex.setNext(field);
    }
    
    
    {
      El cell = createCell(div, "column-details");
      cell.classList.add("cell-field");
      String detailsHref = item.details().value;
      
      El field = new El("label", cell.context);
      {
        El a = new El("a", field.context)
            .setAttribute("href", context.getContextPath() + "/" + detailsHref)
            .setAttribute("title", "detali");// TODO NON-NLS
        El img = new El("img", field.context)
            .setAttribute("src", context.getContextPath() + "/img/jk/details.png")
            .addClass("button-details");
        a.appendChild(img);
        field.appendChild(a);
      }
      addField(cell, field);
      tabIndex.setNext(field);
      
      if (isEditable()) {
        addStrike(cell);
      }
    }
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  public El createRowCreated(Record item, Table.TabIndex tabIndex) {
    throw new UnsupportedOperationException();// TODO
  }

  @Override
  protected El createHeader() {
    El row = new El("div", context);
    row.classList.add("header");
    
    El cell, div, label;
    
    cell = createCell(row, "column-active");// empty cell
    cell.classList.add("column-left");
    
    div = new El("div", row.context);
    div.classList.add("flexColumns");
    
    cell = createCell(div, "column-application");
    label = new El("label", cell.context);
    label.setInnerHTML("apple");// TODO non-nls
    cell.appendChild(label);
    
    createCell(div, "column-details");
    
    row.appendChild(div);
    
    return row;
  }
  
  @Override
  protected boolean isEditable() {
    return false;
  }
}

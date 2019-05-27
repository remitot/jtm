package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.BindingDetailsTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.FieldTextLabel;
import org.jepria.web.ssr.fields.Fields;
import org.jepria.web.ssr.fields.ItemData;
import org.jepria.web.ssr.fields.Table;

public class BindingDetailsTable extends Table<Record> {
  
  public static class Record extends ItemData {
    private static final long serialVersionUID = 1L;

    private final String fieldLabel;
    private final String placeholder;
    
    public Record(String fieldLabel, String placeholder) {
      this.fieldLabel = fieldLabel;
      this.placeholder = placeholder;
      
      put("field", new Field("field"));
    }
    
    public String fieldLabel() {
      return fieldLabel;
    }
    
    public String placeholder() {
      return placeholder;
    }
    
    public Field field() {
      return get("field");
    }
  }
  
  public BindingDetailsTable(Context context) {
    super(context);
    
    addClass("table-details");
    
    addStyle("css/jk/jk.css");
  }
  
  @Override
  protected El createRow(Record item, TabIndex tabIndex) {
    El row = new El("div", context);
    row.addClass("row");
    
    {
      El cell = createCell(row, "column-label");
      
      FieldTextLabel label = new FieldTextLabel(cell.context);
      label.setInnerHTML(item.fieldLabel(), true);
      cell.appendChild(Fields.wrapCellPad(label));
    }
    
    {
      El cell = createCell(row, "column-field");
      cell.classList.add("cell-field");
    
      if ("active".equals(item.getId())) {
        addCheckbox(cell, item.field(), "act!", "inact!");// TODO NON-NLS NON-NLS
        
      } else {
        addField(cell, item.field(), item.placeholder());
      }
    }
    
    return row;
  }



  @Override
  protected El createRowCreated(Record item, TabIndex tabIndex) {
    return null;
  }

  @Override
  protected El createHeader() {
    return null;
  }
}


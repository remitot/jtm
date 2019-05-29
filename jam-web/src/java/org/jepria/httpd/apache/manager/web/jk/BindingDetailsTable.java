package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.BindingDetailsTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
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
    private String hint;
    
    public Record(String fieldLabel, String placeholder) {
      this.fieldLabel = fieldLabel;
      this.placeholder = placeholder;
      
      put("field", new Field("field"));
    }
    
    public void setHint(String hint) {
      this.hint = hint;
    }
    
    public String fieldLabel() {
      return fieldLabel;
    }
    
    public String placeholder() {
      return placeholder;
    }
    
    public String getHint() {
      return hint;
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
  protected El createRow(Record record, TabIndex tabIndex) {
    El row = new El("div", context);
    row.addClass("row");
    
    {
      El cell = createCell(row, "column-label");
      
      FieldTextLabel field = new FieldTextLabel(cell.context);
      field.setInnerHTML(record.fieldLabel(), true);
      cell.appendChild(Fields.wrapCellPad(field));
    }
    
    {
      El cell = createCell(row, "column-field");
      cell.classList.add("cell-field");
    
      if ("active".equals(record.getId())) {
        addCheckbox(cell, record.field(), "act!", "inact!");// TODO NON-NLS NON-NLS
        
      } else if ("link".equals(record.getId())) {
        
        cell.addClass("field-link");
        
        String href = record.field().value;
        El fieldEl = new FieldTextLabel(cell.context);
        String hrefEscaped = HtmlEscaper.escape(href);
        El a = new El("a", fieldEl.context)
            .setAttribute("href", hrefEscaped)
            .setAttribute("target", "_blank")
            .setInnerHTML(hrefEscaped, false);
        fieldEl.appendChild(a);
        El wrapper = Fields.wrapCellPad(fieldEl);
        cell.appendChild(wrapper);
        
      } else {
        addField(cell, record.field(), record.placeholder());
      }
    }
    
    String hint = record.getHint(); 
    if (hint != null) {
      El cell = createCell(row, "column-hint");
    
      El img = new El("img", cell.context).addClass("hint")
          .setAttribute("src", cell.context.getContextPath() + "/img/jk/hint.png")
          .setAttribute("title", HtmlEscaper.escape(hint));
      cell.appendChild(Fields.wrapCellPad(img));
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


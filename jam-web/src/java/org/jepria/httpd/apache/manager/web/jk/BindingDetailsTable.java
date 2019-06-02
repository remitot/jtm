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

    private String hint;
    
    // every details table record has ID
    public Record(String id) {
      super.setId(id);
      put("field", new Field("field"));
    }
    
    @Override
    public void setId(String id) {
      throw new UnsupportedOperationException("ID can only be set in constructor");
    }
    
    public void setHint(String hint) {
      this.hint = hint;
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
  
    
    final El cellLabel = createCell(row, "column-label");
    
    final El cell = createCell(row, "column-field");
    cell.classList.add("cell-field");
  

    switch(record.getId()) {
    case "active": {
      addFieldLabel(cellLabel, "Active"); // TODO NON-NLS
      addCheckbox(cell, record.field(), "act!", "inact!");// TODO NON-NLS NON-NLS
      break;
    }
    case "application": {
      addFieldLabel(cellLabel, "Application"); // TODO NON-NLS
      addField(cell, record.field(), "__application"); // TODO NON-NLS
      break;
    }
    case "workerName": {
      addFieldLabel(cellLabel, "Worker name"); // TODO NON-NLS
      addField(cell, record.field(), "__worker name"); // TODO NON-NLS
      break;
    }
    case "host": {
      addFieldLabel(cellLabel, "Host"); // TODO NON-NLS
      addField(cell, record.field(), "__host"); // TODO NON-NLS
      break;
    }
    case "ajpPort": {
      addFieldLabel(cellLabel, "AJP port"); // TODO NON-NLS
      addField(cell, record.field(), "800900"); // TODO NON-NLS
      break;
    }
    case "httpPort": {
      addFieldLabel(cellLabel, "HTTP port"); // TODO NON-NLS
      addField(cell, record.field(), "808000"); // TODO NON-NLS
      break;
    }
    case "link": {
      addFieldLabel(cellLabel, "Link"); // TODO NON-NLS
      
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
      
      break;
    }
    default: {
      throw new IllegalArgumentException(record.getId());
    }
    }
    
    
    String hint = record.getHint(); 
    if (hint != null) {
      El cellHint = createCell(row, "column-hint");
    
      El img = new El("img", cellHint.context).addClass("hint")
          .setAttribute("src", cellHint.context.getContextPath() + "/img/jk/hint.png")
          .setAttribute("title", HtmlEscaper.escape(hint));
      cellHint.appendChild(Fields.wrapCellPad(img));
    }
    
    return row;
  }
  
  protected void addFieldLabel(El cell, String label) {
    FieldTextLabel field = new FieldTextLabel(cell.context);
    field.setInnerHTML(label, true);
    cell.appendChild(Fields.wrapCellPad(field));
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


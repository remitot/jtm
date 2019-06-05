package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.BindingDetailsTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Text;
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
  
    Text text = context.getText();
    
    switch(record.getId()) {
    case "active": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.active"));
      addCheckbox(cell, record.get("field"), null, null);
      break;
    }
    case "application": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.application"));
      addField(cell, record.get("field"), "Application");
      break;
    }
    case "workerName": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.workerName"));
      addField(cell, record.get("field"), "worker");
      break;
    }
    case "host": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.host"));
      addField(cell, record.get("field"), "hostname");
      break;
    }
    case "ports": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.port"));
      
      // TODO awful workaround! (because the cell has been already added)
      row.childs.remove(cell);
      
      El cellAjpPortLabel = createCell(row, "column-ajp-port-label");
      addFieldLabel(cellAjpPortLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.port.ajp"));
      
      El cellAjpPort = createCell(row, "column-ajp-port");
      cellAjpPort.classList.add("cell-field");
      addField(cellAjpPort, record.get("ajpPort"), "8009");
      
      El cellHttpPortLabel = createCell(row, "column-http-port-label");
      addFieldLabel(cellHttpPortLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.port.or_http"));
      
      El cellHttpPort = createCell(row, "column-http-port");
      cellHttpPort.classList.add("cell-field");
      addField(cellHttpPort, record.get("httpPort"), "8080");
      
      break;
    }
    case "link": {
      addFieldLabel(cellLabel, text.getString("org.jepria.httpd.apache.manager.web.jk.Details.label.link"));
      
      cell.addClass("field-link");
      String href = record.get("field").value;
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


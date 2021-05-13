package org.jepria.tomcat.manager.web.log;

import org.jepria.tomcat.manager.web.log.LogTable.Record;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Text;
import org.jepria.web.ssr.fields.Field;
import org.jepria.web.ssr.fields.FieldTextLabel;
import org.jepria.web.ssr.fields.ItemData;
import org.jepria.web.ssr.fields.Table;

public class LogTable extends Table<Record> {

  public static class Record extends ItemData {
    
    private static final long serialVersionUID = 1L;

    public Record() {
      put("name", new Field("name"));
      put("lastmod", new Field("lastmod"));
      put("size", new Field("size"));
      put("download", new Field("download"));
      put("open", new Field("open"));
      put("monitor", new Field("monitor"));
    }
    
    public Field name() {
      return get("name");
    }
    
    public Field lastmod() {
      return get("lastmod");
    }
    
    public Field size_() {
      return get("size");
    }
    
    public Field download() {
      return get("download");
    }
    
    public Field open() {
      return get("open");
    }
    
    public Field monitor() {
      return get("monitor");
    }
    
    /**
     * Is the log file denoted by this record of a large size
     */
    public boolean largeFile = false;
    
    public String sizeHint = null;
  }

  
  public LogTable(Context context) {
    super(context);
    addStyle("css/log/log.css");
  }
  
  @Override
  public El createRow(Record item, Table.TabIndex tabIndex) {
    
    Text text = context.getText();
    
    final El row = new El("div", context);
    row.classList.add("row");
    
    El cell, field;
    
    El div = new El("div", row.context);
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-name");
    cell.classList.add("cell-field");
    addField(cell, item.name(), null);
    
    cell = createCell(div, "column-lastmod");
    cell.classList.add("cell-field");
    field = new FieldTextLabel(cell.context, item.lastmod().value); 
    addField(cell, field);

    String largeFileHintTitle = null;
    {
      if (item.largeFile) {
        largeFileHintTitle = text.getString("org.jepria.tomcat.manager.web.log.item.largeFile") + " (" + item.sizeHint + ")";
      }
    }

    cell = createCell(div, "column-size");
    cell.classList.add("cell-field");
    final String html;
    {
      String valueEsc = HtmlEscaper.escape(item.size_().value, true);
      if (item.largeFile) {
        html = "<b class=\"b_large-file\""
                + (item.largeFile && largeFileHintTitle != null ? (" title=\"" + largeFileHintTitle + "\"") : "")
                + ">" + valueEsc + "</b>";
      } else {
        html = valueEsc;
      }
    }
    field = new FieldTextLabel(cell.context, html); 
    addField(cell, field);
    
    cell = createCell(div, "column-download");
    cell.classList.add("cell-field");
    field = new FieldTextLabel(cell.context);
    {
      El a = new El("a", field.context).setAttribute("href", context.getAppContextPath() + "/" + item.download().value)
          .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_download.title"))
          .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_download.text"));
      field.appendChild(a);
      
      if (item.largeFile) {
        El img = new El("img", field.context).addClass("field-text__hint_large-file")
            .setAttribute("src", field.context.getAppContextPath() + "/img/log/hint.png");
        if (largeFileHintTitle != null) {
          img.setAttribute("title", largeFileHintTitle);
        }
        field.appendChild(img);
      }
    }
    addField(cell, field);
    
    cell = createCell(div, "column-open");
    cell.classList.add("cell-field");
    field = new FieldTextLabel(cell.context);
    {
      El a = new El("a", field.context).setAttribute("href", context.getAppContextPath() + "/" + item.open().value)
          .setAttribute("target", "_blank")
          .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_open.title"))
          .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_open.text"));
      field.appendChild(a);
      
      if (item.largeFile) {
        String hintTitle = text.getString("org.jepria.tomcat.manager.web.log.item.largeFile") + " (" + item.sizeHint + ")";
        El img = new El("img", field.context).addClass("field-text__hint_large-file")
            .setAttribute("src", field.context.getAppContextPath() + "/img/log/hint.png")
            .setAttribute("title", hintTitle);
        field.appendChild(img);
      }
    }
    addField(cell, field);
    
    cell = createCell(div, "column-monitor");
    cell.classList.add("cell-monitor");
    field = new FieldTextLabel(cell.context);
    {
      El a = new El("a", field.context).setAttribute("href", context.getAppContextPath() + "/" + item.monitor().value)
          .setAttribute("target", "_blank")
          .setAttribute("title", text.getString("org.jepria.tomcat.manager.web.log.item_monitor.title"))
          .setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.item_monitor.text"));
      field.appendChild(a);
    }
    addField(cell, field);
    
    row.appendChild(div);
    
    return row;
  }

  @Override
  public El createRowCreated(Record item, Table.TabIndex tabIndex) {
    // the table is unmodifiable and must not allow creating rows
    throw new UnsupportedOperationException();
  }

  @Override
  protected El createHeader() {
    
    Text text = context.getText();
    
    final El row = new El("div", context);
    row.classList.add("header");
    
    El div, cell, label;
    
    div = new El("div", row.context);
    div.classList.add("flexColumns");
    div.classList.add("column-left");
    
    cell = createCell(div, "column-name");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_name"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-lastmod");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_lastmod"));
    cell.appendChild(label);
    
    cell = createCell(div, "column-size");
    label = new El("label", cell.context);
    label.setInnerHTML(text.getString("org.jepria.tomcat.manager.web.log.Table.header.column_size"));
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

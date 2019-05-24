package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.List;

import org.jepria.httpd.apache.manager.web.jk.BindingDetailsTable.Record;
import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
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
  
  public BindingDetailsTable() {
    addClass("table-details");
    addStyle("css/jk/jk.css");
  }
  
  public void load(JkMountDto mount, WorkerDto worker) {
    
    List<Record> items = new ArrayList<>();
    
    // mount fields
    {
      Record item = new Record("Active", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("active"));
      item.setId("active");
      items.add(item);
    }
      
//      El cellDelete = new El("div").addClass("cell column-delete cell_field");// TODO stopped here: remove column-delete from non-table
//      addFieldDelete(cellDelete, "del", "und");
//      
//          
//      El row = new El("div");
//      row.addClass("row");
//      
//      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
//      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
//      row.appendChild(cellDelete);
//      
//      appendChild(row);
    {
      Record item = new Record("Application", null); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (mount == null ? null : mount.map.get("application"));
      item.setId("application");
      items.add(item);
    }
    
    // worker fields
    {
      Record item = new Record("Worker", "worker1"); // TODO NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("name"));
      item.setId("workerName");
      items.add(item);
    }
    {
      Record item = new Record("Type", "ajp13"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("type"));
      item.setId("workerType");
      items.add(item);
    }
    {
      Record item = new Record("Host", "server.com"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("host"));
      item.setId("workerHost");
      items.add(item);
    }
    {
      Record item = new Record("Port", "8080"); // TODO NON-NLS NON-NLS
      item.field().value = item.field().valueOriginal = (worker == null ? null : worker.map.get("port"));
      item.setId("workerPort");
      items.add(item);
    }
    
    load(items, null, null);
  }
  
//  protected El addFieldDelete(El cell, String titleDelete, String titleUndelete) {
//
//    El field = new El("div");
//    
//    El buttonDelete = new El("input");
//    buttonDelete.classList.add("button-delete");
//    buttonDelete.classList.add("button-delete_delete");
//    buttonDelete.setAttribute("type", "image");
//    buttonDelete.setAttribute("src", "img/delete.png");
//    if (titleDelete != null) {
//      buttonDelete.setAttribute("title", titleDelete);
//    }
//    
//    El buttonUndelete = new El("input");
//    buttonUndelete.classList.add("button-delete");
//    buttonUndelete.classList.add("button-delete_undelete");
//    buttonUndelete.setAttribute("type", "image");
//    buttonUndelete.setAttribute("src", "img/undelete.png");
//    if (titleUndelete != null) {
//      buttonUndelete.setAttribute("title", titleUndelete);
//    }
//    
//    field.appendChild(buttonDelete);
//    field.appendChild(buttonUndelete);
//    
//    El wrapper = Fields.wrapCellPad(field);  
//    cell.appendChild(wrapper);
//    
//    return field;
//  }



  @Override
  protected El createRow(Record item, TabIndex tabIndex) {
    El row = new El("div");
    row.addClass("row");
    
    {
      El cell = createCell(row, "column-label");
      
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML(item.fieldLabel(), true);
      cell.appendChild(Fields.wrapCellPad(label));
    }
    
    {
      El cell = createCell(row, "column-field");
    
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


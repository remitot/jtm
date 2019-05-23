package org.jepria.httpd.apache.manager.web.jk;

import org.jepria.httpd.apache.manager.web.jk.dto.JkMountDto;
import org.jepria.httpd.apache.manager.web.jk.dto.WorkerDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.fields.FieldCheckBox;
import org.jepria.web.ssr.fields.FieldTextInput;
import org.jepria.web.ssr.fields.FieldTextLabel;
import org.jepria.web.ssr.fields.Fields;

public class JkDetails extends El {
  
  public JkDetails() {
    super("div");
    
    addClass("details");
    setAttribute("id", "table");
    
    addStyle("css/table.css");// TODO refactor: table.css must be required for Table.java descendants only
    addStyle("css/jk/jk.css");
  }
  
  public void load(JkMountDto mount, WorkerDto worker) {
    
    // mount fields
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("active", true);// TODO NON-NLS
      label.addClass("field-label");
      
      boolean value;
      if (mount != null) {
        value = !"false".equals(mount.map.get("active"));
      } else {
        value = true;
      }
      FieldCheckBox field = new FieldCheckBox(
          "active", value, value, false, null);
      
      El cellDelete = new El("div").addClass("cell column-delete cell_field");// TODO stopped here: remove column-delete from non-table
      addFieldDelete(cellDelete, "del", "und");
      
          
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      row.appendChild(cellDelete);
      
      appendChild(row);
    }
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("application", true);// TODO NON-NLS
      label.addClass("field-label");
      
      String value;
      if (mount != null) {
        value = mount.map.get("application");
      } else {
        value = null;
      }
      FieldTextInput field = new FieldTextInput(
          "application", value, value, "Application", false, null);// TODO NON-NLS placeholder
      
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      
      appendChild(row);
    }
    
    
    // worker fields
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("worker name", true);// TODO NON-NLS
      label.addClass("field-label");
      
      String value;
      if (worker != null) {
        value = worker.map.get("name");
      } else {
        value = null;
      }
      FieldTextInput field = new FieldTextInput(
          "worker-name", value, value, "worker-tomcat-8080", false, null);// TODO NON-NLS placeholder
      
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      
      appendChild(row);
    }
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("worker host", true);// TODO NON-NLS
      label.addClass("field-label");
      
      String value;
      if (worker != null) {
        value = worker.map.get("host");
      } else {
        value = null;
      }
      FieldTextInput field = new FieldTextInput(
          "worker-host", value, value, "server.com", false, null);// TODO NON-NLS placeholder
      
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      
      appendChild(row);
    }
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("worker protocol", true);// TODO NON-NLS
      label.addClass("field-label");
      
      String value;
      if (worker != null) {
        value = worker.map.get("type");
      } else {
        value = null;
      }
      FieldTextInput field = new FieldTextInput(
          "worker-type", value, value, "ajp13", false, null);// TODO NON-NLS placeholder
      
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      
      appendChild(row);
    }
    {
      FieldTextLabel label = new FieldTextLabel();
      label.setInnerHTML("worker port", true);// TODO NON-NLS
      label.addClass("field-label");
      
      String value;
      if (worker != null) {
        value = worker.map.get("port");
      } else {
        value = null;
      }
      FieldTextInput field = new FieldTextInput(
          "worker-port", value, value, "8009", false, null);// TODO NON-NLS placeholder
      
      El row = new El("div");
      row.addClass("row");
      
      row.appendChild(new El("div").addClass("cell cell_field-label").appendChild(Fields.wrapCellPad(label)));
      row.appendChild(new El("div").addClass("cell cell_field").appendChild(Fields.wrapCellPad(field)));
      
      appendChild(row);
    }
  }
  
  protected El addFieldDelete(El cell, String titleDelete, String titleUndelete) {

    El field = new El("div");
    
    El buttonDelete = new El("input");
    buttonDelete.classList.add("button-delete");
    buttonDelete.classList.add("button-delete_delete");
    buttonDelete.setAttribute("type", "image");
    buttonDelete.setAttribute("src", "img/delete.png");
    if (titleDelete != null) {
      buttonDelete.setAttribute("title", titleDelete);
    }
    
    El buttonUndelete = new El("input");
    buttonUndelete.classList.add("button-delete");
    buttonUndelete.classList.add("button-delete_undelete");
    buttonUndelete.setAttribute("type", "image");
    buttonUndelete.setAttribute("src", "img/undelete.png");
    if (titleUndelete != null) {
      buttonUndelete.setAttribute("title", titleUndelete);
    }
    
    field.appendChild(buttonDelete);
    field.appendChild(buttonUndelete);
    
    El wrapper = Fields.wrapCellPad(field);  
    cell.appendChild(wrapper);
    
    return field;
  }
}


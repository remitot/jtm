package org.jepria.web.ssr.fields;

import java.util.List;

import org.jepria.web.ssr.El;

public class Details extends El {
  
  public Details() {
    super("div");
    
    addStyle("css/common.css"); // for .field-text
  }
  
  public static class FieldWithLabel {
    public final String label;
    public final Field field;
    
    public FieldWithLabel(String label, Field field) {
      this.label = label;
      this.field = field;
    }
  }
  
  public void load(List<FieldWithLabel> fields) {
    if (fields != null) {
      for (FieldWithLabel fieldWithLabel: fields) {
        El row = new El("div");
        String label = fieldWithLabel.label;
        if (label != null) {
          El cellLabel = new El("div");
          El fieldEl = new FieldTextLabel(label);
          Fields.addField(cellLabel, fieldEl);
          row.appendChild(cellLabel);
        }
        Field field = fieldWithLabel.field;
        if (field != null) {
          El cellField = new El("div");
          Fields.addField(cellField, field, null, false);
          row.appendChild(cellField);
        }
        appendChild(row);
      }
    }
  }
}

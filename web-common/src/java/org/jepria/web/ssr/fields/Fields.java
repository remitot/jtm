package org.jepria.web.ssr.fields;

import org.jepria.web.ssr.El;

public class Fields {
  
  public static El wrapCellPad(El element) {
    El cellPadding = new El("div").addClass("cell__padding");
    cellPadding.appendChild(element);
    return cellPadding;
  }
}

package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Node;

public class BindingDetailsPageContent implements Iterable<Node> {

  private final Iterable<Node> elements;
  
  @Override
  public Iterator<Node> iterator() {
    return elements.iterator();
  }
  
  /**
   * Creates content for details of a newly created binding
   * @param context
   * @param binding
   */
  public BindingDetailsPageContent(Context context, List<BindingDetailsTable.Record> records) {
    final List<El> elements = new ArrayList<>();
    
    BindingDetailsTable details = new BindingDetailsTable(context);
    details.load(records, null, null);
    
    elements.add(details);
    
    
    final BindingDetailsControlButtons controlButtons = new BindingDetailsControlButtons(context);
    controlButtons.addButtonSave(context.getContextPath() + "/jk/new-binding");// TODO such url will erase any path- or request params of the current page
    elements.add(controlButtons);
    
    
    this.elements = Collections.unmodifiableList(elements);
  }
  
  /**
   * Creates content for details
   * @param context
   * @param binding
   */
  public BindingDetailsPageContent(Context context, List<BindingDetailsTable.Record> records, String mountId) {
    final List<El> elements = new ArrayList<>();
    
    BindingDetailsTable details = new BindingDetailsTable(context);
    details.load(records, null, null);
    
    elements.add(details);
    
    
    // control buttons
    final BindingDetailsControlButtons controlButtons = new BindingDetailsControlButtons(context);
    controlButtons.addButtonSave(context.getContextPath() + "/jk/" + mountId + "/mod");// TODO such url will erase any path- or request params of the current page
    controlButtons.addButtonDelete(context.getContextPath() + "/jk/" + mountId + "/del");// TODO such url will erase any path- or request params of the current page
    elements.add(controlButtons);
    

    this.elements = Collections.unmodifiableList(elements);
  }
}

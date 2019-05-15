package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jepria.httpd.apache.manager.web.jk.dto.BindingDto;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public class JkPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  public JkPageContent(Text text, List<BindingDto> ports) {
    
    final List<El> elements = new ArrayList<>();
    
    // table html
    El table = new El("div").setAttribute("style", "width:100px;height:100px;background-color:blue;");
    
    elements.add(table);

    this.elements = Collections.unmodifiableList(elements);
  }
}

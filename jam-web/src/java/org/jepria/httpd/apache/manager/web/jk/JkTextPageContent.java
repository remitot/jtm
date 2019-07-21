package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.ControlButtons;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.Text;

public class JkTextPageContent implements Iterable<El> {

  private final Iterable<El> elements;
  
  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }
  
  public JkTextPageContent(Context context, List<String> lines, List<String> itemModRequestLines,
      CurrentMenuItem currentMenuItem) {
    
    Text text = context.getText();
    
    final List<El> elements = new ArrayList<>();
    
    {
      El textContentWrapper = new El("div", context);
      textContentWrapper.classList.add("text-content-wrapper");
      
      {
        El textarea = new El("textarea", context);
        textarea.classList.add("text-content");
        textarea.addScript("js/jk/jk.js");
        
        StringBuilder innerHTMLSb = new StringBuilder();
        for (String line: (itemModRequestLines != null ? itemModRequestLines : lines)) {
          innerHTMLSb.append(line).append("\n");
        }
        textarea.setInnerHTML(innerHTMLSb.toString());
        
        if (itemModRequestLines != null) {
          textarea.classList.add("modified");
        }
        
        textContentWrapper.appendChild(textarea);
      }
      
      elements.add(textContentWrapper);
    }
    
    
    { // control buttons
      final ControlButtons controlButtons = new ControlButtons(context);
      
      final String saveActionUrl;
      final String resetActionUrl;
      if (currentMenuItem == CurrentMenuItem.JK_MODJK) {
        saveActionUrl = context.getContextPath() + "/jk/mod_jk/mod";// TODO such url will erase any path- or request params of the current page
        resetActionUrl = context.getContextPath() + "/jk/mod_jk";// TODO such url will erase any path- or request params of the current page
      } else if (currentMenuItem == CurrentMenuItem.JK_WORKERS) {
        saveActionUrl = context.getContextPath() + "/jk/workers/mod";// TODO such url will erase any path- or request params of the current page
        resetActionUrl = context.getContextPath() + "/jk/workers";// TODO such url will erase any path- or request params of the current page
      } else {
        throw new IllegalArgumentException();
      }
      
      El buttonSave = controlButtons.addButtonSave(saveActionUrl);
      // override the button title
      buttonSave.setAttribute("org.jepria.web.ssr.ControlButtons.buttonSave.title.save", text.getString("org.jepria.httpd.apache.manager.web.jk.ControlButtons.buttonSave.title.save"));
      
      El buttonReset = controlButtons.addButtonReset(resetActionUrl);
      // override the button title
      buttonReset.setAttribute("org.jepria.web.ssr.ControlButtons.buttonReset.title.reset", text.getString("org.jepria.httpd.apache.manager.web.jk.ControlButtons.buttonReset.title.reset"));
      
      // overrides default (common) control-buttons.css
      controlButtons.addStyle("css/jk/jk-control-buttons.css");
      
      elements.add(controlButtons);
    }
    
    
    if (elements.size() > 0) {
      elements.iterator().next().addStyle("css/jk/jk.css");
    }
    
    this.elements = Collections.unmodifiableList(elements);
  }
}

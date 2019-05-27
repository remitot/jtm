package org.jepria.tomcat.manager.web.logmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Text;

public class LogMonitorPageContent implements Iterable<El> {

  private final Iterable<El> elements;

  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }

  /**
   * @param context
   * @param contentLinesTop log file content lines to be displayed above the split anchor
   * @param contentLinesBottom log content lines to be displayed below the split anchor
   * @param loadMoreLinesUrl the url to load more lines, relative to the current url
   * (and thus monitor state). {@code null} if no more line can be loaded 
   * (the beginning of the file has been reached)
   * @param loadMoreLinesPortion number or lines which about to be loaded
   * by {@code loadMoreLinesUrl} (to display the number on the control).
   * If {@code loadMoreLinesUrl} is {@code null}, then the value does not matter 
   * @param resetAnchorUrl the url to reset the current anchor, relative to the current url 
   * (and thus monitor state). {@code null} if the current anchor cannot be reset
   */
  public LogMonitorPageContent(Context context,
      List<String> contentLinesTop,
      List<String> contentLinesBottom,
      String loadMoreLinesUrl,
      int loadMoreLinesPortion,
      String resetAnchorUrl) {

    Text text = context.getText();
    
    final List<El> elements = new ArrayList<>();

    final boolean hasLinesTop = contentLinesTop != null && !contentLinesTop.isEmpty();
    final boolean hasLinesBottom = contentLinesBottom != null && !contentLinesBottom.isEmpty();
    final boolean canLoadMoreLines = loadMoreLinesUrl != null;
    final boolean canResetAnchor = hasLinesBottom && resetAnchorUrl != null;

    {
      final El controlTop = new El("button", context);
      controlTop.classList.add("control-top");
  
      if (canLoadMoreLines) {
        controlTop.classList.add("control-top__load-more-lines");
        controlTop.setAttribute("onclick", "onControlTopClick();");
        String innerHtml = String.format(
            text.getString("org.jepria.tomcat.manager.web.logmonitor.load_more_lines"), 
            loadMoreLinesPortion);
        controlTop.setInnerHTML(innerHtml);
      } else {
        if (hasLinesTop || hasLinesBottom) {
          controlTop.classList.add("control-top__file-begin-reached");
          controlTop.setAttribute("disabled");
          controlTop.setInnerHTML(text.getString(
              "org.jepria.tomcat.manager.web.logmonitor.file_begin_reached"));
        } else {
          controlTop.setAttribute("disabled");
          controlTop.setInnerHTML(text.getString(
              "org.jepria.tomcat.manager.web.logmonitor.file_empty"));
        }
      }
      
      elements.add(controlTop);
    }

    
    {
      El mainDiv = new El("div", context);
      {
        El anchorArea = new El("div", mainDiv.context)
            .addClass("anchor-area");
        {
          if (hasLinesTop) {
            El anchorAreaTop = new El("div", anchorArea.context)
                .addClass("anchor-area__panel").addClass("top")
                .setInnerHTML("&nbsp;");
            anchorArea.appendChild(anchorAreaTop);
          }
          if (hasLinesBottom) {
            El anchorAreaBtm = new El("div", anchorArea.context)
                .addClass("anchor-area__panel").addClass("bottom")
                .setInnerHTML("&nbsp;");
            anchorArea.appendChild(anchorAreaBtm);
          }
        }
        mainDiv.appendChild(anchorArea);
      }
      
      {
        El contentArea = new El("div", mainDiv.context)
            .addClass("content-area");
        
        if (hasLinesTop) {
          El contentAreaTop = new El("div", contentArea.context)
              .addClass("content-area__lines").addClass("top");
          
          StringBuilder innerHtml = new StringBuilder();
          for (String line: contentLinesTop) {
            try {
              HtmlEscaper.escape(line, innerHtml, true);
            } catch (IOException e) {
              // impossible
              throw new RuntimeException(e);
            }
            innerHtml.append("<br/>");      
          }
          contentAreaTop.setInnerHTML(innerHtml.toString());
          
          contentArea.appendChild(contentAreaTop);
        }
        if (hasLinesBottom) {
          El contentAreaBtm = new El("div", contentArea.context)
              .addClass("content-area__lines").addClass("bottom");
          
          StringBuilder innerHtml = new StringBuilder();
          for (String line: contentLinesBottom) {
            try {
              HtmlEscaper.escape(line, innerHtml, true);
            } catch (IOException e) {
              // impossible
              throw new RuntimeException(e);
            }
            innerHtml.append("<br/>");
          }
          
          contentAreaBtm.setInnerHTML(innerHtml.toString());
          
          contentArea.appendChild(contentAreaBtm);
        }
        mainDiv.appendChild(contentArea);
      }
      
      {
        El resetAnchorButton = new El("button", mainDiv.context)
            .addClass("control-button_reset-anchor").addClass("control-button")
            .addClass("big-black-button").addClass("hidden")
            .setAttribute("onclick", "onResetAnchorButtonClick();")
            .setAttribute("title", text.getString(
                "org.jepria.tomcat.manager.web.logmonitor.buttonResetAnchor.title"))
            .setInnerHTML(text.getString(
                "org.jepria.tomcat.manager.web.logmonitor.buttonResetAnchor.text"));
        
        resetAnchorButton.addStyle("css/common.css");
        resetAnchorButton.addStyle("css/control-buttons.css");
        resetAnchorButton.addScript("js/common.js");
        
        mainDiv.appendChild(resetAnchorButton);
      }

      mainDiv.addStyle("css/log-monitor/log-monitor.css");
      mainDiv.addScript("js/log-monitor/log-monitor.js");
      
      elements.add(mainDiv);
    }
    
    
    {
      El script = new El("script", context)
          .setAttribute("type", "text/javascript");
      
      // constants for using in log-monitor.js
      StringBuilder innerHtml = new StringBuilder();
      
      innerHtml.append("var logmonitor_linesTop = document.querySelectorAll(\".content-area__lines.top\")[0];");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_linesBottom = document.querySelectorAll(\".content-area__lines.bottom\")[0];");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_loadMoreLinesUrl = \"" + loadMoreLinesUrl + "\";");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_canResetAnchor = " + canResetAnchor + ";");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_resetAnchorUrl = \"" + resetAnchorUrl + "\";");
      innerHtml.append("\n");
      
      script.setInnerHTML(innerHtml.toString());
      
      elements.add(script);
    }
    
    
    this.elements = elements;
  }
}

package org.jepria.tomcat.manager.web.logmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jepria.web.ssr.El;
import org.jepria.web.ssr.HtmlEscaper;
import org.jepria.web.ssr.Text;

public class LogMonitorPageContent implements Iterable<El> {

  private final Iterable<El> elements;

  @Override
  public Iterator<El> iterator() {
    return elements.iterator();
  }

  public LogMonitorPageContent(Text text, Params params) {

    final List<El> elements = new ArrayList<>();

    final boolean hasLinesTop = !params.contentLinesTop.isEmpty();
    final boolean hasLinesBottom = !params.contentLinesBottom.isEmpty();
    final boolean canResetAnchor = hasLinesBottom && params.resetAnchorUrl != null;

    {
      final El controlTop = new El("button");
      controlTop.classList.add("control-top");
  
      if (params.canLoadMoreLines) {
        controlTop.classList.add("control-top__load-more-lines");
        controlTop.setAttribute("onclick", "onControlTopClick();");
        controlTop.setInnerHTML("ЗАГРУЗИТЬ ЕЩЁ 500 СТРОК"); //NON-NLS
      } else {
        if (hasLinesTop || hasLinesBottom) {
          controlTop.classList.add("control-top__file-begin-reached");
          controlTop.setAttribute("disabled");
          controlTop.setInnerHTML("НАЧАЛО ФАЙЛА"); //NON-NLS
        } else {
          controlTop.setAttribute("disabled");
          controlTop.setInnerHTML("ФАЙЛ ПУСТ"); //NON-NLS
        }
      }
      
      elements.add(controlTop);
    }

    
    {
      El mainDiv = new El("div");
      {
        El anchorArea = new El("div")
            .addClass("anchor-area");
        {
          if (hasLinesTop) {
            El anchorAreaTop = new El("div")
                .addClass("anchor-area__panel").addClass("top")
                .setInnerHTML("&nbsp;");
            anchorArea.appendChild(anchorAreaTop);
          }
          if (hasLinesBottom) {
            El anchorAreaBtm = new El("div")
                .addClass("anchor-area__panel").addClass("bottom")
                .setInnerHTML("&nbsp;");
            anchorArea.appendChild(anchorAreaBtm);
          }
        }
        mainDiv.appendChild(anchorArea);
      }
      
      {
        El contentArea = new El("div")
            .addClass("content-area");
        
        if (hasLinesTop) {
          El contentAreaTop = new El("div")
              .addClass("content-area__lines").addClass("top");
          
          StringBuilder innerHtml = new StringBuilder();
          for (String line: params.contentLinesTop) {
            try {
              HtmlEscaper.escapeAndWrite(line, innerHtml);
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
          El contentAreaBtm = new El("div")
              .addClass("content-area__lines").addClass("bottom");
          
          StringBuilder innerHtml = new StringBuilder();
          for (String line: params.contentLinesBottom) {
            try {
              HtmlEscaper.escapeAndWrite(line, innerHtml);
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
        El resetAnchorButton = new El("button")
            .addClass("control-button_reset-anchor").addClass("control-button")
            .addClass("big-black-button").addClass("hidden")
            .setAttribute("onclick", "onResetAnchorButtonClick();")
            .setAttribute("title", "Снять подсветку с новых записей") // NON-NLS
            .setInnerHTML("ПРОЧИТАНО");// // NON-NLS
        
        resetAnchorButton.addStyle("css/jtm-common.css");
        resetAnchorButton.addStyle("css/control-buttons.css");
        resetAnchorButton.addScript("js/jtm-common.js");
        
        mainDiv.appendChild(resetAnchorButton);
      }

      mainDiv.addStyle("css/log-monitor/log-monitor.css");
      mainDiv.addScript("js/log-monitor/log-monitor.js");
      
      elements.add(mainDiv);
    }
    
    
    {
      El script = new El("script")
          .setAttribute("type", "text/javascript");
      
      // constants for using in log-monitor.js
      StringBuilder innerHtml = new StringBuilder();
      
      innerHtml.append("var logmonitor_linesTop = document.querySelectorAll(\".content-area__lines.top\")[0];");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_linesBottom = document.querySelectorAll(\".content-area__lines.bottom\")[0];");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_loadMoreLinesUrl = \"" + params.loadMoreLinesUrl + "\";");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_canResetAnchor = " + canResetAnchor + ";");
      innerHtml.append("\n");
      innerHtml.append("var logmonitor_resetAnchorUrl = \"" + params.resetAnchorUrl + "\";");
      innerHtml.append("\n");
      
      script.setInnerHTML(innerHtml.toString());
      
      elements.add(script);
    }
    
    
    this.elements = elements;
  }
  
  public static class Params {
    /**
     * Non-null, maybe empty. Log content lines to be displayed above the split anchor.
     */
    public final List<String> contentLinesTop;
    /**
     * Non-null, maybe empty. Log content lines to be displayed below the split anchor.
     */
    public final List<String> contentLinesBottom;
    /**
     * Non-null. Whether it is possible to load more lines (whether the beginning of the file has not been reached).
     */
    public final boolean canLoadMoreLines;
    /**
     * Nullable. If the log monitor client window can be scrolled up (to load the next portion of top lines), then the value is the url to load.
     */
    public final String loadMoreLinesUrl;
    /**
     * Nullable. If the displayed anchor can be reset (to the last loaded line, then the value is the url to reset.
     */
    public final String resetAnchorUrl;
    
    public Params(List<String> contentLinesTop, List<String> contentLinesBottom,
        boolean canLoadMoreLines, String loadTopUrl, String resetAnchorUrl) {
      this.contentLinesTop = contentLinesTop;
      this.contentLinesBottom = contentLinesBottom;
      this.canLoadMoreLines = canLoadMoreLines;
      this.loadMoreLinesUrl = loadTopUrl;
      this.resetAnchorUrl = resetAnchorUrl;
    }
  }
}

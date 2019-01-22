package org.jepria.tomcat.manager.web.logmonitor;

import java.util.List;

public class InitMonitorResultDto {
  
  private int anchorLine;
  private List<String> contentLinesBeforeAnchor;
  
  public InitMonitorResultDto() {}

  public int getAnchorLine() {
    return anchorLine;
  }

  public void setAnchorLine(int anchorLine) {
    this.anchorLine = anchorLine;
  }

  public List<String> getContentLinesBeforeAnchor() {
    return contentLinesBeforeAnchor;
  }

  public void setContentLinesBeforeAnchor(List<String> contentLinesBeforeAnchor) {
    this.contentLinesBeforeAnchor = contentLinesBeforeAnchor;
  }
  
  
  
  
}

package org.jepria.tomcat.manager.web.logmonitor;

import java.util.List;

public class MonitorResultDto {
  
  private List<String> contentLinesBeforeAnchor;
  private List<String> contentLinesAfterAnchor;
  
  public MonitorResultDto() {}

  public List<String> getContentLinesBeforeAnchor() {
    return contentLinesBeforeAnchor;
  }

  public void setContentLinesBeforeAnchor(List<String> contentLinesBeforeAnchor) {
    this.contentLinesBeforeAnchor = contentLinesBeforeAnchor;
  }

  public List<String> getContentLinesAfterAnchor() {
    return contentLinesAfterAnchor;
  }

  public void setContentLinesAfterAnchor(List<String> contentLinesAfterAnchor) {
    this.contentLinesAfterAnchor = contentLinesAfterAnchor;
  }
  
  
}

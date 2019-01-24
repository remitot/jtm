package org.jepria.tomcat.manager.web.logmonitor;

import java.util.List;

public class MonitorGuiParams {
  private final String filename;
  private final String host;
  private final List<String> contentLinesTop;
  private final List<String> contentLinesBottom;
  private final boolean fileBeginReached;
  private final String loadTopUrl;
  private final String resetAnchorUrl;
  
  public MonitorGuiParams(String filename, String host, List<String> contentLinesTop, List<String> contentLinesBottom,
      boolean fileBeginReached, String loadTopUrl, String resetAnchorUrl) {
    this.filename = filename;
    this.host = host;
    this.contentLinesTop = contentLinesTop;
    this.contentLinesBottom = contentLinesBottom;
    this.fileBeginReached = fileBeginReached;
    this.loadTopUrl = loadTopUrl;
    this.resetAnchorUrl = resetAnchorUrl;
  }

  public String getFilename() {
    return filename;
  }

  public String getHost() {
    return host;
  }

  public List<String> getContentLinesTop() {
    return contentLinesTop;
  }

  public List<String> getContentLinesBottom() {
    return contentLinesBottom;
  }

  public boolean isFileBeginReached() {
    return fileBeginReached;
  }

  public String getLoadTopUrl() {
    return loadTopUrl;
  }

  public String getResetAnchorUrl() {
    return resetAnchorUrl;
  }
  
  
}

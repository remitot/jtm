package org.jepria.tomcat.manager.web.logmonitor;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class MonitorView {

  protected final Reader fileReader;
  public int linesAboveAnchor;

  public MonitorView(Reader fileReader, int linesAboveAnchor) {
    this(fileReader, linesAboveAnchor, (Integer)null);
  }

  public MonitorView(Reader fileReader, int linesAboveAnchor, int anchorLine) {
    this(fileReader, linesAboveAnchor, (Integer)anchorLine);
  }

  private MonitorView(Reader fileReader, int linesAboveAnchor, Integer anchorLine) {
    this.fileReader = fileReader;
    this.linesAboveAnchor = linesAboveAnchor;
    this.anchorLine = anchorLine;
    init();
  }

  public Integer anchorLine;
  public boolean fileBeginReached = true;
  public boolean fileEndReached = false;
  public Integer linesInFile = null;

  public List<String> contentLinesAboveAnchor;
  public List<String> contentLinesBelowAnchor;

  private void init() {

    if (linesAboveAnchor < 1) {
      throw new IllegalArgumentException();
    }

    final LinkedList<String> contentLines = new LinkedList<String>();
    int anchorSplitIndex = 0;


    // total char count
    long charCount = 0;

    int lineIndex = 0;

    try (BufferedReader reader = new BufferedReader(fileReader)) {

      String line;

      // load lines above the anchor
      while (true) {

        line = reader.readLine();
        if (line == null) {
          // file end reached
          break;
        }

        lineIndex++;

        if (anchorLine == null || anchorLine < 1 || lineIndex <= anchorLine) {
          contentLines.add(line);
          charCount += line.length();

          if (contentLines.size() > linesAboveAnchor) {
            String removed = contentLines.removeFirst();
            charCount -= removed.length();
            fileBeginReached = false;
          }
        } else {
          // enough lines loaded
          break;
        }
      }


      // set the anchor
      if (contentLines.size() > 0) {
        anchorSplitIndex = contentLines.size();
      }


      // check load limit
      if (charCount > LOAD_LIMIT) {
        // TODO no throw, crop the load until the limit succeeds
        throw new RuntimeException("Load limit overflow");
      }


      
      // load lines below the anchor
      
      boolean limitExceeded = false;
      
      while (true) {

        line = reader.readLine();
        if (line == null) {
          // file end reached
          break;
        }
        
        lineIndex++;

        contentLines.add(line);
        charCount += line.length();

        // check load limit
        limitExceeded = charCount > LOAD_LIMIT;
        
        if (limitExceeded) {
          break;
        }
      }
      
      if (limitExceeded) {
        while (true) {
          
          while (charCount > LOAD_LIMIT && contentLines.size() > 0) {
            String removed = contentLines.removeFirst();
            charCount -= removed.length();
            anchorSplitIndex--;
            linesAboveAnchor--;
          }
          
          
          while (true) {

            line = reader.readLine();
            if (line == null) {
              // file end reached
              break;
            }
            
            lineIndex++;

            contentLines.add(line);
            charCount += line.length();

            // check load limit
            limitExceeded = charCount > LOAD_LIMIT;
            
            if (limitExceeded) {
              break;
            }
          }
        }
        
        lineIndex++;
      }


      if (line == null) {
        fileEndReached = true;
        linesInFile = lineIndex;
      }
      
      

    }
    
    contentLinesAboveAnchor = contentLines.subList(0, anchorSplitIndex);
    contentLinesBelowAnchor = contentLines.subList(anchorSplitIndex, contentLines.size());
  }
}

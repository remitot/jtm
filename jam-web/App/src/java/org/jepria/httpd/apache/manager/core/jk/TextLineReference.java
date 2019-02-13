package org.jepria.httpd.apache.manager.core.jk;

import java.util.List;

import org.jepria.httpd.apache.manager.core.jk.TextLineReference.Impl.OnDeleteHandler;

/**
 * Interface representing a single text line in a file
 */
public interface TextLineReference extends CharSequence {
  /**
   * @return 1 for the first line in the file
   */
  int lineNumber();
  
  void setContent(CharSequence content);
  
  CharSequence getContent();
  /**
   * Delete the line from containing file
   */
  void delete();
  
  
  @Override
  default int length() {
    return getContent().length();
  }
  @Override
  default char charAt(int index) {
    return getContent().charAt(index);
  }
  @Override
  default CharSequence subSequence(int start, int end) {
    return getContent().subSequence(start, end);
  }
  
  public static class Impl implements TextLineReference {
    /**
     * Begins from 1
     */
    private final int lineNumber;
    private CharSequence content;
    private OnDeleteHandler onDeleteHandler;

    public static interface OnDeleteHandler {
      void onDelete(TextLineReference line);
    }
    
    /**
     * @param lineNumber
     * @param content not null
     * @param onDeleteHandler handler to be invoked on {@link #delete()} invocation
     */
    public Impl(int lineNumber, String content, OnDeleteHandler onDeleteHandler) {
      this.lineNumber = lineNumber;
      this.content = content;
      this.onDeleteHandler = onDeleteHandler;
    }
    
    @Override
    public CharSequence getContent() {
      return content;
    }
    
    @Override
    public int lineNumber() {
      return lineNumber;
    }
    
    @Override
    public void setContent(CharSequence content) {
      this.content = content;
    }
    
    @Override
    public String toString() {
      return getContent().toString();
    }
    
    @Override
    public void delete() {
      if (onDeleteHandler != null) {
        onDeleteHandler.onDelete(this);
      }
    }
  }
  
  /**
   * Creates a new line and appends it to the end of the List container
   * @param container
   * @return
   */
  public static TextLineReference addNewLine(List<TextLineReference> container) {
    TextLineReference line = new Impl(container.size() + 1, "", new OnDeleteHandler() {
      @Override
      public void onDelete(TextLineReference line) {
        container.remove(line);
      }
    });
    container.add(line);
    return line;
  }
}

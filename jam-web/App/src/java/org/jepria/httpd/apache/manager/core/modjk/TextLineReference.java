package org.jepria.httpd.apache.manager.core.modjk;

public interface TextLineReference extends CharSequence {
  int lineNumber();
  void setContent(CharSequence content);
  CharSequence getContent();
  
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
}

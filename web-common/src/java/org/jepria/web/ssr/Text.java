package org.jepria.web.ssr;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

public interface Text {
  String getString(String key);
  
  public static final String DEFAULT_LANG = "ru";
  
  public static Text fromRequest(HttpServletRequest request) {
    return new Text() {
      
      private final String requestLang = request.getParameter("lang");
      
      private final String localeCode = requestLang == null ? DEFAULT_LANG : requestLang;
      
      private final ResourceBundle bundle = ResourceBundle.getBundle("text/bundle", new Locale(localeCode));
      
      @Override
      public String getString(String key) {
        return bundle.getString(key);
      }
    };
  }
}

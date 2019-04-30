package org.jepria.web.ssr;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

public interface Text {
  String getString(String key);
  
  public static final String DEFAULT_LANG = "ru";

  /**
   * 
   * @param request
   * @param bundleBaseName applicational resource bundle base name
   * @return
   */
  public static Text get(HttpServletRequest request, String bundleBaseName) {
    return new Text() {

      private final String requestLang = request.getParameter("lang");

      private final String localeCode = requestLang == null ? DEFAULT_LANG : requestLang;

      private final ResourceBundle bundleApp;
      private final ResourceBundle bundleCommon;

      {
        bundleApp = ResourceBundle.getBundle(bundleBaseName, new Locale(localeCode));
        bundleCommon = ResourceBundle.getBundle("text/org_jepria_web_ssr_Text", new Locale(localeCode));
      }

      @Override
      public String getString(String key) {
        if (bundleApp.containsKey(key)) {
          return bundleApp.getString(key);
        } else {
          return bundleCommon.getString(key);
        }
      }
    };
  }
}

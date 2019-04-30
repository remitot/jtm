package org.jepria.web.ssr;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

public class Texts {

  public static final String DEFAULT_LANG = "ru";

  /**
   * Gets common and applicational text resources (both from jtm-common and application) 
   * @param request
   * @param bundleBaseName applicational resource bundle base name
   * @return
   */
  public static Text get(HttpServletRequest request, String bundleBaseName) {
    return new Text() {

      private final ResourceBundle bundleApp;
      private final ResourceBundle bundleCommon;

      {
        bundleApp = ResourceBundle.getBundle(bundleBaseName, getLocale(request));
        bundleCommon = getBundleCommon(request);
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
  
  protected static ResourceBundle getBundleCommon(HttpServletRequest request) {
    return ResourceBundle.getBundle("text/org_jepria_web_ssr_Text", getLocale(request));
  }
  
  /**
   * Gets common text resources (from jtm-common only) 
   * @param request
   * @return
   */
  public static Text getCommon(HttpServletRequest request) {
    return new Text() {

      private final ResourceBundle bundleCommon;

      {
        bundleCommon = getBundleCommon(request);
      }

      @Override
      public String getString(String key) {
        return bundleCommon.getString(key);
      }
    };
  }
  
  protected static Locale getLocale(HttpServletRequest request) {
    String requestLang = request.getParameter("lang");
    String localeCode = requestLang == null ? DEFAULT_LANG : requestLang;
    return new Locale(localeCode);
  }
}

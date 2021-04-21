package org.jepria.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

/**
 * Them static methods are used to get parameters from {@link HttpServletRequest} in UTF-8 encoding.
 * By default the browser encodes the parameters (on posting form data) in ISO-8859-1,
 * regardless the {@code <form accept-encoding>} attribute
 */
// TODO implement proper client-to-server data transfer encoding and remove this class and all its usages
public class HttpDataEncoding {
  
  public static String getParameterUtf8(HttpServletRequest request, String paramName) {
    String s = request.getParameter(paramName);
    if (s == null) {
      return null;
    } else {
      try {
        return new String(s.getBytes("ISO-8859-1"), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        // impossible
        throw new RuntimeException(e);
      }
    }
  }
}

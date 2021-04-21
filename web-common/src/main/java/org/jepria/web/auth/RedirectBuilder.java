package org.jepria.web.auth;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

public class RedirectBuilder {
  /**
   * Builds value for {@code redirect} parameter for redirecting to the same resource
   * @param request the resource to redirect back to, not null
   * @return
   */
  public static String self(HttpServletRequest request) {
    Objects.requireNonNull(request);
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(request.getRequestURI()); 
    
    String qs = request.getQueryString();
    
    if (qs != null) {
      sb.append('?').append(qs);
    }
    
    return sb.toString();
  }
}

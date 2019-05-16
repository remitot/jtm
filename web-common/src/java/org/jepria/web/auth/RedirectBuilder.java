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
    
    String uri = request.getRequestURI();
    String ctx = request.getContextPath();
    String uriNoCtx = uri.substring(uri.indexOf(ctx) + ctx.length()); // uri must start with ctx
    
    if (uriNoCtx.startsWith("/")) {
      uriNoCtx = uriNoCtx.substring(1);
    }
    
    sb.append(uriNoCtx); 
    
    String qs = request.getQueryString();
    
    if (qs != null) {
      sb.append('?').append(qs);
    }
    
    return sb.toString();
  }
}

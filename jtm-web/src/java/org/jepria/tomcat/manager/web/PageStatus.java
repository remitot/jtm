package org.jepria.tomcat.manager.web;

import javax.servlet.http.HttpServletRequest;

import org.jepria.web.ssr.StatusBar;

public class PageStatus {
  
  public static void set(HttpServletRequest request, StatusBar pageStatus) {
    request.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus", pageStatus);
  }

  /**
   * remove and get
   * @param request
   * @return
   */
  public static StatusBar consume(HttpServletRequest request) {
    StatusBar pageStatus = (StatusBar)request.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus");
    request.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus");
    return pageStatus;
  }
}

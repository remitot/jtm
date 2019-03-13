package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.HtmlPage;
import org.jepria.tomcat.manager.web.HtmlPageUnauthorized;
import org.jepria.tomcat.manager.web.PageStatus;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.StatusBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends HttpServlet {

  private static final long serialVersionUID = -2556094883694667549L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    if (auth(req, resp)) {
      
      final Environment env = EnvironmentFactory.get(req);
      
      final HtmlPage htmlPage;
      
      final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
      final PageHeader pageHeader = new PageHeader(managerApacheHref, CurrentMenuItem.JDBC);

      final List<ConnectionDto> connections = new JdbcApi().list(env);
      final ServletModStatus servletModStatus = (ServletModStatus)req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus");
      
      htmlPage = new JdbcHtmlPage(pageHeader, connections, servletModStatus);
      htmlPage.setStatusBar(PageStatus.consume(req));
  
      // reset the servlet mod status after the first request 
      modReset(req);
      
      htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
      htmlPage.respond(resp);
    }
  }
  
  protected boolean auth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (req.getUserPrincipal() == null || !req.isUserInRole("manager-gui")) {
      
      final Environment env = EnvironmentFactory.get(req);
      
      final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
      final PageHeader pageHeader = new PageHeader(managerApacheHref, CurrentMenuItem.JDBC);
      
      HtmlPage htmlPage = new HtmlPageUnauthorized(pageHeader, "jdbc/login"); // TODO this will erase any path- or request params of the current page
      htmlPage.setStatusBar(PageStatus.consume(req));
      
      htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
      htmlPage.respond(resp);
      
      return false;
      
    } else {
      return true;
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      final String username = req.getParameter("username");
      final String password = req.getParameter("password");
      
      try {
        
        // TODO Tomcat bug?
        // when logged into vsmlapprfid1:8081/manager-ext/jdbc, then opening vsmlapprfid1:8080/manager-ext/jdbc results 401 
        // (on tomcat's container security check level) -- WHY? (with SSO valve turned on!)
        // OK, but after that, if we do vsmlapprfid1:8080/manager-ext/api/login -- the userPrincipal IS null, but req.login() throws
        // 'javax.servlet.ServletException: This request has already been authenticated' -- WHY? Must be EITHER request authenticated OR userPrincipal==null!
        
        // So, as a workaround -- logout anyway...
        
//          // logout if logged in
//          if (req.getUserPrincipal() != null) {
//            req.logout();
//          }
        
        req.logout();
        
        req.login(username, password);

      } catch (ServletException e) {
        e.printStackTrace();
        
        final StatusBar pageStatus = new StatusBar(StatusBar.Type.ERROR, "<span class=\"span-bold\">Неверные данные, попробуйте ещё раз.</span>"); // NON-NLS
        PageStatus.set(req, pageStatus);
        
      }
      
      // jdbc/login -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/mod".equals(path)) {

      final Gson gson = new Gson();
      final List<ItemModRequestDto> itemModRequests;
        
      // read list from request parameter (as passed by form.submit)
      try {
        String data = req.getParameter("data");
        Type type = new TypeToken<List<ItemModRequestDto>>(){}.getType();
        itemModRequests = gson.fromJson(data, type);
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }

      if (auth(req, resp)) {
      
        if (itemModRequests != null && itemModRequests.size() > 0) {
        
          // Map<modRequestId, modStatus>
          final Map<String, ItemModStatus> itemModStatuses = new HashMap<>();
          
          final Environment env = EnvironmentFactory.get(req);
          
          final boolean createContextResources = "true".equals(
              env.getProperty("org.jepria.tomcat.manager.web.jdbc.createContextResources"));
          
          final TomcatConfJdbc tomcatConf = new TomcatConfJdbc(
              () -> env.getContextXmlInputStream(), 
              () -> env.getServerXmlInputStream(),
              createContextResources);
    
          final JdbcApi api = new JdbcApi();
          
          boolean modSuccess = true; 
          
          // 1) perform all updates
          for (ItemModRequestDto itemModRequest: itemModRequests) {
            if ("update".equals(itemModRequest.getAction())) {
              ItemModStatus itemModStatus = api.updateConnection(
                  itemModRequest.getId(), itemModRequest.getData(), tomcatConf);
              if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
                modSuccess = false;
              }
              itemModStatuses.put(itemModRequest.getId(), itemModStatus);
            }
          }
    
    
          // 2) perform all deletions
          for (ItemModRequestDto itemModRequest: itemModRequests) {
            if ("delete".equals(itemModRequest.getAction())) {
              ItemModStatus itemModStatus = api.deleteConnection(
                  itemModRequest.getId(), tomcatConf);
              if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
                modSuccess = false;
              }
              itemModStatuses.put(itemModRequest.getId(), itemModStatus);
            }
          }
    
    
          // 3) perform all creations
          for (ItemModRequestDto itemModRequest: itemModRequests) {
            if ("create".equals(itemModRequest.getAction())) {
              ItemModStatus itemModStatus = api.createConnection(itemModRequest.getData(), tomcatConf, 
                  env.getResourceInitialParams());
              if (itemModStatus.code != ItemModStatus.SC_SUCCESS) {
                modSuccess = false;
              }
              itemModStatuses.put(itemModRequest.getId(), itemModStatus);
            }
          }
    
    
          // 4) ignore illegal actions
    
          final ServletModStatus servletModStatus = new ServletModStatus();
          
          if (modSuccess) {
            // save modifications and add a new _list to the response
            
            // Note: it is safe to save modifications to context.xml file here (before servlet response), 
            // because although Tomcat reloads the context after context.xml modification, 
            // it still fulfills the servlet requests currently under processing. 
            tomcatConf.save(env.getContextXmlOutputStream(), 
                env.getServerXmlOutputStream());
            
            servletModStatus.success = true;
            
            final StatusBar pageStatus = new StatusBar(StatusBar.Type.SUCCESS, "<span class=\"span-bold\">Все изменения сохранены.</span>"); // NON-NLS 
            PageStatus.set(req, pageStatus);
            
          } else {
            
            servletModStatus.success = false;
            servletModStatus.itemModRequests = itemModRequests;
            servletModStatus.itemModStatuses = itemModStatuses;
            
            
            final String statusHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
                "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS
            final StatusBar pageStatus = new StatusBar(StatusBar.Type.ERROR, statusHTML);
            PageStatus.set(req, pageStatus);
          }
  
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus", servletModStatus);
        }
        
      } else {
        // at least save itemModRequests without authorization
        
      }
      
      // jdbc/mod -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/mod-reset".equals(path)) {
      
      if (auth(req, resp)) {
        modReset(req);
      } else {
        // TODO
      }
      
      // jdbc/mod-reset -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
    }
  }
  
  private void modReset(HttpServletRequest req) {
    req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.servletModStatus");
  }
}

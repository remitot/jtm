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

    if (checkAuth(req, resp)) {
      
      final Environment env = EnvironmentFactory.get(req);
      
      final HtmlPage htmlPage;
      
      final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
      final PageHeader pageHeader = new PageHeader(managerApacheHref, "jdbc/logout", CurrentMenuItem.JDBC);

      final List<ConnectionDto> connections = new JdbcApi().list(env);
      
      
      // session state
      if (req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet") != null) {
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet");
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests");
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModStatuses");
      }
      
      @SuppressWarnings("unchecked")
      final List<ItemModRequestDto> itemModRequests = (List<ItemModRequestDto>)req.getSession()
          .getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests");
      @SuppressWarnings("unchecked")
      final Map<String, ItemModStatus> itemModStatuses = (Map<String, ItemModStatus>)req.getSession()
          .getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModStatuses");

      req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet", new Object());
      //
      
      
      htmlPage = new JdbcHtmlPage(pageHeader, connections, itemModRequests, itemModStatuses);
      htmlPage.setStatusBar(getStatusBar((PageStatus)req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus")));
  
      
      
      htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
      htmlPage.respond(resp);
      
    } else {
      
      doLogin(req, resp);
    }
    
    req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus.removeOnNextGet", new Object());
  }
  
  protected boolean checkAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  protected void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final Environment env = EnvironmentFactory.get(req);
    
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    final PageHeader pageHeader = new PageHeader(managerApacheHref, null, CurrentMenuItem.JDBC);
    
    if (req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus.removeOnNextGet") != null) {
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus.removeOnNextGet");
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus");
    }
    final LoginStatus loginStatus = (LoginStatus)req.getSession().getAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus");
    
    HtmlPage htmlPage = new HtmlPageUnauthorized(pageHeader, "jdbc/login"); // TODO this will erase any path- or request params of the current page
    htmlPage.setStatusBar(createStatusBar(loginStatus));
    
    htmlPage.setTitle("Tomcat manager: датасорсы (JDBC)"); // NON-NLS
    htmlPage.respond(resp);
    
    req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus.removeOnNextGet", new Object());
  }
  
  protected boolean login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String username = req.getParameter("username");
    final String password = req.getParameter("password");
    
    try {
      
      // TODO Tomcat bug?
      // when logged into vsmlapprfid1:8081/manager-ext/jdbc, then opening vsmlapprfid1:8080/manager-ext/jdbc results 401 
      // (on tomcat's container security check level) -- WHY? (with SSO valve turned on!)
      // OK, but after that, if we do vsmlapprfid1:8080/manager-ext/api/login -- the userPrincipal IS null, but req.login() throws
      // 'javax.servlet.ServletException: This request has already been authenticated' -- WHY? Must be EITHER request authenticated OR userPrincipal==null!
      
      // So, as a workaround -- logout anyway...
      
//        // logout if logged in
//        if (req.getUserPrincipal() != null) {
//          req.logout();
//        }
      
      req.logout();
      
      req.login(username, password);

      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus");
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus.removeOnNextGet");
      
      return true;
      
    } catch (ServletException e) {
      e.printStackTrace();
      
      req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus", LoginStatus.LOGIN_FAILURE);
      req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus.removeOnNextGet");
      
      return false;
    }
  }
  
  protected void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
    req.logout();
    req.getSession().invalidate();
    
    req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus", LoginStatus.LOGOUT_SUCCESS);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      login(req, resp);
      
      // jdbc/login -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req, resp);
      
      // jdbc/logout -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
        
    } else if ("/mod".equals(path)) {

      final Gson gson = new Gson();
      final List<ItemModRequestDto> itemModRequests;
        
      // read list from request parameter (as passed by form.submit)
      try {
        String data = req.getParameter("data");
        
        // convert encoding TODO fix this using accept-charset form attribute?
        data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
        
        Type type = new TypeToken<List<ItemModRequestDto>>(){}.getType();
        itemModRequests = gson.fromJson(data, type);
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }

      if (itemModRequests != null && itemModRequests.size() > 0) {
        
        if (checkAuth(req, resp)) {
          
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
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
              if (itemModStatus.code != ItemModStatus.Code.SUCCESS) {
                modSuccess = false;
              }
              itemModStatuses.put(itemModRequest.getId(), itemModStatus);
            }
          }
    
    
          // 4) ignore illegal actions
    
          if (modSuccess) {
            // save modifications and add a new _list to the response
            
            // Note: it is safe to save modifications to context.xml file here (before servlet response), 
            // because although Tomcat reloads the context after context.xml modification, 
            // it still fulfills the servlet requests currently under processing. 
            tomcatConf.save(env.getContextXmlOutputStream(), 
                env.getServerXmlOutputStream());
            
            // reset the servlet mod status after the successful mod
            req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet");
            req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests");
            req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModStatuses");
            
            req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus", PageStatus.MOD_SUCCESS);
            
          } else {
           
            // save session attributes
            req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet");
            req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests", itemModRequests);
            req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModStatuses", itemModStatuses);
            
            req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.pageStatus", PageStatus.MOD_INCORRECT_FIELD_DATA);
          }
        
        } else {

          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.loginStatus", LoginStatus.MOD_SESSION_EXPIRED);
          req.getSession().setAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests", itemModRequests);
        }
        
      }
      
      // jdbc/mod -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
      
    } else if ("/mod-reset".equals(path)) {
      
      if (checkAuth(req, resp)) {
        
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.removeOnNextGet");
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModRequests");
        req.getSession().removeAttribute("org.jepria.tomcat.manager.web.jdbc.SessionAttributes.mod.itemModStatuses");
        
      } else {
        // TODO
      }
      
      // jdbc/mod-reset -> jdbc
      resp.sendRedirect(".."); // TODO
      return;
    }
  }
 
  protected enum PageStatus {
    MOD_SUCCESS,
    MOD_INCORRECT_FIELD_DATA,
  }
  
  protected enum LoginStatus {
    /**
     * Login attempt failed: incorrect credentials
     */
    LOGIN_FAILURE,
    /**
     * Mod attempt failed: {@link #checkAuth} failed
     */
    MOD_SESSION_EXPIRED,
    /**
     * Logout succeeded
     */
    LOGOUT_SUCCESS,
    
  }

  
  protected StatusBar getStatusBar(PageStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
    case MOD_SUCCESS: {
      return new StatusBar(StatusBar.Type.SUCCESS, "<span class=\"span-bold\">Все изменения сохранены.</span>"); // NON-NLS 
    }
    case MOD_INCORRECT_FIELD_DATA: {
      final String statusHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
          "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS
      return new StatusBar(StatusBar.Type.ERROR, statusHTML);
    }
    }
    throw new IllegalArgumentException(String.valueOf(status));
  }
  
  protected StatusBar createStatusBar(LoginStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
    case LOGIN_FAILURE: {
      return new StatusBar(StatusBar.Type.ERROR, "<span class=\"span-bold\">Неверные данные, попробуйте ещё раз.</span>"); // NON-NLS
    }
    case MOD_SESSION_EXPIRED: {
      return new StatusBar(StatusBar.Type.INFO, "<span class=\"span-bold\">Необходимо авторизоваться.</span>&emsp;Сделанные изменения будут восстановлены.");
    }
    case LOGOUT_SUCCESS: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Разлогинились.</span>");
    }
    }
    throw new IllegalArgumentException(String.valueOf(status));
  }
}

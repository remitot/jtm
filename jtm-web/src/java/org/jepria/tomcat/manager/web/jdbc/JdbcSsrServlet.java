package org.jepria.tomcat.manager.web.jdbc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.jdbc.TomcatConfJdbc;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.jdbc.dto.ConnectionDto;
import org.jepria.tomcat.manager.web.jdbc.dto.ItemModRequestDto;
import org.jepria.web.ssr.ForbiddenFragment;
import org.jepria.web.ssr.HtmlPage;
import org.jepria.web.ssr.HtmlPageForbidden;
import org.jepria.web.ssr.HtmlPageUnauthorized;
import org.jepria.web.ssr.LoginFragment;
import org.jepria.web.ssr.PageHeader;
import org.jepria.web.ssr.PageHeader.CurrentMenuItem;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.StatusBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet producing server-side-rendered pages
 */
public class JdbcSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -2556094883694667549L;

  private static final String APP_STATE_SESSION_ATTR_KEY = "org.jepria.tomcat.manager.web.jdbc.SessionAttributes.appState";
  
  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final AppState appState = getAppState(req);

    final HtmlPage htmlPage;
    
    final PageHeader pageHeader = new PageHeader(CurrentMenuItem.JDBC);
    
    final Environment env = EnvironmentFactory.get(req);
    final String managerApacheHref = env.getProperty("org.jepria.tomcat.manager.web.managerApacheHref");
    pageHeader.setManagerApache(managerApacheHref);
    
    if (checkAuth(req)) {
      
      final List<ConnectionDto> connections = new JdbcApi().list(env);
      List<ItemModRequestDto> itemModRequests = appState.itemModRequests;
      Map<String, ItemModStatus> itemModStatuses = appState.itemModStatuses;
      
      if (itemModRequests == null) {
        itemModRequests = (List<ItemModRequestDto>)getAuthState(req).authPersistentData;
      }
      
      htmlPage = new JdbcHtmlPage(connections, itemModRequests, itemModStatuses);
      htmlPage.setStatusBar(createStatusBar(appState.modStatus));
  
      pageHeader.setButtonLogout("jdbc/logout"); // TODO this will erase any path- or request params of the current page
      
      appState.itemModRequests = null;
      appState.itemModStatuses = null;
      
    } else {

      clearAppState(req);
      
      AuthInfo authInfo = requireAuth(req, "jdbc/login", "jdbc/logout"); // TODO this will erase any path- or request params of the current page
      
      // TODO refactor the following shit!
      if (authInfo.authFragment instanceof LoginFragment) {
        htmlPage = new HtmlPageUnauthorized((LoginFragment)authInfo.authFragment);
        htmlPage.setStatusBar(authInfo.statusBar);
      } else if (authInfo.authFragment instanceof ForbiddenFragment) {
        htmlPage = new HtmlPageForbidden((ForbiddenFragment)authInfo.authFragment);
        pageHeader.setButtonLogout("jdbc/logout"); // TODO this will erase any path- or request params of the current page
        htmlPage.setStatusBar(authInfo.statusBar);
      } else {
        // TODO
        throw new IllegalStateException();
      }
      
      htmlPage.setTitle(JdbcHtmlPage.PAGE_TITLE);
    }
    
    htmlPage.setPageHeader(pageHeader);
    htmlPage.respond(resp);
    
    appState.modStatus = null;
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/login".equals(path)) {
      
      login(req);
      
      resp.sendRedirect("../jdbc"); // TODO
      return;
      
    } else if ("/logout".equals(path)) {
      
      logout(req);
      
      resp.sendRedirect("../jdbc"); // TODO
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
        
        if (checkAuth(req)) {
          
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
            final AppState appState = getAppState(req);
            appState.itemModRequests = null;
            appState.itemModStatuses = null;
            appState.modStatus = ModStatus.MOD_SUCCESS;
            
          } else {
           
            // save session attributes
            AppState appState = getAppState(req);
            appState.itemModRequests = itemModRequests;
            appState.itemModStatuses = itemModStatuses;
            appState.modStatus = ModStatus.MOD_INCORRECT_FIELD_DATA;
          }
        
        } else {

          getAuthState(req).authPersistentData = itemModRequests;
          
          final AppState appState = getAppState(req);
          appState.itemModRequests = itemModRequests;
          appState.itemModStatuses = null;
        }
        
      }
      
      // jdbc/mod -> jdbc
      resp.sendRedirect("../jdbc"); // TODO
      return;
      
    } else if ("/mod-reset".equals(path)) {
      
      
      // TODO no need to checkAuth?
      final AppState appState = getAppState(req);
      appState.itemModRequests = null;
      appState.itemModStatuses = null;
      appState.modStatus = null;

      
      // jdbc/mod-reset -> jdbc
      resp.sendRedirect("../jdbc"); // TODO
      return;
    }
  }

  /**
   * Class stored into a session
   */
  
  protected class AppState {
    public ModStatus modStatus = null;
    public List<ItemModRequestDto> itemModRequests = null;
    public Map<String, ItemModStatus> itemModStatuses = null;
  }
  
  protected AppState getAppState(HttpServletRequest request) {
    AppState state = (AppState)request.getSession().getAttribute(APP_STATE_SESSION_ATTR_KEY);
    if (state == null) {
      state = new AppState();
      request.getSession().setAttribute(APP_STATE_SESSION_ATTR_KEY, state);
    }
    return state;
  }
  
  protected void clearAppState(HttpServletRequest request) {
    request.getSession().removeAttribute(APP_STATE_SESSION_ATTR_KEY);
  }
  
  protected enum ModStatus {
    MOD_SUCCESS,
    MOD_INCORRECT_FIELD_DATA,
  }
  
  protected StatusBar createStatusBar(ModStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
    case MOD_SUCCESS: {
      return new StatusBar(StatusBar.Type.SUCCESS, "Все изменения сохранены"); // NON-NLS 
    }
    case MOD_INCORRECT_FIELD_DATA: {
      final String statusHTML = "При попытке сохранить изменения обнаружились некорректные значения полей (выделены красным). " +
          "<span class=\"span-bold\">На сервере всё осталось без изменений.</span>"; // NON-NLS
      return new StatusBar(StatusBar.Type.ERROR, statusHTML);
    }
    }
    throw new IllegalArgumentException(String.valueOf(status));
  }
}

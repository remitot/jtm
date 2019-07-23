package org.jepria.httpd.apache.manager.web.jkmodjk;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.web.Environment;
import org.jepria.httpd.apache.manager.web.EnvironmentFactory;
import org.jepria.httpd.apache.manager.web.JamPageHeader;
import org.jepria.httpd.apache.manager.web.JamPageHeader.CurrentMenuItem;
import org.jepria.httpd.apache.manager.web.jk.JkApi;
import org.jepria.httpd.apache.manager.web.jk.JkTextPageContent;
import org.jepria.httpd.apache.manager.web.jk.JkTextPageContent.TopPosition;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlPageExtBuilder;
import org.jepria.web.ssr.SsrServletBase;
import org.jepria.web.ssr.StatusBar;
import org.jepria.web.ssr.Text;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JkModjkSsrServlet extends SsrServletBase {

  private static final long serialVersionUID = -6137255466803720552L;

  @Override
  protected boolean checkAuth(HttpServletRequest req) {
    return req.getUserPrincipal() != null && req.isUserInRole("manager-gui");
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final Context context = Context.get(req, "text/org_jepria_httpd_apache_manager_web_Text");
    Text text = context.getText();
    
    final AppState appState = getAppState(req);

    final Environment env = EnvironmentFactory.get(req);
    
    final HtmlPageExtBuilder pageBuilder = HtmlPageExtBuilder.newInstance(context);
    pageBuilder.setTitle(text.getString("org.jepria.httpd.apache.manager.web.jk.title"));
    
    
    final JamPageHeader pageHeader = new JamPageHeader(context, CurrentMenuItem.JK_MODJK); 
    pageBuilder.setHeader(pageHeader);

    
    
    if (checkAuth(req)) {
      pageHeader.setButtonLogout(req);

      
      List<String> modJkConfLines = new JkApi().getMod_jk_ConfLines(env);
      
      // retrieve modRequest and modStatus from the AppState
      @SuppressWarnings("unchecked")
      List<String> modRequestLines = (List<String>)appState.modRequest;
      Boolean modStatus = (Boolean)appState.modStatus;
      if (modRequestLines == null) {
        @SuppressWarnings("unchecked")
        List<String> modRequestLinesCast = (List<String>)getAuthPersistentData(req); 
        modRequestLines = modRequestLinesCast;
      }
      
      JkTextPageContent content = new JkTextPageContent(context, modJkConfLines, modRequestLines, CurrentMenuItem.JK_MODJK);
      pageBuilder.setContent(content);
      pageBuilder.setBodyAttributes("onload", "common_onload();textContent_onload();");

      if (modStatus != null) {
        StatusBar statusBar = createModStatusBar(context, Boolean.TRUE.equals(modStatus));
        pageBuilder.setStatusBar(statusBar);
        content.setTopPosition(TopPosition.BELOW_PAGE_HEADER_AND_STATUS_BAR);
      }
      // clear auth-persistent data
      setAuthPersistentData(req, null);
      
    } else {

      requireAuth(req, pageBuilder);
    }
    
    final HtmlPageExtBuilder.Page page = pageBuilder.build();
    page.respond(resp);
    
    clearAppState(req);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final String path = req.getPathInfo();
    
    if ("/mod".equals(path)) {

      final Gson gson = new Gson();
      final List<String> modRequestLines;
        
      // read list from request parameter (as passed by form.submit)
      try {
        String data = req.getParameter("data");
        
        // convert encoding TODO fix this using accept-charset form attribute?
        data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
        
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = gson.fromJson(data, type);
        if (map == null) {
          modRequestLines = null;
        } else {
          String modRequestText = map.get("data");
          if (modRequestText != null) {
            modRequestLines = Arrays.asList(modRequestText.split("\\R"));
          } else {
            modRequestLines = null;
          }
        }
      } catch (Throwable e) {
        // TODO
        throw new RuntimeException(e);
      }

      if (modRequestLines != null) {
        
        if (checkAuth(req)) {
          
          final Environment env = EnvironmentFactory.get(req);
          
          final ApacheConfJk apacheConf = new ApacheConfJk(
              () -> env.getMod_jk_confInputStream(), 
              () -> env.getWorkers_propertiesInputStream());
          
          final JkApi api = new JkApi();
          
          api.updateMod_jk_Conf(modRequestLines, apacheConf);
          
          apacheConf.save(() -> env.getMod_jk_confOutputStream(), 
              () -> env.getWorkers_propertiesOutputStream());
          
          // clear modRequest after the successful modification (but preserve modStatus)
          AppState appState = getAppState(req);
          appState.modRequest = null;
          appState.modStatus = Boolean.TRUE;
          
        } else {

          AppState appState = getAppState(req);
          appState.modRequest = modRequestLines;
          appState.modStatus = null;
          
          setAuthPersistentData(req, modRequestLines);
        }
      }
      
      resp.sendRedirect(req.getContextPath() + "/jk/mod_jk");
      return;
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not understand the request");
      return;
    }
  }

  /**
   * Creates a StatusBar for a modification status
   * @param context
   * @param success
   * @return
   */
  protected StatusBar createModStatusBar(Context context, boolean success) {
    
    if (success) {
      
      Text text = context.getText();
      
      final String innerHTML = "<span class=\"span-bold\">"
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.saved") 
            + ".</span>&ensp;<a href=\"resturd\">" 
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.restart") 
            + "</a>,&nbsp;" 
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.apply");
      
      return new StatusBar(context, StatusBar.Type.SUCCESS, innerHTML);
      
    } else {
      // either the modifications succeeded and the status bar is required,
      // or the modifications failed and the exception had been thrown
      throw new UnsupportedOperationException();
    }
  }
}

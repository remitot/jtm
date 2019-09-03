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
import org.jepria.web.HttpDataEncoding;
import org.jepria.web.ssr.Context;
import org.jepria.web.ssr.HtmlEscaper;
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

      String pageInfo = HtmlEscaper.escape(env.getMod_jk_confFile().toString());
      pageHeader.setPageInfo(pageInfo);
      
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

      if (modStatus != null) {
        
        // TODO bad assertion here:
        // either the modifications succeeded and the status bar is required,
        // or the modifications failed and the exception had already been thrown
        if (!Boolean.TRUE.equals(modStatus)) {
          throw new IllegalStateException();
        }
        
        final StatusBar statusBar = new StatusBar(context);
        final String innerHTML = "<span class=\"span-bold\">"
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.saved") 
            + ".</span>&nbsp;" 
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.apply") 
            + ".&emsp;<a href=\"" + context.getContextPath() + "/restart" + "\">" 
            + text.getString("org.jepria.httpd.apache.manager.web.jk.status.mod_success.restart")
            + "</a>";
        
        statusBar.setType(StatusBar.Type.SUCCESS);
        statusBar.setHeaderHTML(innerHTML);
        
        pageBuilder.setStatusBar(statusBar);
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
        final String data = HttpDataEncoding.getParameterUtf8(req, "data");
        
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
          
          // clear modRequest after the successful modification
          AppState appState = getAppState(req);
          appState.modRequest = null;
          appState.modStatus = true;
          
        } else {

          AppState appState = getAppState(req);
          appState.modRequest = modRequestLines;
          appState.modStatus = null;
          
          setAuthPersistentData(req, modRequestLines);
        }
      }
      
      resp.sendRedirect(req.getContextPath() + "/jk/mod_jk");
      
    } else {
      // unknown request
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request path [" + path + "]");
      resp.flushBuffer();
      return;
    }
  }
}

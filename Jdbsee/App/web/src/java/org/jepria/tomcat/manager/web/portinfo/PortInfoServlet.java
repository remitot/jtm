package org.jepria.tomcat.manager.web.portinfo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.portinfo.TomcatConfPortInfo;
import org.jepria.tomcat.manager.web.BasicEnvironment;
import org.jepria.tomcat.manager.web.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PortInfoServlet extends HttpServlet {

  private static final long serialVersionUID = 2791033129244689227L;

  private void ajp13(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      
      Environment environment = new BasicEnvironment(req);
      
      TomcatConfPortInfo tomcatConf = new TomcatConfPortInfo(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());
      
      String port = tomcatConf.getConnectorPort("AJP/1.3");
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      
      if (port != null && !"".equals(port)) {
        responseJsonMap.put("status", PortInfoResponseStatus.SUCCESS);
        responseJsonMap.put("port_ajp13", port);
      } else {
        responseJsonMap.put("status", PortInfoResponseStatus.ERR__INTERNAL_ERROR);
      }
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
      
    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");//TODO
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
    return;
  }
  
  
  private void http11(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      
      Environment environment = new BasicEnvironment(req);
      
      TomcatConfPortInfo tomcatConf = new TomcatConfPortInfo(environment.getContextXmlInputStream(), 
          environment.getServerXmlInputStream());
      
      String port = tomcatConf.getConnectorPort("HTTP/1.1");
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      
      if (port == null || "".equals(port)) {
        responseJsonMap.put("status", PortInfoResponseStatus.ERR__INTERNAL_ERROR);
      } else {
        responseJsonMap.put("status", PortInfoResponseStatus.SUCCESS);
        responseJsonMap.put("port_http11", port);
      }
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
      
    } catch (Throwable e) {
      e.printStackTrace();

      resp.getOutputStream().println("Oops! Something went wrong.");//TODO
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.flushBuffer();
    return;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    resp.setContentType("application/json; charset=UTF-8");
    
    String path = req.getPathInfo();
    
    if ("/ajp13".equals(path)) {
      ajp13(req, resp);
      return;
      
    } else if ("/http11".equals(path)) {
      http11(req, resp);
      return;
      
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}

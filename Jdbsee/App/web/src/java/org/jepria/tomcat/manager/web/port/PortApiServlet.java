package org.jepria.tomcat.manager.web.port;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.tomcat.manager.core.TransactionException;
import org.jepria.tomcat.manager.core.port.TomcatConfPort;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PortApiServlet extends HttpServlet {

  private static final long serialVersionUID = 2791033129244689227L;

  private void ajp13(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      Integer port = getConnectorPort(req, "AJP/1.3");

      if (port != null) {
        resp.getOutputStream().print(port);
      }
      
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;

    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  
  private void http11(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      Integer port = getConnectorPort(req, "HTTP/1.1");

      if (port != null) {
        resp.getOutputStream().print(port);
      }
      
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;

    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  /**
   * @param req the request to get the {@link Environment} for
   * @param portName
   * @return
   * @throws TransactionException 
   */
  private Integer getConnectorPort(HttpServletRequest req, String portName) throws TransactionException {
    Environment environment = EnvironmentFactory.get(req);
    
    TomcatConfPort tomcatConf = new TomcatConfPort(environment.getContextXmlInputStream(), 
        environment.getServerXmlInputStream());
    
    String port = tomcatConf.getConnectorPort(portName); 
    return Integer.parseInt(port);
  }
  
  private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");
    
    try {
      Integer ajp13port = getConnectorPort(req, "AJP/1.3");
      Integer http11port = getConnectorPort(req, "HTTP/1.1");
      
      List<Map<String, Object>> ports = new ArrayList<>();
      
      if (ajp13port != null) {
        Map<String, Object> ajp13portJsonMap = new HashMap<>();
        ajp13portJsonMap.put("type", "AJP/1.3");
        ajp13portJsonMap.put("port", ajp13port);
        ports.add(ajp13portJsonMap);
      }
      
      if (http11port != null) {
        Map<String, Object> http11portJsonMap = new HashMap<>();
        http11portJsonMap.put("type", "HTTP/1.1");
        http11portJsonMap.put("port", http11port);
        ports.add(http11portJsonMap);
      }
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(ports, new PrintStream(resp.getOutputStream()));
    
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.flushBuffer();
      return;
      
    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
    String path = req.getPathInfo();
    
    if ("/list".equals(path)) {
      list(req, resp);
      return;
      
    } else if ("/ajp13".equals(path)) {
      ajp13(req, resp);
      return;
      
    } else if ("/http11".equals(path)) {
      http11(req, resp);
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}

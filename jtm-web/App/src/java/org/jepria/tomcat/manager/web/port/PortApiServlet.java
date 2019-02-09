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

import org.jepria.tomcat.manager.core.port.TomcatConfPort;
import org.jepria.tomcat.manager.web.Environment;
import org.jepria.tomcat.manager.web.EnvironmentFactory;
import org.jepria.tomcat.manager.web.port.dto.PortDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PortApiServlet extends HttpServlet {

  private static final long serialVersionUID = 2791033129244689227L;

  private static void port(HttpServletRequest req, HttpServletResponse resp,
      String type) throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("text/plain; charset=UTF-8");
    
    try {
      
      Environment environment = EnvironmentFactory.get(req);
      
      TomcatConfPort tomcatConf = new TomcatConfPort(
          () -> environment.getContextXmlInputStream(), 
          () -> environment.getServerXmlInputStream());
      
      PortDto port = getPort(tomcatConf, type);

      if (port == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        resp.flushBuffer();
        return;
        
      } else {
        
        resp.getWriter().print(port.getNumber());
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
        return;
      }

    } catch (Throwable e) {
      e.printStackTrace();

      // response body must either be empty or match the declared content type
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.flushBuffer();
      return;
    }
  }
  
  
  /**
   * @param tomcatConf
   * @param type (protocol)
   * @return
   */
  private static PortDto getPort(TomcatConfPort tomcatConf, String type) {
    String numberStr = tomcatConf.getConnectorPort(type);
    
    if (numberStr == null) {
      return null;
    }
    
    Integer number = Integer.parseInt(numberStr);
    
    PortDto port = new PortDto();
    port.setType(type);
    port.setNumber(number);
    
    return port;
  }
  
  private static void list(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    
    // the content type is defined for the entire method
    resp.setContentType("application/json; charset=UTF-8");
    
    try {
      
      Environment environment = EnvironmentFactory.get(req);
      
      TomcatConfPort tomcatConf = new TomcatConfPort(
          () -> environment.getContextXmlInputStream(), 
          () -> environment.getServerXmlInputStream());
      
      List<PortDto> ports = new ArrayList<>();
      
      PortDto ajp13port = getPort(tomcatConf, "AJP/1.3");
      if (ajp13port != null) {
        ports.add(ajp13port);
      }

      PortDto http11port = getPort(tomcatConf, "HTTP/1.1");
      if (http11port != null) {
        ports.add(http11port);
      }
      
      Map<String, Object> responseJsonMap = new HashMap<>();
      responseJsonMap.put("_list", ports);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(responseJsonMap, new PrintStream(resp.getOutputStream()));
    
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
      
    } else if ("/http".equals(path)) {
      port(req, resp, "HTTP/1.1");
      return;
      
    } else if ("/ajp".equals(path)) {
      port(req, resp, "AJP/1.3");
      return;
      
    } else {
      
      // TODO set content type for the error case?
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      resp.flushBuffer();
      return;
    }
  }
}

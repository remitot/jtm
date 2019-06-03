package org.jepria.httpd.apache.manager.web.jk;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletResponse;

import org.jepria.httpd.apache.manager.core.ajp.SimpleAjpConnection;

public class AjpAdapter {
  
  public static class AjpException extends Exception {
    private static final long serialVersionUID = 1L;
    public AjpException() {
      super();
    }
    public AjpException(String message, Throwable cause) {
      super(message, cause);
    }
    public AjpException(String message) {
      super(message);
    }
    public AjpException(Throwable cause) {
      super(cause);
    }
  }
  
  /**
   * Request http port of a tomcat instance over ajp
   * @param host
   * @param ajpPort
   * @param tomcatManagerExtCtxPath
   */
  public static int requestHttpPortOverAjp(String host, int ajpPort, String tomcatManagerExtCtxPath) throws AjpException {

    final String uri = tomcatManagerExtCtxPath + MANAGER_EXT_HTTP_PORT_URI;

    final int httpPort;
    
    try {
      
      SimpleAjpConnection connection = SimpleAjpConnection.open(
          host, ajpPort, uri, CONNECT_TIMEOUT_MS);

      connection.connect();

      int status = connection.getStatus();
      
      if (status != HttpServletResponse.SC_OK) {
        throw new AjpException("Ajp response status is " + status);
      }
      
      String responseBody = connection.getResponseBody();
      
      if (responseBody == null) {
        throw new AjpException("Ajp response has empty body");
      }
      
      try {
        httpPort = Integer.parseInt(responseBody);
      } catch (NumberFormatException e) {
        throw new AjpException("Failed to parse ajp response body [" + responseBody + "]");
      }
      
    } catch (UnknownHostException e) {
      // wrong host
      throw new AjpException(e);

    } catch (ConnectException e) {
      // host OK, port is not working at all
      throw new AjpException(e);

    } catch (SocketException e) {
      // host OK, port OK, invalid protocol
      throw new AjpException(e);

    } catch (SocketTimeoutException e) {
      // host OK, port OK, invalid protocol
      throw new AjpException(e);

    } catch (AjpException e) {
      throw e;
      
    } catch (Throwable e) {
      throw new AjpException(e);
    }

    return httpPort;
  }
  
  /**
   * For requesting HTTP port over AJP
   */
  private static final String MANAGER_EXT_HTTP_PORT_URI = "/api/port/http";

  /**
   * For requesting HTTP port over AJP
   */
  private static final int CONNECT_TIMEOUT_MS = 2000;
} 

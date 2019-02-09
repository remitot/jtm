package org.jepria.httpd.apache.manager.web.ajp;

import java.io.IOException;

import org.apache.coyote.ajp.SimpleAjpClient;

public interface SimpleAjpConnection {
  void addHeader(String name, String value);
  void connect();
  int getStatus();
  String getStatusMessage();
  String getResponseBody();
  
  
  static SimpleAjpConnection open(String host, int port, String uri, int timeoutMs) throws IOException {
    SimpleAjpClient ajpClient = new SimpleAjpClient();
    ajpClient.setHost(host);
    ajpClient.setPort(port);
    ajpClient.connect(timeoutMs);
    return new SimpleAjpConnectionImpl(ajpClient, uri);
  }
}

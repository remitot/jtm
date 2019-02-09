package org.jepria.httpd.apache.manager.web.ajp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.coyote.ajp.SimpleAjpClient;
import org.apache.coyote.ajp.TesterAjpMessage;

/**
 * Source from
 * https://apache.googlesource.com/tomcat/+/6d9910302678fd8491f52c4e03efe348a66bdbd8/test/org/apache/coyote/ajp/TestAbstractAjpProcessor.java
 * Refactored.
 */
/*package*/class SimpleAjpConnectionImpl implements SimpleAjpConnection {

  private boolean messageForwarded = false;
  
  private final SimpleAjpClient ajpClient;
  private final String uri;
  
  public SimpleAjpConnectionImpl(SimpleAjpClient ajpClient, String uri) {
    this.ajpClient = ajpClient;
    
    if (uri == null) {
      this.uri = "/";
    } else {
      if (uri.startsWith("/")) {
        this.uri = uri;
      } else {
        this.uri = "/" + uri;
      }
    }
  }
  
  private final Map<String, String> headers = new LinkedHashMap<>();
  
  @Override
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }
  
  @Override
  public void connect() {
    if (!messageForwarded) {
      forwardMessage();
    }
  }
  
  @Override
  public int getStatus() {
    if (!messageForwarded) {
      forwardMessage();
    }
    return status;
  }

  @Override
  public String getStatusMessage() {
    if (!messageForwarded) {
      forwardMessage();
    }
    return statusMessage;
  }

  @Override
  public String getResponseBody() {
    if (!messageForwarded) {
      forwardMessage();
    }
    return responseBodyContent;
  }
  
  private int status;
  private String statusMessage;
  private String responseBodyContent;
  
  private void forwardMessage() {
    try {
      TesterAjpMessage forwardMessage = ajpClient.createForwardMessage(uri);
      // Complete the message - no extra headers required.
      for (String header: headers.keySet()) {
        forwardMessage.addHeader(header, headers.get(header));
      }
      forwardMessage.end();

      TesterAjpMessage responseHeaders = ajpClient.sendMessage(forwardMessage);
      // Expect 3 packets: headers, body, end
      parseResponseHeaders(responseHeaders);
      TesterAjpMessage responseBody = ajpClient.readMessage();
      parseResponseBodyContent(responseBody);

      ajpClient.disconnect();
      
      messageForwarded = true;
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Process response header packet and checks the status. Any other data is
   * ignored.
   */
  private void parseResponseHeaders(TesterAjpMessage message) {
      // Set the start position and read the length
      message.processHeader(false);

      // Should be a header message
      message.readByte();

      // Check status
      this.status = message.readInt();

      // Read the status message
      this.statusMessage = message.readString();

      // Get the number of headers
      int headerCount = message.readInt();

      for (int i = 0; i < headerCount; i++) {
          // Read the header name
          message.readHeaderName();
          // Read the header value
          message.readString();
      }
  }
  
  private void parseResponseBodyContent(TesterAjpMessage message) {
    // Set the start position and read the length
    message.processHeader(false);

    // Should be a body chunk message
    message.readByte();

    int len = message.readInt();
    this.responseBodyContent = message.readString(len);
  }
}

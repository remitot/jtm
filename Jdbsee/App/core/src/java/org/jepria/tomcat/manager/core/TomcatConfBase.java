package org.jepria.tomcat.manager.core;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class TomcatConfBase {
  /**
   * context.xml Document
   */
  protected final Document contextDoc;
  
  /**
   * server.xml Document
   */
  protected final Document serverDoc;
  
  public TomcatConfBase(InputStream contextXmlInputStream,
      InputStream serverXmlInputStream) throws TransactionException {
    
    try (InputStream contextXmlInputStream0 = contextXmlInputStream;
        InputStream serverXmlInputStream0 = serverXmlInputStream) {
      
      contextDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(contextXmlInputStream0);
      serverDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(serverXmlInputStream0);
      
    } catch (Throwable e) {
      throw new TransactionException(e);
    }
  }
  
  protected void handleThrowable(Throwable e) {
    e.printStackTrace();
  }
  
}

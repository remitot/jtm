package org.jepria.tomcat.manager.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tomcat configuration based on the following configuration files:
 * {@code context.xml}, {@code server.xml}  
 */
public class TomcatConfBase {
  
  private final Supplier<InputStream> context_xmlInput;
  private final Supplier<InputStream> server_xmlInput;
  
  public TomcatConfBase(Supplier<InputStream> context_xmlInput,
      Supplier<InputStream> server_xmlInput) {
    this.context_xmlInput = context_xmlInput;
    this.server_xmlInput = server_xmlInput;
  }
  
  
  
  /**
   * Lazily parsed
   */
  private Document context_xmlDoc;
  
  /**
   * @return lazily parsed {@code context.xml} configuration file 
   */
  public Document getContext_xmlDoc() {
    if (context_xmlDoc == null) {
      initContext_xmlDoc();
    }
    return context_xmlDoc;
  }
  
  /**
   * Parses the file into the document
   */
  private void initContext_xmlDoc() {
    try (InputStream stream = context_xmlInput.get()) {
      context_xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }//TODO catch filenotfound, file not readable
  }
  
  
  /**
   * Lazily parsed
   */
  private Document server_xmlDoc;
  
  /**
   * @return lazily parsed {@code server.xml} configuration file 
   */
  public Document getServer_xmlDoc() {
    if (server_xmlDoc == null) {
      initServer_xmlDoc();
    }
    return server_xmlDoc;
  }
  
  /**
   * Parses the file into the document
   */
  private void initServer_xmlDoc() {
    try (InputStream stream = server_xmlInput.get()) {
      server_xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }//TODO catch filenotfound, file not readable
  }
  
  
      
  protected void handleThrowable(Throwable e) {
    e.printStackTrace();
  }
  
  protected Transformer createTransformer() throws TransformerConfigurationException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");//TODO obtain existing indent amount
    return transformer;
  }
  
  public void saveContext_xml(OutputStream context_xmlOutputStream) {
    try (OutputStream stream = context_xmlOutputStream) {
      createTransformer().transform(new DOMSource(getContext_xmlDoc()), new StreamResult(stream));
    } catch (TransformerException | IOException e) {
      throw new RuntimeException(e);
    }// TODO catch filenotwritable
  }
  
  public void saveServer_xml(OutputStream server_xmlOutputStream) {
    try (OutputStream stream = server_xmlOutputStream) {
      createTransformer().transform(new DOMSource(getServer_xmlDoc()), new StreamResult(stream));
    } catch (TransformerException | IOException e) {
      throw new RuntimeException(e);
    }// TODO catch filenotwritable
  } 
  
}

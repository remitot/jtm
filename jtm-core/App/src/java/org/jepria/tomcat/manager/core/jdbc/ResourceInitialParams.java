package org.jepria.tomcat.manager.core.jdbc;

import java.util.Map;

/**
 * Initial connection params to apply to the newly created connections, in the endpoint files
 */
public interface ResourceInitialParams {
  /**
   * @return initial attribute values for the newly created Context/Resource nodes
   */
  Map<String, String> getContextResourceAttrs();
  
  /**
   * @return initial attribute values for the newly created Context/ResourceLink nodes
   */
  Map<String, String> getContextResourceLinkAttrs();
  
  /**
   * @return initial attribute values for the newly created Server/Resource nodes
   */
  Map<String, String> getServerResourceAttrs();

  /**
   * @return initial jdbc protocol for the 'url' attribute 
   * of the newly created resources, with 
   * <pre>
   * jdbc:mysql://
   * jdbc:oracle:thin:@//
   * </pre>
   */
  String getJdbcProtocol();
}

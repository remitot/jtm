package org.jepria.httpd.apache.manager.web.jk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jepria.httpd.apache.manager.core.jk.ApacheConfJk;
import org.jepria.httpd.apache.manager.core.jk.TextLineReference;
import org.jepria.httpd.apache.manager.web.Environment;

public class JkApi {

  /**
   * 
   * @param environment
   * @return non-null, unmodifiable list of file content strings, or else empty list
   */
  public List<String> getMod_jk_ConfLines(Environment environment) {

    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());

    final List<String> ret = new ArrayList<>();
    
    List<TextLineReference> lines = apacheConf.getMod_jk_confLines();
    if (lines != null) {
      for (TextLineReference line: lines) {
        ret.add(line.toString());
      }
    }
    
    return Collections.unmodifiableList(ret);
  }
  
  public void updateMod_jk_Conf(List<String> contentLines, ApacheConfJk apacheConf) {
   
    List<TextLineReference> lines = apacheConf.getMod_jk_confLines();
    
    // clear lines
    while (lines.size() > 0) {
      lines.remove(0);
    }
    
    // add new lines
    if (contentLines != null) {
      for (String contentLine: contentLines) {
        TextLineReference line = TextLineReference.addNewLine(lines);
        line.setContent(contentLine);
      }
    }
  }
  
  /**
   * 
   * @param environment
   * @return non-null, unmodifiable list of file content strings, or else empty list
   */
  public List<String> getWorkers_propertiesLines(Environment environment) {

    final ApacheConfJk apacheConf = new ApacheConfJk(
        () -> environment.getMod_jk_confInputStream(), 
        () -> environment.getWorkers_propertiesInputStream());

    final List<String> ret = new ArrayList<>();
    
    List<TextLineReference> lines = apacheConf.getWorkers_propertiesLines();
    if (lines != null) {
      for (TextLineReference line: lines) {
        ret.add(line.toString());
      }
    }
    
    return Collections.unmodifiableList(ret);
  }
  
  public void updateWorkers_properties(List<String> contentLines, ApacheConfJk apacheConf) {
    
    List<TextLineReference> lines = apacheConf.getWorkers_propertiesLines();
    
    // clear lines
    while (lines.size() > 0) {
      lines.remove(0);
    }
    
    // add new lines
    if (contentLines != null) {
      for (String contentLine: contentLines) {
        TextLineReference line = TextLineReference.addNewLine(lines);
        line.setContent(contentLine);
      }
    }
  }
}

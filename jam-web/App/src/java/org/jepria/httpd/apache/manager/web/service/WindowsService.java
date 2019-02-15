package org.jepria.httpd.apache.manager.web.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*package*/class WindowsService implements ApacheService {
  @Override
  public void restart() {
    try {
      System.out.println("///rest:"+serviceName);
      final String codePage = getCodePage();
      System.out.println("///"+exec("cmd /c net stop " + serviceName, codePage));
      System.out.println("///"+exec("cmd /c net start " + serviceName, codePage));
      System.out.println("///done");
    } catch (Throwable e) {
      throw new RuntimeException("Error restarting service [" + serviceName + "]", e);
    }
  }
  
  private static final String DEFAULT_CODE_PAGE = "866";
  
  private final String serviceName;
  
  public WindowsService(String serviceName) {
    this.serviceName = serviceName;
  }
  
  private static String getCodePage() throws IOException {
    String chcp = exec("cmd /c chcp", DEFAULT_CODE_PAGE);
    Matcher m = Pattern.compile(".*?(\\d+)").matcher(chcp);
    if (m.matches()) {
      return m.group(1);
    } else {
      return DEFAULT_CODE_PAGE;
    }
  }
  
  private static String exec(String cmd, String encoding) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    try (PrintStream out = new PrintStream(baos)) {
      InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoding));
      
      String line;
      boolean first = true;
      while ((line = br.readLine()) != null) {
        if (!first) {
          out.println();
        } else {
          first = false;
        }
        out.print(line);
      }
      br.close();
    }
    
    return baos.toString();
  }
}

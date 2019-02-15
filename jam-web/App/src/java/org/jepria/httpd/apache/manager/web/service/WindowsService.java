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
      final String codePage = getCodePage();
      
      // stop the service
      final String stopExecInput = "cmd /c net stop " + serviceName;
      final String stopExecOutput = exec(stopExecInput, codePage);
      final String stopExecOutputContains = "service was stopped successfully";
      if (stopExecOutput == null || !stopExecOutput.contains(stopExecOutputContains)) {
        throw new WindowsCmdExecutionException("Failed to stop the service [" + serviceName + "] using Windows cmd: command [" +stopExecInput+ "], "
            + "expected output containing [" + stopExecOutputContains + "], "
            + "actual output was "+ (stopExecOutput == null ? "null" : ("[" + stopExecOutput+ "]")) + ". "
            + "Probably the serviceName is invalid (Windows not logging such errors). Try to run the same command manually.");
      }
      
      
      // start the service
      final String startExecInput = "cmd /c net start " + serviceName;
      final String startExecOutput = exec(startExecInput, codePage);
      final String startExecOutputContains = "service was started successfully";
      if (startExecOutput == null || !startExecOutput.contains(startExecOutputContains)) {
        throw new WindowsCmdExecutionException("Failed to start the service [" + serviceName + "] using Windows cmd: command [" +startExecInput+ "], "
            + "expected output containing [" + startExecOutputContains + "], "
            + "actual output was "+ (startExecOutput == null ? "null" : ("[" + startExecOutput+ "]")) + ". "
            + "Probably the serviceName is invalid (Windows not logging such errors). Try to run the same command manually.");
      }

    } catch (WindowsCmdExecutionException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Error restarting service [" + serviceName + "]", e);
    }
  }
  
  private static class WindowsCmdExecutionException extends RuntimeException {
    private static final long serialVersionUID = -5790540321423348833L;
    public WindowsCmdExecutionException(String message) {
      super(message);
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

package org.jepria.catalina.suspender;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jepria.catalina.suspender.ContextWrapper.GetContextListener;

public class SuspenderValve extends ValveBase {

  private static void log(String msg) {
    System.out.println(SuspenderValve.class.getCanonicalName() + " " + new java.util.Date() + ": " + msg);
  }
  
  private static void err(String msg) {
    System.err.println(SuspenderValve.class.getCanonicalName() + " " + new java.util.Date() + ": " + msg);
  }
  
  /**
   * Initial server.xml Valve attribute value, not used regularly (on start only)
   */
  private String webappsPathStr = null;
  
  private Environment environment = null;
  
  /**
   * server.xml Valve attribute
   */
  public void setWebappsPath(String webappsPath) {
    this.webappsPathStr = webappsPath;
  }
  
  
  /**
   * Initial server.xml Valve attribute value, not used regularly (on start only)
   */
  private String periodStr = null;
  
  // in minutes
  private int period = 1440; // 24h
  
  /**
   * server.xml Valve attribute
   */
  public void setPeriod(String period) {
    this.periodStr = period;
  }
  
  
  /**
   * Initial server.xml Valve attribute value, not used regularly (on start only)
   */
  private String suspenderIgnorePathStr = null;
  
  /**
   * server.xml Valve attribute
   */
  public void setSuspenderIgnorePath(String suspenderIgnorePath) {
    this.suspenderIgnorePathStr = suspenderIgnorePath;
  }
  
  
  
  /**
   * Initial server.xml Valve attribute value, not used regularly (on start only)
   */
  private String startStr = null;
  
  private Calendar start = Calendar.getInstance();
  
  /**
   * server.xml Valve attribute
   */
  public void setStart(String start) {
    this.startStr = start;
  }

  private boolean watchCrossContext = true;
  
  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
  
    String requestUri = request.getRequestURI();
    if (requestUri != null) {
      if (requestUri.startsWith("/")) {
        requestUri = requestUri.substring(1);
      }
      
      String matchingDeployedAppContext = environment.getMatchingDeployedAppContext(requestUri);
      String matchingWarSuspendedAppContext = environment.getMatchingWarSuspendedAppContext(requestUri);
  
      // priority the appContext
      final String appContext;
      if (matchingWarSuspendedAppContext != null) {
        if (matchingDeployedAppContext != null) {
          if (matchingWarSuspendedAppContext.length() > matchingDeployedAppContext.length()) {
            // found both suspended and deployed app contexts, prefer suspended due to the stricter (longer) match
            appContext = matchingWarSuspendedAppContext;
          } else {
            // found both suspended and deployed app contexts, prefer deployed due to the stricter (longer) match
            appContext = matchingDeployedAppContext;
          }
        } else {
          // no deployed app context, found suspended app context
          appContext = matchingWarSuspendedAppContext;
        }
      } else {
        if (matchingDeployedAppContext != null) {
          // no suspended app context, found deployed app context
          appContext = matchingDeployedAppContext;
        } else {
          // neither suspended nor deployed app context found
          appContext = null;
        }
      }
      
      if (appContext != null) {
        if (appContext == matchingDeployedAppContext) {
          // access the deployed app context
          
          synchronized (accessedAppContexts) {
            accessedAppContexts.add(appContext);
          }
          
        } else if (appContext == matchingWarSuspendedAppContext) {
          // unsuspend and access the suspended app context
          
          if (unsuspendAppContext(appContext)) {
            
            try {
              ContextLoadAwaiter.await(appContext, 30);
            } catch (InterruptedException e) {
              e.printStackTrace();
              // no crash, further redirect is OK
            }
            
            // tell the client to repeat the same request
            String requestQS = request.getQueryString();
            String requestURLQS = request.getRequestURL() + (requestQS != null ? ("?" + requestQS) : "");
            response.sendRedirect(requestURLQS);
            
            return;
            
          } else {
            err("Failed to unsuspend app " + appContext);
          }
        }
        
      }
    }

    
    if (watchCrossContext) {
      
      GetContextListener getContextListener = new GetContextListener() {
        @Override
        public void onGetContext(String context) {
          System.out.println("/// implicit context requested: " + context);
          // TODO stopped here
        }
      };
      
      request.setContext(new ContextWrapper(request.getContext(), getContextListener));
    }
    
    
    getNext().invoke(request, response);
  }
  
  
  @Override
  protected synchronized void startInternal() throws LifecycleException {
    
    if (!initAttributes()) {
      throw new LifecycleException("Failed to initialize critical attributes");
    }
    
    initTimer();
    
    super.startInternal();
  }
  
  /**
   * 
   * @return true if initialization of attributes succeeded, false otherwise
   */
  private boolean initAttributes() {
    
    // validate 'webappsPath' attribute
    if (webappsPathStr == null || "".equals(webappsPathStr)) {
      err("Missing mandatory attribute 'webappsPath'");
      return false;
    }
    Path webappsPath = Paths.get(webappsPathStr);
    if (!Files.isDirectory(webappsPath)) {
      err("Illegal value [" + webappsPath + "] of the 'webappsPath' attribute: the path does not exist or is not a directory");
      return false;
    }
    this.environment = EnvironmentFactory.get(webappsPath.toFile());
    
    
    // validate 'period' attribute
    if (periodStr == null) {
      log("'period' attribute is missing, will use default value: " + period);
      
    } else {
      try {
        period = Integer.parseInt(periodStr);
      } catch (NumberFormatException e) {
        err("Incorrect 'period' attribute value: " + periodStr + ", will use default: " + period);
      }
    }
    
    
    // validate 'start' attribute
    if (startStr == null) {
      log("'start' attribute is missing, the first suspension will be performed immediately after the server startup");
      
    } else {
      Matcher m = Pattern.compile("(\\d\\d):(\\d\\d)").matcher(startStr);
      if (m.matches()) {
        int startH = Integer.parseInt(m.group(1));
        int startM = Integer.parseInt(m.group(2));
        
        start.set(Calendar.HOUR_OF_DAY, startH);
        start.set(Calendar.MINUTE, startM);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        
        // if the time is in the past today, start tomorrow
        if (start.before(Calendar.getInstance())) {
          start.add(Calendar.DATE, 1);
        }
        
        
        // log
        Calendar now = Calendar.getInstance();
        long secLeft = ChronoUnit.SECONDS.between(now.toInstant(), start.toInstant()) % 60;
        long minLeft = ChronoUnit.MINUTES.between(now.toInstant(), start.toInstant()) % 60;
        long hrsLeft = ChronoUnit.HOURS.between(now.toInstant(), start.toInstant());
        String remainsStr = String.format("%02d:%02d:%02d", hrsLeft, minLeft, secLeft);
        log("'start' attribute value is [" + startStr + "], time remains before the first suspension: " + remainsStr);
        
      } else {
        err("Illegal 'start' attribute value: " + startStr + ", the first suspension will be performed immediately after the server startup");
      }
    }
    
    
    return true;
  }
  
  
  ////////////////////// SUSPENSION SCHEDULING ///////////////////// 
  
  /**
   * Application names (context names) accessed since the last suspension iteration
   */
  private final Set<String> accessedAppContexts = new HashSet<>();
  
  private final Timer timer = new Timer();
  
  private final TimerTask task = new TimerTask() {
    @Override
    public void run() {
      
      final Set<String> warAppContexts = environment.getWarAppContexts();
      final Set<String> ignoreAppContexts = getIgnoreAppContexts();
      
      synchronized (accessedAppContexts) {
        for (String warAppContext: warAppContexts) {
          
          boolean ignore = false;
          for (String ignoreAppContext: ignoreAppContexts) {
            if (warAppContext.matches(ignoreAppContext)) {
              ignore = true;
              break;
            }
          }
          
          if (!accessedAppContexts.contains(warAppContext) && !ignore) {
            if (suspendAppContext(warAppContext)) {
              log("Suspended app " + warAppContext);
            } else {
              err("Failed to suspend app " + warAppContext);
            }
          }
        }
        
        accessedAppContexts.clear();
      }
    }
  };
  
  
  /**
   * Reads the suspender.ignore configuration file and get the application context patterns to ignore
   */
  private Set<String> getIgnoreAppContexts() {
    final Set<String> ret = new HashSet<>();
    
    // re-read the file each time
    if (suspenderIgnorePathStr != null) {
      Path suspenderIgnorePath = Paths.get(suspenderIgnorePathStr);
      if (Files.isRegularFile(suspenderIgnorePath)) {
        try (Scanner sc = new Scanner(suspenderIgnorePath)) {
          while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line != null && line.length() > 0 && !line.startsWith("--")) {
              ret.add(line);
            }              
          }
        } catch (IOException e) {
          // TODO log but no fail
          e.printStackTrace();
        }
      } else {
        // TODO log but no fail
      }
    }
    
    return ret;
  }
  
  private void initTimer() {
    timer.schedule(task, start.getTime(), period * 60 * 1000);
  }
  
  @Override
  protected synchronized void stopInternal() throws LifecycleException {

    // Critical! Otherwise the tomcat service stopping will hang for a while
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    
    
    super.stopInternal();
  }
  
  /**
   * 
   * @param warAppContext
   * @return {@code true} if suspension succeeded, {@code false} otherwise
   */
  private boolean suspendAppContext(String warAppContext) {
    
    // rename war
    final File war = environment.getWar(warAppContext);
    
    if (war != null && war.isFile()) {
      final File warSus = environment.getWarSuspended(warAppContext);
      if (warSus.exists()) {
        // if deletion of the existing suspended war file fails, still try to replace it further
        warSus.delete();
      }
      boolean renameResult = war.renameTo(warSus);
      if (!renameResult) {
        return false;
      }
    }
    
    
    // remove deployed app (necessary for immediate suspension on server startup only)
    final File deployedApp = environment.getDeployedDirectory(warAppContext);
    
    if (deployedApp != null && deployedApp.isDirectory()) {
      try {
        deleteDirectory(deployedApp.toPath());
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    
    return true;
  }
  
  private static void deleteDirectory(Path path) throws IOException {
    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
      try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
        for (Path entry : entries) {
          deleteDirectory(entry);
        }
      }
    }
    Files.delete(path);
  }
  
  /**
   * 
   * @param suspendedAppContext
   * @return {@code true} if unsuspension succeeded, {@code false} otherwise
   */
  private boolean unsuspendAppContext(String suspendedAppContext) {
    
    final File warSus = environment.getWarSuspended(suspendedAppContext);
    
    if (warSus != null && warSus.isFile()) {
      final File war = environment.getWar(suspendedAppContext);
      return warSus.renameTo(war);
    }
    
    return false;
  }
  
}

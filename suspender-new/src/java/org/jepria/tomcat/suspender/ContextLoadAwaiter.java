package org.jepria.tomcat.suspender;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A singleton that performs awaits for the context loadings 
 */
// Thread-safe singleton, from https://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code
public class ContextLoadAwaiter {
  
  private ContextLoadAwaiter() {
  }
  
  
  
  
  private static class ApplicationLatchMap extends HashMap<String, CountDownLatch> {

    private static final long serialVersionUID = -4657510621814787598L;
    
    private static volatile ApplicationLatchMap instance;
    private static Object mutex = new Object();

    private ApplicationLatchMap() {
    }

    public static ApplicationLatchMap getInstance() {
      ApplicationLatchMap result = instance;
      if (result == null) {
        synchronized (mutex) {
          result = instance;
          if (result == null) {
            instance = result = new ApplicationLatchMap();
          }
        }
      }
      return result;
    }
  }
  
  
  /**
   * Starts awaiting for the app context loading.
   * Hands the current thread until the app context is loaded (as triggered by {@link ContextLoadListener#contextInitialized}),
   * or the specified timeout is out (if either the context loading has run out of that timeout or 
   * there is no {@link ContextLoadListener} initialized on the server)
   * @param app
   * @param timeoutSec
   * @throws InterruptedException 
   * @return {@code true} on successful application context load await; {@code false} if the waiting time elapsed
   */
  public static boolean await(String app, int timeoutSec) throws InterruptedException {
    CountDownLatch latch = ApplicationLatchMap.getInstance().get(app);
    if (latch == null) {
      latch = new CountDownLatch(1);
      ApplicationLatchMap.getInstance().put(app, latch);
    }
    
    return latch.await(timeoutSec, TimeUnit.SECONDS);
  }
  
  public static void contextLoaded(String app) {
    if (app != null) {
      CountDownLatch latch = ApplicationLatchMap.getInstance().remove(app);
      if (latch != null) {
        latch.countDown();
      }
    }
  }
  
}

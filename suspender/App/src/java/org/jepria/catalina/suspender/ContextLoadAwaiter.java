package org.jepria.catalina.suspender;

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
   * Starts awaiting for the context loading.
   * Returns when the context is loaded (as triggered by {@link ContextLoadListener#contextInitialized}),
   * or after the period specified (if either the context loading has run out of that period or 
   * there is no {@link ContextLoadListener} initialized on the server)
   * @param appContextName
   * @param seconds
   * @throws InterruptedException 
   */
  public static void await(String appContextName, int seconds) throws InterruptedException {
    CountDownLatch latch = ApplicationLatchMap.getInstance().get(appContextName);
    if (latch == null) {
      latch = new CountDownLatch(1);
      ApplicationLatchMap.getInstance().put(appContextName, latch);
    }
    
    latch.await(seconds, TimeUnit.SECONDS);
  }
  
  public static void contextLoaded(String appContextName) {
    CountDownLatch latch = ApplicationLatchMap.getInstance().remove(appContextName);
    if (latch != null) {
      latch.countDown();
    }
  }
  
}

package org.jepria.tomcat.suspender.core;

/*package*/class SuspenderImpl implements Suspender {

  @Override
  public boolean unsuspend(String contextPath) {
    // TODO Auto-generated method stub
    System.out.println("///requested unsuspend:" + contextPath);
    return true;
  }

  @Override
  public boolean suspend(String contextPath) {
    // TODO Auto-generated method stub
    System.out.println("///requested suspend:" + contextPath);
    return true;
  }

}

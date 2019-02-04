package org.jepria.catalina.suspender;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.apache.catalina.AccessLog;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.deploy.ApplicationListener;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.CharsetMapper;
import org.apache.juli.logging.Log;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.http.mapper.Mapper;

public class ContextWrapper implements Context {
  
  private final Context delegate;
  private final GetContextListener getContextListener;
  
  public ContextWrapper(Context delegate, GetContextListener getContextListener) {
    this.delegate = delegate;
    this.getContextListener = getContextListener;
  }
  
  ///////////////////////////////////////////
  
  @Override
  public ServletContext getServletContext() {
    return new ServletContextWrapper(delegate.getServletContext());
  }
  
  ///////////////////////////////////////////

  @Override
  public void addApplicationListener(ApplicationListener arg0) {
    delegate.addApplicationListener(arg0);
  }

  @Override
  public void addApplicationListener(String arg0) {
    delegate.addApplicationListener(arg0);
  }

  @Override
  public void addApplicationParameter(ApplicationParameter arg0) {
    delegate.addApplicationParameter(arg0);
  }

  @Override
  public void addChild(Container arg0) {
    delegate.addChild(arg0);
  }

  @Override
  public void addConstraint(SecurityConstraint arg0) {
    delegate.addConstraint(arg0);
  }

  @Override
  public void addContainerListener(ContainerListener arg0) {
    delegate.addContainerListener(arg0);
  }

  @Override
  public void addErrorPage(ErrorPage arg0) {
    delegate.addErrorPage(arg0);
  }

  @Override
  public void addFilterDef(FilterDef arg0) {
    delegate.addFilterDef(arg0);
  }

  @Override
  public void addFilterMap(FilterMap arg0) {
    delegate.addFilterMap(arg0);
  }

  @Override
  public void addFilterMapBefore(FilterMap arg0) {
    delegate.addFilterMapBefore(arg0);
  }

  @Override
  public void addInstanceListener(String arg0) {
    delegate.addInstanceListener(arg0);
  }

  @Override
  public void addLifecycleListener(LifecycleListener arg0) {
    delegate.addLifecycleListener(arg0);
  }

  @Override
  public void addLocaleEncodingMappingParameter(String arg0, String arg1) {
    delegate.addLocaleEncodingMappingParameter(arg0, arg1);
  }

  @Override
  public void addMimeMapping(String arg0, String arg1) {
    delegate.addMimeMapping(arg0, arg1);
  }

  @Override
  public void addParameter(String arg0, String arg1) {
    delegate.addParameter(arg0, arg1);
  }

  @Override
  public void addPostConstructMethod(String arg0, String arg1) {
    delegate.addPostConstructMethod(arg0, arg1);
  }

  @Override
  public void addPreDestroyMethod(String arg0, String arg1) {
    delegate.addPreDestroyMethod(arg0, arg1);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener arg0) {
    delegate.addPropertyChangeListener(arg0);
  }

  @Override
  public void addResourceJarUrl(URL arg0) {
    delegate.addResourceJarUrl(arg0);
  }

  @Override
  public void addRoleMapping(String arg0, String arg1) {
    delegate.addRoleMapping(arg0, arg1);
  }

  @Override
  public void addSecurityRole(String arg0) {
    delegate.addSecurityRole(arg0);
  }

  @Override
  public void addServletContainerInitializer(ServletContainerInitializer arg0,
      Set<Class<?>> arg1) {
    delegate.addServletContainerInitializer(arg0, arg1);
  }

  @Override
  public void addServletMapping(String arg0, String arg1, boolean arg2) {
    delegate.addServletMapping(arg0, arg1, arg2);
  }

  @Override
  public void addServletMapping(String arg0, String arg1) {
    delegate.addServletMapping(arg0, arg1);
  }

  @Override
  public Set<String> addServletSecurity(ApplicationServletRegistration arg0,
      ServletSecurityElement arg1) {
    return delegate.addServletSecurity(arg0, arg1);
  }

  @Override
  public void addWatchedResource(String arg0) {
    delegate.addWatchedResource(arg0);
  }

  @Override
  public void addWelcomeFile(String arg0) {
    delegate.addWelcomeFile(arg0);
  }

  @Override
  public void addWrapperLifecycle(String arg0) {
    delegate.addWrapperLifecycle(arg0);
  }

  @Override
  public void addWrapperListener(String arg0) {
    delegate.addWrapperListener(arg0);
  }

  @Override
  public void backgroundProcess() {
    delegate.backgroundProcess();
  }

  @Override
  public Wrapper createWrapper() {
    return delegate.createWrapper();
  }

  @Override
  public void destroy() throws LifecycleException {
    delegate.destroy();
  }

  @Override
  public String[] findApplicationListeners() {
    return delegate.findApplicationListeners();
  }

  @Override
  public ApplicationParameter[] findApplicationParameters() {
    return delegate.findApplicationParameters();
  }

  @Override
  public Container findChild(String arg0) {
    return delegate.findChild(arg0);
  }

  @Override
  public Container[] findChildren() {
    return delegate.findChildren();
  }

  @Override
  public SecurityConstraint[] findConstraints() {
    return delegate.findConstraints();
  }

  @Override
  public ContainerListener[] findContainerListeners() {
    return delegate.findContainerListeners();
  }

  @Override
  public ErrorPage findErrorPage(int arg0) {
    return delegate.findErrorPage(arg0);
  }

  @Override
  public ErrorPage findErrorPage(String arg0) {
    return delegate.findErrorPage(arg0);
  }

  @Override
  public ErrorPage[] findErrorPages() {
    return delegate.findErrorPages();
  }

  @Override
  public FilterDef findFilterDef(String arg0) {
    return delegate.findFilterDef(arg0);
  }

  @Override
  public FilterDef[] findFilterDefs() {
    return delegate.findFilterDefs();
  }

  @Override
  public FilterMap[] findFilterMaps() {
    return delegate.findFilterMaps();
  }

  @Override
  public String[] findInstanceListeners() {
    return delegate.findInstanceListeners();
  }

  @Override
  public LifecycleListener[] findLifecycleListeners() {
    return delegate.findLifecycleListeners();
  }

  @Override
  public String findMimeMapping(String arg0) {
    return delegate.findMimeMapping(arg0);
  }

  @Override
  public String[] findMimeMappings() {
    return delegate.findMimeMappings();
  }

  @Override
  public String findParameter(String arg0) {
    return delegate.findParameter(arg0);
  }

  @Override
  public String[] findParameters() {
    return delegate.findParameters();
  }

  @Override
  public String findPostConstructMethod(String arg0) {
    return delegate.findPostConstructMethod(arg0);
  }

  @Override
  public Map<String, String> findPostConstructMethods() {
    return delegate.findPostConstructMethods();
  }

  @Override
  public String findPreDestroyMethod(String arg0) {
    return delegate.findPreDestroyMethod(arg0);
  }

  @Override
  public Map<String, String> findPreDestroyMethods() {
    return delegate.findPreDestroyMethods();
  }

  @Override
  public String findRoleMapping(String arg0) {
    return delegate.findRoleMapping(arg0);
  }

  @Override
  public boolean findSecurityRole(String arg0) {
    return delegate.findSecurityRole(arg0);
  }

  @Override
  public String[] findSecurityRoles() {
    return delegate.findSecurityRoles();
  }

  @Override
  public String findServletMapping(String arg0) {
    return delegate.findServletMapping(arg0);
  }

  @Override
  public String[] findServletMappings() {
    return delegate.findServletMappings();
  }

  @Override
  public String findStatusPage(int arg0) {
    return delegate.findStatusPage(arg0);
  }

  @Override
  public int[] findStatusPages() {
    return delegate.findStatusPages();
  }

  @Override
  public String[] findWatchedResources() {
    return delegate.findWatchedResources();
  }

  @Override
  public boolean findWelcomeFile(String arg0) {
    return delegate.findWelcomeFile(arg0);
  }

  @Override
  public String[] findWelcomeFiles() {
    return delegate.findWelcomeFiles();
  }

  @Override
  public String[] findWrapperLifecycles() {
    return delegate.findWrapperLifecycles();
  }

  @Override
  public String[] findWrapperListeners() {
    return delegate.findWrapperListeners();
  }

  @Override
  public void fireContainerEvent(String arg0, Object arg1) {
    delegate.fireContainerEvent(arg0, arg1);
  }

  @Override
  public boolean fireRequestDestroyEvent(ServletRequest arg0) {
    return delegate.fireRequestDestroyEvent(arg0);
  }

  @Override
  public boolean fireRequestInitEvent(ServletRequest arg0) {
    return delegate.fireRequestInitEvent(arg0);
  }

  @Override
  public AccessLog getAccessLog() {
    return delegate.getAccessLog();
  }

  @Override
  public boolean getAllowCasualMultipartParsing() {
    return delegate.getAllowCasualMultipartParsing();
  }

  @Override
  public String getAltDDName() {
    return delegate.getAltDDName();
  }

  @Override
  public Object[] getApplicationEventListeners() {
    return delegate.getApplicationEventListeners();
  }

  @Override
  public Object[] getApplicationLifecycleListeners() {
    return delegate.getApplicationLifecycleListeners();
  }

  @Override
  public Authenticator getAuthenticator() {
    return delegate.getAuthenticator();
  }

  @Override
  public boolean getAvailable() {
    return delegate.getAvailable();
  }

  @Override
  public int getBackgroundProcessorDelay() {
    return delegate.getBackgroundProcessorDelay();
  }

  @Override
  public String getBaseName() {
    return delegate.getBaseName();
  }

  @Override
  public String getCharset(Locale arg0) {
    return delegate.getCharset(arg0);
  }

  @Override
  public CharsetMapper getCharsetMapper() {
    return delegate.getCharsetMapper();
  }

  @Override
  public Cluster getCluster() {
    return delegate.getCluster();
  }

  @Override
  public URL getConfigFile() {
    return delegate.getConfigFile();
  }

  @Override
  public boolean getConfigured() {
    return delegate.getConfigured();
  }

  @Override
  public String getContainerSciFilter() {
    return delegate.getContainerSciFilter();
  }

  @Override
  public boolean getCookies() {
    return delegate.getCookies();
  }

  @Override
  public boolean getCrossContext() {
    return delegate.getCrossContext();
  }

  @Override
  public String getDisplayName() {
    return delegate.getDisplayName();
  }

  @Override
  public boolean getDistributable() {
    return delegate.getDistributable();
  }

  @Override
  public String getDocBase() {
    return delegate.getDocBase();
  }

  @Override
  public int getEffectiveMajorVersion() {
    return delegate.getEffectiveMajorVersion();
  }

  @Override
  public int getEffectiveMinorVersion() {
    return delegate.getEffectiveMinorVersion();
  }

  @Override
  public String getEncodedPath() {
    return delegate.getEncodedPath();
  }

  @Override
  public boolean getFireRequestListenersOnForwards() {
    return delegate.getFireRequestListenersOnForwards();
  }

  @Override
  public boolean getIgnoreAnnotations() {
    return delegate.getIgnoreAnnotations();
  }

  @Override
  public String getInfo() {
    return delegate.getInfo();
  }

  @Override
  public InstanceManager getInstanceManager() {
    return delegate.getInstanceManager();
  }

  @Override
  public JarScanner getJarScanner() {
    return delegate.getJarScanner();
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    return delegate.getJspConfigDescriptor();
  }

  @Override
  public Loader getLoader() {
    return delegate.getLoader();
  }

  @Override
  public boolean getLogEffectiveWebXml() {
    return delegate.getLogEffectiveWebXml();
  }

  @Override
  public Log getLogger() {
    return delegate.getLogger();
  }

  @Override
  public LoginConfig getLoginConfig() {
    return delegate.getLoginConfig();
  }

  @Override
  public Manager getManager() {
    return delegate.getManager();
  }

  @Override
  public Mapper getMapper() {
    return delegate.getMapper();
  }

  @Override
  public boolean getMapperContextRootRedirectEnabled() {
    return delegate.getMapperContextRootRedirectEnabled();
  }

  @Override
  public boolean getMapperDirectoryRedirectEnabled() {
    return delegate.getMapperDirectoryRedirectEnabled();
  }

  @Override
  public Object getMappingObject() {
    return delegate.getMappingObject();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public NamingResources getNamingResources() {
    return delegate.getNamingResources();
  }

  @Override
  public ObjectName getObjectName() {
    return delegate.getObjectName();
  }

  @Override
  public boolean getOverride() {
    return delegate.getOverride();
  }

  @Override
  public Container getParent() {
    return delegate.getParent();
  }

  @Override
  public ClassLoader getParentClassLoader() {
    return delegate.getParentClassLoader();
  }

  @Override
  public String getPath() {
    return delegate.getPath();
  }

  @Override
  public boolean getPaused() {
    return delegate.getPaused();
  }

  @Override
  public Pipeline getPipeline() {
    return delegate.getPipeline();
  }

  @Override
  public boolean getPreemptiveAuthentication() {
    return delegate.getPreemptiveAuthentication();
  }

  @Override
  public boolean getPrivileged() {
    return delegate.getPrivileged();
  }

  @Override
  public String getPublicId() {
    return delegate.getPublicId();
  }

  @Override
  public String getRealPath(String arg0) {
    return delegate.getRealPath(arg0);
  }

  @Override
  public Realm getRealm() {
    return delegate.getRealm();
  }

  @Override
  public boolean getReloadable() {
    return delegate.getReloadable();
  }

  @Override
  public String getResourceOnlyServlets() {
    return delegate.getResourceOnlyServlets();
  }

  @Override
  public DirContext getResources() {
    return delegate.getResources();
  }

  @Override
  public boolean getSendRedirectBody() {
    return delegate.getSendRedirectBody();
  }

  @Override
  public String getSessionCookieDomain() {
    return delegate.getSessionCookieDomain();
  }

  @Override
  public String getSessionCookieName() {
    return delegate.getSessionCookieName();
  }

  @Override
  public String getSessionCookiePath() {
    return delegate.getSessionCookiePath();
  }

  @Override
  public boolean getSessionCookiePathUsesTrailingSlash() {
    return delegate.getSessionCookiePathUsesTrailingSlash();
  }

  @Override
  public int getSessionTimeout() {
    return delegate.getSessionTimeout();
  }

  @Override
  public int getStartStopThreads() {
    return delegate.getStartStopThreads();
  }

  @Override
  public LifecycleState getState() {
    return delegate.getState();
  }

  @Override
  public String getStateName() {
    return delegate.getStateName();
  }

  @Override
  public boolean getSwallowAbortedUploads() {
    return delegate.getSwallowAbortedUploads();
  }

  @Override
  public boolean getSwallowOutput() {
    return delegate.getSwallowOutput();
  }

  @Override
  public boolean getTldNamespaceAware() {
    return delegate.getTldNamespaceAware();
  }

  @Override
  public boolean getTldValidation() {
    return delegate.getTldValidation();
  }

  @Override
  public boolean getUseHttpOnly() {
    return delegate.getUseHttpOnly();
  }

  @Override
  public boolean getUseRelativeRedirects() {
    return delegate.getUseRelativeRedirects();
  }

  @Override
  public boolean getValidateClientProvidedNewSessionId() {
    return delegate.getValidateClientProvidedNewSessionId();
  }

  @Override
  public String getWebappVersion() {
    return delegate.getWebappVersion();
  }

  @Override
  public String getWrapperClass() {
    return delegate.getWrapperClass();
  }

  @Override
  public boolean getXmlBlockExternal() {
    return delegate.getXmlBlockExternal();
  }

  @Override
  public boolean getXmlNamespaceAware() {
    return delegate.getXmlNamespaceAware();
  }

  @Override
  public boolean getXmlValidation() {
    return delegate.getXmlValidation();
  }

  @Override
  public void init() throws LifecycleException {
    delegate.init();
  }

  @Override
  public void invoke(Request arg0, Response arg1)
      throws IOException, ServletException {
    delegate.invoke(arg0, arg1);
  }

  @Override
  public boolean isResourceOnlyServlet(String arg0) {
    return delegate.isResourceOnlyServlet(arg0);
  }

  @Override
  public boolean isServlet22() {
    return delegate.isServlet22();
  }

  @Override
  public void logAccess(Request arg0, Response arg1, long arg2, boolean arg3) {
    delegate.logAccess(arg0, arg1, arg2, arg3);
  }

  @Override
  public void reload() {
    delegate.reload();
  }

  @Override
  public void removeApplicationListener(String arg0) {
    delegate.removeApplicationListener(arg0);
  }

  @Override
  public void removeApplicationParameter(String arg0) {
    delegate.removeApplicationParameter(arg0);
  }

  @Override
  public void removeChild(Container arg0) {
    delegate.removeChild(arg0);
  }

  @Override
  public void removeConstraint(SecurityConstraint arg0) {
    delegate.removeConstraint(arg0);
  }

  @Override
  public void removeContainerListener(ContainerListener arg0) {
    delegate.removeContainerListener(arg0);
  }

  @Override
  public void removeErrorPage(ErrorPage arg0) {
    delegate.removeErrorPage(arg0);
  }

  @Override
  public void removeFilterDef(FilterDef arg0) {
    delegate.removeFilterDef(arg0);
  }

  @Override
  public void removeFilterMap(FilterMap arg0) {
    delegate.removeFilterMap(arg0);
  }

  @Override
  public void removeInstanceListener(String arg0) {
    delegate.removeInstanceListener(arg0);
  }

  @Override
  public void removeLifecycleListener(LifecycleListener arg0) {
    delegate.removeLifecycleListener(arg0);
  }

  @Override
  public void removeMimeMapping(String arg0) {
    delegate.removeMimeMapping(arg0);
  }

  @Override
  public void removeParameter(String arg0) {
    delegate.removeParameter(arg0);
  }

  @Override
  public void removePostConstructMethod(String arg0) {
    delegate.removePostConstructMethod(arg0);
  }

  @Override
  public void removePreDestroyMethod(String arg0) {
    delegate.removePreDestroyMethod(arg0);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener arg0) {
    delegate.removePropertyChangeListener(arg0);
  }

  @Override
  public void removeRoleMapping(String arg0) {
    delegate.removeRoleMapping(arg0);
  }

  @Override
  public void removeSecurityRole(String arg0) {
    delegate.removeSecurityRole(arg0);
  }

  @Override
  public void removeServletMapping(String arg0) {
    delegate.removeServletMapping(arg0);
  }

  @Override
  public void removeWatchedResource(String arg0) {
    delegate.removeWatchedResource(arg0);
  }

  @Override
  public void removeWelcomeFile(String arg0) {
    delegate.removeWelcomeFile(arg0);
  }

  @Override
  public void removeWrapperLifecycle(String arg0) {
    delegate.removeWrapperLifecycle(arg0);
  }

  @Override
  public void removeWrapperListener(String arg0) {
    delegate.removeWrapperListener(arg0);
  }

  @Override
  public void setAllowCasualMultipartParsing(boolean arg0) {
    delegate.setAllowCasualMultipartParsing(arg0);
  }

  @Override
  public void setAltDDName(String arg0) {
    delegate.setAltDDName(arg0);
  }

  @Override
  public void setApplicationEventListeners(Object[] arg0) {
    delegate.setApplicationEventListeners(arg0);
  }

  @Override
  public void setApplicationLifecycleListeners(Object[] arg0) {
    delegate.setApplicationLifecycleListeners(arg0);
  }

  @Override
  public void setBackgroundProcessorDelay(int arg0) {
    delegate.setBackgroundProcessorDelay(arg0);
  }

  @Override
  public void setCharsetMapper(CharsetMapper arg0) {
    delegate.setCharsetMapper(arg0);
  }

  @Override
  public void setCluster(Cluster arg0) {
    delegate.setCluster(arg0);
  }

  @Override
  public void setConfigFile(URL arg0) {
    delegate.setConfigFile(arg0);
  }

  @Override
  public void setConfigured(boolean arg0) {
    delegate.setConfigured(arg0);
  }

  @Override
  public void setContainerSciFilter(String arg0) {
    delegate.setContainerSciFilter(arg0);
  }

  @Override
  public void setCookies(boolean arg0) {
    delegate.setCookies(arg0);
  }

  @Override
  public void setCrossContext(boolean arg0) {
    delegate.setCrossContext(arg0);
  }

  @Override
  public void setDisplayName(String arg0) {
    delegate.setDisplayName(arg0);
  }

  @Override
  public void setDistributable(boolean arg0) {
    delegate.setDistributable(arg0);
  }

  @Override
  public void setDocBase(String arg0) {
    delegate.setDocBase(arg0);
  }

  @Override
  public void setEffectiveMajorVersion(int arg0) {
    delegate.setEffectiveMajorVersion(arg0);
  }

  @Override
  public void setEffectiveMinorVersion(int arg0) {
    delegate.setEffectiveMinorVersion(arg0);
  }

  @Override
  public void setFireRequestListenersOnForwards(boolean arg0) {
    delegate.setFireRequestListenersOnForwards(arg0);
  }

  @Override
  public void setIgnoreAnnotations(boolean arg0) {
    delegate.setIgnoreAnnotations(arg0);
  }

  @Override
  public void setInstanceManager(InstanceManager arg0) {
    delegate.setInstanceManager(arg0);
  }

  @Override
  public void setJarScanner(JarScanner arg0) {
    delegate.setJarScanner(arg0);
  }

  @Override
  public void setLoader(Loader arg0) {
    delegate.setLoader(arg0);
  }

  @Override
  public void setLogEffectiveWebXml(boolean arg0) {
    delegate.setLogEffectiveWebXml(arg0);
  }

  @Override
  public void setLoginConfig(LoginConfig arg0) {
    delegate.setLoginConfig(arg0);
  }

  @Override
  public void setManager(Manager arg0) {
    delegate.setManager(arg0);
  }

  @Override
  public void setMapperContextRootRedirectEnabled(boolean arg0) {
    delegate.setMapperContextRootRedirectEnabled(arg0);
  }

  @Override
  public void setMapperDirectoryRedirectEnabled(boolean arg0) {
    delegate.setMapperDirectoryRedirectEnabled(arg0);
  }

  @Override
  public void setName(String arg0) {
    delegate.setName(arg0);
  }

  @Override
  public void setNamingResources(NamingResources arg0) {
    delegate.setNamingResources(arg0);
  }

  @Override
  public void setOverride(boolean arg0) {
    delegate.setOverride(arg0);
  }

  @Override
  public void setParent(Container arg0) {
    delegate.setParent(arg0);
  }

  @Override
  public void setParentClassLoader(ClassLoader arg0) {
    delegate.setParentClassLoader(arg0);
  }

  @Override
  public void setPath(String arg0) {
    delegate.setPath(arg0);
  }

  @Override
  public void setPreemptiveAuthentication(boolean arg0) {
    delegate.setPreemptiveAuthentication(arg0);
  }

  @Override
  public void setPrivileged(boolean arg0) {
    delegate.setPrivileged(arg0);
  }

  @Override
  public void setPublicId(String arg0) {
    delegate.setPublicId(arg0);
  }

  @Override
  public void setRealm(Realm arg0) {
    delegate.setRealm(arg0);
  }

  @Override
  public void setReloadable(boolean arg0) {
    delegate.setReloadable(arg0);
  }

  @Override
  public void setResourceOnlyServlets(String arg0) {
    delegate.setResourceOnlyServlets(arg0);
  }

  @Override
  public void setResources(DirContext arg0) {
    delegate.setResources(arg0);
  }

  @Override
  public void setSendRedirectBody(boolean arg0) {
    delegate.setSendRedirectBody(arg0);
  }

  @Override
  public void setSessionCookieDomain(String arg0) {
    delegate.setSessionCookieDomain(arg0);
  }

  @Override
  public void setSessionCookieName(String arg0) {
    delegate.setSessionCookieName(arg0);
  }

  @Override
  public void setSessionCookiePath(String arg0) {
    delegate.setSessionCookiePath(arg0);
  }

  @Override
  public void setSessionCookiePathUsesTrailingSlash(boolean arg0) {
    delegate.setSessionCookiePathUsesTrailingSlash(arg0);
  }

  @Override
  public void setSessionTimeout(int arg0) {
    delegate.setSessionTimeout(arg0);
  }

  @Override
  public void setStartStopThreads(int arg0) {
    delegate.setStartStopThreads(arg0);
  }

  @Override
  public void setSwallowAbortedUploads(boolean arg0) {
    delegate.setSwallowAbortedUploads(arg0);
  }

  @Override
  public void setSwallowOutput(boolean arg0) {
    delegate.setSwallowOutput(arg0);
  }

  @Override
  public void setTldNamespaceAware(boolean arg0) {
    delegate.setTldNamespaceAware(arg0);
  }

  @Override
  public void setTldValidation(boolean arg0) {
    delegate.setTldValidation(arg0);
  }

  @Override
  public void setUseHttpOnly(boolean arg0) {
    delegate.setUseHttpOnly(arg0);
  }

  @Override
  public void setUseRelativeRedirects(boolean arg0) {
    delegate.setUseRelativeRedirects(arg0);
  }

  @Override
  public void setValidateClientProvidedNewSessionId(boolean arg0) {
    delegate.setValidateClientProvidedNewSessionId(arg0);
  }

  @Override
  public void setWebappVersion(String arg0) {
    delegate.setWebappVersion(arg0);
  }

  @Override
  public void setWrapperClass(String arg0) {
    delegate.setWrapperClass(arg0);
  }

  @Override
  public void setXmlBlockExternal(boolean arg0) {
    delegate.setXmlBlockExternal(arg0);
  }

  @Override
  public void setXmlNamespaceAware(boolean arg0) {
    delegate.setXmlNamespaceAware(arg0);
  }

  @Override
  public void setXmlValidation(boolean arg0) {
    delegate.setXmlValidation(arg0);
  }

  @Override
  public void start() throws LifecycleException {
    delegate.start();
  }

  @Override
  public void stop() throws LifecycleException {
    delegate.stop();
  }
  
  ///////////////////////////////////////////
  
  private class ServletContextWrapper implements ServletContext {
    
    private final ServletContext delegate;
    
    public ServletContextWrapper(ServletContext delegate) {
      this.delegate = delegate;
    }

    @Override
    public ServletContext getContext(String arg0) {
      if (getContextListener != null) {
        getContextListener.onGetContext(arg0);
      }
      return delegate.getContext(arg0);
    }
    
    ///////////////////////////////////////////
    
    @Override
    public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
      return delegate.addFilter(arg0, arg1);
    }

    @Override
    public Dynamic addFilter(String arg0, Filter arg1) {
      return delegate.addFilter(arg0, arg1);
    }

    @Override
    public Dynamic addFilter(String arg0, String arg1) {
      return delegate.addFilter(arg0, arg1);
    }

    @Override
    public void addListener(Class<? extends EventListener> arg0) {
      delegate.addListener(arg0);
    }

    @Override
    public void addListener(String arg0) {
      delegate.addListener(arg0);
    }

    @Override
    public <T extends EventListener> void addListener(T arg0) {
      delegate.addListener(arg0);
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
        Class<? extends Servlet> arg1) {
      return delegate.addServlet(arg0, arg1);
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
        Servlet arg1) {
      return delegate.addServlet(arg0, arg1);
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
        String arg1) {
      return delegate.addServlet(arg0, arg1);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> arg0)
        throws ServletException {
      return delegate.createFilter(arg0);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> arg0)
        throws ServletException {
      return delegate.createListener(arg0);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> arg0)
        throws ServletException {
      return delegate.createServlet(arg0);
    }

    @Override
    public void declareRoles(String... arg0) {
      delegate.declareRoles(arg0);
    }

    @Override
    public Object getAttribute(String arg0) {
      return delegate.getAttribute(arg0);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
      return delegate.getAttributeNames();
    }

    @Override
    public ClassLoader getClassLoader() {
      return delegate.getClassLoader();
    }

    @Override
    public String getContextPath() {
      return delegate.getContextPath();
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
      return delegate.getDefaultSessionTrackingModes();
    }

    @Override
    public int getEffectiveMajorVersion() {
      return delegate.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() {
      return delegate.getEffectiveMinorVersion();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
      return delegate.getEffectiveSessionTrackingModes();
    }

    @Override
    public FilterRegistration getFilterRegistration(String arg0) {
      return delegate.getFilterRegistration(arg0);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
      return delegate.getFilterRegistrations();
    }

    @Override
    public String getInitParameter(String arg0) {
      return delegate.getInitParameter(arg0);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      return delegate.getInitParameterNames();
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
      return delegate.getJspConfigDescriptor();
    }

    @Override
    public int getMajorVersion() {
      return delegate.getMajorVersion();
    }

    @Override
    public String getMimeType(String arg0) {
      return delegate.getMimeType(arg0);
    }

    @Override
    public int getMinorVersion() {
      return delegate.getMinorVersion();
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String arg0) {
      return delegate.getNamedDispatcher(arg0);
    }

    @Override
    public String getRealPath(String arg0) {
      return delegate.getRealPath(arg0);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
      return delegate.getRequestDispatcher(arg0);
    }

    @Override
    public URL getResource(String arg0) throws MalformedURLException {
      return delegate.getResource(arg0);
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
      return delegate.getResourceAsStream(arg0);
    }

    @Override
    public Set<String> getResourcePaths(String arg0) {
      return delegate.getResourcePaths(arg0);
    }

    @Override
    public String getServerInfo() {
      return delegate.getServerInfo();
    }

    @Override
    public Servlet getServlet(String arg0) throws ServletException {
      return delegate.getServlet(arg0);
    }

    @Override
    public String getServletContextName() {
      return delegate.getServletContextName();
    }

    @Override
    public Enumeration<String> getServletNames() {
      return delegate.getServletNames();
    }

    @Override
    public ServletRegistration getServletRegistration(String arg0) {
      return delegate.getServletRegistration(arg0);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
      return delegate.getServletRegistrations();
    }

    @Override
    public Enumeration<Servlet> getServlets() {
      return delegate.getServlets();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
      return delegate.getSessionCookieConfig();
    }

    @Override
    public void log(Exception arg0, String arg1) {
      delegate.log(arg0, arg1);
    }

    @Override
    public void log(String arg0, Throwable arg1) {
      delegate.log(arg0, arg1);
    }

    @Override
    public void log(String arg0) {
      delegate.log(arg0);
    }

    @Override
    public void removeAttribute(String arg0) {
      delegate.removeAttribute(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
      delegate.setAttribute(arg0, arg1);
    }

    @Override
    public boolean setInitParameter(String arg0, String arg1) {
      return delegate.setInitParameter(arg0, arg1);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> arg0)
        throws IllegalStateException, IllegalArgumentException {
      delegate.setSessionTrackingModes(arg0);
    }
  }
  
  ///////////////////////////////////////////
  
  public static interface GetContextListener {
    void onGetContext(String context);
  }
}

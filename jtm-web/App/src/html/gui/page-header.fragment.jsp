<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
  String managerApacheHref = request.getServletContext().getInitParameter("org.jepria.tomcat.manager.web.managerApacheHref");
  if (managerApacheHref == null) {
    managerApacheHref = "/manager-apache";
  }
%>

<div class="page-header">
  
  <a class="page-header__menu-item" href="<%= managerApacheHref %>" target="_blank">Apache HTTPD</a> <!-- NON-NLS -->
  &emsp;&emsp;&emsp;
  <a class="page-header__menu-item" href="jdbc">JDBC коннекты</a> <!-- NON-NLS -->
  &emsp;
  <a class="page-header__menu-item" href="log">Логи</a> <!-- NON-NLS -->
  &emsp;
  <a class="page-header__menu-item" href="port">Порты</a> <!-- NON-NLS -->
  &emsp;&emsp;&emsp;
  <a class="page-header__menu-item" href="api" target="_blank">API docs</a> <!-- NON-NLS -->
  
  <div class="page-header__button-logout big-black-button" onclick="onLogoutButtonClick();">ВЫЙТИ</div> <!-- NON-NLS -->
</div>

<style type="text/css">
  .page-header {
    background-color: black;
    
    height: 45px;
    
    padding-left: 28px;
  }
  
  /* the page header affects the login screen on the same page (if the one is present) */
  .page-header ~ #loginScreen {
     top: 45px; /* same with height of the page-header */
  }

  .page-header__menu-item {
    font-family: serif;
    font-size: 16px;
    font-weight: bold;
    
    color: #777;
  }
  
  .page-header__menu-item:hover {
    color: green;
  }
    
  a.page-header__menu-item {
    text-decoration: none;
  }
  
  .page-header__menu-item_current {
    color: white;
  }
  
  
  .page-header__menu-item:hover {
    color: #0089bd;
  }
  .page-header__menu-item_current:hover {
    color: white;
  }
  
  
  .page-header > * {
    line-height: 45px;
  }
  
  .page-header__button-logout {
    float: right;
    
    padding-left: 28px;
    padding-right: 28px;
  }
  
  .hidden {
    display: none;
  }
  
</style>

<script>
  // disable the current menu item (which links to the same location)
  var links = document.getElementsByClassName("page-header__menu-item");
  for (var i = 0; i < links.length; i++) {
    var hrefMatch = document.location + "";
    var qsIndex = hrefMatch.indexOf("?");
    if (qsIndex != -1) {
      hrefMatch = hrefMatch.substring(0, qsIndex);      
    }
    if (links[i].href == hrefMatch) {
      links[i].removeAttribute("href");
      links[i].classList.add("page-header__menu-item_current");
    }
  }
  
  function onLogoutButtonClick() {
    logout(function(){
      windowReload();
    });
  }
</script>
function pageHeader_onload() {
  document.getElementsByClassName("page-header__button-logout")[0].onclick = function() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4) {
        // reload page; location.reload() not working in FF and Chrome 
        window.location.href = window.location.href;
      }
    };
    xhttp.open("POST", "logout", true);
    xhttp.send();
  };
}

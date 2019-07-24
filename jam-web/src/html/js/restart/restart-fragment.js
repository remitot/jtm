function restart_fragment_onload() {
  
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    
    if (this.readyState === XMLHttpRequest.DONE) {
      windowReload();
    }
  }
  xhr.open("POST", 'restart/exec', true);
  xhr.send();
}
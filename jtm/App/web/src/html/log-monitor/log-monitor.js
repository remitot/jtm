/**
 * Returns scroll offset (the viewport position) from the bottom of the page, in pixels
 */
function getOffset() {
  var offset = window.location.hash.substring(1);
  if (offset) {
    return offset;
  } else {
    return 0;
  }
}

function logmonitor_onload() {
  // scroll to the offset
  document.body.style.height = (document.documentElement.clientHeight + 10) + "px";
  
  window.scrollTo(0, document.body.scrollHeight - getOffset());
}

window.onscroll = function() {
  var scrolled = window.pageYOffset || document.documentElement.scrollTop;
  
  window.location.hash = "#" + (document.body.scrollHeight - scrolled); 
  
  if (scrolled == 0) {
    // top reached
    loadNext();
  }
}
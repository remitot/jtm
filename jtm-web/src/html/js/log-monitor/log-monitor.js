function getSplitY() {
  return logmonitor_linesTop.offsetTop + logmonitor_linesTop.clientHeight;
}

/** 
 * Returns scroll offset (the viewport position) from the split position, in pixels 
 */ 
function getOffset() { 
  var offset = window.location.hash.substring(1); 
  if (offset) { 
    return Number(offset); 
  } else { 
    return null; 
  } 
} 

function onControlTopClick() {
  var controlTop = document.getElementsByClassName("control-top")[0];
  controlTop.disabled = true;
  
  var offset = getSplitY() - getScrolled();
  windowReload(logmonitor_loadMoreLinesUrl + "#" + offset);
}

function onResetAnchorButtonClick() {
  var resetAnchorButton = document.getElementsByClassName("control-button_reset-anchor")[0];
  resetAnchorButton.disabled = true;
  resetAnchorButton.removeAttribute("title");

  // reset anchor
  var offset = getOffset() + document.querySelectorAll(".content-area__lines.bottom")[0].clientHeight;
  window.location.hash = "#" + offset; // TODO remove this action? (The window will be reloaded immediately anyway)
  windowReload(logmonitor_resetAnchorUrl + "#" + offset);
  
}

function logmonitor_onload() { 
  /* scroll to the offset */ 

  var offset = getOffset(); 
  if (offset) {
    // scroll to the particular offset
    scrollVerticalTo(getSplitY() - offset); 
  } else {
    contentArea = document.getElementsByClassName("content-area")[0];
    if (contentArea.clientHeight <= window.innerHeight) {
      if (contentArea.clientHeight > 0) {
        // scroll to the top of the content
        scrollVerticalTo(contentArea.getBoundingClientRect().top);
      }
    } else {
      // scroll to the very bottom
      scrollVerticalTo(contentArea.getBoundingClientRect().top + contentArea.clientHeight - window.innerHeight); 
    } 
  }


  addHoverForBigBlackButton(document.getElementsByClassName("big-black-button")[0]);

  adjustResetAnchorButtonVisiblity();
} 


function scrollVerticalTo(y) {
  if (document.body.scrollHeight <= window.innerHeight + y) {
    // adjust body height to the requested scroll
    document.body.style.height = (window.innerHeight + y) + "px";
  }
  window.scrollTo(0, y); 
} 

function getScrolled() {
  return window.pageYOffset || document.documentElement.scrollTop;
}

function getDocHeight() {
  return Math.max(
      document.body.scrollHeight, document.documentElement.scrollHeight,
      document.body.offsetHeight, document.documentElement.offsetHeight,
      document.body.clientHeight, document.documentElement.clientHeight
  );
}

window.onscroll = function() { 

  var offset = getSplitY() - getScrolled(); 
  window.location.hash = "#" + offset; 

  adjustResetAnchorButtonVisiblity();
}


function adjustResetAnchorButtonVisiblity() {
  if (logmonitor_canResetAnchor) {
    if (getScrolled() + window.innerHeight >= logmonitor_linesBottom.offsetTop) {
      document.getElementsByClassName("control-button_reset-anchor")[0].classList.remove("hidden");
    } else {
      document.getElementsByClassName("control-button_reset-anchor")[0].classList.add("hidden");
    }
  }
}

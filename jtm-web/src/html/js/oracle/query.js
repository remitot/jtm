function onTableDataWrapperScroll() {
  document.getElementsByClassName("tableHeaderWrapper")[0].scrollLeft = 
    document.getElementsByClassName("tableDataWrapper")[0].scrollLeft;
}
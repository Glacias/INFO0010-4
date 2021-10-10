function display(id) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      var resp = JSON.parse(xhttp.response);
      if(resp['state']==0)
      {
        document.getElementById(id).innerHTML = missImage;
      }
      else
      {
        document.getElementById(id).innerHTML = hitImage;
      }
      document.getElementById("tries").innerHTML = "Number of tries : &nbsp " + resp['tries'];
      document.getElementById("ship").innerHTML = "Number of ships left : &nbsp " + resp['ship'];
      if(resp['ship']==0)
      {
        window.location.href = "win.html";
      }
      else if(resp['tries']>=70)
      {
        window.location.href = "lose.html";
      }
    }
  };
  xhttp.open("GET", "play.html?fire="+id, true);
  xhttp.send();
}

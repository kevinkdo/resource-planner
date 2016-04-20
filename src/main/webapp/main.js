var send_xhr = function(verb, endpoint, token, data, success_callback, error_callback) {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        var responseObject = JSON.parse(xhr.responseText);
        if (responseObject.is_error) {
          error_callback(responseObject);
        } else {
          success_callback(responseObject);
        }
      } else if (xhr.status != 0) {
        alert('Backed has crashed. Please reload the page.');
      }
    }
  };
  xhr.open(verb, endpoint, true);
  if (verb != "GET") {
    xhr.setRequestHeader("Content-Type", "application/json");
  }
  xhr.setRequestHeader("Accept", "application/json");
  if (token) {
    xhr.setRequestHeader("Authorization", "Bearer " + token);
  }
  xhr.send(data);
  return xhr;
};

//Convert Date object to y-m-d string for date input
var formatDate = function(d) {
  var month = '' + (d.getMonth() + 1);
  var day = '' + d.getDate();
  var year = '' + d.getFullYear();

  while (year.length < 4) { year = '0' + year; }
  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join('-');
};

//Converts Date object to h:m string for time input
var formatTime = function(d) {
  var h = '' + d.getHours();
  var m = '' + d.getMinutes();

  if (h.length < 2) h = '0' + h;
  if (m.length < 2) m = '0' + m;

  return h + ":" + m;
};

//Rounds a date down to the nearest minute
var round = function(d) {
  d.setMilliseconds(0);
  d.setSeconds(0);
  return d;
}

function uniq(a) {
    var seen = {};
    return a.filter(function(item) {
        return seen.hasOwnProperty(item) ? false : (seen[item] = true);
    });
}

var userId = function() {
  return jwt_decode(sessionStorage.getItem("session")).user_id;
};

var nodeId = 100;

ReactDOM.render(<Router />, document.getElementById("main"));

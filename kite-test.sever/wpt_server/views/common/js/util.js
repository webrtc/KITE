
function trace(arg) {
  var logs = document.getElementById('logs');
  var now = (window.performance.now() / 1000).toFixed(3);
  if (logs) {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(now + ': ' + arg));
    logs.appendChild(li);
  }
  console.log(now + ': ', arg);
}
//
//	Copyright 2017 Google Inc.
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	    https://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//


var resultMap = new Map();

function transformResultList (){
  jsonData.forEach(result => {
    var id='';
    result.browsers.forEach (browser => {
      id += browser.id;
    });
    resultMap.set(id, result.result);
  });
}

function displayMatrix() {
  transformResultList();
  var id = '';
  var matrixContainer = $('#matrix-container');
  var htmlString = '';
  var firstLine = '';
  var lines = '';
  firstLine += '<tr><td>.</td>';

  jsonBrowserList.forEach(browser => {
    var line = '';
    line += '<tr><td data-id="' + browser.id + '">';
    firstLine += '<td data-id="' + browser.id + '">';
    switch(browser.name) {
      case 'chrome':
        line+='<img src="assets/img/chrome.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        firstLine+='<img src="assets/img/chrome.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        break;
      case 'firefox':
        line+='<img src="assets/img/firefox.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        firstLine+='<img src="assets/img/firefox.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        break;
      case 'safari':
        line+='<img src="assets/img/safari.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        firstLine+='<img src="assets/img/safari.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        break;
      case 'MicrosoftEdge':
        line+='<img src="assets/img/edge.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        firstLine+='<img src="assets/img/edge.png" height="35" width="35" data-toggle="tooltip" data-placement="top" title=" Version: ' + browser.version + '">';
        break;
    }
    line += '<h5 class="small-boy">' + browser.platform + '</h5>';
    firstLine += '<h5 class="small-boy">' + browser.platform + '</h5>';
    line += '</td>';
    firstLine += '</td>';
    jsonBrowserList.forEach(other_browser => {
      line += '<td class ="'+browser.platform.charAt(0) + '-' + other_browser.platform.charAt(0) +'">';
      id = '' + browser.id + other_browser.id;
      var result = resultMap.get(id);
      if (typeof result == 'undefined' || result == 'SCHEDULED') {
        result = 'N/A';
      }
      switch (result){
        case 'SUCCESSFUL':
          line += '<i class="light fa fa-check result-brick ok" data-toggle="tooltip" data-placement="top" title="' + result + '"></i>';
          break;
        case 'FAILED':
          line += '<i class="light fa fa-times result-brick notok" data-toggle="tooltip" data-placement="top" title="' + result + '"></i>';
          break;
        case 'TIME OUT':
          line += '<i class="light fa fa-times result-brick notok" data-toggle="tooltip" data-placement="top" title="' + result + '"></i>';
          break;
        case 'N/A':
          line += '<i class="light fa fa-ban result-brick na" data-toggle="tooltip" data-placement="top" title="' + result + '"></i>';
          break;
        default:
          line += '<i class="light fa fa-question result-brick error" data-toggle="tooltip" data-placement="top" title="' + result + '"></i>';
      }
      line += '</td>';
    });
    line += '</tr>';
    lines += line;
  });
  firstLine += '</tr>';
  htmlString += firstLine + lines;
  matrixContainer.html(htmlString);
}

$(document).on("click", ".out-button", function(e) {
    var link = $(this).attr('data-link');
    window.open(''+link);
});

$(document).ready(function(){
  if (size == 2) {
    displayMatrix();
  }
  $(function () {
    $('[data-toggle="tooltip"]').tooltip()
  })
});
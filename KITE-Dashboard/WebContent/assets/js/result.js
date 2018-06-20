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

var defaultBrowser = 'all';
var defaultVersion = 'all';
var defaultPlatform = 'all';
var currentResultList = jsonResultList;
var filteredByBrowserResultList = currentResultList.slice();
var filteredByOsResultList = currentResultList.slice();
var overallStats = [0,0,0,0];

function updateOverall (){
    myChart.data.datasets.forEach((dataset) => {
        dataset.data = overallStats;
    });
    myChart.update();
}

function initOverallChart (){
    var ctx = document.getElementById("overall").getContext('2d');
    var maxValue = Math.max(...overallStats);
    var factor = 5;
    maxValue = maxValue+ factor - maxValue%factor;
    myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["Success", "Failed", "Error", "Pending"],
            datasets: [{
                backgroundColor: [
                "#42f4aa",
                "#ff4b30",
                "#30b3ff",
                "#f9e75e"
                ],
                data: overallStats
                }]
            },
        options: {
            responsive: true,
            legend: {
                display: false,
                position: 'top',
                labels: {
                    boxWidth: 30,
                    fontSize: 12
                }
            },
            scales: {
                yAxes: [{
                    ticks: {
                        suggestedMax: maxValue,
                        stepSize: maxValue/factor
                    }
                }]
            }
        }
    });
}



function onChangeVersion(){
  var versionList = document.getElementById('Version');
  defaultVersion = versionList.options[versionList.selectedIndex].value;
  if (defaultVersion == 'all') {
    filterByBrowser();
  } else {
    filterByBrowser();
  }
}

function gerVersionsList(){
  var options = [];
  jsonBrowserList.forEach(browser => {
    if (browser.name == defaultBrowser){
      if (!options.includes(browser.version)){
        options.push(browser.version);
      }
    }
  });
  return options;
}


function populateOption (id, options){
  var list = document.getElementById(id);
  while (list.options.length) {
      list.remove(0);
  }
  if (options) {
    var i;
    var defaultOption = new Option(id, 'all');
    list.options.add(defaultOption);
    for (i = 1; i <= options.length; i++) {
        var option = new Option(options[i-1], options[i-1]);
        list.options.add(option);
    }
  }
}

function populateResultContainer (){
  var resultContainer = $("#result-container");
  var resultCount = $("#result-count");
  resultContainer.empty();
  resultCount.empty();
  overallStats = [0,0,0,0];
  var selectedResultList;
  if (defaultBrowser != 'all' && defaultPlatform!= 'all'){
    if (filteredByOsResultList.length > filteredByBrowserResultList.length) {
      selectedResultList = filteredByBrowserResultList;
    } else {
      selectedResultList = filteredByOsResultList;
    }
  } else if (defaultBrowser == 'all' && defaultPlatform == 'all'){
    selectedResultList = currentResultList;
  } else {
    if (defaultPlatform == 'all') {
      selectedResultList = filteredByBrowserResultList;
    }
    if (defaultBrowser == 'all') {
      selectedResultList = filteredByOsResultList;
    }
  }
  resultCount.html(' ( Showing '+selectedResultList.length + ')');
  var htmlString = '';
  selectedResultList.forEach(result => {
    var browsers = result.browsers;
    var id = result.id;
    var resultVisibility = "visible";
    htmlString += '<tr class="small-boy">';

    result.browsers.forEach(browser => {
      htmlString += '<td>';
      switch(browser.name) {
        case 'chrome':
          htmlString+='<img src="assets/img/chrome.png" height="35" width="35">';
          break;
        case 'firefox':
          htmlString+='<img src="assets/img/firefox.png" height="35" width="35">';
          break;
        case 'safari':
          htmlString+='<img src="assets/img/safari.png" height="35" width="35">';
          break;
        case 'MicrosoftEdge':
          htmlString+='<img src="assets/img/edge.png" height="35" width="35">';
          break;
      }
      htmlString += '</td>';
      htmlString += '<td>';
      htmlString += browser.version ;
      htmlString += '</td>';
      htmlString += '<td>';
      htmlString += browser.platform ;
      htmlString += '</td>';
    });
    if (result.stat) {
      htmlString += '<td><button type="button" class="btn btn-light stats" data-id="' + id + '" data-stat="yes">';
      htmlString += '<i class=\"light fa fa-line-chart ok result-brick \"></i>';
    } else {
      htmlString += '<td><button type="button" class="btn btn-light stats" data-id="' + id + '" data-stat="no">';
      htmlString += '<i class=\"light fa fa-line-chart notok result-brick\"></i>';
    }
    htmlString += '</button></td>';

    htmlString += '<td>';
    htmlString += '<span class=\"small-boy\">' + (result.duration/1000).toFixed(2) + 's</span>';
    htmlString += '</td>';
    htmlString += '<td><button type="button" class="btn btn-light" data-id="' + id + '" data-show="show">';
    switch (result.result){
      case 'SUCCESSFUL':
        htmlString += '<i class="light fa fa-check result-brick ok"></i>';
        overallStats[0] += 1;
        resultVisibility = "hidden";
        break;
      case 'FAILED':
        htmlString += '<i class="light fa fa-times result-brick notok"></i>';
        overallStats[1] += 1;
        break;
      case 'TIME OUT':
        htmlString += '<i class="light fa fa-times result-brick notok"></i>';
        overallStats[1] += 1;
        break;
      case 'SCHEDULED':
        htmlString += '<i class="light fa fa-refresh result-brick pending"></i>';
        overallStats[3] += 1;
        break;
      default:
        htmlString += '<i class="light fa fa-question result-brick error"></i>';
        overallStats[2] += 1;
    }
    htmlString += '</td>';

    htmlString += '<td colspan="2">';
    htmlString += '<div class="result-detail" id="result-line-' + id + '" style="visibility: ' + resultVisibility + ';">'+result.result+'</div>';
    htmlString += '</td>';

    htmlString += '<td>';
    htmlString += '<button type="button" class="btn btn-light log" data-id="' + id + '" data-show="show">Show logs</button>'
    htmlString += '</td>';
    htmlString += '</tr>';


  });
  resultContainer.html(htmlString);
}



$(document).on("click", ".browser-image", function(e) {
  if ($(this).height() == 45){
    $(this).attr('style','height:30px;width:30px;');
    defaultBrowser = 'all';
    $('#Version').attr('style', 'visibility: hidden;');
    filteredByBrowserResultList = [];
    filterByBrowser();
  } else {
    $(".browser-image").attr('style','height:30px;width:30px;');
    $(this).attr('style','height:45px;width:45px;');
    var id = $(this).attr('id');
    defaultBrowser = id;
    $('#Version').attr('style', 'visibility: visible;')
    populateOption('Version', gerVersionsList());
    filterByBrowser();
  }
});

$(document).on("click", ".os-image", function(e) {
  if ($(this).height() == 45){
    $(this).attr('style','height:30px;width:30px;');
    defaultPlatform = 'all';
    filteredByOsResultList = [];
    filterByOs();
  } else {
    $(".os-image").attr('style','height:30px;width:30px;');
    $(this).attr('style','height:45px;width:45px;');
    var id = $(this).attr('id');
    defaultPlatform = id;
    filterByOs();
  }
});

$(document).on("click", ".log", function(e) {
  var id = $(this).attr('data-id');
  $('.log').attr('style','background: none;');
  $(this).attr('style','background: #42f4aa;');
  (function getAndDisplayResult() {
    $.ajax({
      url: 'getlog?name='+resultTableName+'&id='+id,
      success: function(result){
        console.log(result);
        displayLog(JSON.parse(result));
      }
    });
  })();
});

$(document).on("click", ".stats", function(e) {
    var stat = $(this).attr('data-stat');
    if (stat == 'yes') {
      var id = $(this).attr('data-id');
      window.open('stat?name='+resultTableName+'&id='+id, '_blank');
    } else {
      alert ('No stats available.');
    }
});

$(document).on("click", ".less", function(e) {
  var id = $(this).attr('id');
  if ($(this).height() > 16){ //Makes no sense this value but it is what it is
    id = 'all';
    $(this).attr('style','height:30px;width:40px;');
    $(".less").attr('style','height:30px;width:40px;');
  } else {
    $(".less").attr('style','height:30px;width:40px;background:grey;');
    $(this).attr('style','height:45px;width:60px;');
  }
  filterByResult(id);
});


$(document).on("click", ".clear", function(e) {
  var target = $(this).attr('data-clear');
  switch (target){
    case 'browser':
      $(".browser-image").attr('style','height:30px;width:30px;');
      $('#Version').attr('style', 'visibility: hidden;')
      defaultBrowser = 'all';
      defaultVersion = 'all';
      filteredByBrowserResultList = [];
      filterByBrowser();
      break;
    case 'result':
      $(".less").attr('style','height:30px;width:40px;');
      filterByResult('all');
      break;
    case 'os':
      $(".os-image").attr('style','height:30px;width:30px;');
      defaultPlatform = 'all';
      filteredByOsResultList = [];
      filterByOs();
      break;
  }
});

function filterByResult(type){
  (function requestByResult() {
      $.ajax({
          url: 'getjson?resultTableName='+resultTableName+'&testId='+testId+'&type='+type,
          success: function(result){
            currentResultList = JSON.parse(result);
            console.log(currentResultList);
            filteredByBrowserResultList = JSON.parse(result);
            if (defaultBrowser != 'all') {
              filterByBrowser();
            }
            if (defaultPlatform != 'all') {
              filterByOs();
            }
            populateResultContainer();
            updateOverall();
          }
      });
  })();
}

function filterByBrowser () {
  var add = false;
  filteredByBrowserResultList = []
  if (defaultPlatform == 'all') {
    filteredByBrowserResultList = currentResultList.slice();
  } else {
    filteredByBrowserResultList = filteredByOsResultList.slice();
  }
  var tempFilteredList = [];
  if (defaultBrowser == 'all') {
    // Do nothing
  } else {
    filteredByBrowserResultList.forEach(result => {
      add = false;
      result.browsers.forEach(browser =>{
        if(browser.name == defaultBrowser){
          add = true;
        }
      });
      if (add) {
        tempFilteredList.push(result);
      }
    });
    filteredByBrowserResultList = [];
    filteredByBrowserResultList = tempFilteredList.slice();
    tempFilteredList = [];
    if (defaultVersion == 'all') {
      // Do nothing
    } else {
      filteredByBrowserResultList.forEach(result => {
        add = false;
        result.browsers.forEach(browser =>{
          if(browser.version == defaultVersion){
            add = true;
          }
        });
        if (add) {
          tempFilteredList.push(result);
        }
      });
      filteredByBrowserResultList = [];
      filteredByBrowserResultList = tempFilteredList.slice();
    }
  }
  populateResultContainer();
  updateOverall();
}


function filterByOs() {
  var add = false;
  var tempFilteredList = [];
  filteredByOsResultList = []
  if (defaultBrowser == 'all') {
    filteredByOsResultList = currentResultList.slice();
  } else {
    filteredByOsResultList = filteredByBrowserResultList.slice();
  }
  if (defaultPlatform == 'all'){
    // Do nothing
  } else {
    filteredByOsResultList.forEach(result => {
      add = false;
      result.browsers.forEach(browser =>{
        if(browser.platform.startsWith(defaultPlatform)){
          add = true;
        }
      });
      if (add) {
        tempFilteredList.push(result);
      }
    });
    filteredByOsResultList = [];
    filteredByOsResultList = tempFilteredList.slice();
  }
  populateResultContainer();
  updateOverall();
}

function displayLog(resultObject){
  var myTab = $('#myTab');
  var myTabContent = $('#myTabContent');
  myTab.empty();
  myTabContent.empty();
  var myTabHtml = '';
  var myTabContentHtml = '';
  Object.keys(resultObject).forEach(function(key){
    var id = key.replace(new RegExp('\\.', 'g'), '_');
    var log = resultObject[key];
    myTabHtml +=
      '<li class="nav-item">' +
        '<a class="nav-link" id="'+id+'-tab" data-toggle="tab" href="#'+id+'-tab-content" role="tab" aria-controls="'+id+'-tab-content" aria-selected="false">' +
          '<i class="fa fa-bullseye"></i>' + id +
        '</a>' +
      '</li>';
    myTabContentHtml += '<div class="tab-pane" id="'+id+'-tab-content" role="tabpanel" aria-labelledby="'+id+'-tab">' +
      '<div class="list-group list-group-flush small">' +
         '<pre class="small-container log-message">';
    console.log('log: ' + log);
    var lines = log.split('/r/n');
    lines.forEach (function(line){
      console.log('adding:' + line);
      myTabContentHtml += line + '</br>';
    })
    myTabContentHtml+= '</pre>'
    + '</div>'
    + '</div>' ;
  })

  $(myTabHtml).appendTo(myTab);
  $(myTabContentHtml).appendTo(myTabContent);
  $('#myTab a:first').tab('show');
}

$(document).ready(function(){
    populateResultContainer();
    initOverallChart();
});
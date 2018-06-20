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

/**
* Display the scoreboard unto container;
*/

var test;
var path;
var color_map = new Map();

function populateColorMap(){
  var listOfBrowsers = Object.keys(scoreJson);
  var listOfTestCategories = Object.keys(scoreJson[listOfBrowsers[0]]);
  listOfTestCategories.forEach(function(testCategory){
    var count = 0;
    var passed = 0;
    var total = 0;
    listOfBrowsers.forEach(function(browser){
      var testCategoryResult = scoreJson[browser][testCategory];
      if (testCategoryResult.total !== 0){
        count += 1;
        var percent = 100*testCategoryResult.passed/testCategoryResult.total.toFixed(0);
        if (percent > 50) {
          passed += 1;
        }
      }
    });

    if (count < 2) {
      if (count === 0) {
        color_map.set(testCategory, 'black');
      } else {
        color_map.set(testCategory, 'blue');
      }
    } else {
      if (passed > 2) {
        color_map.set(testCategory, 'green');
      } else {
        color_map.set(testCategory, 'orange');
      }
    }
  });
}

function displayScoreboard(){
  populateColorMap();
  console.log(color_map);
  var scoreboardHead = $('#scoreboardHead');
  var scoreboardBody = $('#scoreboardBody');
  var firstRow = '<tr><th>';
  if (path === '#') {
    firstRow += '<a class="small-boy" href="#">.</a>'
  } else {
    firstRow += '<a class="medium-boy" href="score"><i class="fa fa-chevron-circle-left"></i></a>'
  }
  firstRow += '</th>';
  var content = '';
  var listOfTestSuite = [];


  var listOfBrowsers = Object.keys(scoreJson);
  var listOfTestCategories = Object.keys(scoreJson[listOfBrowsers[0]]);
  listOfBrowsers.forEach(function(browser){
    var browserJson = scoreJson[browser];
    var tmp = "";
    firstRow += '<th text-align="center" id="'+browser+'">';
    firstRow += getClientHTML(browser,tmp);
    firstRow += '</th>';
  });
  firstRow +='</tr>';
  listOfTestCategories.forEach(function(testCategory){
    var color_code = color_map.get(testCategory);
    var link = 'testCategory';
    if (scoreJson[listOfBrowsers[0]][testCategory]['isTest'] == true){
       link += '=' + path + '&test';
    }
    content += '<tr><td class="test-name"><a href="?'+link+'='+testCategory+'" data-toggle="tooltip" data-placement="top" title="'+
            scoreJson[listOfBrowsers[0]][testCategory]['description'] + '" class="small-boy">' + testCategory + '</a></td>';
    listOfBrowsers.forEach(function(browser){
      var testCategoryResult = scoreJson[browser][testCategory];
      var total = testCategoryResult.total;
      var passed = testCategoryResult.passed;
      var class_name = '';
      var style = '';
/*      if (total === 0){
        class_name += 'non-existing"';
      } else {
        if (passed === 0) {
          class_name += 'strange-color"';
        } else {
          var percent = 100*passed/total.toFixed(0);
          if (percent > 50) {
            class_name += 'ready"';
          } else {
            class_name += 'not-ready"';
          }
        }
      }*/
      switch (color_code){
        case 'black':
          class_name += 'non-existing';
          break;
        case 'blue':
          class_name += 'strange-color';
          break;
        case 'green':
          if (total === 0) {
            class_name += 'non-existing"';
          } else {
/*            if (passed === 0){
              class_name += 'non-passed"';
            } else {*/
              if (passed === total) {
                style += 'background: rgb(0, 255, 100);';
              } else {
                var percent = 10*passed/total.toFixed(0);
                var red = 255 - 15*percent;
                var blue = 255 - 15*percent;
                style += 'background: rgb(' + red + ',255,' + blue +');';
              }
            /*}*/
          }
          break;
        case 'orange':
          if (total === 0) {
            class_name += 'non-existing"';
          } else {
            /*if (passed === 0){
              class_name += 'non-passed"';
            } else {*/
              if (passed === total) {
                style += '255, 155, 0';
              } else {
                var percent = 10*passed/total.toFixed(0);
                var blue = 255 - 15*percent;
                var green = 255 - 10*percent;
                style += 'background: rgb(255,' + green + ',' + blue +');';
              }
            /*}*/
          }
          break;
      }
      content += '<td align="center" class="'+class_name+'" style="'+style+'" id="'+browser+'"><a href="?'+link+'='+testCategory+'" class="score-tile">' + passed+'/'+ total + '</a></td>'
    });
    content += '</tr>'
  });

  scoreboardHead.html(firstRow);
  scoreboardBody.html(content);
}

function displayIndividualTests(){
  var scoreboardHead = $('#scoreboardHead');
  var scoreboardBody = $('#scoreboardBody');
  var firstRow = '<tr><th><a class="small-boy" href="?testCategory='+path+'"><i class="fa fa-chevron-circle-left"> '+path+'</a></th>';
  var content = '';
  var subTests = [];
  console.log("Displaying for: " + test);
  var listOfBrowsers = Object.keys(scoreJson);
  var listOfTestCategories = Object.keys(scoreJson[listOfBrowsers[0]]);
  listOfBrowsers.forEach(function(browser){
    var browserJson = scoreJson[browser];
    testJson = browserJson[''+test];
    if (Object.keys(testJson).length === 0 && testJson.constructor === Object){
      //skip
    } else {
      var tests = testJson.tests;
      if (Object.keys(tests).length > subTests.length){
        subTests = Object.keys(tests);
      }
    }
    var tmp = "";
    firstRow += '<th text-align="center" id="'+browser+'">';
    firstRow += getClientHTML(browser,tmp);
    firstRow += '</th>';
  });
  firstRow +='</tr>';
  console.log("sub tests: ");
  console.log(subTests);

  subTests.forEach( function(subTest){
    content += '<tr><td class="test-name"><a class="small-boy">' + subTest + '</a></td>';
    listOfBrowsers.forEach(function(browser){
      var browserJson = scoreJson[browser];
      testJson = browserJson[''+test];
      if (Object.keys(testJson).length === 0 && testJson.constructor === Object){
        content += '<td align="center" id="'+browser+'"><a class="small-boy"> tbd </a></td>'
      } else {
        var result = testJson.tests[subTest];
        var style = 'style="background: rgb('
        if (result === 'passed'){
          style += '56, 234, 88';
        }
        style += ');"';
        content += '<td id="'+browser+'" '+style+'><div><a class="small-boy detail">' + result[subTest] + '</a></div></td>';
      }
    });
    content += '</tr>'
  })
  scoreboardHead.html(firstRow);
  scoreboardBody.html(content);
}

function getClientHTML(browser, timeStamp){
  var html = '';
  var splitBrowser = browser.split("_");
  var name = splitBrowser[0];
  var version = splitBrowser[1];
  var platform = splitBrowser[2];
  var logo = '';
  switch(name){
    case 'safari':
      if (version==='11.0') {
        logo = 'safari';
      } else {
        logo = 'stp';
      }
      break;
    case 'firefox':
      if (version==='60.0'){
        logo = 'firefox';
      } else {
        logo = 'nightly';
      }
      break;
    case 'chrome':
      if (version==='67.0'){
        logo = 'chrome';
      } else {
        logo = 'canary';
      }
      break;
    case 'MicrosoftEdge':
      if (version==='17.17134'){
        logo = 'edge';
      } else {
        logo = 'insider';
      }
      break;
  }
  html+='<h4 class="small-boy" data-toggle="tooltip" data-placement="top" title="Version: '+version+' on '+platform+'-'+timeStamp+' "><img src=\"assets/img/'+logo+'.png\" height=\"25\" width=\"25\">';

  switch(platform.substring(0,1)){
    case 'L':
      html+='<img src=\"assets/img/ubuntu.png\" height=\"14\" width=\"14\">';
      break;
    case 'W':
      html+='<img src=\"assets/img/windows.png\" height=\"14\" width=\"14\">';
      break;
    case 'O':
      html+='<img src=\"assets/img/mac.png\" height=\"14\" width=\"14\">';
      break;
    case 'A':
      html+='<img src=\"assets/img/android.png\" height=\"14\" width=\"14\">';
      break;
    case 'I':
      html+='<img src=\"assets/img/ios.png\" height=\"14\" width=\"14\">';
      break;
  }
  html+='</h4>';
  html+='<h5 class="small-boy">'+timeStamp+'</h5>';

  return html;
}



$(document).ready(function(){
  if (scoreJson.isTest == true ){
    delete scoreJson.isTest;
    test = scoreJson.test;
    delete scoreJson.test;
    console.log(scoreJson);
    path = scoreJson.path;
    delete scoreJson.path;
    console.log(path);
    displayIndividualTests();
  } else {
    delete scoreJson.isTest;
    if (typeof scoreJson.path === 'undefined') {
      path = "#";
    } else {
      path = scoreJson.path;
      delete scoreJson.path;
    }
    displayScoreboard();
  }
  $(window).scroll(function () {
      if ($(this).scrollTop() > 50) {
          $('#back-to-top').fadeIn();
      } else {
          $('#back-to-top').fadeOut();
      }
  });
  // scroll body to 0px on click
  $('#back-to-top').click(function () {
      $('#back-to-top').tooltip('hide');
      $('body,html').animate({
          scrollTop: 0
      }, 800);
      return false;
  });

  $(document).on("click", ".detail", function(e) {
    alert('coming soon!');
  });

  $('#back-to-top').tooltip('show');

  $('[data-toggle="tooltip"]').tooltip();

  $(".sticky-header").floatThead({
      scrollingTop: 1
  });
});


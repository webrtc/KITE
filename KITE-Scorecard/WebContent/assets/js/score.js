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
function displayScoreboardFirstLayer(){
    var scoreboardHead = $('#scoreboardHead');
    var scoreboardBody = $('#scoreboardBody');
    var firstRow = '<tr><th><a class="medium-boy" href="#">WPT</a></th>';
    var content = '';
    var listOfTestSuite = [];


    var listOfBrowsers = Object.keys(scoreJson);
    listOfBrowsers.forEach(function(browser){
        var browserJson = scoreJson[browser];
        var timeStamp = browserJson.time_stamp;
        var options = { day: 'numeric', month: 'long',year: 'numeric' };
        var tmp = new Date(timeStamp).toLocaleDateString('en-GB', options);
        firstRow += '<th text-align="center" id="'+browser+'">';
        firstRow += getClientHTML(browser,tmp);
        firstRow += '</th>';

        var browserResultJson = browserJson.results;
        var browserTestSuiteList = Object.keys(browserResultJson);
        if (browserTestSuiteList.length > listOfTestSuite.length)
            listOfTestSuite = browserTestSuiteList;
    });
    firstRow +='</tr>';
    listOfTestSuite.forEach(function(testSuite){
        content += '<tr><td class="test-name"><a href="?testsuite='+testSuite+'" class="small-boy">' + testSuite + '</a></td>'
        listOfBrowsers.forEach(function(browser){
            var browserJson = scoreJson[browser];
            var browserResultJson = browserJson.results;
            var testSuiteResults = browserResultJson[testSuite];
            var total = testSuiteResults.total;
            var pass = testSuiteResults.passed;
            content += '<td align="center" id="'+browser+'"><a href="?testsuite='+testSuite+'" class="small-boy">' + pass+'/'+ total + '</a></td>'
        });
    });

    scoreboardHead.html(firstRow);
    scoreboardBody.html(content);
}

/**
* Display the scoreboard unto container;
*/
function displayScoreboardMiddleLayers(){
    var scoreboardHead = $('#scoreboardHead');
    var scoreboardBody = $('#scoreboardBody');
    var firstRow = '<tr><th><a class="small-boy" href="/score-card">WPT</a></br>/<a class="medium-boy" href="#">'+testSuite+'</a></th>';
    var content = '';
    var contentMap = new Map();
    var testList = [];

    var listOfBrowsers = Object.keys(scoreJson);
    listOfBrowsers.forEach(function(browser){
        var browserJson = scoreJson[browser];
        var timeStamp = browserJson.time_stamp;
        var options = { day: 'numeric', month: 'long',year: 'numeric' };
        var tmp = new Date(timeStamp).toLocaleDateString('en-GB', options);
        firstRow += '<th text-align="center" id="'+browser+'">';
        firstRow += getClientHTML(browser,tmp);
        firstRow += '</th>';

        var browserResultJson = browserJson.results;
        var browserTestSuiteResult = browserResultJson[testSuite].result;
        var browserTestList = Object.keys(browserTestSuiteResult);
        if (browserTestList.length > testList.length)
            testList = browserTestList;
        browserTestList.forEach(function(test){
            if (test === 'passed' || test === 'total' || test === 'isTest' ){
                // skip these
            } else {
                var testJson = browserTestSuiteResult[test];
                var total = testJson.total;
                var pass = testJson.passed;

                if (!contentMap.has(test)){
                    contentMap.set(test, '<tr><td align="left" class="test-name"><a href="?testsuite='+testSuite+'&test='+test+'" class="small-boy">' + test + '</a></td>');
                }
                var tmpContent = contentMap.get(test);
                if(pass===total)
                    tmpContent += '<td align="center" class="result passed" data-hover="'+browser+'"><a href="?testsuite='+testSuite+'&test='+test+'" class="small-boy">' + pass+'/'+ total + '</a></td>';
                else{
                    if(pass===0)
                        tmpContent += '<td align="center" class="result failed" data-hover="'+browser+'"><a href="?testsuite='+testSuite+'&test='+test+'" class="small-boy">' + pass+'/'+ total + '</a></td>';
                    else{
                        var percentage = pass/total;
                        var marge = (255*percentage);
                        var style = 'style="background: rgb(';
                        if (percentage>=0.5)
                            style+= (255-(marge/2).toFixed(0))+','+(90+(marge/2).toFixed(0))+',100);"';
                        else
                            style+= (90+(marge/2).toFixed(0))+','+(255-(marge/2).toFixed(0))+',100);"';
                        tmpContent += '<td align="center"'+style+' class="result" data-hover="'+browser+'"><a href="?testsuite='+testSuite+'&test='+test+'" class="small-boy">' + pass+'/'+ total + '</a></td>';
                    }
                }
                contentMap.set(test,tmpContent);
            }
        });
    });
    testList.forEach(function(test){
        var tmpContent = contentMap.get(test);
        tmpContent += '</tr>';
        content += tmpContent;
    });
    firstRow +='</tr>';
    scoreboardHead.html(firstRow);
    scoreboardBody.html(content);
}


/**
* Display the scoreboard unto container;
*/
function displayScoreboardLastLayer(){
    var scoreboardHead = $('#scoreboardHead');
    var scoreboardBody = $('#scoreboardBody');
    var firstRow = '<tr><th><a class="small-boy" href="/score-card">WPT</a></br>/<a class="small-boy" href="?testsuite='
                +testSuite+'">'+testSuite+'</a></br>/<a class="medium-boy" href="#">'+test+'</a></th>';
    var content = '';
    var contentMap = new Map();
    var testList = [];

    var listOfBrowsers = Object.keys(scoreJson);

    listOfBrowsers.forEach(function(browser){
        var browserJson = scoreJson[browser];
        var timeStamp = browserJson.time_stamp;
        var options = { day: 'numeric', month: 'long',year: 'numeric' };
        var tmp = new Date(timeStamp).toLocaleDateString('en-GB', options);
        firstRow += '<th text-align="center" id="'+browser+'">';
        firstRow += getClientHTML(browser,tmp);
        firstRow += '</th>';

        var browserResultJson = browserJson.results;
        var browserTestResult = browserResultJson[testSuite].result;
        var testJson = browserTestResult[test];
        var subTests = testJson.tests;
        var targetedSubTest = testJson.tests;
        var subTestList = Object.keys(subTests);
        testList = subTestList;
        subTestList.forEach(function(subTest){
            var subTestResult = subTests[subTest];
            if (!contentMap.has(subTest)){
                contentMap.set(subTest, '<tr><td class="test-name"><h4 class="small-boy" align="left">' + subTest + '</h4></td>');
            }
            var tmpContent = contentMap.get(subTest);
            if (typeof subTestResult === 'undefined'){
                subTestResult = 'undefined';
            }
            switch(subTestResult){
                case 'passed':
                    tmpContent += '<td class="" align="center" id="'+browser+'"><h4 class="medium-boy"><i class="pe-7s-check green"></i></h4></td>';
                    break;
                default:
                    subTestResult = subTestResult.replace(/['"]+/g, '');
                    tmpContent += '<td class="result" align="center"><h4  id="'+browser+'" class="medium-boy error-message" data-test="'+subTest
                                    +'" data-message="'+subTestResult+'" data-toggle="tooltip" data-placement="top" title="Message: '
                                    +subTestResult+'"><i class="pe-7s-close-circle red"></i></h4></td>';
            }
            contentMap.set(subTest,tmpContent);
        });

    });
    testList.forEach(function(test){
        var tmpContent = contentMap.get(test);
        tmpContent += '</tr>';
        content += tmpContent;
    });
    firstRow +='</tr>';

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
            if (version==='11.0')
                logo = 'safari';
            else
                logo = 'stp';
            break;
        case 'firefox':
            if (version==='59.0')
                logo = 'firefox';
            else
                logo = 'nightly';
            break;
        case 'chrome':
            if (version==='65.0')
                logo = 'chrome';
            else
                logo = 'canary';
            break;
        case 'MicrosoftEdge':
            if (version==='16.0')
                logo = 'edge';
            else
                logo = 'insider';
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
    html+='<h5 class="tiny-boy">'+timeStamp+'</h4>';

    return html;
}

$(document).on('click', '.error-message', function(e){
    var browser = $(this).attr('id');
    var test = $(this).attr('data-test');
    var message = $(this).attr('data-message');
    $('#error-test').html(getClientHTML(browser)+test );
    $('#error-message').html(message);
    $('#error-log').modal();
});


$(document).ready(function(){
    switch(layer) {
        case 2:
            displayScoreboardMiddleLayers();
            break;
        case 3:
            displayScoreboardLastLayer();
            break;
        default:
            displayScoreboardFirstLayer()
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

    $('#back-to-top').tooltip('show');

    $('[data-toggle="tooltip"]').tooltip();

    $(".sticky-header").floatThead({
        scrollingTop: 1
    });
});


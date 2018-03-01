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
function displayScoreboard(){

    var scoreHTML='';
    var scoreMap = new Map();
    var ratioMap = new Map();
    var percentageMap = new Map();
    var descriptionMap = new Map();
    var tests = Object.keys(scoreJson);
    var longestBrowserScoreArr;
    var size = 0;
    tests.forEach(function(test){
        var browserScoreArr = scoreJson[test];
        browserScoreArr.forEach(function(browserScore){
        var key = test + browserScore.name + browserScore.version + browserScore.platform;
            scoreMap.set(key, browserScore.result);
            ratioMap.set(key, browserScore.ratio);
            percentageMap.set(key, browserScore.percentage);
            descriptionMap.set(test, browserScore.description)
        });
        if (browserScoreArr.length>size){
            longestBrowserScoreArr = browserScoreArr;
        }
    });
    scoreHTML+=getBrowserHTML(longestBrowserScoreArr);
    tests.forEach(function(testName){
        scoreHTML+=getTestScore(percentageMap,ratioMap,scoreMap,longestBrowserScoreArr,testName,descriptionMap.get(testName));
    });
    $("#scoreboard").html(scoreHTML);
}

function getTestScore(percentageMap,ratioMap,scoreMap,browserArr,testName,description){
    var html='<tr><td><a href="public" class="test-link" data-toggle="tooltip" data-placement="top" title="'+description+'">'+testName+'</a></td>';
    //var html='<tr><td><a href="public?testname='+testName+'" class="status">'+testName+'</a></td>'
    browserArr.forEach(function(browser){
        var key = testName + browser.name + browser.version + browser.platform;
        var result = scoreMap.get(key);
        var ratio = ratioMap.get(key);
        var percentage = percentageMap.get(key);
        html += '<td align="center" class="status" style="color:white;background:';
        if (result){
            switch(result){
                case 'SUCCESSFUL':
                    html+='green"><i class="pe-7s-check"></i>';
                    break;
                case 'FAILED':
                    html+='red"><i class="pe-7s-close-circle"></i>';
                    break;
                case 'ELSE':
                    html+='gold; opacity:'+percentage/100+'"><i class="pe-7s-help1"></i>';
                    break;
            }
            html+= ratio+'</td>';
        }
        else {
            html+='<i class="pe-7s-power grey"></i></td>';
        }
    });
    html += '</tr>'
    return html;
}

function getBrowserHTML(browserArr){
    var html='<tr><td></td>'
    browserArr.forEach(function(browser){
        html += '<td align="center">';
        switch(browser.name){
            case 'safari':
                html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title=" Version: '+browser.version+'"><img src=\"assets/img/safari.png\" height=\"25\" width=\"25\">';
                break;
            case 'firefox':
                html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title=" Version: '+browser.version+'"><img src=\"assets/img/firefox.png\" height=\"25\" width=\"25\">';
                break;
            case 'chrome':
                html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title=" Version: '+browser.version+'"><img src=\"assets/img/chrome.png\" height=\"25\" width=\"25\">';
                break;
            case 'MicrosoftEdge':
                html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title=" Version: '+browser.version+'"><img src=\"assets/img/edge.png\" height=\"25\" width=\"25\">';
                break;
        }
        html+='</h4>';
        html+='<h4 class="status">'+browser.platform+'</h4>';
        html+='</td>';
    });
    html += '</tr>'
    return html;
}

$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
});
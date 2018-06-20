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
* Transform results to 2D matrix form.
*/
function to2D(load){

    var results = new Map();
    var matrix = new Map();
    var durations = [];
    var stats = {};
    stats.ok = 0;
    stats.failed = 0;
    stats.error = 0;
    load.forEach(function (result){
        var browserList = result.browsers;
        if(browserList.length==2){
            var browser1 = browserList[0];
            var browser2 = browserList[1];
            if(matrix.has(JSON.stringify(browser1))){
                matrix.get(JSON.stringify(browser1)).push(browser2);
            } else {
                matrix.set(JSON.stringify(browser1),[browser2]);
            }
            var key = JSON.stringify(browser1)+JSON.stringify(browser2);
            results.set(key, result.result);
            durations.push(result.duration/1000);
            switch(result.result){
                case 'SUCCESSFUL':
                    stats.ok += 1;
                    break;
                case 'TIME OUT':
                    stats.failed += 1;
                    break;
                default:
                    stats.error+=1;
            }
        }
    });
    var data = {};
    data.matrix = matrix;
    data.results = results;
    data.durations = durations;
    data.stats = stats;
    return data;
}

/**
* Display the matrix unto container;
*/
function displayMatrix(load, container){
    var data = to2D(load);
    var matrix = data.matrix.entries();
    var results = data.results;
    var stats = data.stats;
    var iterator = matrix.next();
    var callerHtml = '<tr><td></td>'
    var resultHtml = '';
    var grid = [];
    var content = new Map();
    var callers = [];
    while(!iterator.done){
        var caller = iterator.value[0];
        var jsonCaller = JSON.parse(caller);
        callerHtml+= '<td align="center">'+getClientHTML(jsonCaller, true)+'</td>';
        content.set(caller,'<td align="center">'+getClientHTML(jsonCaller, false)+'</td>');
        iterator=matrix.next();
        callers.push(caller);
    }

    callers.forEach(function(caller){
        callers.forEach(function(callee){
            var jsonCaller = JSON.parse(caller);
            var jsonCallee = JSON.parse(callee);
            var result = content.get(caller);
            var key = callee + caller;
            switch(jsonCaller.platform){
                case 'WINDOWS 10':{
                    switch(jsonCallee.platform){
                        case 'WINDOWS 10':
                            result += '<td class="tinted" align="center">';
                            break;
                        case 'LINUX':
                            result += '<td class="tinted" align="center">';
                            break;
                        default:
                            result += '<td align="center">';
                    }
                    break;
                }case 'LINUX':{
                    switch(jsonCallee.platform){
                        case 'WINDOWS 10':
                            result += '<td class="tinted" align="center">';
                            break;
                        case 'LINUX':
                            result += '<td class="tinted" align="center">';
                            break;
                        default:
                            result += '<td align="center">';
                    }
                    break;
                }default:
                    if (jsonCallee.platform==jsonCaller.platform)
                        result += '<td class="tinted" align="center">';
                    else
                        result += '<td align="center">';
            }
            if (results.get(key)){

                result += getResultHTML(results.get(key))+'</td>';
            }
            else{
                result+= '<i class=\"pe-7s-timer\" data-toggle=\"popover\" data-placement=\"bottom\" data-content=\"NA\"></td>';
            }
            content.set(caller, result);
            iterator=matrix.next();
        });
    });

    callerHtml+='</tr>';
    if (callers.length == 0){
        resultHtml += '<tr><td class="medium-boy run-time">No result for any of the stable versions during this run.</td></tr>';
    }else{
        resultHtml+=callerHtml;
        content.forEach(function(row){
            resultHtml+=row+'</tr>';
        });
    }
    container.html(resultHtml);
}


function getClientHTML(json, caller){
    html = '';
    switch(json.name){
        case 'safari':
        if (caller){
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Caller - Version: '+json.version+'"><img src=\"assets/img/safari.png\" height=\"25\" width=\"25\">';
        } else {
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Callee - Version: '+json.version+'"><img src=\"assets/img/safari.png\" height=\"25\" width=\"25\">';
            }break;
        case 'firefox':
        if (caller){
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Caller - Version: '+json.version+'"><img src=\"assets/img/firefox.png\" height=\"25\" width=\"25\">';
        } else {
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Callee - Version: '+json.version+'"><img src=\"assets/img/firefox.png\" height=\"25\" width=\"25\">';
            }break;
        case 'chrome':
        if (caller){
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Caller - Version: '+json.version+'"><img src=\"assets/img/chrome.png\" height=\"25\" width=\"25\">';
        } else {
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Callee - Version: '+json.version+'"><img src=\"assets/img/chrome.png\" height=\"25\" width=\"25\">';
            }break;
        case 'MicrosoftEdge':
        if (caller){
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Caller - Version: '+json.version+'"><img src=\"assets/img/edge.png\" height=\"25\" width=\"25\">';
        } else {
            html+='<h4 class="status" data-toggle="tooltip" data-placement="top" title="Callee - Version: '+json.version+'"><img src=\"assets/img/edge.png\" height=\"25\" width=\"25\">';
            }break;
    }
    html+='</h4>';
    html+='<h4 class="status">'+json.platform+'</h4>';
    return html;
}

function getResultHTML(result){
    html = '';
    switch(result){
        case 'SUCCESSFUL':
            html+="<i class=\"pe-7s-check\" style=\"color:green;\"  data-toggle=\"popover\" data-placement=\"bottom\" data-content=\""+result+"\">";
            break;
        case 'TIME OUT':
            html+="<i class=\"pe-7s-close\" style=\"color:red;\" data-toggle=\"popover\" data-placement=\"bottom\" data-content=\""+result+"\">";
            break;
        case 'FAILED':
            html+="<i class=\"pe-7s-close-circle\" style=\"color:red;\" data-toggle=\"popover\" data-placement=\"bottom\" data-content=\""+result+"\">";
            break;
        default:
            html+="<i class=\"pe-7s-attention\" style=\"color:blue;\" data-toggle=\"popover\" data-placement=\"bottom\" data-content=\""+result+"\">";
    }
    return html;
}

$(document).on("click", ".out-button", function(e) {
    var link = $(this).attr('data-link');
    console.log(link);
    window.open(''+link);
});

$(document).ready(function(){
    $('[data-toggle="popover"]').popover({
        trigger:'click hover',
        placement:'right'
    });

    $('[data-toggle="tooltip"]').tooltip();
});
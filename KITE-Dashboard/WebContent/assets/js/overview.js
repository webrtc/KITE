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


$(document).on("click", "button", function(e) {
    var id = $(this).attr('id');
    if($(this).attr('class')=='btn unpick')
        $(this).attr('class', classes[id]);
    else
        $(this).attr('class', 'btn unpick');
    display[id]=!display[id];
    var val='';
    for(i=0; i<4;i++){
        if(display[i])
            val+='-1';
        else
            val+='-0';
    }
    if (browser=='all'){
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&location=overview',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    drawCircle(JSON.parse(result).sunburst);
                }
            });
        })();
    } else {
        var data = browser.split('-');
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&name='+data[0]+'&version='+data[1]+'&platform='+data[2]+'&location=overview',
                success: function(result){
                    console.log(result);
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    drawCircle(JSON.parse(result).sunburst);
                }
            });
        })();
    }
});


$(document).on("click", ".by-browser", function(e) {
    browser = $(this).attr('id');
    var val='';
        for(i=0; i<4;i++){
            if(display[i])
                val+='-1';
            else
                val+='-0';
        }
    if (browser=='all'){
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&location=overview',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#filter-by-browser" ).empty();
                    $( "#filter-by-browser" ).html('All browsers');
                    drawCircle(JSON.parse(result).sunburst);
                }
            });
        })();
    } else{
        var data = browser.split('-');
        var filterHtmlString = '';
        if (data[0] == "firefox")
            filterHtmlString+="<img src=\"assets/img/firefox.png\" height=\"20\" width=\"20\">";
        if (data[0] == "chrome")
            filterHtmlString+="<img src=\"assets/img/chrome.png\" height=\"20\" width=\"20\">";
        if (data[0] == "MicrosoftEdge")
            filterHtmlString+="<img src=\"assets/img/edge.png\" height=\"20\" width=\"20\">";
        if (data[0]== "safari")
            filterHtmlString+="<img src=\"assets/img/safari.png\" height=\"20\" width=\"20\">";
        filterHtmlString += data[1]+'-'+data[2];
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&name='+data[0]+'&version='+data[1]+'&platform='+data[2]+'&location=overview',
                success: function(result){
                    console.log(result);
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#filter-by-browser" ).empty();
                    $( "#filter-by-browser" ).html(filterHtmlString);
                    drawCircle(JSON.parse(result).sunburst);
                }
            });
        })();
    }
});
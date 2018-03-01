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

function getProgress(testId) {
    var val='';
    for(i=0; i<4;i++){
        if(display[i]){
            val+='-1';
        }else{
            val+='-0';
        }
    }
    if (browser=='all'){
        $.ajax({
            url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&location=result',
            success: function(result){
                if(result!=='done'){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    drawCircle(JSON.parse(result).sunburst);
                    updateOverall(JSON.parse(result).overall);
                    setTimeout(function(){getProgress(testId)}, 7000);
                }
                else{
                    location.reload();
                }
            }
        });
    } else {
        var data = browser.split('-');
        $.ajax({
            url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&name='+data[0]+'&version='+data[1]+'&platform='+data[2]+'&location=result',
            success: function(result){
                if(result!=='done'){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    drawCircle(JSON.parse(result).sunburst);
                    updateOverall(JSON.parse(result).overall);
                    setTimeout(function(){getProgress(testId)}, 7000);
                }
                else{
                    location.reload();
                }
            }
        });
    }
}


function updateResult (load,total){
    var statMap = new Map();
    var div = $("#resultTable");

    var htmlString = "";
    for (i=0;i<total;i++){
        //statMap.set("result"+i,load[i].stats);
        var trID = "";
        var htmlStringTmp="<td class=\"status\" id=\"r"+i+"\" width=12% X>"+i+".";
        var tmp = "<i class=\"pe-7s-attention\" style=\"color:blue;\">";
                if(load[i].result == "SUCCESSFUL")
                    tmp="<i class=\"pe-7s-check\" style=\"color:green;\">";
                if(load[i].result == "TIME OUT" || load[i].result == "FAILED")
                    tmp="<i class=\"pe-7s-close-circle\"  style=\"color:red;\">";
                if(load[i].result == "SCHEDULED")
                    tmp="<i class=\"pe-7s-timer\">";

        htmlStringTmp+=tmp;
        htmlStringTmp+="</td><td class=\"status\" id=\"d"+i+"\" width=5%>";
        htmlStringTmp+=Math.ceil(load[i].duration/1000)+"s</td>";
        htmlStringTmp+="<td class=\"status\">";
        for (j=0;j<load[i].browsers.length;j++){
            trID+=load[i].browsers[j].id +"_";
            if (load[i].browsers[j].name == "firefox")
               htmlStringTmp+="<img src=\"assets/img/firefox.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "chrome")
                htmlStringTmp+="<img src=\"assets/img/chrome.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "MicrosoftEdge")
                htmlStringTmp+="<img src=\"assets/img/edge.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "safari")
                htmlStringTmp+="<img src=\"assets/img/safari.png\" height=\"20\" width=\"20\">";
            htmlStringTmp+=load[i].browsers[j].version;
            htmlStringTmp+=load[i].browsers[j].platform+"&nbsp;&nbsp;";
        }
        htmlString+="<tr class=\"result\" id =\""+trID+"\" style=\"cursor:pointer;\" data-toggle=\"popover\" data-content=\""+load[i].result+"\">"+htmlStringTmp;
        htmlString+="</td>";
        htmlString+="</tr>";
        trID ="";
    }
    div.html(htmlString);

    window.statMap = statMap;
    $('[data-toggle="popover"]').popover({
        trigger:'click',
        placement:'right',
        html: true,
        template: '<div class="popover"><div class="arrow"></div><div class="popover-content"></div><div class="popover-footer"><a class="small-boy" style="color:red">&nbsp;&nbsp;[ Close ]</a></div></div>'
    });
}


function showStats (statObj){
    statObject = statObj;
    $("#stats").empty();
    if (JSON.stringify(statObj)=='{"stats":"NA"}'||JSON.stringify(statObj)=='{}')
        $("#stats").html('No stats are available.');
    else{
        var statsHtml='';
        //var statsHtml='<div class="tab">';
        var browsers = Object.keys(statObj);
        browsers.forEach(function(browser){
            statsHtml+='<h5 class="medium-boy no-margin">'+browser+'<h5>';
            //statsHtml+='<button class="tablinks" onclick="openCity(event, '+browser+')">'+browser+'</button>';
            var statTypes = Object.keys(statObj[browser]);
            var browserHtml='<table class="table fixed stat-table"><tbody id="'+browser+'" class="small-boy"><tr>';
            statTypes.forEach(function(statType){
                browserHtml+='<td class="statType" data-browser="'+browser+'" data-type="'+statType+'">'+statType+'</td>'
            });
            browserHtml+='</tr></body></table>';
            statsHtml+=browserHtml;
        });
        $("#stats").html(statsHtml);
    }
}
function updateOverall ( load){
    myChart.data.datasets.forEach((dataset) => {
        dataset.data = load;
    });
    myChart.update();
}

function initOverallChart (load){
    var ctx = document.getElementById("overall").getContext('2d');
    var maxValue = Math.max(...load);
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
                "grey"
                ],
                data: load
                }]
            },
        options: {
            responsive: false,
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


$(document).on("click", ".statType", function(e) {
    var browser = $(this).attr('data-browser');
    var type = $(this).attr('data-type');
    $('#stat-content').empty();
    $('#stat-type').empty();
    $('#stat-type').html('['+browser+'] - <a class="stat-type">'+type+'</a>');
    $('#stat-content').jsonbrowser(JSON.stringify(statObject[browser][type]));
    $("#stat-modal").modal();
});

$(document).on("click", ".popover-footer .small-boy" , function(){
    $(this).parents(".popover").popover('hide');
});

$(document).on("click", ".result", function(e) {
    $(".result").attr('style','cursor:pointer;background:white;');
    $(this).attr('style','cursor:pointer;background:#aff7c8;');
    var id = $(this).attr('id');
    (function updateStats() {
        $.ajax({
            url: 'getstat?name='+testName+'&id='+id,
            success: function(result){
                //console.log(result);
                showStats(JSON.parse(result));
            }
        });
    })();
});


$(document).on("click", ".less", function(e) {
    var id = $(this).attr('id');
    if($(this).attr('class')=='btn unpick less')
        $(this).attr('class', classes[id]);
    else
        $(this).attr('class', 'btn unpick less');
    display[id]=!display[id];
    var val='';
    for(i=0; i<4;i++){
        if(display[i]){
            val+='-1';
        }else{
            val+='-0';
        }
    }
    if (browser=='all'){
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&location=result',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#resultTable" ).empty();
                    //$( "#overall" ).empty();
                    $( "#filter-by-browser" ).html('All browsers');
                    drawCircle(JSON.parse(result).sunburst);
                    updateResult(JSON.parse(result).results, JSON.parse(result).total);
                    updateOverall(JSON.parse(result).overall);
                }
            });
        })();
    } else {
        var data = browser.split('-');
        (function updateChart() {
            $.ajax({
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&name='+data[0]+'&version='+data[1]+'&platform='+data[2]+'&location=result',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#resultTable" ).empty();
                    //$( "#overall" ).empty();
                    drawCircle(JSON.parse(result).sunburst);
                    updateResult(JSON.parse(result).results, JSON.parse(result).total);
                    updateOverall(JSON.parse(result).overall);
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
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&location=result',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#resultTable" ).empty();
                    $( "#overall" ).empty();
                    $( "#filter-by-browser" ).html('All browsers');
                    drawCircle(JSON.parse(result).sunburst);
                    updateResult(JSON.parse(result).results, JSON.parse(result).total);
                    updateOverall(JSON.parse(result).overall);
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
                url: 'getjson?testName='+testName+'&size='+tupleSize+'&val='+val+'&name='+data[0]+'&version='+data[1]+'&platform='+data[2]+'&location=result',
                success: function(result){
                    $( "#legend" ).empty();
                    $( "#sequence" ).empty();
                    $( "#oldLayer" ).empty();
                    $( "#chart" ).empty();
                    $( "#resultTable" ).empty();
                    $( "#overall" ).empty();
                    $( "#filter-by-browser" ).html(filterHtmlString);
                    drawCircle(JSON.parse(result).sunburst);
                    updateResult(JSON.parse(result).results, JSON.parse(result).total);
                    updateOverall(JSON.parse(result).overall);
                }
            });
        })();
    }
    (function updateChart() {
        $.ajax({
            url: 'getjson?name='+testName+'&size='+tupleSize+'&val='+val+'&location=result',
            success: function(result){
                $( "#legend" ).empty();
                $( "#sequence" ).empty();
                $( "#oldLayer" ).empty();
                $( "#chart" ).empty();
                $( "#resultTable" ).empty();
                drawCircle(JSON.parse(result).sunburst);
                updateResult(JSON.parse(result).results, JSON.parse(result).total);
                updateOverall(JSON.parse(result).overall);
            }
        });
    })();
});

$(document).ready(function(){
    drawCircle(myData.sunburst);
    updateResult(myData.results, total);
    initOverallChart(myData.overall);
    if (!isDone)
        getProgress(id);
});
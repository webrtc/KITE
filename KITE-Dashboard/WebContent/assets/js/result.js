+//
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
    console.log(load);
    var statMap = new Map();
    var div = $("#resultTable");

    var htmlString = "";
    for (i=0;i<total;i++){
        //statMap.set("result"+i,load[i].stats);
        var trID = "";
        var htmlStringTmp="<td class=\"status\" id=\"r"+i+"\" width:\"10%\" X>";
        var tmp = "<i class=\"pe-7s-attention\" style=\"color:blue;\">";
                if(load[i].result == "SUCCESSFUL")
                    tmp="<i class=\"pe-7s-check\" style=\"color:green;\">";
                if(load[i].result == "TIME OUT" || load[i].result == "FAILED")
                    tmp="<i class=\"pe-7s-close-circle\"  style=\"color:red;\">";
                if(load[i].result == "SCHEDULED")
                    tmp="<i class=\"pe-7s-timer\">";

        htmlStringTmp+=tmp;
        htmlStringTmp+="</td><td class=\"status\" id=\"d"+i+"\" width:\"10%\">";
        htmlStringTmp+=Math.ceil(load[i].duration/1000)+"s</td>";


        htmlStringTmp+="<td class=\"status\" width:\"75%\">";
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
        var htmlStatStringTmp ="";
        if(load[i].stats){
            htmlStatStringTmp+="<td id=\""+trID+"\" class=\"stats\" width:\"5%\">";
            htmlStatStringTmp+=i+".<i class=\"pe-7s-graph1\" style=\"color:green;\"></td>";
        } else {
           htmlStatStringTmp+="<td width:\"5%\">";
           htmlStatStringTmp+=i+".<i class=\"pe-7s-graph1\"></td>";
        }

/*        htmlString+="<tr id=\""+trID+"\" class=\"result\" style=\"cursor:pointer;\" data-toggle=\"popover\" data-content=\""+load[i].result+"\">";*/
        htmlString+="<tr id=\""+trID+"\" class=\"result\" style=\"cursor:pointer;\">";
        htmlString+=htmlStatStringTmp;
        htmlString+=htmlStringTmp;
        htmlString+="</td>";
        htmlString+="</tr>";
        trID ="";
    }
    div.html(htmlString);

    window.statMap = statMap;
}


function showResult (result){
    $("#result-display").html(result);
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
            url: 'getresult?name='+testName+'&id='+id,
            success: function(result){
                //console.log(result);
                showResult(result);
            }
        });
    })();
});

$(document).on("click", ".stats", function(e) {
    var id = $(this).attr('id');
    window.open('stat?name='+testName+'&id='+id, '_blank');
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
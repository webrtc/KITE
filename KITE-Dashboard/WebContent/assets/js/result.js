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


function updateResult (load,total){
    var div = $("#resultTable");
    var htmlString = "";
    for (i=0;i<total;i++){
        htmlString+="<tr class=\"result\" style=\"cursor:pointer;\" data=\""+load[i].result+"\">";
        htmlString+="<td id=\"r"+i+"\" width=5%>";
        var tmp = "<i class=\"pe-7s-news-paper\">";
        if(load[i].result == "SUCCESSFUL")
            tmp="<i class=\"pe-7s-smile\">";
        if(load[i].result == "TIME OUT" || load[i].result == "FAILED")
            tmp="<i class=\"pe-7s-close\">";
        if(load[i].result == "SCHEDULED")
            tmp="<i class=\"pe-7s-wristwatch\">";

        htmlString+=tmp;
        htmlString+="</td><td class=\"status\" id=\"d"+i+"\" width=5%>";
        htmlString+=Math.ceil(load[i].duration/1000)+"s</td>";
        htmlString+="<td class=\"status\">";
        for (j=0;j<load[i].browsers.length;j++){
            if (load[i].browsers[j].name == "firefox")
               htmlString+="<img src=\"assets/img/firefox.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "chrome")
                htmlString+="<img src=\"assets/img/chrome.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "MicrosoftEdge")
                htmlString+="<img src=\"assets/img/edge.png\" height=\"20\" width=\"20\">";
            if (load[i].browsers[j].name == "safari")
                htmlString+="<img src=\"assets/img/safari.png\" height=\"20\" width=\"20\">";
            htmlString+=load[i].browsers[j].version;
            htmlString+=load[i].browsers[j].platform+"&nbsp;&nbsp;";
        }
        htmlString+="</td>";
        htmlString+="</tr>";
    }
    div.html(htmlString);
}

function updateExtendedResult (log){
    var div = $("#extended-result");
    div.html(log);
}


$(document).on("click", ".result", function(e) {
    var log = $(this).attr('data');
    updateExtendedResult(log);
});


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
    console.log(val);
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
                console.log(result);
                updateResult(JSON.parse(result).results, JSON.parse(result).total);
            }
        });
    })();
});
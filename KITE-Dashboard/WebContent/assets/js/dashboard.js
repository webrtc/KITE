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


$(document).on("click", "i", function(e) {
    var id = $(this).attr('name');
    if($(this).attr('class')=='pe-7s-angle-down-circle green'){
        $("#"+id).css("height","300px");
        $("#"+id).css("overflow","visible");
        $(this).attr('class', 'pe-7s-angle-up-circle red');

    }
    else{
        if($(this).attr('class')=='pe-7s-angle-up-circle red'){
            $("#"+id).css("height","60px");
            $("#"+id).css("overflow","hidden");
            $(this).attr('class', 'pe-7s-angle-down-circle green');
        }
    }
});

$(document).on("click", "#submit", function(e) {
    var chosenTest = $("#chosen-test").find(":selected").text();
    var chosenCaller = $("#chosen-caller").find(":selected").text();
    var chosenCallee = $("#chosen-callee").find(":selected").text();
    if (chosenTest==='Choose a test')
        alert('Please choose a test');
    else if (chosenCaller==='Caller')
        alert('Please choose a caller');
    else if (chosenCallee==='Callee')
        alert('Please choose a callee');
    else {
        var request = "getstat?test="+chosenTest+"&caller="+chosenCaller+"&callee="
            +chosenCallee+"&overtime=yes";
        getStats(request);
    }
});

function getStats (request){

    (function requestStats() {
        $.ajax({
            url: request,
            success: function(result){
                //console.log(result);
                displayStats(JSON.parse(result));
            }
        });
    })();
}
function displayStats (load){
    var dates = load.run_dates;
    var callerStatArray = load.caller;
    var calleeStatArray = load.callee;
    if (callerStatArray.length > 30){
        callerStatArray = callerStatArray.slice(0,29);
        calleeStatArray = calleeStatArray.slice(0,29);
        dates = dates.slice(0,29);
    }


    var caller_audio = [];
    var callee_audio = [];
    var caller_video_bytes_sent = [];
    var caller_video_bytes_received = [];
    var caller_video_packets_sent = [];
    var caller_video_packets_received = [];
    var caller_audio_bytes_sent = [];
    var caller_audio_bytes_received = [];
    var caller_audio_packets_sent = [];
    var caller_audio_packets_received = [];

    var caller_video = [];
    var callee_video = [];
    var callee_video_bytes_sent = [];
    var callee_video_bytes_received = [];
    var callee_video_packets_sent = [];
    var callee_video_packets_received = [];
    var callee_audio_bytes_sent = [];
    var callee_audio_bytes_received = [];
    var callee_audio_packets_sent = [];
    var callee_audio_packets_received = [];

    var audio_bytes_sent_canvas = $('#overtime-video-bytes-sent');
    var audio_bytes_received_canvas = $('#overtime-video-bytes-received');
    var audio_packets_sent_canvas = $('#overtime-video-packets-sent');
    var audio_packets_received_canvas = $('#overtime-video-packets-received');

    var options = {  day: 'numeric', month: 'long',year: 'numeric'  };
    labels = [];
    dates.forEach(function(date){
        var tmp = new Date(date);
        labels.unshift(tmp.toLocaleDateString('en-GB', options));
    });
    callerStatArray.forEach(function(stat){
        if(!isEmpty(stat)){
            caller_video.unshift(stat.video);
            caller_video_bytes_sent.unshift(stat.video.avgBytesSent);
            caller_video_bytes_received.unshift(stat.video.avgBytesReceived);
            caller_video_packets_sent.unshift(stat.video.avgPacketsSent);
            caller_video_packets_received.unshift(stat.video.avgPacketsReceived);

            caller_audio.unshift(stat.audio);
            caller_audio_bytes_sent.unshift(stat.audio.avgBytesSent);
            caller_audio_bytes_received.unshift(stat.audio.avgBytesReceived);
            caller_audio_packets_sent.unshift(stat.audio.avgPacketsSent);
            caller_audio_packets_received.unshift(stat.audio.avgPacketsReceived);
        } else {
            caller_video.unshift(stat.video);
            caller_video_bytes_sent.unshift(0);
            caller_video_bytes_received.unshift(0);
            caller_video_packets_sent.unshift(0);
            caller_video_packets_received.unshift(0);

            caller_audio.unshift(stat.audio);
            caller_audio_bytes_sent.unshift(0);
            caller_audio_bytes_received.unshift(0);
            caller_audio_packets_sent.unshift(0);
            caller_audio_packets_received.unshift(0);
        }
    });


    calleeStatArray.forEach(function(stat){
        if(!isEmpty(stat)){
            callee_video.unshift(stat.video);
            callee_video_bytes_sent.unshift(stat.video.avgBytesSent);
            callee_video_bytes_received.unshift(stat.video.avgBytesReceived);
            callee_video_packets_sent.unshift(stat.video.avgPacketsSent);
            callee_video_packets_received.unshift(stat.video.avgPacketsReceived);

            callee_audio.unshift(stat.audio);
            callee_audio_bytes_sent.unshift(stat.audio.avgBytesSent);
            callee_audio_bytes_received.unshift(stat.audio.avgBytesReceived);
            callee_audio_packets_sent.unshift(stat.audio.avgPacketsSent);
            callee_audio_packets_received.unshift(stat.audio.avgPacketsReceived);
        } else {
            callee_video.unshift(stat.video);
            callee_video_bytes_sent.unshift(0);
            callee_video_bytes_received.unshift(0);
            callee_video_packets_sent.unshift(0);
            callee_video_packets_received.unshift(0);

            callee_audio.unshift(stat.audio);
            callee_audio_bytes_sent.unshift(0);
            callee_audio_bytes_received.unshift(0);
            callee_audio_packets_sent.unshift(0);
            callee_audio_packets_received.unshift(0);
        }
    });

    /*var stat_content = '';
    $("#stat_content").html(stat_content);*/
    if (labels.length>0){
        if (caller_video_bytes_sent.reduce(add)===0){
            $('#stat-status').html("No stats are available for the last 30 runs");
            clearAllGraphs();
        } else {
            $('#stat-status').empty();
            paintCanvas('overtime-video-bytes-sent', caller_video_bytes_sent,callee_video_bytes_sent, "Bytes Sent (kbps)");
            paintCanvas('overtime-video-bytes-received', caller_video_bytes_received,callee_video_bytes_received, "Bytes Received (kbps)");
            paintCanvas('overtime-video-packets-sent', caller_video_packets_sent,callee_video_packets_sent, "Packets Sent");
            paintCanvas('overtime-video-packets-received', caller_video_packets_received,callee_video_packets_received, "Packets Received");
            paintCanvas('overtime-audio-bytes-sent', caller_audio_bytes_sent,callee_audio_bytes_sent, "Bytes Sent (kbps)");
            paintCanvas('overtime-audio-bytes-received', caller_audio_bytes_received,callee_audio_bytes_received, "Bytes Received (kbps)");
            paintCanvas('overtime-audio-packets-sent', caller_audio_packets_sent,callee_audio_packets_sent, "Packets Sent");
            paintCanvas('overtime-audio-packets-received', caller_audio_packets_received,callee_audio_packets_received, "Packets Received");
        }
    } else {
        $('#stat-status').html("No stats are available for the last 30 runs");
        clearAllGraphs();
    }
}

function add(a, b) {
    return a + b;
}

function paintCanvas(canvas,caller_data, callee_data,title){
    if (typeof graphMap.get(canvas) === 'undefined'){
        var ctx = document.getElementById(canvas);
        $('#'+canvas).empty()
    /*    var labels = [];
        for (i = caller_data.length; i > 0; i--){
            labels.push('.');
        }*/
        var config = {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Caller',
                        backgroundColor: '#42f4aa',
                        borderColor: '#42f4aa',
                        fill: false,
                        data: caller_data,
                        pointRadius: 2,
                        lineTension: 0
                    },
                    {
                        label: 'Callee',
                        backgroundColor: '#ef2b3e',
                        borderColor: '#ef2b3e',
                        fill: false,
                        data: callee_data,
                        pointRadius: 2,
                        lineTension: 0
                    }
                ]
            },
            options: {
                responsive: true,
                title:{
                    display:true,
                    text: title
                },
                scales: {
                    xAxes: [{
                        display: true,
                    }],
                    yAxes: [{
                        display: true,
                        type: 'linear',
                    }]
                },
                showLines: true
            }
        };
        var myChart = new Chart(ctx,config);
        graphMap.set(canvas, myChart)
    } else {
        var myChart = graphMap.get(canvas);
        myChart.data.datasets[0].data = caller_data;
        myChart.data.datasets[1].data = callee_data;
        myChart.update();
        graphMap.set(canvas, myChart);
    }
}

function clearAllGraphs(){
    if(graphMap.size>0){
        graphMap.forEach(function(graph){
            graph.data.datasets[0].data = [0];
            graph.data.datasets[1].data = [0];
            graph.update();
        });
    }
}

function isEmpty(obj) {
    for(var key in obj) {
        if(obj.hasOwnProperty(key))
            return false;
    }
    return true;
}

$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
    var labels = [];
    for (i = stats[0].length; i > 0; i--){
        labels.push('.');
    }
    var config = {
        type: 'line',
        data: {
            labels: config_labels,
            datasets: [
                {
                    label: "Success",
                    backgroundColor: '#42f4aa',
                    borderColor: '#42f4aa',
                    fill: false,
                    data: stats[0],
                    pointRadius: 2
                },
                {
                    label: "Failed",
                    backgroundColor: '#ff4b30',
                    borderColor: '#ff4b30',
                    fill: false,
                    data: stats[1],
                    pointRadius: 2
                },
                {
                    label: "Error",
                    backgroundColor: '#30b3ff',
                    borderColor: '#30b3ff',
                    fill: false,
                    data: stats[2],
                    pointRadius: 2
                }
            ]
        },
        options: {
            responsive: true,
            title:{
                display:false,
                text:'Result throughout configurations'
            },
            scales: {
                xAxes: [{
                    display: true,
                }],
                yAxes: [{
                    display: true,
                    type: 'linear',
                }]
            },
            showLines: true
        }
    };

var myLineChart = new Chart($("#overtime-result"),config);
});

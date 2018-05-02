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

function showStats (statObj){
    plotStats (true, statObj.caller)
    plotStats (false, statObj.callee)
};


function plotStats (isCaller, data , other ) {
    var audio;
    var video;

    if(isCaller){
        title = $("#caller-browser");
        audioByteAvg = $("#caller-audio-bytes");
        audioPacketAvg = $("#caller-audio-packets");
        videoByteAvg = $("#caller-video-bytes");
        videoPacketAvg = $("#caller-video-packets");
        candidatePair = $("#caller-candidate-pairs");
        audioDetails = $("#caller-detail-audio");
        videoDetails = $("#caller-detail-video");
        sdpOffer = $("#caller-sdp-offer-detail");
        sdpAnswer = $("#caller-sdp-answer-detail");

        audioBytes = document.getElementById("caller-audio-bytes-plotting");
        audioPackets = document.getElementById("caller-audio-packets-plotting");
        videoBytes = document.getElementById("caller-video-bytes-plotting");
        videoPackets = document.getElementById("caller-video-packets-plotting");

    } else {
        title = $("#callee-browser");
        audioByteAvg = $("#callee-audio-bytes");
        audioPacketAvg = $("#callee-audio-packets");
        videoByteAvg = $("#callee-video-bytes");
        videoPacketAvg = $("#callee-video-packets");
        candidatePair = $("#callee-candidate-pairs");
        audioDetails = $("#callee-detail-audio");
        videoDetails = $("#callee-detail-video");
        sdpOffer = $("#callee-sdp-offer-detail");
        sdpAnswer = $("#callee-sdp-answer-detail");

        audioBytes = document.getElementById("callee-audio-bytes-plotting");
        audioPackets = document.getElementById("callee-audio-packets-plotting");
        videoBytes = document.getElementById("callee-video-bytes-plotting");
        videoPackets = document.getElementById("callee-video-packets-plotting");
    }


    if (Object.keys(data.audio).length !== 0 && data.audio.constructor === Object){
        audioByteAvg.html('Bytes (avg S/R: <a style="color:#42f4aa;">'+data.audio.avgBytesSent+'</a>/<a style="color:#ef2b3e;">'+data.audio.avgBytesReceived+'</a> kB/s)');
        audioPacketAvg.html('Packets (avg S/R: <a style="color:#42f4aa;">'+data.audio.avgPacketsSent+'</a>/<a style="color:#ef2b3e;">'+data.audio.avgPacketsReceived+' </a> /s)');
        plotByteStat(audioBytes,data.audio, 'audio');
        plotPacketStat(audioPackets,data.audio), 'audio';
        var loss = data.audio.PacketsLostOvertime[data.audio.PacketsLostOvertime.length-1];
        var avgJitter = data.audio.avgJitterOvertime;
        audioDetails.html('( Packets lost: '+loss +' | Avg jitter: ' + avgJitter.toFixed(5)+')');
    } else {
             audioByteAvg.html('No stat was available');
     }
    if (Object.keys(data.video).length !== 0 && data.video.constructor === Object){
        videoByteAvg.html('Bytes (avg S/R: <a style="color:#42f4aa;">'+data.video.avgBytesSent+'</a>/<a style="color:#ef2b3e;">'+data.video.avgBytesReceived+'</a> kB/s)');
        videoPacketAvg.html('Packets (avg S/R: <a style="color:#42f4aa;">'+data.video.avgPacketsSent+'</a>/<a style="color:#ef2b3e;">'+data.video.avgPacketsReceived+' </a> /s)');
        plotByteStat(videoBytes,data.video, 'video');
        plotPacketStat(videoPackets,data.video,'video');
        var loss = data.video.PacketsLostOvertime[data.video.PacketsLostOvertime.length-1];
        var avgJitter = data.video.avgJitterOvertime;
        videoDetails.html('( Packets lost: '+loss +' | Avg jitter: ' + avgJitter.toFixed(5)+')');
    } else {
        videoByteAvg.html('No stat was available');
    }

    if (Object.keys(data.candidates).length !== 0 && data.candidates.constructor === Object){
        candidatePair.html(getCandidatesHtml(data));
    }

    if (Object.keys(data.sdp).length !== 0 && data.sdp.constructor === Object){
        showSDP(sdpOffer, data.sdp.offer, 'offer');
        showSDP(sdpAnswer, data.sdp.answer, 'answer');
    }


    $('[data-toggle="popover"]').popover({
        trigger:'hover',
        placement:'right',
        html: true
    });

};

function showSDP(ctx, sdp, type){
    var html='<pre class="sdp-message">' + sdp + '</pre>' ;
    ctx.html(html);
}

function getCandidatesHtml (data) {
    candidate_pairs = data.candidates.candidates;
    candidates_local = data['local-candidate'].candidates;

    candidates_remote = data['remote-candidate'].candidates;
    var html = '';
    candidate_pairs.forEach(function(candidate){
        if (typeof candidate != 'undefined')
            html += getCandidateHtml(candidate, candidates_local, candidates_remote);
    })
    return html;
}

function getCandidateHtml (candidate, candidates_local, candidates_remote){
    var html ='';
    if (candidate.state === 'succeeded')
        html +='<tr class="status succeeded-candidate">';
    else
        html +='<tr class="status failed-candidate">';
    html+='<td><a data-toggle="popover" data-content="'+getCandidateInfo(candidates_local[candidate.localCandidateId])+'">' + candidate.localCandidateId + '</a></td>';
    html+='<td><a data-toggle="popover" data-content="'+getCandidateInfo(candidates_remote[candidate.remoteCandidateId])+'">' + candidate.remoteCandidateId + '</a></td>';
    html+='<td>' + candidate.state + '</td>';
    html+='<td>' + candidate.nominated + '</td>';
    html+='<td>' + candidate.priority + '</td>';
    html+='<td>' + candidate.bytesSent + '</td>';
    html+='<td>' + candidate.bytesReceived + '</td>';
    html += '</tr>';
    return html;
}

function getCandidateInfo (candidate){
    var html ='';
    if (typeof candidate != 'undefined'){
        html+= 'ip: '+ candidate.ip + ', ';
        html+= 'port: '+ candidate.port + ', ';
        html+= 'protocol: '+ candidate.protocol + ', ';
        html+= 'candidateType: '+ candidate.candidateType + ', ';
        html+= 'priority: '+ candidate.priority + ', ';
        html+= 'url: '+ candidate.url;
    } else {
        html += 'candidate not found.'
    }
    return html;
}

function plotByteStat (ctx, data, type ) {
    var factor = 3;
    var arrayTmp = data['BytesReceivedOvertime'];
    var max = Math.max(...arrayTmp);
    var min = Math.min(...arrayTmp);
    var margin = (max - min) /2;
    var labels =  new Array();
    for (i = 1; i <= arrayTmp.length;i++){
        labels.push(''+i);
/*        if (i%5===0)
            labels.push(''+i+'');
        else
            labels.push('.');*/
    }

    var config = {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'BytesReceived (kB/s)',
                    backgroundColor: '#ef2b3e',
                    borderColor: '#ef2b3e',
                    fill: false,
                    data: data['BytesReceivedOvertime'],
                    pointRadius: 2,
                    lineTension: 0
                },
                {
                    label: 'BytesSent (kB/s)',
                    backgroundColor: '#42f4aa',
                    borderColor: '#42f4aa',
                    fill: false,
                    data: data['BytesSentOvertime'],
                    pointRadius: 2,
                    lineTension: 0
                }
            ]
        },
        options: {
            responsive: true,
            title:{
                display:true,
                text: 'Byte transmission overtime.'

            },
            showLines: true,
            scales: {
                xAxes: [{
                    display: true,
                    text:'Bytes transmission',
                    gridLines: {
                        display : false
                    }
                }],
                yAxes: [{
                    display: true,
                    type: 'linear',
                    ticks: {
                        suggestedMax: max+margin,
                        suggestedMin: min-margin,
                        stepSize: (max+margin)/factor
                    },
                    gridLines: {
                        display : false
                    }
                }]
            }
        }
    };
    var myChart = new Chart(ctx,config);
}

function plotPacketStat (ctx, data, type ) {
    var factor = 3;
    var arrayTmp = data['PacketsReceivedOvertime'];
    var max = Math.max(...arrayTmp);
    var min = Math.min(...arrayTmp);
    var margin = (max - min) /2;
    var labels =  new Array();
    for (i = 1; i <= arrayTmp.length;i++){
        labels.push(''+i);
/*        if (i%5===0)
            labels.push(''+i+'');
        else
            labels.push('.');*/
    }

    var config = {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'PacketsReceived (/s)',
                    backgroundColor: '#ef2b3e',
                    borderColor: '#ef2b3e',
                    fill: false,
                    data: data['PacketsReceivedOvertime']
                },
                {
                    label: 'PacketsSent (/s)',
                    backgroundColor: '#42f4aa',
                    borderColor: '#42f4aa',
                    fill: false,
                    data: data['PacketsSentOvertime']
                }
            ]
        },
        options: {
            responsive: true,
            title:{
                display:false,

            },
            scales: {
                xAxes: [{
                    display: true,
                    text:'Packets transmission',
                    gridLines: {
                        display : false
                    }
                }],
                yAxes: [{
                    display: true,
                    type: 'linear',
                    ticks: {
                        suggestedMax: max+margin,
                        suggestedMin: min-margin,
                        stepSize: max/factor
                    },
                    gridLines: {
                        display : false
                    }
                }]
            }
        }
    };
    var myChart = new Chart(ctx,config);
}


function getStats () {
    (function requestStats() {
        $.ajax({
            url: request,
            success: function(result){
                console.log(result);
                showStats(JSON.parse(result));
            }
        });
    })();
};


$(document).ready(function(){
    getStats ();

});
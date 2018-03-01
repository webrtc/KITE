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
        $("#"+id).css("height","230px");
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

$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
    var labels = [];
    for (i = stats[0].length; i > 0; i--){
        labels.push(i);
    }
    var config = {
        type: 'line',
        data: {
            labels: labels,
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

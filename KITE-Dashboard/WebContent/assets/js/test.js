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

function initialSetup(testId){
    $.ajax({
        url: 'getprogress?id='+testId+'&result=false',
        success: function(load){
            var result = JSON.parse(load);
            var stats = result.stats;
            initChart(result.name, stats);
        }
    });
}


function getProgress(testId, index) {
    $.ajax({
        url: 'getprogress?id='+testId+'&result=false',
        success: function(load){
            var result = JSON.parse(load);
            var total = result.total;
            var finished = result.done;
            var stats = result.stats;
            var percentage = 100*finished/total - done;
            done = 100*finished/total;
            if(done<100){
                var id = '#test'+index;
                if( percentage!=0){
                    var tmp = percentage.toFixed(2) + "%";
                    var start = result.start;
                    var currentTime = new Date().getTime();
                    var ETA = (currentTime - start)/finished/1000*(total-finished);
                    var hour =  Math.floor(ETA/3600);
                    ETA = ETA-hour*3600;
                    var min =  Math.floor(ETA/60);
                    ETA = ETA-min*60;
                    $('#progress'+index).text(finished+'/'+total+'  ('+(100*finished/total).toFixed(2)+'%)');
                    $('#endTime'+index).text(hour+'h'+min+'m'+ETA.toFixed(0)+'s');
                    //drawProgressBar(id, tmp);
                    updateChart(result.name, stats);
                }
                setTimeout(function(){getProgress(testId,index)}, 7000);
            }
            else{
                location.reload();
            }
        }
    });
}

function drawProgressBar(id, tmp){
    var bar = new ProgressBar.Line(id, {
    strokeWidth: 4,
    easing: 'easeInOut',
    duration: 1400,
    color: '#FFEA82',
    trailColor: '#eee',
    trailWidth: 1,
    svgStyle: {width: tmp, height: '100%'},
    from: {color: '#7e7f82'},
    to: {color: '#42f4aa'},
        step: (state, bar) => {
            bar.path.setAttribute('stroke', state.color);
        }
    });
    bar.animate(1.0);
}

function updateChart(name, dataX){
    chartMap.get(name).data.datasets.forEach((dataset) => {
        dataset.data = dataX;
    });
    chartMap.get(name).update();
}
function initChart(name, dataX){
    var ctx = document.getElementById(name).getContext('2d');
    $("#"+name).empty();
    var myChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
        labels: ["Success", "Failed", "Error", "Pending"],
        datasets: [{
            backgroundColor: [
            "#42f4aa",
            "#ff4b30",
            "#30b3ff",
            "grey"
            ],
            data: dataX
            }]
        },
    options: {
        responsive: false,
        legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        boxWidth: 10,
                        fontSize: 10
                    }
                }
        }
    });
    chartMap.set(name,myChart);
}
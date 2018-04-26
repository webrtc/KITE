//
// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.
//

'use strict';

// Display test result on appropriate HTML elements.
function show_sdp(offer, answer){
  let result_element = document.getElementById('result');
  let offer_element = document.getElementById('offer');
  let answer_element = document.getElementById('answer');

  if (typeof offer != 'undefined') {
    offer_element.innerHTML = '<pre>' + offer + '</pre>';
  }
  if (typeof answer != 'undefined'){
    answer_element.innerHTML = '<pre>' + answer + '</pre>';
  }
}


// Wait for video to play before call mediaCapture().
async function waitOnPlay(video) {
  return new Promise(resolve =>
      video.addEventListener('play', resolve))
}

// Validate whether sdp follows Unified Plan format.
function validate_unified_plan(offer , track_count){
  let sdp = offer.sdp;
  let lines = sdp.split('\n');
  const m_lines = [];
  const ssrc_lines = [];
  const msid_lines = [];
  lines.forEach(line => {
    if (line.startsWith('m=')) {
      m_lines.push(line);
    }
    if (line.startsWith('a=ssrc:')) {
      ssrc_lines.push(line);
    }
    if (line.startsWith('a=msid:')) {
      msid_lines.push(line);
    }
  })
  var result;
  if (m_lines.length === ssrc_lines.length &&
          m_lines.length === msid_lines.length &&
          m_lines.length === track_count){
    return {value:true};
  } else {
    var error_msg;
    if (m_lines.length != track_count){
      error_msg = "Incorrect number of m lines: " + m_lines.length;
      return {value:false, error:error_msg};
    }
    if (ssrc_lines.length != track_count){
      error_msg = "Incorrect number of ssrc lines: " + ssrc_lines.length;
      return {value:false, error:error_msg};
    }
    if (msid_lines.length != track_count){
      error_msg = "Incorrect number of msid lines: " + msid_lines.length;
      return {value:false, error:error_msg};
    }
  }

}

// Validate whether received stream ids are correct.
function validate_track_id(track_id , remote_description){
  let sdp = remote_description.sdp;
  for (const line of sdp.split('\n')) {
    if (line.startsWith('a=msid:') && line.includes(''+track_id)) {
      return true
    }
  }

  return false
}

function timeout(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}


// Validate whether videos are actually splaying.
async function validate_video_playback(video){
  var result;
  await timeout(2000);
  var canvas = document.createElement('canvas');
  var ctx = canvas.getContext('2d');
  ctx.drawImage(video,0,0,video.videoHeight-1,video.videoWidth-1);
  var imageData = ctx.getImageData(0,0,video.videoHeight-1,video.videoWidth-1).data;
  const sum = imageData.reduce((total, x) => (total + x))
  if (sum === 255*(Math.pow(video.videoHeight-1,(video.videoWidth-1)*(video.videoWidth-1)))){
    sum = 0;
  }
  if (sum == 0){
    return false;
  } else {
    return true;
  }
}

// Log to console and on the page
function trace(arg) {
  if (tracing) {
    var now = (window.performance.now() / 1000).toFixed(3);
    console.log(now + ': ', arg);
    var log = document.getElementById('log')
    var para = document.createElement('p');
    var node = document.createTextNode( now + ': ' + arg );
    para.appendChild(node);
    log.appendChild(para);
  }
}

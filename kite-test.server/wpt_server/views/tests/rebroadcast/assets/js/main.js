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

//Based on basic PeerConnection sample from https://webrtc.github.io/samples
//https://github.com/webrtc/samples/blob/gh-pages/LICENSE.md

'use strict';

var getMediaButton = document.getElementById('getMediaButton');
var sendMediaButton = document.getElementById('sendMediaButton');
var rebroadcastButton = document.getElementById('rebroadcastButton');
var originalVideo = document.getElementById('originalVideo');
var pc1Video = document.getElementById('pc1Video');
var pc2Video = document.getElementById('pc2Video');
var pc3Video = document.getElementById('pc3Video');

var mediaStream;
var receivedStream;
var pc2MediaStream = new MediaStream();
var pc12, pc21, pc23, pc32;

sendMediaButton.disabled = true;
rebroadcastButton.disabled = true;

getMediaButton.onclick = getMedia;
sendMediaButton.onclick = sendMedia;
rebroadcastButton.onclick = rebroadcast;

function getName(pc) {
  if (pc === pc12){
    return 'pc12';
  }
  if (pc === pc21){
    return 'pc21';
  }
  if (pc === pc32){
    return 'pc32';
  }
}

async function getMedia() {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: true
    });
    originalVideo.srcObject = mediaStream;
    originalVideo.play();
    getMediaButton.disabled = true;
    sendMediaButton.disabled = false;
  } catch (error){
    trace(error)
  }
}



async function sendMedia(){
  sendMediaButton.disabled = true;
  var videoTracks = mediaStream.getVideoTracks();
  var audioTracks = mediaStream.getAudioTracks();
  if (videoTracks.length > 0) {
    trace('Using video device: ' + videoTracks[0].label);
  }
  if (audioTracks.length > 0) {
    trace('Using audio device: ' + audioTracks[0].label);
  }
  try {
    var servers = null;
    pc12 = new RTCPeerConnection(servers);
    trace('Created local peer connection object pc12');
    pc12.onicecandidate = function(e) {
      onIceCandidatePhase1(pc12, e);
    };

    pc21 = new RTCPeerConnection(servers);
    trace('Created remote peer connection object pc21');
    pc21.onicecandidate = function(e) {
      onIceCandidatePhase1(pc21, e);
    };

    pc23 = new RTCPeerConnection(servers);
    trace('Created remote peer connection object pc32');
    pc23.onicecandidate = function(e) {
      onIceCandidatePhase2(pc23, e);
    };

    pc32 = new RTCPeerConnection(servers);
    trace('Created remote peer connection object pc32');
    pc32.onicecandidate = function(e) {
      onIceCandidatePhase2(pc32, e);
    };

    pc12.oniceconnectionstatechange = function(e) {
      onIceStateChange(pc12, e);
    };
    pc21.oniceconnectionstatechange = function(e) {
      onIceStateChange(pc21, e);
    };
    pc23.oniceconnectionstatechange = function(e) {
      onIceStateChange(pc23, e);
    };
    pc32.oniceconnectionstatechange = function(e) {
      onIceStateChange(pc32, e);
    };

    pc21.ontrack = gotRemoteStreamPc2;
    pc32.ontrack = gotRemoteStreamPc3;

    mediaStream.getTracks().forEach( function(track){
      pc12.addTrack(track,mediaStream);
    });
    trace('Added local stream to pc12');
    trace('pc12 createOffer start');
    var pc1Offer = await pc12.createOffer();
    trace(pc1Offer);
    await pc12.setLocalDescription(pc1Offer);
    await pc21.setRemoteDescription(pc1Offer);
    trace('pc21 createAnswer start');
    var pc2Answer = await pc21.createAnswer();
    await pc21.setLocalDescription(pc2Answer);
    await pc12.setRemoteDescription(pc2Answer);
    trace(pc2Answer);
  } catch (error){
    trace(error);
  }
}



async function rebroadcast(){
  rebroadcastButton.disabled = true;
  pc2MediaStream.getTracks().forEach( function(track){
    pc23.addTrack(track,mediaStream);
    console.log('track')
    console.log(track)
  });
  try {
    trace('pc23 createOffer start');
    var pc2Offer = await pc23.createOffer();
    trace(pc2Offer);
    await pc23.setLocalDescription(pc2Offer);
    await pc32.setRemoteDescription(pc2Offer);
    trace('pc32 createAnswer start');
    var pc3Answer = await pc32.createAnswer();
    await pc32.setLocalDescription(pc3Answer);
    await pc23.setRemoteDescription(pc3Answer);
    trace(pc3Answer);
  } catch (error){
    trace(error);
  }
}

function onCreateSessionDescriptionError(error) {
  trace('Failed to create session description: ' + error.toString());
}

function onSetLocalSuccess(pc) {
  trace(getName(pc) + ' setLocalDescription complete');
}

function onSetRemoteSuccess(pc) {
  trace(getName(pc) + ' setRemoteDescription complete');
}

function onSetSessionDescriptionError(error) {
  trace('Failed to set session description: ' + error.toString());
}

function gotRemoteStreamPc2(e) {
  if (pc2Video.srcObject !== e.streams[0]) {
    pc2Video.srcObject = e.streams[0];
    pc2MediaStream = e.streams[0];
    pc2Video.play();
    trace('pc21 received remote stream');
    sendMediaButton.disabled = true;
    rebroadcastButton.disabled = false;
  }
}

function gotRemoteStreamPc3(e) {
  if (pc3Video.srcObject !== e.streams[0]) {
    pc3Video.srcObject = e.streams[0];
    pc3Video.play();
    trace('pc32 received remote stream');
    trace(e.streams[0]);
  }
}

function onCreateAnswerSuccess(desc) {
  trace('Answer from pc21:\n' + desc.sdp);
  trace('pc21 setLocalDescription start');
  pc21.setLocalDescription(desc).then(
    function() {
      onSetLocalSuccess(pc21);
    },
    onSetSessionDescriptionError
  );
  trace('pc12 setRemoteDescription start');
  pc12.setRemoteDescription(desc).then(
    function() {
      onSetRemoteSuccess(pc12);
    },
    onSetSessionDescriptionError
  );
}


async function onIceCandidatePhase1(pc, event) {
  try {
    if (pc === pc12) {
      await pc21.addIceCandidate(event.candidate)
    } else{
      await pc12.addIceCandidate(event.candidate)
    }
    onAddIceCandidateSuccess(pc);
  } catch (error){
    onAddIceCandidateError(pc, error);
  }
  trace(getName(pc) + ' ICE candidate: \n' + (event.candidate ?
      event.candidate.candidate : '(null)'));
}
async function onIceCandidatePhase2(pc, event) {
  try {
    if (pc === pc32) {
      await pc23.addIceCandidate(event.candidate)
    } else{
      await pc32.addIceCandidate(event.candidate)
    }
    onAddIceCandidateSuccess(pc);
  } catch (error){
    onAddIceCandidateError(pc, error);
  }
  trace(getName(pc) + ' ICE candidate: \n' + (event.candidate ?
      event.candidate.candidate : '(null)'));
}

function onAddIceCandidateSuccess(pc) {
  trace(getName(pc) + ' addIceCandidate success');
}

function onAddIceCandidateError(pc, error) {
  trace(getName(pc) + ' failed to add ICE Candidate: ' + error.toString());
}

function onIceStateChange(pc, event) {
  if (pc) {
    trace(getName(pc) + ' ICE state: ' + pc.iceConnectionState);
    console.log('ICE state change event: ', event);
  }
}
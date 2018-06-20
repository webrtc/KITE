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

var tracing = true;

var startButton = document.getElementById('startButton');
var hangupButton = document.getElementById('hangupButton');
var channelIdInput = document.getElementById('channelId');


hangupButton.disabled = true;
startButton.onclick = start;
hangupButton.onclick = hangup;

var socket = io();

const myId = `${new Date().getTime()}${(Math.random() * 100000)|0}`

var channelId = null;

var startTime;

var readyForCalls = false;
var localStreams = [];
var remoteStreams = [];
var streamId = 'tbd';
var peerConn = null;
var offerOptions = {
  offerToReceiveAudio: 1,
  offerToReceiveVideo: 1
};

var servers = null;

var local_validation;
var remote_validation;


async function start() {
  if (channelId != null) {
    console.error('start(): channelId was not null: ', channelId);
  }

  var video_1 = document.getElementById('video1');
  var video_2 = document.getElementById('video2');

  channelId = channelIdInput.value;
  trace('Starting with channelId: ' + channelId);
  trace('Collecting tracks');
  startButton.disabled = true;
  var constraints = window.constraints = {
    audio: true,
    video: { width: 1280, height: 720 }
  };
  try {
    var constraints = window.constraints = {
      audio: true,
      video: { width: 1280, height: 720 }
    };

    var stream = await navigator.mediaDevices.getUserMedia(constraints);
    localStreams.push(stream);
    video_1.srcObject = stream;
    video_1.play();
    trace('Got user media with first set of constraints.');

    constraints = window.constraints = {
      audio: true,
      video: { width: 300, height: 300}
    };

    stream = await navigator.mediaDevices.getUserMedia(constraints);
    localStreams.push(stream);
    video_2.srcObject = stream;
    video_2.play();
    trace('Got user media with second set of constraints.')

    await setHelloHandler();
    await sendHello();

    trace('Set readyForCalls');
    readyForCalls = true;

  } catch(error) {
    trace('Cannot get user media with constraints.');
  }
}

async function call(remoteId) {
  if (channelId == null) {
    channelId = channelIdInput.value;
  }
  trace('Calling remoteId: ' + remoteId + ' on channel: ' + channelId);
  if (peerConn != null) {
    console.error('peerConn was not null !!!!', peerConn);
  }
  try {
    peerConn =  createPeerConnection(remoteId);
    trace('createOffer start');
    var offer = await peerConn.createOffer(offerOptions);
    await onCreateOfferSuccess(remoteId, peerConn, offer);
  } catch (error){
    onCreateSessionDescriptionError(remoteId, peerConn, error);
  }
}

// peer connection functions
//==========================================================================================================

function createPeerConnection(remoteId) {
  startButton.disabled = true;
  hangupButton.disabled = false;
  trace('Starting call');
  startTime = window.performance.now();


  if (localStreams.length == 0){
    trace('No stream was found.');
  }
  var pc = window.pc = new RTCPeerConnection(servers);
  trace('Created peer connection object');
  pc.onicecandidate = function(e) {
    onIceCandidate(remoteId, pc, e);
  };
  pc.oniceconnectionstatechange = function(e) {
    onIceStateChange(remoteId, pc, e);
  };
  localStreams.forEach( async function (stream){
    if (stream.active){
      trace('Adding stream to pc:');
      trace(stream);
      let video_track = stream.getVideoTracks()[0];
      let audio_track = stream.getAudioTracks()[0];
      await pc.addTrack(video_track, stream);
      await pc.addTrack(audio_track, stream);
    }
  })

  trace('Added local stream to peerConn');
  pc.ontrack = function(e) {
    gotRemoteStream(pc, e);
  };
  return pc;
}

async function onCreateOfferSuccess(remoteId, pc, desc) {
  trace('Got offer from peerConn');
  trace('setLocalDescription start');
  try {
    await pc.setLocalDescription(desc);
    onSetLocalSuccess(remoteId, pc);
  } catch (error){
    onSetSessionDescriptionError(remoteId, pc, error);
  }
}

function onCreateSessionDescriptionError(remoteId, pc, error) {
  trace('Failed to create session description: ' + error.toString());
}

function onSetLocalSuccess(remoteId, pc) {
  trace('setLocalDescription complete');
  var localDesc = pc.localDescription;
  socket.emit('webrtc', { channel: channelId, from: myId, to: remoteId, msg: { type: 'desc', desc: localDesc } });
}

function onSetRemoteSuccess(remoteId, pc) {
  trace('setRemoteDescription complete');
  trace('setRemoteDescription complete');
}

function onSetSessionDescriptionError(remoteId, pc, error) {
  trace('Failed to set session description: ' + error.toString());
}

function gotRemoteStream(pc, e) {
  trace('peerConn received remote stream');
  console.log(e.streams[0]);
  var received_video_1 = document.getElementById('received_video_1');
  var received_video_2 = document.getElementById('received_video_2');

  if (remoteStreams.length === 0){
    var stream = e.streams[0];
    received_video_1.srcObject = stream;
    remoteStreams.push(stream);
  } else {
    if (remoteStreams[0].id === e.streams[0].id){
      remoteStreams[0].addTrack(e.track);
    } else {
      if (remoteStreams.length > 1){
      } else {
        var stream = e.streams[0];
        received_video_2.srcObject = stream;
        remoteStreams.push(stream);
      }
    }
  }
}

async function onCreateAnswerSuccess(srcId, pc, desc) {
  try {
    await pc.setLocalDescription(desc)
    onSetLocalSuccess(srcId, pc);
  } catch (error) {
    onSetSessionDescriptionError(srcId, pc, error);
    add_error_message ('setLocalDescription : ' + error);
  }
}

function onIceCandidate(remoteId, pc, event) {
  trace('ICE candidate: \n' + (event.candidate ?
      event.candidate.candidate : '(null)'));
  if (event.candidate != null) {
    socket.emit('webrtc', { channel: channelId, from: myId, to: remoteId, msg: { type: 'candidate', candidate: event.candidate } });
  }
}

function onAddIceCandidateSuccess(remoteId, pc) {
  trace('addIceCandidate success');
}

function onAddIceCandidateError(remoteId, pc, error) {
  trace('Failed to add ICE Candidate: ' + error.toString());
  add_error_message ('onAddIceCandidateError : ' + error);
}

async function onIceStateChange(remoteId, pc, event) {
  if (pc) {
    trace('ICE state: ' + pc.iceConnectionState);
    switch(pc.iceConnectionState) {
      case "connected":
        // The connection has become fully connected
        received_video_1.play();
        received_video_2.play();
        show_sdp (peerConn.localDescription.sdp, peerConn.remoteDescription.sdp)
        break;
      case "disconnected":
      case "failed":
      case "closed":
        // Transport terminated, transport in error or connection closed
        hangup();
        break;
    }
  }
}

// socket functions
//==========================================================================================================

function hangup() {
  trace('Ending call');
  if (peerConn != null) {
    peerConn.close();
  }
  peerConn = null;
  channelId = null;
  hangupButton.disabled = true;
  startButton.disabled = false;
  socket.emit('goodbye', { id: myId });
}

socket.on(myId, function (data) {
});

socket.on('webrtc', webrtc_event_handler);

async function webrtc_event_handler (data) {
  if (data.channel != channelId) {
    return;
  }
  var srcId = data.from;
  var srcMsg = data.msg;
  if (srcMsg.type == 'candidate') {
    try {
      await peerConn.addIceCandidate(srcMsg.candidate);
      onAddIceCandidateSuccess(srcId, peerConn);
    } catch (error){
      onAddIceCandidateError(srcId, peerConn, error);
    }
  }
  else if (srcMsg.type == 'desc') {
    var remoteDesc = srcMsg.desc;
    if (remoteDesc.type == 'offer') {
      peerConn = createPeerConnection(srcId);
      trace('setRemoteDescription start');
      try {
        await peerConn.setRemoteDescription(remoteDesc);
        onSetRemoteSuccess(srcId, peerConn);
        var answer = await peerConn.createAnswer();
        await onCreateAnswerSuccess(srcId, peerConn, answer);
      } catch (error) {
        trace (' Error exchanging sdp after getting offer: ' + error.name);
      }
    }
    else if (remoteDesc.type == 'answer') {
      trace('setRemoteDescription start');
      try {
        await peerConn.setRemoteDescription(remoteDesc);
        onSetRemoteSuccess(srcId, peerConn);
      } catch (error){
        trace (' Error exchanging sdp after getting answer: ' + error.name);
      }
    }
    else {
      console.error('Unrecognized desc type: ', srcMsg);
    }
  }
  else {
    console.error('Unrecognized msg: ', srcMsg);
  }
}

function setHelloHandler() {
  socket.on('hello',async function (data) {
    var remoteId = data.id;
    await call(remoteId);
  });
}

socket.on('goodbye', function (data) {
  var remoteId = data.id;
});

// announce our presence 
function sendHello() {
  socket.emit('hello', { id: myId });
}


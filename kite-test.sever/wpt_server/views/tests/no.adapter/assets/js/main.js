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

var startButton = document.getElementById('startButton');
var hangupButton = document.getElementById('hangupButton');
var channelIdInput = document.getElementById('channelId');
var pc1Video = document.getElementById('pc1Video');
var pc2Video = document.getElementById('pc2Video');
var socket = io();
var channelId = null;
var startTime;
var readyForCalls = false;
var free = true;
var localStream;
var peerConn = null;
var offerOptions = {
  offerToReceiveAudio: 1,
  offerToReceiveVideo: 1
};
var servers = null;

hangupButton.disabled = true;
startButton.onclick = start;
hangupButton.onclick = hangup;


socket.on('serverNews', function (data) {
  //console.log(data);
});

var myId = new Date().getTime();
//console.log('myId: ', myId);

async function start(){
  if (channelId != null) {
    console.error('start(): channelId was not null: ', channelId);
  }
  channelId = channelIdInput.value;
  trace('Requesting local stream');
  startButton.disabled = true;

  try {
    localStream = await navigator.mediaDevices.getUserMedia({audio: true,video: true});
    trace('Received local stream');
    await setHelloHandler();
    await sendHello();
    trace('Set readyForCalls');
    readyForCalls = true;
  } catch (error) {
    alert('getUserMedia() error: ' + error.name);
  }
}

async function call(remoteId) {
  if (channelId == null) {
    channelId = channelIdInput.value;
  }
  console.log('Calling remoteId: ' + remoteId + ' on channel: ' + channelId);
  if (peerConn != null) {
    //console.error('peerConn was not null !!!!', peerConn);
  }
  try {
    peerConn = createPeerConnection(remoteId);
    trace('createOffer start');
    var offer = await peerConn.createOffer(offerOptions);
    await onCreateOfferSuccess(remoteId, peerConn, offer);
  } catch (error) {
    onCreateSessionDescriptionError(remoteId, peerConn, error);
  }
}

function createPeerConnection(remoteId) {
  startButton.disabled = true;
  hangupButton.disabled = false;
  trace('Starting call');
  startTime = window.performance.now();
  var videoTracks = localStream.getVideoTracks();
  var audioTracks = localStream.getAudioTracks();
  if (videoTracks.length > 0) {
    trace('Using video device: ' + videoTracks[0].label);
  }
  if (audioTracks.length > 0) {
    trace('Using audio device: ' + audioTracks[0].label);
  }
  var pc = window.pc = new RTCPeerConnection(servers);
  trace('Created peer connection object');
  pc.onicecandidate = function(e) {
    onIceCandidate(remoteId, pc, e);
  };
  pc.oniceconnectionstatechange = function(e) {
    onIceStateChange(remoteId, pc, e);
  };
  localStream.getTracks().forEach(
    function(track) {
      pc.addTrack(
        track,
        localStream
      );
    }
  );
  pc1Video.srcObject = localStream;
  pc1Video.play();
  trace('Added local stream to peerConn');
  pc.ontrack = function(e) {
    gotRemoteStream(pc, e);
  };
  return pc;
}

async function onCreateOfferSuccess(remoteId, pc, desc) {
  trace('Got offer from peerConn');
  //trace('Offer from peerConn\n' + desc.sdp);
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
  //console.log('Sending description to remote ' + remoteId);
  socket.emit('webrtc', { channel: channelId, from: myId, to: remoteId, msg: { type: 'desc', desc: localDesc } });
}

function onSetRemoteSuccess(remoteId, pc) {
  trace('setRemoteDescription complete');
}

function onSetSessionDescriptionError(remoteId, pc, error) {
  trace('Failed to set session description: ' + error.toString());
}

function gotRemoteStream(pc, e) {
  trace('peerConn received remote stream');
  pc2Video.srcObject = e.streams[0];
  pc2Video.play();
}

async function onCreateAnswerSuccess(srcId, pc, desc) {
  trace('Got answer from peerConn');
  //trace('Answer from peerConn:\n' + desc.sdp);
  trace('setLocalDescription start');
  try{
    await pc.setLocalDescription(desc);
    onSetLocalSuccess(srcId, pc);
  } catch (error){
    onSetSessionDescriptionError(srcId, pc, error);
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
}

function onIceStateChange(remoteId, pc, event) {
  if (pc) {
    trace('ICE state: ' + pc.iceConnectionState);
    //console.log('ICE state change event: ', event);
    switch(pc.iceConnectionState) {
      case "connected":
        // The connection has become fully connected
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
  //console.log('for me: ', data);  
});

socket.on('webrtc', async function (data) {
  if (data.channel != channelId) {
    return;
  }
  free = false;
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
});

function setHelloHandler() {
  socket.on('hello', function (data) {
    if (free){
    //console.log('hello: ', data);
      var remoteId = data.id;
      call(remoteId);
    }
  });
}

socket.on('goodbye', function (data) {
  //console.log('goodbye: ', data);
  var remoteId = data.id;
  //console.log('remoteId: ', remoteId);
});

// announce our presence 
function sendHello() {
  socket.emit('hello', { id: myId });
}


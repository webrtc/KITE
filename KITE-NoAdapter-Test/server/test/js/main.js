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

function trace(arg) {
  if (tracing) {
    var now = (window.performance.now() / 1000).toFixed(3);
    console.log(now + ': ', arg);
  }
}

var startButton = document.getElementById('startButton');
var hangupButton = document.getElementById('hangupButton');
var channelIdInput = document.getElementById('channelId');
hangupButton.disabled = true;
startButton.onclick = start;
hangupButton.onclick = hangup;

var socket = io();

socket.on('serverNews', function (data) {
  //console.log(data);
});

var myId = new Date().getTime();
//console.log('myId: ', myId);

var channelId = null;

var startTime;

var readyForCalls = false;
var localStream;
var peerConn = null;
var offerOptions = {
  offerToReceiveAudio: 1,
  offerToReceiveVideo: 1
};

var servers = null;

function start() {
  if (channelId != null) {
    console.error('start(): channelId was not null: ', channelId);
  }
  channelId = channelIdInput.value;
  //console.log('Starting with channelId: ' + channelId);
  trace('Requesting local stream');
  startButton.disabled = true;
  navigator.mediaDevices.getUserMedia({
    audio: true,
    video: true
  })
  .then(function(stream) {
    trace('Received local stream');
    localStream = stream;
  })
  .then(setHelloHandler)
  .then(sendHello)
  .then(function() {
    trace('Set readyForCalls');
    readyForCalls = true;
  })
  .catch(function(e) {
    alert('getUserMedia() error: ' + e.name);
  });
}

function call(remoteId) {
  if (channelId == null) {
    channelId = channelIdInput.value;
  }
  //console.log('Calling remoteId: ' + remoteId + ' on channel: ' + channelId);
  if (peerConn != null) {
    //console.error('peerConn was not null !!!!', peerConn);
  }
  peerConn = createPeerConnection(remoteId);
  trace('createOffer start');
  peerConn.createOffer(
    offerOptions
  ).then(
    function(desc) {
      onCreateOfferSuccess(remoteId, peerConn, desc);
    },
    function(error) {
      onCreateSessionDescriptionError(remoteId, peerConn, error);
    }
  );
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
  var pc = new RTCPeerConnection(servers);
  trace('Created peer connection object');
  pc.onicecandidate = function(e) {
    onIceCandidate(remoteId, pc, e);
  };
  pc.oniceconnectionstatechange = function(e) {
    onIceStateChange(remoteId, pc, e);
  };
  //pc.addStream(localStream); // deprecated !!!
  localStream.getTracks().forEach(
    function(track) {
      pc.addTrack(
        track,
        localStream
      );
    }
  );
  trace('Added local stream to peerConn');
  pc.ontrack = function(e) {
    gotRemoteStream(pc, e);
  };
  return pc;
}

function onCreateOfferSuccess(remoteId, pc, desc) {
  trace('Got offer from peerConn');
  //trace('Offer from peerConn\n' + desc.sdp);
  trace('setLocalDescription start');
  pc.setLocalDescription(desc).then(
    function() {
      onSetLocalSuccess(remoteId, pc);
    },
    function(error) {
      onSetSessionDescriptionError(remoteId, pc, error);
    }
  );
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
}

function onCreateAnswerSuccess(srcId, pc, desc) {
  trace('Got answer from peerConn');
  //trace('Answer from peerConn:\n' + desc.sdp);
  trace('setLocalDescription start');
  pc.setLocalDescription(desc).then(
    function() {
      onSetLocalSuccess(srcId, pc);
    },
    function(error) {
      onSetSessionDescriptionError(srcId, pc, error);
    }
  );
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

socket.on('webrtc', function (data) {
  //console.log('webrtc msg recieved for channel: ' + data.channel, data);
  if (data.channel != channelId) {
    //console.log('webrtc msg ignored - channel: ' + data.channel + ' - my channel: ' + channelId);
    return;
  }
  var srcId = data.from;
  var srcMsg = data.msg;
  //console.log('srcId: ', srcId);
  //console.log('desc: ', srcMsg);
  if (srcMsg.type == 'candidate') {
    var iceCandidate = srcMsg.candidate;
    //console.log('ICE candidate received: ', iceCandidate);
    peerConn.addIceCandidate(iceCandidate).then(
      function() {
        onAddIceCandidateSuccess(srcId, peerConn);
      },
      function(err) {
        onAddIceCandidateError(srcId, peerConn, err);
      }
    );
  }
  else if (srcMsg.type == 'desc') {
    var remoteDesc = srcMsg.desc;
    if (remoteDesc.type == 'offer') {
      peerConn = createPeerConnection(srcId);
      trace('setRemoteDescription start');
      peerConn.setRemoteDescription(remoteDesc).then(
        function() {
          onSetRemoteSuccess(srcId, peerConn);
        },
        function(error) {
          onSetSessionDescriptionError(srcId, peerConn, error);
        }
      ).then(
        function() {
          peerConn.createAnswer().then(
            function(desc) {
              onCreateAnswerSuccess(srcId, peerConn, desc);
            },
            function(error) {
              onCreateSessionDescriptionError(srcId, peerConn, error);
            }
        );
      });
    }
    else if (remoteDesc.type == 'answer') {
      trace('setRemoteDescription start');
      peerConn.setRemoteDescription(remoteDesc).then(
        function() {
          onSetRemoteSuccess(srcId, peerConn);
        },
        function(error) {
          onSetSessionDescriptionError(srcId, peerConn, error);
        }
      );
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
    //console.log('hello: ', data);
    var remoteId = data.id;
    call(remoteId);
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


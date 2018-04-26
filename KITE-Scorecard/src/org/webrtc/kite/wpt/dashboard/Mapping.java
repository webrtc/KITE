/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.wpt.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing the information on browsers, versioning and platforms.
 */
public class Mapping {


  public static Map<String, List> WPTMapping = new HashMap<>();
  public static Map<String, String> WPTDescriptionMapping = new HashMap<>();

  static {
    List<String> PeerConnectionAPI = new ArrayList<>();
    PeerConnectionAPI.add("RTCConfiguration-bundlePolicy");
    PeerConnectionAPI.add("RTCConfiguration-iceCandidatePoolSize");
    PeerConnectionAPI.add("RTCConfiguration-iceServers");
    PeerConnectionAPI.add("RTCConfiguration-iceTransportPolicy");
    PeerConnectionAPI.add("RTCConfiguration-rtcpMuxPolicy");
    PeerConnectionAPI.add("RTCDTMFSender-insertDTMF.https");
    PeerConnectionAPI.add("RTCDTMFSender-ontonechange-long.https");
    PeerConnectionAPI.add("RTCDTMFSender-ontonechange.https");
    PeerConnectionAPI.add("RTCDataChannel-bufferedAmount");
    PeerConnectionAPI.add("RTCDtlsTransport-getRemoteCertificates");
    PeerConnectionAPI.add("RTCIceCandidate-constructor");
    PeerConnectionAPI.add("RTCIceTransport");
    PeerConnectionAPI.add("RTCPeerConnection-addIceCandidate");
    PeerConnectionAPI.add("RTCPeerConnection-addTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection-addTransceiver");
    PeerConnectionAPI.add("RTCPeerConnection-canTrickleIceCandidates");
    PeerConnectionAPI.add("RTCPeerConnection-connectionState");
    PeerConnectionAPI.add("RTCPeerConnection-constructor");
    PeerConnectionAPI.add("RTCPeerConnection-createAnswer");
    PeerConnectionAPI.add("RTCPeerConnection-createDataChannel");
    PeerConnectionAPI.add("RTCPeerConnection-createOffer-offerToReceive");
    PeerConnectionAPI.add("RTCPeerConnection-createOffer");
    PeerConnectionAPI.add("RTCPeerConnection-generateCertificate");
    PeerConnectionAPI.add("RTCPeerConnection-getDefaultIceServers");
    PeerConnectionAPI.add("RTCPeerConnection-getIdentityAssertion");
    PeerConnectionAPI.add("RTCPeerConnection-getTransceivers");
    PeerConnectionAPI.add("RTCPeerConnection-iceConnectionState");
    PeerConnectionAPI.add("RTCPeerConnection-iceGatheringState");
    PeerConnectionAPI.add("RTCPeerConnection-ondatachannel");
    PeerConnectionAPI.add("RTCPeerConnection-onnegotiationneeded");
    PeerConnectionAPI.add("RTCPeerConnection-ontrack.https");
    PeerConnectionAPI.add("RTCPeerConnection-peerIdentity");
    PeerConnectionAPI.add("RTCPeerConnection-removeTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection-setDescription-transceiver");
    PeerConnectionAPI.add("RTCPeerConnection-setLocalDescription-answer");
    PeerConnectionAPI.add("RTCPeerConnection-setLocalDescription-offer");
    PeerConnectionAPI.add("RTCPeerConnection-setLocalDescription-rollback");
    PeerConnectionAPI.add("RTCPeerConnection-setLocalDescription");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-answer");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-offer");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-pranswer");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-replaceTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-rollback");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription-tracks.https");
    PeerConnectionAPI.add("RTCPeerConnection-setRemoteDescription");
    PeerConnectionAPI.add("RTCPeerConnectionIceEvent-constructor");
    PeerConnectionAPI.add("RTCRtpParameters-codecs");
    PeerConnectionAPI.add("RTCRtpParameters-degradationPreference");
    PeerConnectionAPI.add("RTCRtpParameters-encodings");
    PeerConnectionAPI.add("RTCRtpParameters-headerExtensions");
    PeerConnectionAPI.add("RTCRtpParameters-rtcp");
    PeerConnectionAPI.add("RTCRtpParameters-transactionId");
    PeerConnectionAPI.add("RTCRtpReceiver-getCapabilities");
    PeerConnectionAPI.add("RTCRtpReceiver-getContributingSources.https");
    PeerConnectionAPI.add("RTCRtpReceiver-getParameters");
    PeerConnectionAPI.add("RTCRtpReceiver-getSynchronizationSources.https");
    PeerConnectionAPI.add("RTCRtpSender-getCapabilities");
    PeerConnectionAPI.add("RTCRtpSender-replaceTrack");
    PeerConnectionAPI.add("RTCRtpSender-setParameters");
    PeerConnectionAPI.add("RTCRtpTransceiver-setCodecPreferences");
    PeerConnectionAPI.add("RTCRtpTransceiver-setDirection");
    PeerConnectionAPI.add("RTCSctpTransport-constructor");
    PeerConnectionAPI.add("RTCSctpTransport-maxMessageSize");
    PeerConnectionAPI.add("RTCTrackEvent-constructor");
    PeerConnectionAPI.add("webrtc-historical");
    PeerConnectionAPI.add("interfaces.https");
    PeerConnectionAPI.add("no-media-call");
    PeerConnectionAPI.add("promises-call");
    PeerConnectionAPI.add("simplecall.https");


    List<String> getUserMedia = new ArrayList<>();

    getUserMedia.add("GUM-api.https");
    getUserMedia.add("GUM-deny.https");
    getUserMedia.add("GUM-empty-option-param.https");
    getUserMedia.add("GUM-unknownkey-option-param.https");
    getUserMedia.add("mediacapture-streams-historical");
    getUserMedia.add("MediaDevices-getUserMedia.https");

    List<String> dataChannels = new ArrayList<>();

    dataChannels.add("RTCDataChannel-id");
    dataChannels.add("RTCDataChannel-send");
    dataChannels.add("RTCDataChannelEvent-constructor");
    dataChannels.add("datachannel-emptystring");

    List<String> TURNsupport = new ArrayList<>();
    List<String> Echocancellation = new ArrayList<>();
    List<String> MediaStreamAPI = new ArrayList<>();

    MediaStreamAPI.add("MediaDevices-enumerateDevices.https");
    MediaStreamAPI.add("MediaDevices-IDL-all");
    MediaStreamAPI.add("MediaDevices-IDL-enumerateDevices");
    MediaStreamAPI.add("MediaStream-add-audio-track.https");
    MediaStreamAPI.add("MediaStream-audio-only.https");
    MediaStreamAPI.add("MediaStream-default-feature-policy.https");
    MediaStreamAPI.add("MediaStream-finished-add.https");
    MediaStreamAPI.add("MediaStream-gettrackid.https");
    MediaStreamAPI.add("MediaStream-idl.https");
    MediaStreamAPI.add("MediaStream-id-manual.https");
    MediaStreamAPI.add("MediaStream-removetrack.https");
    MediaStreamAPI.add("MediaStreamTrack-end-manual.https");
    MediaStreamAPI.add("MediaStreamTrackEvent-constructor.https");
    MediaStreamAPI.add("MediaStreamTrack-getCapabilities.https");
    MediaStreamAPI.add("MediaStreamTrack-getSettings.https");
    MediaStreamAPI.add("MediaStreamTrack-id.https");
    MediaStreamAPI.add("MediaStreamTrack-idl.https");
    MediaStreamAPI.add("MediaStreamTrack-init.https");
    MediaStreamAPI.add("MediaStream-video-only.https");

    List<String> mediaConstraints = new ArrayList<>();
    mediaConstraints.add("GUM-impossible-constraint.https");
    mediaConstraints.add("GUM-optional-constraint.https");
    mediaConstraints.add("GUM-trivial-constraint.https");


    List<String> MultipleStreams = new ArrayList<>();
    List<String> Simulcast = new ArrayList<>();
    List<String> ScreenSharing = new ArrayList<>();
    List<String> Streamrebroadcasting = new ArrayList<>();
    List<String> getStatsAPI = new ArrayList<>();

    getStatsAPI.add("RTCPeerConnection-getStats");
    getStatsAPI.add("RTCPeerConnection-track-stats");
    getStatsAPI.add("RTCRtpSender-getStats");
    getStatsAPI.add("RTCRtpReceiver-getStats");
    getStatsAPI.add("getstats");

    List<String> ORTCAPI = new ArrayList<>();
    List<String> H264video = new ArrayList<>();
    List<String> VP8video = new ArrayList<>();
    List<String> Solidinteroperability = new ArrayList<>();
    List<String> srcObjectinmediaelement = new ArrayList<>();

    srcObjectinmediaelement.add("MediaStream-MediaElement-preload-none.https");
    srcObjectinmediaelement.add("MediaStream-MediaElement-srcObject.https");
    srcObjectinmediaelement.add("MediaStreamTrack-MediaElement-disabled-audio-is-silence.https");
    srcObjectinmediaelement.add("MediaStreamTrack-MediaElement-disabled-video-is-black.https");

    List<String> PromiseBasedGetUserMedia = new ArrayList<>();
    List<String> PromiseBasedPeerConnectionAPI = new ArrayList<>();
    List<String> WebAudioIntegration = new ArrayList<>();
    List<String> MediaRecorderIntegration = new ArrayList<>();

    MediaRecorderIntegration.add("mediacapture-record/idlharness");

    List<String> CanvasIntegration = new ArrayList<>();

    CanvasIntegration.add("mediacapture-image/idlharness");

    List<String> Testsupport = new ArrayList<>();

    WPTMapping.put("PeerConnection API", PeerConnectionAPI);
    WPTMapping.put("getUserMedia", getUserMedia);
    WPTMapping.put("dataChannels", dataChannels);
    WPTMapping.put("TURN support", TURNsupport);
    WPTMapping.put("Echo cancellation", Echocancellation);
    WPTMapping.put("MediaStream API", MediaStreamAPI);
    WPTMapping.put("Media Constraints", mediaConstraints);
    WPTMapping.put("Multiple Streams", MultipleStreams);
    WPTMapping.put("Simulcast", Simulcast);
    WPTMapping.put("Screen Sharing", ScreenSharing);
    WPTMapping.put("Stream re-broadcasting", Streamrebroadcasting);
    WPTMapping.put("getStats API", getStatsAPI);
    WPTMapping.put("ORTC API", ORTCAPI);
    WPTMapping.put("H.264 video", H264video);
    WPTMapping.put("VP8 video", VP8video);
    WPTMapping.put("Solid interoperability", Solidinteroperability);
    WPTMapping.put("srcObject in media element", srcObjectinmediaelement);
    WPTMapping.put("Promise based getUserMedia", PromiseBasedGetUserMedia);
    WPTMapping.put("Promise based PeerConnection API", PromiseBasedPeerConnectionAPI);
    WPTMapping.put("WebAudio Integrationt", WebAudioIntegration);
    WPTMapping.put("MediaRecorder Integration", MediaRecorderIntegration);
    WPTMapping.put("Canvas Integration", CanvasIntegration);
    WPTMapping.put("Test support", Testsupport);

    WPTDescriptionMapping.put("PeerConnection API", "The basic building block for connecting to peers. Defined by the W3C WebRTC Working Group.");
    WPTDescriptionMapping.put("getUserMedia", "The ability to request access to a user's webcam and microphone.");
    WPTDescriptionMapping.put("dataChannels", "Data channel support comes in two flavors: \"reliable\" means they are guaranteed to arrive and arrive in the correct order, \"unreliable\" means that order doesn't matter and dropped messages are acceptable.");
    WPTDescriptionMapping.put("TURN support", "TURN servers are necessary for situations where STUN fails to produce two addressable endpoints. Which is a fancy way of saying, \"getting around firewalls.\"");
    WPTDescriptionMapping.put("Echo cancellation", "This may be the most subjective item in this list. 3 or 4 users should be able to use a service like Talky without headphones on and not experience feedback problems.");
    WPTDescriptionMapping.put("MediaStream API", "When you request user media or get media from a peer it's held in a MediaStream object. Having the ability to add/remove and mute tracks it possible to do audio processing and control bandwidth consumption. ");
    WPTDescriptionMapping.put("Media Constraints", "The ability to specify restricted video sizes. This is important for bandwidth control. There are two aspects to this. The first is being able to specify constraints when first requesting access. The second is allowing you to apply constraints to an existing MediaStream.");
    WPTDescriptionMapping.put("Multiple Streams", "The ability to transport multiple audio and video streams on the same peerconnection. This is important for transporting multiple streams from a centralized server or rebroadcasting streams from other peers.");
    WPTDescriptionMapping.put("Simulcast", "The ability to acquire media streams at multiple resolutions and framerates and send them to the peer. Chrome allows this through undocumented SDP mangling.");
    WPTDescriptionMapping.put("Screen Sharing", "The ability to request access to a MediaStream of the computer screen. This is crucial for feature parity with existing communication solutions. In Chrome, this requires an extension, in Firefox there is a whitelist maintained by Mozilla. It's understandable there are some security and privacy concerns around screen sharing as a feature, so browser vendors are stepping cautiously as they try to work out how this should be implemented.");
    WPTDescriptionMapping.put("Stream re-broadcasting", "In order to support some of the more interesting topologies for routing data, we need to be able to take a MediaStream object from one peer and add it to another PeerConnection. Without this, the only way of doing multi-user is a mesh network or a centralized server.");
    WPTDescriptionMapping.put("getStats API", "The Statistics API allows to query various information about the PeerConnection like the current bitrate, round trip time or the number of video frames decoded.");
    WPTDescriptionMapping.put("ORTC API", "An alternative to the PeerConnection API defined by the W3C ORTC Community Group.");
    WPTDescriptionMapping.put("H.264 video", "Support for H.264 video. A WebRTC compliant browser should support both H.264 and VP8 video.");
    WPTDescriptionMapping.put("VP8 video", "Support for VP8 video. A WebRTC compliant browser should support both H.264 and VP8 video.");
    WPTDescriptionMapping.put("Solid interoperability", "Multiple browsers consistently being able to talk to each other is essential to making WebRTC a true web technology and not just something that makes for a nice demo. ");
    WPTDescriptionMapping.put("srcObject in media element", "srcObject is the way to indicate to a video or audio element that it should play a MediaStream (createObjectURL is obsoleted in the standard and leaks streams until the document dies).");
    WPTDescriptionMapping.put("Promise based getUserMedia", "GetUserMedia has moved to mediaDevices (instead of hanging directly on navigator), and uses Promises instead of callbacks. In Chrome this was shimmed by adapter.js.");
    WPTDescriptionMapping.put("Promise based PeerConnection API", "According to the latest W3C specification Promises should be used instead of callbacks. Natively supported in Chrome and Firefox, shims in adapter.js are available for older versions (or Edge which does not include a native RTCPeerConnection. ");
    WPTDescriptionMapping.put("WebAudio Integrationt", "Support for modifying audio using WebAudio on both input and output is important.");
    WPTDescriptionMapping.put("MediaRecorder Integration", "Support for recording video/audio media streams to a local blob rather than sending to a remote peer.");
    WPTDescriptionMapping.put("Canvas Integration", "Support for inserting generated or modified video into a MediaStream from a canvas. Important for funny hats (and sharing games, etc).");
    WPTDescriptionMapping.put("Test support", "Support for commandline or javascript flags that use fake devices or files for getUserMedia and allow bypassing user prompts. This allows for automatic testing using services such as travis-ci.");
  }

}

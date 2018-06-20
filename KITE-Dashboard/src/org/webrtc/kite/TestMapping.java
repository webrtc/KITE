/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE.2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing the information on browsers, versioning and platforms.
 */
public class TestMapping {


  public static Map<String, List> TestMapping = new HashMap<>();
  public static Map<String, String> TestCategoryMapping = new HashMap<>();
  public static Map<String, String> TestDescriptionMapping = new HashMap<>();
  private static List<List> testListList = new ArrayList<>();

  static {
    List<String> PeerConnectionAPI = new ArrayList<>();
    PeerConnectionAPI.add("RTCConfiguration.bundlePolicy");
    PeerConnectionAPI.add("RTCConfiguration.iceCandidatePoolSize");
    PeerConnectionAPI.add("RTCConfiguration.iceServers");
    PeerConnectionAPI.add("RTCConfiguration.iceTransportPolicy");
    PeerConnectionAPI.add("RTCConfiguration.rtcpMuxPolicy");
    PeerConnectionAPI.add("RTCDTMFSender.insertDTMF.https");
    PeerConnectionAPI.add("RTCDTMFSender.ontonechange.long.https");
    PeerConnectionAPI.add("RTCDTMFSender.ontonechange.https");
    PeerConnectionAPI.add("RTCDataChannel.bufferedAmount");
    PeerConnectionAPI.add("RTCDtlsTransport.getRemoteCertificates");
    PeerConnectionAPI.add("RTCIceCandidate.constructor");
    PeerConnectionAPI.add("RTCIceTransport");
    PeerConnectionAPI.add("RTCPeerConnection.addIceCandidate");
    PeerConnectionAPI.add("RTCPeerConnection.addTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection.addTransceiver");
    PeerConnectionAPI.add("RTCPeerConnection.canTrickleIceCandidates");
    PeerConnectionAPI.add("RTCPeerConnection.connectionState");
    PeerConnectionAPI.add("RTCPeerConnection.constructor");
    PeerConnectionAPI.add("RTCPeerConnection.createAnswer");
    PeerConnectionAPI.add("RTCPeerConnection.createDataChannel");
    PeerConnectionAPI.add("RTCPeerConnection.createOffer.offerToReceive");
    PeerConnectionAPI.add("RTCPeerConnection.createOffer");
    PeerConnectionAPI.add("RTCPeerConnection.generateCertificate");
    PeerConnectionAPI.add("RTCPeerConnection.getDefaultIceServers");
    PeerConnectionAPI.add("RTCPeerConnection.getIdentityAssertion");
    PeerConnectionAPI.add("RTCPeerConnection.getTransceivers");
    PeerConnectionAPI.add("RTCPeerConnection.iceConnectionState");
    PeerConnectionAPI.add("RTCPeerConnection.iceGatheringState");
    PeerConnectionAPI.add("RTCPeerConnection.ondatachannel");
    PeerConnectionAPI.add("RTCPeerConnection.onnegotiationneeded");
    PeerConnectionAPI.add("RTCPeerConnection.ontrack.https");
    PeerConnectionAPI.add("RTCPeerConnection.peerIdentity");
    PeerConnectionAPI.add("RTCPeerConnection.removeTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection.setDescription.transceiver");
    PeerConnectionAPI.add("RTCPeerConnection.setLocalDescription.answer");
    PeerConnectionAPI.add("RTCPeerConnection.setLocalDescription.offer");
    PeerConnectionAPI.add("RTCPeerConnection.setLocalDescription.rollback");
    PeerConnectionAPI.add("RTCPeerConnection.setLocalDescription");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.answer");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.offer");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.pranswer");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.replaceTrack.https");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.rollback");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription.tracks.https");
    PeerConnectionAPI.add("RTCPeerConnection.setRemoteDescription");
    PeerConnectionAPI.add("RTCPeerConnectionIceEvent.constructor");
    PeerConnectionAPI.add("RTCRtpParameters.codecs");
    PeerConnectionAPI.add("RTCRtpParameters.degradationPreference");
    PeerConnectionAPI.add("RTCRtpParameters.encodings");
    PeerConnectionAPI.add("RTCRtpParameters.headerExtensions");
    PeerConnectionAPI.add("RTCRtpParameters.rtcp");
    PeerConnectionAPI.add("RTCRtpParameters.transactionId");
    PeerConnectionAPI.add("RTCRtpReceiver.getCapabilities");
    PeerConnectionAPI.add("RTCRtpReceiver.getContributingSources.https");
    PeerConnectionAPI.add("RTCRtpReceiver.getParameters");
    PeerConnectionAPI.add("RTCRtpReceiver.getSynchronizationSources.https");
    PeerConnectionAPI.add("RTCRtpSender.getCapabilities");
    PeerConnectionAPI.add("RTCRtpSender.replaceTrack");
    PeerConnectionAPI.add("RTCRtpSender.setParameters");
    PeerConnectionAPI.add("RTCRtpTransceiver.setCodecPreferences");
    PeerConnectionAPI.add("RTCRtpTransceiver.setDirection");
    PeerConnectionAPI.add("RTCSctpTransport.constructor");
    PeerConnectionAPI.add("RTCSctpTransport.maxMessageSize");
    PeerConnectionAPI.add("RTCTrackEvent.constructor");
    PeerConnectionAPI.add("webrtc.historical");
    PeerConnectionAPI.add("interfaces.https");
    PeerConnectionAPI.add("no.media.call");
    PeerConnectionAPI.add("promises.call");
    PeerConnectionAPI.add("simplecall.https");


    List<String> getUserMedia = new ArrayList<>();

    getUserMedia.add("GUM.api.https");
    getUserMedia.add("GUM.deny.https");
    getUserMedia.add("GUM.empty.option.param.https");
    getUserMedia.add("GUM.unknownkey.option.param.https");
    getUserMedia.add("mediacapture.streams.historical");
    getUserMedia.add("MediaDevices.getUserMedia.https");

    List<String> dataChannels = new ArrayList<>();

    dataChannels.add("RTCDataChannel.id");
    dataChannels.add("RTCDataChannel.send");
    dataChannels.add("RTCDataChannelEvent.constructor");
    dataChannels.add("datachannel.emptystring");

    List<String> TURNsupport = new ArrayList<>();
    List<String> Echocancellation = new ArrayList<>();
    List<String> MediaStreamAPI = new ArrayList<>();

    MediaStreamAPI.add("MediaDevices.enumerateDevices.https");
    MediaStreamAPI.add("MediaDevices.IDL.all");
    MediaStreamAPI.add("MediaDevices.IDL.enumerateDevices");
    MediaStreamAPI.add("MediaStream.add.audio.track.https");
    MediaStreamAPI.add("MediaStream.audio.only.https");
    MediaStreamAPI.add("MediaStream.default.feature.policy.https");
    MediaStreamAPI.add("MediaStream.finished.add.https");
    MediaStreamAPI.add("MediaStream.gettrackid.https");
    MediaStreamAPI.add("MediaStream.idl.https");
    MediaStreamAPI.add("MediaStream.id.manual.https");
    MediaStreamAPI.add("MediaStream.removetrack.https");
    MediaStreamAPI.add("MediaStreamTrack.end.manual.https");
    MediaStreamAPI.add("MediaStreamTrackEvent.constructor.https");
    MediaStreamAPI.add("MediaStreamTrack.getCapabilities.https");
    MediaStreamAPI.add("MediaStreamTrack.getSettings.https");
    MediaStreamAPI.add("MediaStreamTrack.id.https");
    MediaStreamAPI.add("MediaStreamTrack.idl.https");
    MediaStreamAPI.add("MediaStreamTrack.init.https");
    MediaStreamAPI.add("MediaStream.video.only.https");

    List<String> mediaConstraints = new ArrayList<>();
    mediaConstraints.add("GUM.impossible.constraint.https");
    mediaConstraints.add("GUM.optional.constraint.https");
    mediaConstraints.add("GUM.trivial.constraint.https");


    List<String> MultipleStreams = new ArrayList<>();

    MultipleStreams.add("MulticastTest");

    List<String> Simulcast = new ArrayList<>();

    Simulcast.add("SimulcastTest");

    List<String> ScreenSharing = new ArrayList<>();

    ScreenSharing.add("ScreenSharingTest");

    List<String> Streamrebroadcasting = new ArrayList<>();

    Streamrebroadcasting.add("RebroadcastTest");

    List<String> getStatsAPI = new ArrayList<>();

    getStatsAPI.add("RTCPeerConnection.getStats.https");
    getStatsAPI.add("RTCPeerConnection.track.stats.https");
    getStatsAPI.add("RTCRtpSender.getStats.https");
    getStatsAPI.add("RTCRtpReceiver.getStats.https");
    getStatsAPI.add("getstats");

    List<String> ORTCAPI = new ArrayList<>();
    List<String> H264video = new ArrayList<>();

    H264video.add("H264OnlyTest");

    List<String> VP8video = new ArrayList<>();

    VP8video.add("VP8OnlyTest");

    List<String> Solidinteroperability = new ArrayList<>();

    Solidinteroperability.add("ARBitRateTest");
    Solidinteroperability.add("HDTest");
    Solidinteroperability.add("IceConnectionTest");
    Solidinteroperability.add("LoopBackTest");
    Solidinteroperability.add("NoAdapterTest");
    Solidinteroperability.add("NoAudioTest");
    Solidinteroperability.add("NoVideoTest");
    Solidinteroperability.add("Opus48kTest");
    Solidinteroperability.add("OpusDTXTest");
    Solidinteroperability.add("OpusFECTest");
    Solidinteroperability.add("ReceivingVideoCodecTest");
    Solidinteroperability.add("ResolutionTest");
    Solidinteroperability.add("SendingVideoCodecTest");
    Solidinteroperability.add("VSBitRateTest");
    Solidinteroperability.add("VideoFECTest");

    List<String> srcObjectinmediaelement = new ArrayList<>();

    srcObjectinmediaelement.add("MediaStream.MediaElement.preload.none.manual.https");
    srcObjectinmediaelement.add("MediaStream.MediaElement.srcObject.https");
    srcObjectinmediaelement.add("MediaStreamTrack.MediaElement.disabled.audio.is.silence.https");
    srcObjectinmediaelement.add("MediaStreamTrack.MediaElement.disabled.video.is.black.https");

    List<String> WebAudioIntegration = new ArrayList<>();

    WebAudioIntegration.add("WebAudioInputTest");

    List<String> MediaRecorderIntegration = new ArrayList<>();

    MediaRecorderIntegration.add("MediaRecorderAPITest");

    List<String> CanvasIntegration = new ArrayList<>();

    CanvasIntegration.add("CanvasStreamToPcTest");
    CanvasIntegration.add("mediacapture.image/idlharness");

    testListList.add(PeerConnectionAPI);
    testListList.add(getUserMedia);
    testListList.add(dataChannels);
    //testListList.add(TURNsupport);
    //testListList.add(Echocancellation);
    testListList.add(MediaStreamAPI);
    testListList.add(mediaConstraints);
    testListList.add(MultipleStreams);
    testListList.add(Simulcast);
    testListList.add(ScreenSharing);
    testListList.add(Streamrebroadcasting);
    //testListList.add(ORTCAPI);
    testListList.add(H264video);
    testListList.add(VP8video);
    testListList.add(Solidinteroperability);
    testListList.add(srcObjectinmediaelement);
    testListList.add(WebAudioIntegration);
    testListList.add(MediaRecorderIntegration);
    testListList.add(CanvasIntegration);





    TestMapping.put("PeerConnection API", PeerConnectionAPI);
    TestMapping.put("getUserMedia", getUserMedia);
    TestMapping.put("dataChannels", dataChannels);
    //TestMapping.put("TURN support", TURNsupport);
    //TestMapping.put("Echo cancellation", Echocancellation);
    TestMapping.put("MediaStream API", MediaStreamAPI);
    TestMapping.put("Media Constraints", mediaConstraints);
    TestMapping.put("Multiple Streams", MultipleStreams);
    TestMapping.put("Simulcast", Simulcast);
    TestMapping.put("Screen Sharing", ScreenSharing);
    TestMapping.put("Stream re.broadcasting", Streamrebroadcasting);
    TestMapping.put("getStats API", getStatsAPI);
    //TestMapping.put("ORTC API", ORTCAPI);
    TestMapping.put("H.264 video", H264video);
    TestMapping.put("VP8 video", VP8video);
    TestMapping.put("Solid interoperability", Solidinteroperability);
    TestMapping.put("srcObject in media element", srcObjectinmediaelement);
    TestMapping.put("WebAudio Integration", WebAudioIntegration);
    TestMapping.put("MediaRecorder Integration", MediaRecorderIntegration);
    TestMapping.put("Canvas Integration", CanvasIntegration);

    for (String category: TestMapping.keySet())   {
      for (String test: (List<String> )TestMapping.get(category)) {
        TestCategoryMapping.put(test,category);
      }
    }

    TestDescriptionMapping.put("PeerConnection API", "The basic building block for connecting to peers. Defined by the W3C WebRTC Working Group.");
    TestDescriptionMapping.put("getUserMedia", "The ability to request access to a user's webcam and microphone.");
    TestDescriptionMapping.put("dataChannels", "Data channel support comes in two flavors: \"reliable\" means they are guaranteed to arrive and arrive in the correct order, \"unreliable\" means that order doesn't matter and dropped messages are acceptable.");
    //TestDescriptionMapping.put("TURN support", "TURN servers are necessary for situations where STUN fails to produce two addressable endpoints. Which is a fancy way of saying, \"getting around firewalls.\"");
    //TestDescriptionMapping.put("Echo cancellation", "This may be the most subjective item in this list. 3 or 4 users should be able to use a service like Talky without headphones on and not experience feedback problems.");
    TestDescriptionMapping.put("MediaStream API", "When you request user media or get media from a peer it's held in a MediaStream object. Having the ability to add/remove and mute tracks it possible to do audio processing and control bandwidth consumption. ");
    TestDescriptionMapping.put("Media Constraints", "The ability to specify restricted video sizes. This is important for bandwidth control. There are two aspects to this. The first is being able to specify constraints when first requesting access. The second is allowing you to apply constraints to an existing MediaStream.");
    TestDescriptionMapping.put("Multiple Streams", "The ability to transport multiple audio and video streams on the same peerconnection. This is important for transporting multiple streams from a centralized server or rebroadcasting streams from other peers.");
    TestDescriptionMapping.put("Simulcast", "The ability to acquire media streams at multiple resolutions and framerates and send them to the peer. Chrome allows this through undocumented SDP mangling.");
    TestDescriptionMapping.put("Screen Sharing", "The ability to request access to a MediaStream of the computer screen. This is crucial for feature parity with existing communication solutions. In Chrome, this requires an extension, in Firefox there is a whitelist maintained by Mozilla. It's understandable there are some security and privacy concerns around screen sharing as a feature, so browser vendors are stepping cautiously as they try to work out how this should be implemented.");
    TestDescriptionMapping.put("Stream re.broadcasting", "In order to support some of the more interesting topologies for routing data, we need to be able to take a MediaStream object from one peer and add it to another PeerConnection. Without this, the only way of doing multi.user is a mesh network or a centralized server.");
    TestDescriptionMapping.put("getStats API", "The Statistics API allows to query various information about the PeerConnection like the current bitrate, round trip time or the number of video frames decoded.");
    //TestDescriptionMapping.put("ORTC API", "An alternative to the PeerConnection API defined by the W3C ORTC Community Group.");
    TestDescriptionMapping.put("H.264 video", "Support for H.264 video. A WebRTC compliant browser should support both H.264 and VP8 video.");
    TestDescriptionMapping.put("VP8 video", "Support for VP8 video. A WebRTC compliant browser should support both H.264 and VP8 video.");
    TestDescriptionMapping.put("Solid interoperability", "Multiple browsers consistently being able to talk to each other is essential to making WebRTC a true web technology and not just something that makes for a nice demo. ");
    TestDescriptionMapping.put("srcObject in media element", "srcObject is the way to indicate to a video or audio element that it should play a MediaStream (createObjectURL is obsoleted in the standard and leaks streams until the document dies).");
    TestDescriptionMapping.put("WebAudio Integrationt", "Support for modifying audio using WebAudio on both input and output is important.");
    TestDescriptionMapping.put("MediaRecorder Integration", "Support for recording video/audio media streams to a local blob rather than sending to a remote peer.");
    TestDescriptionMapping.put("Canvas Integration", "Support for inserting generated or modified video into a MediaStream from a canvas. Important for funny hats (and sharing games, etc).");
    //TestDescriptionMapping.put("Test support", "Support for commandline or javascript flags that use fake devices or files for getUserMedia and allow bypassing user prompts. This allows for automatic testing using services such as travis.ci.");
  }

}

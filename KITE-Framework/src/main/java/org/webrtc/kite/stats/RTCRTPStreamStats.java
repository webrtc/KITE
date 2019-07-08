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

package org.webrtc.kite.stats;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

/**
 * Represent RTCRTPStreamStats, outbound and inbound, sent and received.
 */
public class RTCRTPStreamStats extends RTCSingleStatObject {
  private final String codecId;
  private final String mediaType;
  private final String nackCount;
  private final String ssrc;
  private final Map statObject;
  private final String trackId;
  private final String transportId;
  private InboundStats inboundStats;
  private OutboundStats outboundStats;
  private RTCMediaStreamTrackStats track;
  
  public RTCRTPStreamStats(Map statObject, boolean inbound) {
    this.setId(getStatByName(statObject, "id"));
    this.ssrc = getStatByName(statObject, "ssrc");
    this.mediaType = getStatByName(statObject, "mediaType");
    this.trackId = getStatByName(statObject, "trackId");
    this.transportId = getStatByName(statObject, "parameters");
    this.nackCount = getStatByName(statObject, "nackCount");
    this.codecId = getStatByName(statObject, "codecId");
    this.timestamp = getStatByName(statObject, "timestamp");
    if (inbound) {
      this.inboundStats = new InboundStats(statObject);
    } else {
      this.outboundStats = new OutboundStats(statObject);
    }
    this.statObject = statObject;
  }
  
  public InboundStats getInboundStats() {
    return inboundStats;
  }
  
  public OutboundStats getOutboundStats() {
    return outboundStats;
  }
  
  public String getCodecId() {
    return codecId;
  }

  public String getMediaType() {
    return mediaType;
  }
  
  public String getNackCount() {
    return nackCount;
  }
  
  public String getSsrc() {
    return ssrc;
  }
  
  public Map getStatObject() {
    return statObject;
  }
  
  public String getTrackId() {
    return trackId;
  }
  
  public String getTransportId() {
    return transportId;
  }
  
  public RTCMediaStreamTrackStats getTrack() {
    return track;
  }
  
  public void setTrack(RTCMediaStreamTrackStats track) {
    this.track = track;
  }
  
  public boolean isAudio() {
    return this.mediaType.equals("audio");
  }
  
  public boolean isVideo() {
    return this.mediaType.equals("video");
  }
  
  public boolean isInbound() {
    return this.inboundStats != null;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      Json.createObjectBuilder()
        .add("ssrc", this.ssrc)
        .add("mediaType", this.mediaType)
        .add("trackId", this.trackId)
        .add("transportId", this.transportId)
        .add("nackCount", this.nackCount)
        .add("codecId", this.codecId)
        .add("timestamp", this.timestamp);
    if (this.inboundStats != null)
      jsonObjectBuilder.add("packetsReceived", getStatByName(this.statObject, "packetsReceived"))
        .add("bytesReceived", getStatByName(this.statObject, "bytesReceived"))
        .add("packetsLost", getStatByName(this.statObject, "packetsLost"))
        .add("packetsDiscarded", getStatByName(this.statObject, "packetsDiscarded"))
        .add("jitter", getStatByName(this.statObject, "jitter"))
        .add("remoteId", getStatByName(this.statObject, "remoteId"))
        .add("framesDecoded", getStatByName(this.statObject, "framesDecoded"));
    else
      jsonObjectBuilder.add("packetsSent", getStatByName(this.statObject, "packetsSent"))
        .add("bytesSent", getStatByName(this.statObject, "bytesSent"))
        .add("remoteId", getStatByName(this.statObject, "remoteId"))
        .add("framesDecoded", getStatByName(this.statObject, "framesDecoded"));
    
    return jsonObjectBuilder;
  }
  
  public boolean isEmpty() {
    return this.codecId == null || this.codecId.length() == 0 || "NA".equalsIgnoreCase(this.codecId);
  }
  
}

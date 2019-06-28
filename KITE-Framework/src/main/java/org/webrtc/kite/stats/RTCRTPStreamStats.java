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
public class RTCRTPStreamStats extends RTCStatObject {
  private final String codecId;
  private final boolean inbound;
  private final String mediaType;
  private final String nackCount;
  private final String ssrc;
  private final Map<Object, Object> statObject;
  private final String timestamp;
  private final String trackId;
  private final String transportId;
  
  public RTCRTPStreamStats(Map<Object, Object> statObject, boolean inbound) {
    this.setId(getStatByName(statObject, "id"));
    this.ssrc = getStatByName(statObject, "ssrc");
    this.mediaType = getStatByName(statObject, "mediaType");
    this.trackId = getStatByName(statObject, "trackId");
    this.transportId = getStatByName(statObject, "parameters");
    this.nackCount = getStatByName(statObject, "nackCount");
    this.codecId = getStatByName(statObject, "codecId");
    this.timestamp = getStatByName(statObject, "timestamp");
    this.inbound = inbound;
    this.statObject = statObject;
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
    if (this.inbound) {
      jsonObjectBuilder
          .add("packetsReceived", getStatByName(this.statObject, "packetsReceived"))
          .add("bytesReceived", getStatByName(this.statObject, "bytesReceived"))
          .add("packetsLost", getStatByName(this.statObject, "packetsLost"))
          .add("packetsDiscarded", getStatByName(this.statObject, "packetsDiscarded"))
          .add("jitter", getStatByName(this.statObject, "jitter"))
          .add("remoteId", getStatByName(this.statObject, "remoteId"))
          .add("framesDecoded", getStatByName(this.statObject, "framesDecoded"));
    } else {
      jsonObjectBuilder
          .add("packetsSent", getStatByName(this.statObject, "packetsSent"))
          .add("bytesSent", getStatByName(this.statObject, "bytesSent"))
          .add("remoteId", getStatByName(this.statObject, "remoteId"))
          .add("framesSent", getStatByName(this.statObject, "framesSent"));
      }
    
    return jsonObjectBuilder;
  }
}

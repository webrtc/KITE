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

package org.webrtc.kite.stat;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

/**
 * Represent RTCRTPStreamStats, outbound and inbound, sent and received.
 */
public class RTCRTPStreamStats extends StatObject {
  private String ssrc, mediaType, trackId, transportId, codecId, nackCount;
  private Map<Object, Object> statObject;
  private boolean inbound;

  public RTCRTPStreamStats(Map<Object, Object> statObject, boolean inbound) {
    this.setId(Utility.getStatByName(statObject, "id"));
    this.ssrc = Utility.getStatByName(statObject, "ssrc");
    this.mediaType = Utility.getStatByName(statObject, "mediaType");
    this.trackId = Utility.getStatByName(statObject, "trackId");
    this.transportId = Utility.getStatByName(statObject, "parameters");
    this.nackCount = Utility.getStatByName(statObject, "nackCount");
    this.codecId = Utility.getStatByName(statObject, "codecId");
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
        .add("codecId", this.codecId);
    if (this.inbound)
      jsonObjectBuilder.add("packetsReceived", Utility.getStatByName(this.statObject, "packetsReceived"))
        .add("bytesReceived", Utility.getStatByName(this.statObject, "bytesReceived"))
        .add("packetsLost", Utility.getStatByName(this.statObject, "packetsLost"))
        .add("packetsDiscarded", Utility.getStatByName(this.statObject, "packetsDiscarded"))
        .add("jitter", Utility.getStatByName(this.statObject, "jitter"))
        .add("remoteId", Utility.getStatByName(this.statObject, "remoteId"))
        .add("framesDecoded", Utility.getStatByName(this.statObject, "framesDecoded"));
    else
      jsonObjectBuilder.add("packetsSent", Utility.getStatByName(this.statObject, "packetsSent"))
        .add("bytesSent", Utility.getStatByName(this.statObject, "bytesSent"))
        .add("remoteId", Utility.getStatByName(this.statObject, "remoteId"))
        .add("framesDecoded", Utility.getStatByName(this.statObject, "framesDecoded"));

    return jsonObjectBuilder;
  }
}

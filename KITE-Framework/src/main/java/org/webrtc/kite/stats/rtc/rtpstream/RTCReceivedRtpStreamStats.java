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

package org.webrtc.kite.stats.rtc.rtpstream;

import java.util.Map;
import javax.json.JsonObjectBuilder;

/**
 * Represent RTCRtpStreamStats, outbound and inbound, sent and received.
 */
public class RTCReceivedRtpStreamStats extends RTCRtpStreamStats {
  private final String packetsReceived;
  private final String packetsLost;
  private final String packetsDiscarded;
  private final String jitter;
  private final String bytesReceived;

  public RTCReceivedRtpStreamStats(Map statObject) {
    super(statObject);
    this.packetsReceived = getStatByName( "packetsReceived");
    this.packetsLost = getStatByName( "packetsLost");
    this.packetsDiscarded = getStatByName( "packetsDiscarded");
    this.jitter = getStatByName( "jitter");
    this.bytesReceived = getStatByName( "bytesReceived");
  }

  public Double getPacketsReceived() {
    return parseDouble(packetsReceived);
  }

  public Double getPacketsLost() {
    return parseDouble(packetsLost);
  }

  public Double getPacketsDiscarded() {
    return parseDouble(packetsDiscarded);
  }

  public Double getBytesReceived() {
    return parseDouble(bytesReceived);
  }

  public Double getJitter() {
    return 1000*parseDouble(jitter);
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("packetsReceived", this.packetsReceived)
        .add("packetsLost", this.packetsLost)
        .add("packetsDiscarded", this.packetsDiscarded)
        .add("jitter", this.jitter)
        .add("bytesReceived", this.bytesReceived);
  }
  
}

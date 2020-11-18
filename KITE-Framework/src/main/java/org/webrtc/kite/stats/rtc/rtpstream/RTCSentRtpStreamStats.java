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
public class RTCSentRtpStreamStats extends RTCRtpStreamStats {
  private final String packetsSent;
  private final String bytesSent;

  public RTCSentRtpStreamStats(Map statObject) {
    super(statObject);
    this.packetsSent = getStatByName( "packetsSent");
    this.bytesSent = getStatByName( "bytesSent");
  }

  public double getPacketsSent() {
    return parseDouble(packetsSent);
  }

  public double getBytesSent() {
    return parseDouble(bytesSent);
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("packetsSent", this.packetsSent)
        .add("bytesSent", this.bytesSent);
  }
  
}

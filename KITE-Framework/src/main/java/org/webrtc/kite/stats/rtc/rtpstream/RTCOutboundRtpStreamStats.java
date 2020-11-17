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
public class RTCOutboundRtpStreamStats extends RTCSentRtpStreamStats {
  protected final String senderId;
  protected final String remoteId;
  protected final String nackCount;
  protected final String framesEncoded;
  protected final String framesSent;
  protected final String framesPerSecond;




  public RTCOutboundRtpStreamStats(Map statObject) {
    super(statObject);
    this.senderId = getStatByName( "senderId");
    this.remoteId = getStatByName( "remoteId");
    this.nackCount = getStatByName( "nackCount");
    this.framesEncoded = getStatByName( "framesEncoded");
    this.framesSent = getStatByName( "framesSent");
    this.framesPerSecond = getStatByName( "framesPerSecond");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("senderId", this.senderId)
        .add("remoteId", this.remoteId)
        .add("nackCount", this.nackCount)
        .add("framesEncoded", this.framesEncoded)
        .add("framesSent", this.framesSent)
        .add("framesPerSecond", this.framesPerSecond);
  }

  public String getSenderId() {
    return senderId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public Double getNackCount() {
    return parseDouble(nackCount);
  }

  public Double getFramesEncoded() {
    return parseDouble(framesEncoded);
  }

  public Double getFramesSent() {
    return parseDouble(framesSent);
  }

  public Double getFramesPerSecond() {
    return parseDouble(framesPerSecond);
  }
}

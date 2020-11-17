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

import javax.json.JsonObjectBuilder;
import java.util.Map;
import org.webrtc.kite.stats.rtc.RTCSingleStatObject;
import org.webrtc.kite.stats.rtc.msource.RTCMediaSourceStats;

/**
 * Represent RTCRtpStreamStats, outbound and inbound, sent and received.
 */
public class RTCRtpStreamStats extends RTCSingleStatObject {
  protected final String codecId;
  protected final String kind;
  protected final String ssrc;
  protected final String transportId;
//  protected RTCMediaSourceStats mediaSourceStats;

  public RTCRtpStreamStats(Map statObject) {
    super(statObject);
    this.ssrc = getStatByName( "ssrc");
    this.kind = getStatByName( "kind");
    this.transportId = getStatByName( "transportId");
    this.codecId = getStatByName( "codecId");
  }

  public String getCodecId() {
    return codecId;
  }

  public String getKind() {
    return kind;
  }

  public String getSsrc() {
    return ssrc;
  }
  
  public Map getStatObject() {
    return statObject;
  }
  
  public String getTransportId() {
    return transportId;
  }


  // not all browser supports this
//  public void setMediaSourceStats(RTCMediaSourceStats mediaSourceStats) {
//    this.mediaSourceStats = mediaSourceStats;
//  }

//  public RTCMediaSourceStats getMediaSourceStats() {
//    return mediaSourceStats;
//  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("ssrc", this.ssrc)
        .add("kind", this.kind)
        .add("transportId", this.transportId)
        .add("codecId", this.codecId)
        .add("timestamp", this.timestamp);
  }
  
  public boolean isEmpty() {
    return this.codecId == null || this.codecId.length() == 0 || "NA".equalsIgnoreCase(this.codecId);
  }
  
}

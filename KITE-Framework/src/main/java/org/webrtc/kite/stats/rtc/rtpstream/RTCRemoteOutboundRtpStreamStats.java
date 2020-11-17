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
public class RTCRemoteOutboundRtpStreamStats extends RTCOutboundRtpStreamStats {
  protected final String localId;
  protected final String remoteTimestamp;




  public RTCRemoteOutboundRtpStreamStats(Map statObject) {
    super(statObject);
    this.localId = getStatByName( "localId");
    this.remoteTimestamp = getStatByName( "remoteTimestamp");
  }



  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("localId", this.localId)
        .add("remoteTimestamp", this.remoteTimestamp);
  }


}

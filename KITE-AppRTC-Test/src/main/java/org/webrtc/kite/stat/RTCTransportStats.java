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
 * RTCTransportStats, with attributes bytesSent, bytesReceived, rtcpTransportStatsId, selectedCan
 */
public class RTCTransportStats extends StatObject {

  private String bytesSent, bytesReceived, rtcpTransportStatsId, selectedCandidatePairId,
    localCertificateId, remoteCertificateId;

  public RTCTransportStats(Map<Object, Object> statObject) {
    this.setId(Utility.getStatByName(statObject, "id"));
    this.rtcpTransportStatsId = Utility.getStatByName(statObject, "rtcpTransportStatsId");
    this.selectedCandidatePairId = Utility.getStatByName(statObject, "selectedCandidatePairId");
    this.localCertificateId = Utility.getStatByName(statObject, "localCertificateId");
    this.remoteCertificateId = Utility.getStatByName(statObject, "remoteCertificateId");
    this.bytesSent = Utility.getStatByName(statObject, "bytesSent");
    this.bytesReceived = Utility.getStatByName(statObject, "bytesReceived");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      Json.createObjectBuilder()
        .add("rtcpTransportStatsId", this.rtcpTransportStatsId)
        .add("selectedCandidatePairId", this.selectedCandidatePairId)
        .add("localCertificateId", this.localCertificateId)
        .add("remoteCertificateId", this.remoteCertificateId)
        .add("bytesSent", this.bytesSent)
        .add("bytesReceived", this.bytesReceived);
    return jsonObjectBuilder;
  }
}

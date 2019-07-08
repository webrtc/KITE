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
 * RTCTransportStats, with attributes bytesSent, bytesReceived, rtcpTransportStatsId, selectedCan
 */
public class RTCTransportStats extends RTCSingleStatObject {

  private final String bytesReceived;
  private final String bytesSent;
  private final String localCertificateId;
  private final String remoteCertificateId;
  private final String rtcpTransportStatsId;
  private final String selectedCandidatePairId;


  public RTCTransportStats(Map statObject) {
    this.setId(getStatByName(statObject, "id"));
    this.rtcpTransportStatsId = getStatByName(statObject, "rtcpTransportStatsId");
    this.selectedCandidatePairId = getStatByName(statObject, "selectedCandidatePairId");
    this.localCertificateId = getStatByName(statObject, "localCertificateId");
    this.remoteCertificateId = getStatByName(statObject, "remoteCertificateId");
    this.bytesSent = getStatByName(statObject, "bytesSent");
    this.bytesReceived = getStatByName(statObject, "bytesReceived");
    this.timestamp = getStatByName(statObject, "timestamp");
  }
  
  public String getBytesReceived() {
    return bytesReceived;
  }
  
  public String getBytesSent() {
    return bytesSent;
  }
  
  public String getLocalCertificateId() {
    return localCertificateId;
  }
  
  public String getRemoteCertificateId() {
    return remoteCertificateId;
  }
  
  public String getRtcpTransportStatsId() {
    return rtcpTransportStatsId;
  }
  
  public String getSelectedCandidatePairId() {
    return selectedCandidatePairId;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("rtcpTransportStatsId", this.rtcpTransportStatsId)
      .add("selectedCandidatePairId", this.selectedCandidatePairId)
      .add("localCertificateId", this.localCertificateId)
      .add("remoteCertificateId", this.remoteCertificateId)
      .add("bytesSent", this.bytesSent)
      .add("bytesReceived", this.bytesReceived)
      .add("timestamp", this.timestamp);
  }
}

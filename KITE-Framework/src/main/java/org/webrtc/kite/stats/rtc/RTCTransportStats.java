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

package org.webrtc.kite.stats.rtc;


import javax.json.JsonObjectBuilder;
import java.util.Map;

/**
 * RTCTransportStats, with attributes bytesSent, bytesReceived, rtcpTransportStatsId, selectedCan
 */
public class RTCTransportStats extends RTCSingleStatObject {

  private final String bytesSent;
  private final String bytesReceived;
  private final String selectedCandidatePairId;
  private final String localCertificateId;
  private final String remoteCertificateId;


  public RTCTransportStats(Map statObject) {
    super(statObject);
    this.bytesSent = getStatByName( "bytesSent");
    this.bytesReceived = getStatByName( "bytesReceived");
    this.selectedCandidatePairId = getStatByName( "selectedCandidatePairId");
    this.localCertificateId = getStatByName( "localCertificateId");
    this.remoteCertificateId = getStatByName( "remoteCertificateId");
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
  
  public String getSelectedCandidatePairId() {
    return selectedCandidatePairId;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("bytesSent", this.bytesSent)
        .add("bytesReceived", this.bytesReceived)
        .add("selectedCandidatePairId", this.selectedCandidatePairId)
        .add("localCertificateId", this.localCertificateId)
        .add("remoteCertificateId", this.remoteCertificateId);
  }
}

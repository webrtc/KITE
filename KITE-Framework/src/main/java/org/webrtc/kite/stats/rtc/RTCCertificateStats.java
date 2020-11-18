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
 * RTCCertificateStats, with attributes fingerprint,
 * fingerprintAlgorithm, base64Certificate, issuerCertificateId
 */
public class RTCCertificateStats extends RTCSingleStatObject {

  private final String fingerprint;
  private final String fingerprintAlgorithm;
  private final String base64Certificate;
  private final String issuerCertificateId;

  public RTCCertificateStats(Map statObject) {
    super(statObject);
    this.fingerprint = getStatByName( "fingerprint");
    this.fingerprintAlgorithm = getStatByName( "fingerprintAlgorithm");
    this.base64Certificate = getStatByName( "base64Certificate");
    this.issuerCertificateId = getStatByName( "issuerCertificateId");

  }
  
  public String getBase64Certificate() {
    return base64Certificate;
  }
  
  public String getFingerprint() {
    return fingerprint;
  }
  
  public String getFingerprintAlgorithm() {
    return fingerprintAlgorithm;
  }
  
  public String getIssuerCertificateId() {
    return issuerCertificateId;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("fingerprint", this.fingerprint)
      .add("fingerprintAlgorithm", this.fingerprintAlgorithm)
      .add("base64Certificate", this.base64Certificate)
      .add("issuerCertificateId", this.issuerCertificateId);
  }
}

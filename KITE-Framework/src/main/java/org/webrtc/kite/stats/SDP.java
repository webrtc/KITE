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
 * RTCCertificateStats, with attributes fingerprint,
 * fingerprintAlgorithm, base64Certificate, issuerCertificateId
 */
public class SDP extends RTCSingleStatObject {

  private final String sdp;
  private final String type;

  public SDP(Map statObject) {
    this.type = getStatByName(statObject, "type");
    this.sdp = getStatByName(statObject, "sdp");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("type", this.type)
      .add("sdp", this.sdp);
  }
}

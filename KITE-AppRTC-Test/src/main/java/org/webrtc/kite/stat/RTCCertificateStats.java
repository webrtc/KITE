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
 * RTCCertificateStats, with attributes fingerprint,
 * fingerprintAlgorithm, base64Certificate, issuerCertificateId
 */
public class RTCCertificateStats extends StatObject {

    private String fingerprint, fingerprintAlgorithm, base64Certificate, issuerCertificateId;

    public RTCCertificateStats(Map<Object, Object> statObject) {
        this.setId(Utility.getStatByName(statObject, "id"));
        this.fingerprint = Utility.getStatByName(statObject, "fingerprint");
        this.fingerprintAlgorithm = Utility.getStatByName(statObject, "fingerprintAlgorithm");
        this.base64Certificate = Utility.getStatByName(statObject, "base64Certificate");
        this.issuerCertificateId = Utility.getStatByName(statObject, "issuerCertificateId");

    }

    @Override
    public JsonObjectBuilder getJsonObjectBuilder() {
        JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder()
                        .add("fingerprint", this.fingerprint)
                        .add("fingerprintAlgorithm", this.fingerprintAlgorithm)
                        .add("base64Certificate", this.base64Certificate)
                        .add("issuerCertificateId", this.issuerCertificateId);
        return jsonObjectBuilder;
    }
}

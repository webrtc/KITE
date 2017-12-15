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

import org.webrtc.kite.Utility;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

/**
 * RTCIceCandidatePairStats, with attributes transportId, localCandidateId, remoteCandidateId, state, priority, nominated, bytesSent,
 * bytesReceived, totalRoundTripTime, currentRoundTripTime
 */
public class RTCIceCandidatePairStats extends StatObject {

    private String transportId, localCandidateId, remoteCandidateId, state, priority, nominated,
            bytesSent, bytesReceived, totalRoundTripTime, currentRoundTripTime;

    public RTCIceCandidatePairStats(Map<Object, Object> statObject) {
        this.setId(Utility.getStatByName(statObject, "id"));
        this.transportId = Utility.getStatByName(statObject, "transportId");
        this.localCandidateId = Utility.getStatByName(statObject, "localCandidateId");
        this.remoteCandidateId = Utility.getStatByName(statObject, "remoteCandidateId");
        this.state = Utility.getStatByName(statObject, "state");
        this.priority = Utility.getStatByName(statObject, "priority");
        this.nominated = Utility.getStatByName(statObject, "nominated");
        this.bytesSent = Utility.getStatByName(statObject, "bytesSent");
        this.bytesReceived = Utility.getStatByName(statObject, "bytesReceived");
        this.totalRoundTripTime = Utility.getStatByName(statObject, "totalRoundTripTime");
        this.currentRoundTripTime = Utility.getStatByName(statObject, "currentRoundTripTime");
    }

    @Override
    public JsonObjectBuilder getJsonObjectBuilder() {
        JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder()
                        .add("transportId", this.transportId)
                        .add("localCandidateId", this.localCandidateId)
                        .add("remoteCandidateId", this.remoteCandidateId)
                        .add("state", this.state)
                        .add("priority", this.priority)
                        .add("nominated", this.nominated)
                        .add("bytesSent", this.bytesSent)
                        .add("currentRoundTripTime", this.currentRoundTripTime)
                        .add("totalRoundTripTime", this.totalRoundTripTime)
                        .add("bytesReceived", this.bytesReceived);
        return jsonObjectBuilder;
    }
}

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
 * RTCIceCandidateStats, with attributes ip, port, protocol, candidateType, priority, url
 */
public class RTCIceCandidateStats extends StatObject {

  private String ip, port, protocol, candidateType, priority, url;

  public RTCIceCandidateStats(Map<Object, Object> statObject) {
    this.setId(Utility.getStatByName(statObject, "id"));
    this.ip = Utility.getStatByName(statObject, "ip");
    this.port = Utility.getStatByName(statObject, "port");
    this.protocol = Utility.getStatByName(statObject, "protocol");
    this.candidateType = Utility.getStatByName(statObject, "candidateType");
    this.priority = Utility.getStatByName(statObject, "priority");
    this.url = Utility.getStatByName(statObject, "url");

  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      Json.createObjectBuilder()
        .add("ip", this.ip)
        .add("port", this.port)
        .add("protocol", this.protocol)
        .add("candidateType", this.candidateType)
        .add("priority", this.priority)
        .add("url", this.url);
    return jsonObjectBuilder;
  }
}

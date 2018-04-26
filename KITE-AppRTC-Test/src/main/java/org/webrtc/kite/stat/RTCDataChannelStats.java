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
 * RTCDataChannelStats, with attributes label, protocol, datachannelId, state, messagesSent, bytesSent,
 * messagesReceived, bytesReceived
 */
public class RTCDataChannelStats extends StatObject {

  private String label, protocol, datachannelId, state, messagesSent, bytesSent, messagesReceived, bytesReceived;


  public RTCDataChannelStats(Map<Object, Object> statObject) {
    this.setId(Utility.getStatByName(statObject, "id"));
    this.label = Utility.getStatByName(statObject, "label");
    this.protocol = Utility.getStatByName(statObject, "protocol");
    this.datachannelId = Utility.getStatByName(statObject, "datachannelId");
    this.state = Utility.getStatByName(statObject, "state");
    this.messagesSent = Utility.getStatByName(statObject, "messagesSent");
    this.bytesSent = Utility.getStatByName(statObject, "bytesSent");
    this.messagesReceived = Utility.getStatByName(statObject, "messagesReceived");
    this.bytesReceived = Utility.getStatByName(statObject, "bytesReceived");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      Json.createObjectBuilder()
        .add("label", this.label)
        .add("protocol", this.protocol)
        .add("datachannelId", this.datachannelId)
        .add("state", this.state)
        .add("messagesSent", this.messagesSent)
        .add("bytesSent", this.bytesSent)
        .add("messagesReceived", this.messagesReceived)
        .add("bytesReceived", this.bytesReceived);
    return jsonObjectBuilder;
  }
}

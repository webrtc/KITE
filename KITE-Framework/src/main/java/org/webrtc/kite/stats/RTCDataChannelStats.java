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
 * RTCDataChannelStats, with attributes label, protocol, datachannelId, state, messagesSent, bytesSent,
 * messagesReceived, bytesReceived
 */
public class RTCDataChannelStats extends RTCStatObject {

  private final String label;
  private final String protocol;
  private final String datachannelId;
  private final String state;
  private final String messagesSent;
  private final String bytesSent;
  private final String messagesReceived;
  private final String bytesReceived;
  private final String timestamp;


  public RTCDataChannelStats(Map<Object, Object> statObject) {
    this.setId(getStatByName(statObject, "id"));
    this.label = getStatByName(statObject, "label");
    this.protocol = getStatByName(statObject, "protocol");
    this.datachannelId = getStatByName(statObject, "datachannelId");
    this.state = getStatByName(statObject, "state");
    this.messagesSent = getStatByName(statObject, "messagesSent");
    this.bytesSent = getStatByName(statObject, "bytesSent");
    this.messagesReceived = getStatByName(statObject, "messagesReceived");
    this.bytesReceived = getStatByName(statObject, "bytesReceived");
    this.timestamp = getStatByName(statObject, "timestamp");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("label", this.label)
      .add("protocol", this.protocol)
      .add("datachannelId", this.datachannelId)
      .add("state", this.state)
      .add("messagesSent", this.messagesSent)
      .add("bytesSent", this.bytesSent)
      .add("messagesReceived", this.messagesReceived)
      .add("bytesReceived", this.bytesReceived)
      .add("timestamp", this.timestamp);
  }
}

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
 * RTCDataChannelStats, with attributes label, protocol, dataChannelIdentifier, state, messagesSent, bytesSent,
 * messagesReceived, bytesReceived
 */
public class RTCDataChannelStats extends RTCSingleStatObject {

  private final String label;
  private final String protocol;
  private final String dataChannelIdentifier;
  private final String state;
  private final String messagesSent;
  private final String bytesSent;
  private final String messagesReceived;
  private final String bytesReceived;


  public RTCDataChannelStats(Map statObject) {
    super(statObject);
    this.label = getStatByName( "label");
    this.protocol = getStatByName( "protocol");
    this.dataChannelIdentifier = getStatByName( "dataChannelIdentifier");
    this.state = getStatByName( "state");
    this.messagesSent = getStatByName( "messagesSent");
    this.bytesSent = getStatByName( "bytesSent");
    this.messagesReceived = getStatByName( "messagesReceived");
    this.bytesReceived = getStatByName( "bytesReceived");
    this.timestamp = getStatByName( "timestamp");
  }
  
  public double getBytesReceived() {
    return parseDouble(bytesReceived);
  }
  
  public double getBytesSent() {
    return parseDouble(bytesSent);
  }
  
  public String getDataChannelIdentifier() {
    return dataChannelIdentifier;
  }
  
  public String getLabel() {
    return label;
  }
  
  public String getMessagesReceived() {
    return messagesReceived;
  }
  
  public String getMessagesSent() {
    return messagesSent;
  }
  
  public String getProtocol() {
    return protocol;
  }
  
  public String getState() {
    return state;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("label", this.label)
      .add("protocol", this.protocol)
      .add("dataChannelIdentifier", this.dataChannelIdentifier)
      .add("state", this.state)
      .add("messagesSent", this.messagesSent)
      .add("bytesSent", this.bytesSent)
      .add("messagesReceived", this.messagesReceived)
      .add("bytesReceived", this.bytesReceived);
  }
}

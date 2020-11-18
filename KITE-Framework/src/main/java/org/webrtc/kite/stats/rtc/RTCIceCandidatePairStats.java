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
 * RTCIceCandidatePairStats, with attributes transportId, localCandidateId, remoteCandidateId, state, priority, nominated, bytesSent,
 * bytesReceived, totalRoundTripTime, currentRoundTripTime
 */
public class RTCIceCandidatePairStats extends RTCSingleStatObject {

  private final String localCandidateId;
  private final String remoteCandidateId;
  private final String state;
  private final String priority;
  private final String nominated;
  private final String bytesSent;
  private final String bytesReceived;
  private final String currentRoundTripTime;
  private final String totalRoundTripTime;

  public RTCIceCandidatePairStats(Map statObject) {
    super(statObject);
    this.localCandidateId = getStatByName( "localCandidateId");
    this.remoteCandidateId = getStatByName( "remoteCandidateId");
    this.state = getStatByName( "state");
    this.priority = getStatByName( "priority");
    this.nominated = getStatByName( "nominated");
    this.bytesSent = getStatByName( "bytesSent");
    this.bytesReceived = getStatByName( "bytesReceived");
    this.totalRoundTripTime = getStatByName( "totalRoundTripTime");
    this.currentRoundTripTime = getStatByName( "currentRoundTripTime");
  }
  
  public double getBytesReceived() {
    return parseDouble(bytesReceived);
  }
  
  public double getBytesSent() {
    return parseDouble(bytesSent);
  }
  
  public double getCurrentRoundTripTime() {
    return 1000*parseDouble(currentRoundTripTime);
  }
  
  public String getLocalCandidateId() {
    return localCandidateId;
  }
  
  public String getNominated() {
    return nominated;
  }
  
  public String getPriority() {
    return priority;
  }
  
  public String getRemoteCandidateId() {
    return remoteCandidateId;
  }
  
  public String getState() {
    return state;
  }
  
  public double getTotalRoundTripTime() {
    return 1000*parseDouble(totalRoundTripTime);
  }
  

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("localCandidateId", this.localCandidateId)
      .add("remoteCandidateId", this.remoteCandidateId)
      .add("state", this.state)
      .add("priority", this.priority)
      .add("nominated", this.nominated)
      .add("bytesSent", this.bytesSent)
      .add("currentRoundTripTime", this.currentRoundTripTime)
      .add("totalRoundTripTime", this.totalRoundTripTime)
      .add("bytesReceived", this.bytesReceived);
  }
}

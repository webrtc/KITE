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

package org.webrtc.kite.stats.rtc.rtpstream;

import java.util.Map;
import javax.json.JsonObjectBuilder;

/**
 * Represent RTCRtpStreamStats, outbound and inbound, sent and received.
 */
public class RTCInboundRtpStreamStats extends RTCReceivedRtpStreamStats {
  protected final String receiverId;
  protected final String remoteId;
  protected final String nackCount;
  protected final String audioLevel;
  protected final String framesDecoded;
  protected final String framesReceived;
  protected final String framesDropped;
  protected final String partialFramesLost;
  protected final String totalAudioEnergy;
  protected final String totalSamplesDuration;




  public RTCInboundRtpStreamStats(Map statObject) {
    super(statObject);
    this.receiverId = getStatByName( "receiverId");
    this.remoteId = getStatByName( "remoteId");
    this.nackCount = getStatByName( "nackCount");
    this.audioLevel = getStatByName( "audioLevel");
    this.framesDecoded = getStatByName( "framesDecoded");
    this.framesReceived = getStatByName( "framesReceived");
    this.framesDropped = getStatByName( "framesDropped");
    this.partialFramesLost = getStatByName( "partialFramesLost");
    this.totalAudioEnergy = getStatByName( "totalAudioEnergy");
    this.totalSamplesDuration = getStatByName( "totalSamplesDuration");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
        .add("receiverId", this.receiverId)
        .add("remoteId", this.remoteId)
        .add("nackCount", this.nackCount)
        .add("audioLevel", this.audioLevel)
        .add("framesDecoded", this.framesDecoded)
        .add("framesReceived", this.framesReceived)
        .add("framesDropped", this.framesDropped)
        .add("partialFramesLost", this.partialFramesLost)
        .add("totalAudioEnergy", this.totalAudioEnergy)
        .add("totalSamplesDuration", this.totalSamplesDuration);
  }

  public String getReceiverId() {
    return receiverId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public String getNackCount() {
    return nackCount;
  }

  public Double getFramesDecoded() {
    return parseDouble(framesDecoded);
  }

  public Double getAudioLevel() {
    return parseDouble(audioLevel);
  }

  public Double getFramesReceived() {
    return parseDouble(framesReceived);
  }

  public Double getFramesDropped() {
    return parseDouble(framesDropped);
  }

  public Double getPartialFramesLost() {
    return parseDouble(partialFramesLost);
  }

  public Double getTotalAudioEnergy() {
    return parseDouble(totalAudioEnergy);
  }

  public Double getTotalSamplesDuration() {
    return parseDouble(totalSamplesDuration);
  }

}

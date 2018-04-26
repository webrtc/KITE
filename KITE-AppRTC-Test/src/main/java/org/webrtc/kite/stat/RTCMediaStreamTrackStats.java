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
 * RTCMediaStreamTrackStats, with attributes trackIdentifier, remoteSource,
 * ended, detached, frameWidth, frameHeight, framesPerSecond, framesSent, framesReceived,
 * framesDecoded, framesDropped, framesCorrupted, audioLevel
 */
public class RTCMediaStreamTrackStats extends StatObject {
  private String trackIdentifier, remoteSource, ended, detached,
    frameWidth, frameHeight, framesPerSecond, framesSent, framesReceived,
    framesDecoded, framesDropped, framesCorrupted, audioLevel;

  public RTCMediaStreamTrackStats(Map<Object, Object> statObject) {
    this.setId(Utility.getStatByName(statObject, "id"));
    this.trackIdentifier = Utility.getStatByName(statObject, "trackIdentifier");
    this.remoteSource = Utility.getStatByName(statObject, "remoteSource");
    this.ended = Utility.getStatByName(statObject, "ended");
    this.detached = Utility.getStatByName(statObject, "detached");
    this.frameWidth = Utility.getStatByName(statObject, "frameWidth");
    this.frameHeight = Utility.getStatByName(statObject, "frameHeight");
    this.framesPerSecond = Utility.getStatByName(statObject, "framesPerSecond");
    this.framesSent = Utility.getStatByName(statObject, "framesSent");
    this.framesReceived = Utility.getStatByName(statObject, "framesReceived");
    this.framesDecoded = Utility.getStatByName(statObject, "framesDecoded");
    this.framesDropped = Utility.getStatByName(statObject, "framesDropped");
    this.framesCorrupted = Utility.getStatByName(statObject, "framesCorrupted");
    this.audioLevel = Utility.getStatByName(statObject, "audioLevel");

  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      Json.createObjectBuilder()
        .add("trackIdentifier", this.trackIdentifier)
        .add("remoteSource", this.remoteSource)
        .add("ended", this.ended)
        .add("detached", this.detached)
        .add("frameWidth", this.frameWidth)
        .add("frameHeight", this.frameHeight)
        .add("framesPerSecond", this.framesPerSecond)
        .add("framesSent", this.framesSent)
        .add("framesReceived", this.framesReceived)
        .add("frameHeight", this.frameHeight)
        .add("framesDecoded", this.framesDecoded)
        .add("framesDropped", this.framesDropped)
        .add("framesCorrupted", this.framesCorrupted)
        .add("audioLevel", this.audioLevel);

    return jsonObjectBuilder;
  }
}

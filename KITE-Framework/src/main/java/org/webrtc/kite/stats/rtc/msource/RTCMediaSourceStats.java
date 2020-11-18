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

package org.webrtc.kite.stats.rtc.msource;


import javax.json.JsonObjectBuilder;
import java.util.Map;
import org.webrtc.kite.stats.rtc.RTCSingleStatObject;

/**
 * RTCMediaSourceStats, with attributes trackIdentifier, kind
 */
public class RTCMediaSourceStats extends RTCSingleStatObject {
  protected final String trackIdentifier;
  protected final String kind;

  public RTCMediaSourceStats(Map statObject) {
    super(statObject);
    this.trackIdentifier = getStatByName( "trackIdentifier");
    this.kind = getStatByName( "kind");
  }
  
  public String getTrackIdentifier() {
    return trackIdentifier;
  }
  
  public String getKind() {
    return kind;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("trackIdentifier", this.trackIdentifier)
      .add("kind", this.kind);
  }
}

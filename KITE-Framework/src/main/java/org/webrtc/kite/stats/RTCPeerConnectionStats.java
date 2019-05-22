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
 * RTCPeerConnectionStats, with attributes dataChannelsOpened, dataChannelsClosed
 */
public class RTCPeerConnectionStats extends RTCStatObject {
  private final String dataChannelsOpened;
  private final String dataChannelsClosed;

  public RTCPeerConnectionStats(Map<Object, Object> statObject) {
    this.setId(getStatByName(statObject, "id"));
    this.dataChannelsOpened = getStatByName(statObject, "dataChannelsOpened");
    this.dataChannelsClosed = getStatByName(statObject, "dataChannelsClosed");
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder()
      .add("dataChannelsOpened", this.dataChannelsOpened)
      .add("dataChannelsClosed", this.dataChannelsClosed);
  }
}

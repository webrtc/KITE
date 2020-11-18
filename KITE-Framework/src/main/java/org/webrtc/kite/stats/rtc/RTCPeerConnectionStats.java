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
 * RTCPeerConnectionStats, with attributes dataChannelsOpened, dataChannelsClosed
 */
public class RTCPeerConnectionStats extends RTCSingleStatObject {
  private final String dataChannelsClosed;
  private final String dataChannelsOpened;

  public RTCPeerConnectionStats(Map statObject) {
    super(statObject);
    this.dataChannelsOpened = getStatByName( "dataChannelsOpened");
    this.dataChannelsClosed = getStatByName( "dataChannelsClosed");
  }
  
  public String getDataChannelsClosed() {
    return dataChannelsClosed;
  }
  
  public String getDataChannelsOpened() {
    return dataChannelsOpened;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("dataChannelsOpened", this.dataChannelsOpened)
      .add("dataChannelsClosed", this.dataChannelsClosed);
  }
}

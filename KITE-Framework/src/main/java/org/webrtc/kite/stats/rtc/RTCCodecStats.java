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
 * RTCCodecStats, with attributes payloadType, codecType, clockRate, channels, sdpFmtpLine
 */
public class RTCCodecStats extends RTCSingleStatObject {

  private final String payloadType;
  private final String codecType;
  private final String mimeType;
  private final String clockRate;
  private final String channels;
  private final String sdpFmtpLine;


  public RTCCodecStats(Map statObject) {
    super(statObject);
    this.payloadType = getStatByName( "payloadType");
    this.clockRate = getStatByName( "clockRate");
    this.channels = getStatByName( "channels");
    this.codecType = getStatByName( "codecType");
    this.mimeType = getStatByName( "mimeType");
    this.sdpFmtpLine = getStatByName( "sdpFmtpLine");
  }
  
  public String getChannels() {
    return channels;
  }
  
  public String getClockRate() {
    return clockRate;
  }
  
  public String getCodecType() {
    return codecType;
  }
  
  public String getPayloadType() {
    return payloadType;
  }
  
  public String getSdpFmtpLine() {
    return sdpFmtpLine;
  }

  public String getMimeType() {
    return mimeType;
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("payloadType", this.payloadType)
      .add("clockRate", this.clockRate)
      .add("channels", this.channels)
      .add("codecType", this.codecType)
      .add("mimeType", this.mimeType)
      .add("sdpFmtpLine", this.sdpFmtpLine);
  }
}

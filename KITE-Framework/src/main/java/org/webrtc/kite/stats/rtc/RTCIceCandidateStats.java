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
 * RTCIceCandidateStats, with attributes ip, port, protocol, candidateType, address, url
 */
public class RTCIceCandidateStats extends RTCSingleStatObject {

  private final String address;
  private final String port;
  private final String protocol;
  private final String candidateType;
  private final String url;

  public RTCIceCandidateStats(Map statObject) {
    super(statObject);
    this.port = getStatByName( "port");
    this.protocol = getStatByName( "protocol");
    this.candidateType = getStatByName( "candidateType");
    this.address = getStatByName( "address");
    this.url = getStatByName( "url");

  }
  
  public String getCandidateType() {
    return candidateType;
  }
  
  public String getPort() {
    return port;
  }
  
  public String getAddress() {
    return address;
  }
  
  public String getProtocol() {
    return protocol;
  }
  
  public String getUrl() {
    return url;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder()
      .add("address", this.address)
      .add("port", this.port)
      .add("protocol", this.protocol)
      .add("candidateType", this.candidateType)
      .add("url", this.url);
  }
}

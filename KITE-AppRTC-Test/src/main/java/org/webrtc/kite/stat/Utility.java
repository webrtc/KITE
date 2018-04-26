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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class holding various static methods.
 */
public class Utility {

  /**
   * Obtain a value of a key in the data map if not null
   *
   * @param statObject data Map
   * @param statName   name of the key
   * @return true if both the provided objects are not null.
   */
  public static String getStatByName(Map<Object, Object> statObject, String statName) {
    if (statObject.get(statName) != null)
      return statObject.get(statName).toString();
    return "NA";
  }


  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param clientStats array of data sent back from test
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildClientObject(Object clientStats) {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    Map<String, Object> clientStatMap = (Map<String, Object>) clientStats;

    List<Object> clientStatArray = (ArrayList) clientStatMap.get("stats");
    JsonArrayBuilder jsonclientStatArray = Json.createArrayBuilder();
    for (Object stats : clientStatArray) {
      JsonObjectBuilder jsonStatObjectBuilder = buildSingleStatObject(stats);
      jsonclientStatArray.add(jsonStatObjectBuilder);
    }

    JsonObjectBuilder sdpObjectBuilder = Json.createObjectBuilder();
    Map<Object, Object> sdpOffer = (Map<Object, Object>) clientStatMap.get("offer");
    Map<Object, Object> sdpAnswer = (Map<Object, Object>) clientStatMap.get("answer");
    sdpObjectBuilder.add("offer", new SDP(sdpOffer).getJsonObjectBuilder())
      .add("answer", new SDP(sdpAnswer).getJsonObjectBuilder());

    jsonObjectBuilder.add("sdp", sdpObjectBuilder)
      .add("stats", jsonclientStatArray);

    return jsonObjectBuilder;
  }

  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param statArray array of data sent back from test
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildSingleStatObject(Object statArray) {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    Map<String, List<StatObject>> statObjectMap = new HashMap<>();
    if (statArray != null) {
      for (Object map : (ArrayList) statArray) {
        if (map != null) {
          Map<Object, Object> statMap = (Map<Object, Object>) map;
          String type = (String) statMap.get("type");
          StatObject statObject = null;
          switch (type) {
            case "codec": {
              statObject = new RTCCodecStats(statMap);
              break;
            }
            case "track": {
              statObject = new RTCMediaStreamTrackStats(statMap);
              break;
            }
            case "stream": {
              statObject = new RTCMediaStreamStats(statMap);
              break;
            }
            case "inbound-rtp": {
              statObject = new RTCRTPStreamStats(statMap, true);
              break;
            }
            case "outbound-rtp": {
              statObject = new RTCRTPStreamStats(statMap, false);
              break;
            }
            case "peer-connection": {
              statObject = new RTCPeerConnectionStats(statMap);
              break;
            }
            case "transport": {
              statObject = new RTCTransportStats(statMap);
              break;
            }
            case "candidate-pair": {
              statObject = new RTCIceCandidatePairStats(statMap);
              break;
            }
            case "remote-candidate": {
              statObject = new RTCIceCandidateStats(statMap);
              break;
            }
            case "local-candidate": {
              statObject = new RTCIceCandidateStats(statMap);
              break;
            }
          }
          if (statObject != null) {
            if (statObjectMap.get(type) == null) {
              statObjectMap.put(type, new ArrayList<StatObject>());
            }
            statObjectMap.get(type).add(statObject);
          }
        }
      }
    }
    if (!statObjectMap.isEmpty()) {
      for (String type : statObjectMap.keySet()) {
        JsonObjectBuilder tmp = Json.createObjectBuilder();
        for (StatObject stat : statObjectMap.get(type))
          tmp.add(stat.getId(), stat.getJsonObjectBuilder());
        jsonObjectBuilder.add(type, tmp);
      }
    }
    return jsonObjectBuilder;
  }

  /**
   * Create a JsonObject to send back to KITE
   *
   * @param resultMap result map of the test
   * @return JsonObject.
   */
  public static JsonObject developResult(Map<String, Object> resultMap, int tupleSize) {

    JsonObjectBuilder tmp = Json.createObjectBuilder();
    for (int i = 1; i <= tupleSize; i++) {
      String name = "client_" + i;
      if (resultMap.get(name) != null)
        tmp.add(name, Utility.buildClientObject(resultMap.get(name)));
    }

    return Json.createObjectBuilder().add("result", (String) resultMap.get("result"))
      .add("stats", tmp).build();
  }

}

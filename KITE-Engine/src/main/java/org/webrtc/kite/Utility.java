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

package org.webrtc.kite;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.log4j.Logger;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.stat.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class holding various static methods.
 */
public class Utility {

  /**
   * Returns stack trace of the given exception.
   *
   * @param e Exception
   * @return string representation of e.printStackTrace()
   */
  public static String getStackTrace(Exception e) {
    Writer writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  /**
   * Checks if the given key exists in the given JsonObject with a valid value.
   *
   * @param jsonObject JsonObject
   * @param key key
   * @param valueClass Class object for the value of the key.
   * @param isOptional A boolean specifying that the value may be optional. Note: This only works if the valueClass is String.
   * @return the value of the key
   * @throws KiteNoKeyException if the key is not mapped in the JsonObject.
   * @throws KiteBadValueException if the value of the key is invalid.
   */
  public static Object throwNoKeyOrBadValueException(JsonObject jsonObject, String key,
      Class<?> valueClass, boolean isOptional) throws KiteNoKeyException, KiteBadValueException {
    Object value = null;
    try {
      switch (valueClass.getSimpleName()) {
        case "String":
          value = (isOptional) ? jsonObject.getString(key, null) : jsonObject.getString(key);
          break;
        case "JsonArray":
          value = jsonObject.getJsonArray(key);
          break;
        default:
          value = jsonObject.get(key);
      }
      return value;
    } catch (NullPointerException e) {
      throw new KiteNoKeyException(key);
    } catch (ClassCastException e) {
      throw new KiteBadValueException(key);
    }
  }

  /**
   * Closes the given jdbc resources if they are not null.
   *
   * @param s Statement
   * @param rs ResultSet
   */
  public static void closeDBResources(Statement s, ResultSet rs) {
    if (s == null && rs == null) {
      // Both are null, don't do anything.
    } else if (s == null && rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
      }
    } else if (s != null && rs == null) {
      try {
        s.close();
      } catch (SQLException e) {
      }
    } else {
      try {
        rs.close();
      } catch (SQLException e) {
      }
      try {
        s.close();
      } catch (SQLException e) {
      }
    }
  }

  /**
   * Checks whether the given string is not null and is not an empty string.
   *
   * @param value string
   * @return true if the provided value is not null and is not empty.
   */
  public static boolean isNotNullAndNotEmpty(String value) {
    return value != null && !value.isEmpty();
  }

  /**
   * Prints the stack trace of the provided exception object.
   *
   * @param e Exception
   */
  public static void printStackTrace(Logger logger, Exception e) {
    logger.error(e.getStackTrace());
  }

  /**
   * Checks whether both the given objects are null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are null.
   */
  public static boolean areBothNull(Object object1, Object object2) {
    return object1 == null && object2 == null;
  }

  /**
   * Checks whether both the given objects are not null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are not null.
   */
  public static boolean areBothNotNull(Object object1, Object object2) {
    return object1 != null && object2 != null;
  }

  /**
   * Obtain a value of a key in the data map if not null
   *
   * @param statObject data Map
   * @param statName name of the key
   * @return true if both the provided objects are not null.
   */
  public static String getStatByName(Map<Object, Object> statObject, String statName){
    if(statObject.get(statName)!=null)
      return statObject.get(statName).toString();
    return "NA";
  }


  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param statArray array of data sent back from test
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildStatObject(Object statArray){
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    Map<String, List<StatObject>> statObjectMap = new HashMap<>();
    for (Object map: (ArrayList) statArray) {
      Map<Object, Object> statMap = (Map<Object, Object>) map;
      String type = (String) statMap.get("type");
      StatObject statObject = null;
      switch (type){
        case "codec":{
          statObject = new RTCCodecStats(statMap);
          break;
        }
        case "track":{
          statObject = new RTCMediaStreamTrackStats(statMap);
          break;
        }
        case "stream":{
          statObject = new RTCMediaStreamStats(statMap);
          break;
        }
        case "inbound-rtp":{
          statObject = new RTCRTPStreamStats(statMap, true);
          break;
        }
        case "outbound-rtp":{
          statObject = new RTCRTPStreamStats(statMap, false);
          break;
        }
        case "peer-connection":{
          statObject = new RTCPeerConnectionStats(statMap);
          break;
        }
        case "transport":{
          statObject = new RTCTransportStats(statMap);
          break;
        }
        case "candidate-pair":{
          statObject = new RTCIceCandidatePairStats(statMap);
          break;
        }
        case "remote-candidate":{
          statObject = new RTCIceCandidateStats(statMap);
          break;
        }
        case "local-candidate":{
          statObject = new RTCIceCandidateStats(statMap);
          break;
        }
      }
      if (statObject!=null) {
        if (statObjectMap.get(type)==null) {
          statObjectMap.put(type, new ArrayList<StatObject>());
        }
        statObjectMap.get(type).add(statObject);
      }
    }
    if (!statObjectMap.isEmpty()){
      for (String type: statObjectMap.keySet()){
        JsonObjectBuilder tmp = Json.createObjectBuilder();
        for (StatObject stat: statObjectMap.get(type))
          tmp.add(stat.getId(),stat.getJsonObjectBuilder());
        jsonObjectBuilder.add(type,tmp);
      }
    }
    return jsonObjectBuilder;
  }
}

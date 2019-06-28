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

import io.cosmosoftware.kite.entities.Timeouts;
import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.util.ReportUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.json.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.TestUtils.executeJsScript;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;


/**
 * StatsUtils is a Singleton class that collects and save KITE load testing stats into a CSV file.
 */
public class StatsUtils {

  private static final HashMap<String, StatsUtils> instance = new HashMap<String, StatsUtils>();
  private static final KiteLogger logger = KiteLogger.getLogger(StatsUtils.class.getName());
  private static final String[] candidatePairStats = {"bytesSent", "bytesReceived", "currentRoundTripTime", "totalRoundTripTime", "timestamp"};
  private static final String[] inboundStats = {"bytesReceived", "packetsReceived", "packetsLost", "jitter", "timestamp"};
  private static final String[] outboundStats = {"bytesSent", "timestamp"};
  private final String filename;

  private static Map<String, String> keyValMap = new LinkedHashMap<String, String>();

  private FileOutputStream fout = null;
  private PrintWriter pw = null;

  private int testID = 1; // start count at 1
  private boolean initialized = false;

  private StatsUtils(String prefix) {
    filename = prefix + "report_" + new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date()) + ".csv";
  }

  /**
   * @return and instance of StatsUtils
   */
  public static StatsUtils getInstance(String prefix) {
    try {
      if (!instance.containsKey(prefix)) {
        instance.put(prefix,new StatsUtils(prefix));
      }
    } catch (Exception e) {
      logger.error("\r\n" + getStackTrace(e));
    }
    return instance.get(prefix);
  }

  /**
   * Print the test statistic line.
   *
   * @param o Object object containing the test results. Either a JsonObject or any Object with a
   *     toString() method
   * @param path the file path where to save the file.
   */
  public synchronized void println(Object o, String path) {
    try {
      if (!initialized) {
        File dir = new File(path);
        if (!dir.isDirectory()) {
          dir.mkdirs();
        }
        fout = new FileOutputStream(path + filename);
        pw = new PrintWriter(fout, true);
      }
      if (o instanceof JsonObject) {
        logger.info("StatsUtils.println(JsonObject) " + o.toString());
        JsonObject jsonObject = (JsonObject) o;
        Map<String, String> map = StatsUtils.jsonToHashMap(jsonObject);
        if (!initialized) {
          pw.println(keysLine(map));
        }
        pw.println(valuesLine(map));
      } else {
        logger.info("StatsUtils.println(String) " + o.toString());
        pw.println(o.toString());
      }
    } catch (Exception e) {
      logger.error("\r\n" + getStackTrace(e));
    }
    initialized = true;
  }
  
  /**
   * Convert the JSON Object into a line of values that can be printed in the CSV file.
   *
   * @param map map of stats
   * @return line String to be printed in the CSV file
   */
  private String valuesLine(Map<String, String> map) {
    String line = "";
    int i = 0;
    for (String key : map.keySet()) {
      line += map.get(key) + (i++ < map.size() ? "," : "");
    }
    return line;
  }

  /**
   * Convert the JSON Object into a line of keys that can be printed as the header of the CSV file.
   *
   * @param map map of stats
   * @return line String to be printed in the CSV file
   */
  private String keysLine(Map<String, String> map) {
    String line = "";
    int i = 0;
    for (String key : map.keySet()) {
      line += key + (i++ < map.size() ? "," : "");
    }
    return line;
  }

  
  /** Close the printWriter object. It must be called once the test is over. */
  public void close() {
    try {
      if (pw != null) {
        logger.debug("Closing " + filename);
        pw.close();
        fout.close();
      }
    } catch (Exception e) {
      logger.error("\r\n" + getStackTrace(e));
    }
  }


  /**
   * Translate the JsonObject into a flat Map<String,String> of key - value pairs For nested
   * objects, the key becomes parentkey.key, to achieve the flat Map.
   *
   * @param json the JsonObject
   * @return Map<String key, Object: either json value or another Map<String, Object>
   * @throws JsonException if the Json format is not correct
   */
  private static Map<String, String> jsonToHashMap(JsonObject json) throws JsonException {
    Map<String, Object> retMap = new LinkedHashMap<String, Object>();
    keyValMap = new LinkedHashMap<String, String>(); // re-initialise it in case.
    StringBuilder keyBuilder = new StringBuilder("");
    if (json != JsonObject.NULL) {
      retMap = toMap(json, "");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("jsonToHashMap() dump");
      for (String key : keyValMap.keySet()) {
        logger.debug("keyList[" + key + "] = " + keyValMap.get(key));
      }
    }
    return keyValMap;
  }

  /**
   * Recursively browse the jsonObject and returns a Map<String key, Object: either json value or
   * another map
   *
   * @param object JsonObject
   * @param parent json key of the parent json node.
   * @return Map<String key, Object: either json value or another Map<String, Object>
   * @throws JsonException if the Json format is not correct
   */
  private static Map<String, Object> toMap(JsonObject object, String parent) throws JsonException {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    Iterator<String> keysItr = object.keySet().iterator();
    while (keysItr.hasNext()) {
      String key = keysItr.next();
      Object value = object.get(key);
      if (value instanceof JsonArray) {
        value = toList((JsonArray) value, key);
      } else if (value instanceof JsonObject) {
        value = toMap((JsonObject) value, key);
      } else {
        String keyFull = parent + (parent.length() > 0 ? "." : "") + key;
        keyValMap.put(keyFull, value.toString());
      }
      map.put(key, value);
    }
    return map;
  }

  /**
   * Recursively browse the jsonObject and returns a List<Object1> where Object is either a
   * List<Object> or another Map<String, Object> (see toMap)
   *
   * @param array JsonArray
   * @param parent json key of the parent json node.
   * @return List<Object1> where Object is either a List<Object> or another Map<String, Object> (see
   *     toMap)
   * @throws JsonException if the Json format is not correct
   */
  private static List<Object> toList(JsonArray array, String parent) throws JsonException {
    List<Object> list = new ArrayList<Object>();
    for (int i = 0; i < array.size(); i++) {
      Object value = array.get(i);
      parent = parent + "[" + i + "]";
      if (value instanceof JsonArray) {
        value = toList((JsonArray) value, parent);
      } else if (value instanceof JsonObject) {
        value = toMap((JsonObject) value, parent);
      }
      list.add(value);
    }
    return list;
  }

  /**
   * Build a simple JsonObject of selected stats meant to test NW Instrumentation.
   * Stats includes bitrate, packetLoss, Jitter and RTT
   *
   * @param obj stats in json object format
   * @return  a json object builder to create csv file
   */
  public static JsonObjectBuilder extractStats(JsonObject obj) {
    JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
    JsonArray jsonArray = obj.getJsonObject("stats").getJsonArray("statsArray");
    int noStats = 0;
    if (jsonArray != null) {
      noStats = jsonArray.size();
      for (int i = 0; i < noStats; i++) {
        mainBuilder.add("candidate-pair_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), candidatePairStats, "candidate-pair", ""));
        mainBuilder.add("inbound-audio_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), inboundStats, "inbound-rtp", "audio"));
        mainBuilder.add("inbound-video_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), inboundStats, "inbound-rtp", "video"));
        mainBuilder.add("outbound-audio_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), outboundStats, "outbound-rtp", "audio"));
        mainBuilder.add("outbound-video_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), outboundStats, "outbound-rtp", "video"));
      }
    } else {
      logger.error(
          "statsArray is null \r\n ---------------\r\n"
              + obj.toString()
              + "\r\n ---------------\r\n");
    }
    JsonObject result = mainBuilder.build();
    JsonObjectBuilder csvBuilder = Json.createObjectBuilder();
    csvBuilder.add("currentRoundTripTime (ms)", computeRoundTripTime(result, noStats, "current"));
    csvBuilder.add("totalRoundTripTime (ms)", computeRoundTripTime(result, noStats, "total"));
    csvBuilder.add("totalBytesReceived (Bytes)", totalBytes(result, noStats, "Received"));
    csvBuilder.add("totalBytesSent (Bytes)", totalBytes(result, noStats, "Sent"));
    csvBuilder.add("avgSentBitrate (bps)", computeBitrate(result, noStats, "Sent", "candidate-pair"));
    csvBuilder.add("avgReceivedBitrate (bps)", computeBitrate(result, noStats, "Received", "candidate-pair"));
    csvBuilder.add("inboundAudioBitrate (bps)", computeBitrate(result, noStats, "in", "audio"));
    csvBuilder.add("inboundVideoBitrate (bps)", computeBitrate(result, noStats, "in", "video"));
    csvBuilder.add("outboundAudioBitrate (bps)", computeBitrate(result, noStats, "out", "audio"));
    csvBuilder.add("outboundVideoBitrate (bps)", computeBitrate(result, noStats, "out", "video"));
    csvBuilder.add("audioJitter (ms)", computeAudioJitter(result, noStats));
    csvBuilder.add("audioPacketsLoss (%)", computePacketsLoss(result, noStats, "audio"));
    csvBuilder.add("videoPacketsLoss (%)", computePacketsLoss(result, noStats, "video"));
    //uncomment the following line to add the detailed stats to the CSV
//    csvBuilder.add("stats", result);
    return csvBuilder;
  }

  /**
   *  Checks if a String is a double
   *
   * @param s the String to check
   * @return true if the String is a double
   */
  private static boolean isDouble(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   *  Checks if a String is a long
   *
   * @param s the String to check
   * @return true if the String is a long
   */
  private static boolean isLong(String s) {
    try {
      Long.parseLong(s);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param clientStats array of data sent back from test
   *
   * @return JsonObjectBuilder.
   */
  private static JsonObject buildClientStatObject(Map<String, Object> clientStats) {
    return buildClientStatObject(clientStats, null);
  }
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param clientStats   array of data sent back from test
   * @param selectedStats list of selected stats
   * @return JsonObjectBuilder.
   */
  private static JsonObject buildClientStatObject(Map<String, Object> clientStats, JsonArray selectedStats) {
    try {
      JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
      Map<String, Object> clientStatMap = clientStats;
      
      List<Object> clientStatArray = (List) clientStatMap.get("stats");
      JsonArrayBuilder jsonClientStatArray = Json.createArrayBuilder();
      for (Object stats : clientStatArray) {
        JsonObjectBuilder jsonStatObjectBuilder = buildSingleStatObject(stats, selectedStats);
        jsonClientStatArray.add(jsonStatObjectBuilder);
      }
      if (selectedStats == null) {
        //only add SDP offer stuff if selectedStats is null
        JsonObjectBuilder sdpObjectBuilder = Json.createObjectBuilder();
        Map sdpOffer = (Map) clientStatMap.get("offer");
        Map sdpAnswer = (Map) clientStatMap.get("answer");
        sdpObjectBuilder.add("offer", new SDP(sdpOffer).getJsonObjectBuilder())
          .add("answer", new SDP(sdpAnswer).getJsonObjectBuilder());
        jsonObjectBuilder.add("sdp", sdpObjectBuilder);
      }
      jsonObjectBuilder.add("statsArray", jsonClientStatArray);
      return jsonObjectBuilder.build();
    } catch (ClassCastException e) {
      e.printStackTrace();
      return Json.createObjectBuilder().build();
    }
  }
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param statArray array of data sent back from test
   *
   * @return JsonObjectBuilder.
   */
  private static JsonObjectBuilder buildSingleStatObject(Object statArray) {
    return buildSingleStatObject(statArray, null);
  }
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param statArray     array of data sent back from test
   * @param selectedStats list of selected stats
   *
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildSingleStatObject(Object statArray, JsonArray selectedStats) {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    Map<String, List<RTCStatObject>> statObjectMap = new HashMap<>();
    if (statArray != null) {
      for (Object map : (ArrayList) statArray) {
        if (map != null) {
          Map statMap = (Map) map;
          String type = (String) statMap.get("type");
          if (selectedStats == null || selectedStats.size() == 0 || selectedStats.toString().contains(type)) {
            RTCStatObject statObject = null;
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
                statObjectMap.put(type, new ArrayList<RTCStatObject>());
              }
              statObjectMap.get(type).add(statObject);
            }
          }
        }
      }
    }
    if (!statObjectMap.isEmpty()) {
      for (String type : statObjectMap.keySet()) {
//        JsonArrayBuilder tmp = Json.createArrayBuilder();
        JsonObjectBuilder tmp = Json.createObjectBuilder();
        for (RTCStatObject stat : statObjectMap.get(type)) {
          tmp.add(stat.getId(), stat.getJsonObjectBuilder());
//          tmp.add(/*stat.getId(),*/ stat.getJsonObjectBuilder());
        }
        jsonObjectBuilder.add(type, tmp);
      }
    }
    return jsonObjectBuilder;
  }
  
  /**
   * Computes the average audioJitter
   *
   * @param statsArray object containing the list getStats result.
   * @param noStats    how many stats in jsonObject
   *
   * @return the average "audioJitter"
   * @throws KiteTestException the kite test exception
   */
  public static double computeAudioJitter(JsonArray statsArray, int noStats) throws KiteTestException {
    double jitter = 0;
    double ct = 0;
    
    if (noStats < 2) {
      throw new KiteTestException("Not enough stats to compute bitrate", Status.BROKEN);
    }
    try {
      for (int index = 0; index < noStats; index++) {
        JsonObject singleStatObject = statsArray.getJsonObject(index);
        if (singleStatObject.keySet().contains("inbound-rtp")) {
          singleStatObject = getRTCStats(singleStatObject, "inbound-rtp", "audio");
          if (singleStatObject != null) {
            String s = singleStatObject.getString("jitter");
            if (s != null && !"NA".equals(s) && isDouble(s)) {
              jitter += (1000 * Double.parseDouble(s));
              ct++;
            }
          }
        }
      }
      if (ct > 0) {
        return jitter / (1000 * ct);
      }
    } catch (Exception e) {
      throw new KiteTestException(e.getClass().getName() + " while computing audio jitter" + e.getLocalizedMessage(), Status.BROKEN);
    }
    return -1;
  }
  
  /**
   * Computes the average bitrate.
   *
   * @param statsArray object containing the list getStats result.
   * @param noStats    how many stats in jsonObject
   * @param direction  "in" or "out" or "Sent" or "Received"
   * @param mediaType  "audio", "video" or "candidate-pair"
   *
   * @return totalNumberBytes sent or received
   * @throws KiteTestException the kite test exception
   */
  public static double computeBitrate(JsonArray statsArray, int noStats, String direction, String mediaType) throws KiteTestException {
    long bytesStart = 0;
    long bytesEnd = 0;
    long tsStart = 0;
    long tsEnd = 0;
    long avgBitrate = 0;
    if (noStats < 2) {
      throw new KiteTestException("Not enough stats to compute bitrate", Status.FAILED);
    }
    try {
      String jsonObjName = getJsonObjectName(direction, mediaType);
      String jsonKey = getJsonKey(direction);
      for (int index = 0; index < noStats; index++) {
        JsonObject singleStatObject = statsArray.getJsonObject(index);
        if (singleStatObject != null) {
          if (mediaType.equalsIgnoreCase("candidate-pair")) {
            singleStatObject = getSuccessfulCandidate(singleStatObject);
          } else {
            singleStatObject = getRTCStats(singleStatObject, jsonObjName, mediaType);
          }
          if (singleStatObject != null) {
            String s = singleStatObject.getString(jsonKey);
            if (s != null && !"NA".equals(s) && isLong(s)) {
              long b = Long.parseLong(s);
              bytesStart = (bytesStart == 0 || b < bytesStart) ? b : bytesStart;
              bytesEnd = (bytesEnd == 0 || b > bytesEnd) ? b : bytesEnd;
            }
            String ts = singleStatObject.getString("timestamp");
            if (ts != null && !"NA".equals(ts) && isLong(ts)) {
              long b = Long.parseLong(ts);
              if (index == 0) {
                tsStart = b;
              }
              if (index == noStats - 1) {
                tsEnd = b;
              }
            }
          }
        }
      }
      if (tsEnd != tsStart) {
        long timediff = (tsEnd - tsStart);
        avgBitrate = (8000 * (bytesEnd - bytesStart)) / timediff;
        avgBitrate = (avgBitrate < 0) ? avgBitrate * -1 : avgBitrate;
      }
    } catch (NullPointerException npe) {
      throw new KiteTestException("NullPointerException while computing bitrate " + npe.getMessage(), Status.BROKEN);
    }
    return avgBitrate;
  }
  
  /**
   * Computes the packet losses as a % packetLost/total packets
   *
   * @param statsArray object containing the list getStats result.
   * @param noStats    how many stats in jsonObject
   * @param mediaType  "audio" or "video"
   *
   * @return the packet losses (% packetLost/total packets)
   * @throws KiteTestException the kite test exception
   */
  public static double computePacketsLoss(JsonArray statsArray, int noStats, String mediaType) throws KiteTestException {
    if (noStats < 1) {
      throw new KiteTestException("Not enough stats to compute packetLoss", Status.BROKEN);
    }
    try {
      JsonObject myObject = statsArray.getJsonObject(noStats - 1);
      myObject = getRTCStats(myObject, "inbound-rtp", mediaType);
      if (myObject != null) {
        String s = myObject.getString("packetsReceived");
        String l = myObject.getString("packetsLost");
        if (s != null && !"NA".equals(s) && isLong(s)
          && l != null && !"NA".equals(l) && isLong(l)) {
          long packetsLost = Long.parseLong(l);
          long totalPackets = Long.parseLong(s) + packetsLost;
          if (totalPackets > 0) {
            return packetsLost / totalPackets;
          }
        }
      }
    } catch (Exception e) {
      throw new KiteTestException(e.getClass().getName() + " while computing packet loss" + e.getLocalizedMessage(), Status.BROKEN);
    }
    return -1;
  }
  
  /**
   * Computes the average roundTripTime
   *
   * @param statsArray object containing the list getStats result.
   * @param noStats    how many stats in jsonObject
   * @param prefix     the prefix
   *
   * @return the average of valid (> 0) "totalRoundTripTime"
   * @throws KiteTestException the kite test exception
   */
  public static double computeRoundTripTime(JsonArray statsArray, int noStats, String prefix) throws KiteTestException {
    double rtt = 0;
    int ct = 0;
    try {
      for (int index = 0; index < noStats; index++) {
        JsonObject succeededCandidatePair = getSuccessfulCandidate(statsArray.getJsonObject(index));
        if (succeededCandidatePair != null) {
          String s = succeededCandidatePair.getString(prefix + "RoundTripTime");
          if (s != null && !"NA".equals(s) && !"0".equals(s) && isDouble(s)) {
            rtt += 1000 * Double.parseDouble(s);
            ct++;
          }
        }
      }
    } catch (NullPointerException npe) {
      throw new KiteTestException("Unable to find RoundTripTime in the stats " + npe.getLocalizedMessage(), Status.BROKEN);
    }
    if (ct > 0) {
      return rtt / ct;
    }
    return -1;
  }
  
  /**
   * format 1.536834943435905E12 (nano seconds) to 1536834943435 (ms)
   * and convert timestamp to milliseconds
   *
   * @param s raw String obtained from getStats.
   *
   * @return the formatted timestamp
   */
  public static String formatTimestamp(String s) {
    String str = s;
    if (str.contains("E")) {
      //format 1.536834943435905E12 to 1536834943435905
      str = "1" + str.substring(str.indexOf(".") + 1, str.indexOf("E"));
    }
    if (str.length() > 13) {
      // convert timestamps to millisecond (obtained in nano seconds)
      str = str.substring(0, 13);
    }
    return str;
  }
  
  /**
   *
   * @param direction "in" or "out" or "Sent" or "Received"
   * @param mediaType "audio", "video" or "candidate-pair"
   * @return "candidate-pair_" or "inbound-audio_" or "inbound-video_" or "outbound-audio_" or "outbound-video_"
   */
  private static String getJsonObjectName(String direction, String mediaType) {
    if ("candidate-pair".equals(mediaType)) {
      return "candidate-pair_";
    }
    //else  "inbound-audio_"
    return direction + "bound-" + mediaType + "_";
  }
  
  /**
   *
   * @param direction "in" or "out" or "Sent" or "Received"
   * @return bytesSent or bytesReceived
   */
  private static String getJsonKey(String direction) {
    if ("Sent".equals(direction) || "out".equals(direction)) {
      return "bytesSent";
    }
    if ("Received".equals(direction) || "in".equals(direction)) {
      return "bytesReceived";
    }
    return null;
  }
  
  /**
   * Stashes stats into a global variable and collects them 1s after
   *
   * @param webDriver      used to execute command.
   * @param peerConnection the peer connection
   *
   * @return String. pc stat once
   * @throws KiteTestException the KITE test exception
   */
  public static Object getPCStatOnce(WebDriver webDriver, String peerConnection) throws KiteTestException {
    String stashStatsScript = "const getStatsValues = () =>" +
      peerConnection + "  .getStats()" +
      "    .then(data => {" +
      "      return [...data.values()];" +
      "    });" +
      "const stashStats = async () => {" +
      "  window.KITEStats = await getStatsValues();" +
      "  return 0;" +
      "};" +
      "stashStats();";
    String getStashedStatsScript = "return window.KITEStats;";
    
    executeJsScript(webDriver, stashStatsScript);
    waitAround(Timeouts.ONE_SECOND_INTERVAL);
    return executeJsScript(webDriver, getStashedStatsScript);
  }

  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver              used to execute command.
   * @param getStatsConfig         the getStatsConfig
   *
   * @return JsonObjectBuilder of the stat object
   * @throws KiteTestException the kite test exception
   */
  public static List<JsonObject> getPCStatOvertime(WebDriver webDriver, JsonObject getStatsConfig)
    throws KiteTestException {
    ArrayList<JsonObject> result = new ArrayList<>();
    for (JsonString pc : getStatsConfig.getJsonArray("peerConnections").getValuesAs(JsonString.class)) {
      result.add(getPCStatOvertime(
        webDriver, pc.getString(),
        getStatsConfig.getInt("statsCollectionTime"),
        getStatsConfig.getInt("statsCollectionInterval"),
        getStatsConfig.getJsonArray("selectedStats")));
    }
    return result;
  }
  
  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver              used to execute command.
   * @param peerConnection         the peer connection
   * @param durationInMilliSeconds during which the stats will be collected.
   * @param intervalInMilliSeconds between each time getStats gets called.
   * @param selectedStats          list of selected stats.
   *
   * @return JsonObjectBuilder of the stat object
   * @throws KiteTestException the kite test exception
   */
  public static JsonObject getPCStatOvertime(WebDriver webDriver, String peerConnection, int durationInMilliSeconds, int intervalInMilliSeconds, JsonArray selectedStats)
    throws KiteTestException {
    Map<String, Object> statMap = new HashMap<String, Object>();
    for (int timer = 0; timer < durationInMilliSeconds; timer += intervalInMilliSeconds) {
      // No sleep needed since already sleep in getPCStatOnce
      Object stats = getPCStatOnce(webDriver, peerConnection);
      if (timer == 0) {
        statMap.put("stats", new ArrayList<>());
        
        Object offer = getSDPMessage(webDriver, peerConnection, "offer");
        Object answer = getSDPMessage(webDriver, peerConnection, "answer");
        statMap.put("offer", offer);
        statMap.put("answer", answer);
      }
      List tmp = (List) statMap.get("stats");
      tmp.add(stats);
    }
    return buildClientStatObject(statMap, selectedStats);
  }
  
  /**
   * Gets the successful
   *
   * @param jsonObject of the stats
   *
   * @return RTC stats in json format
   */
  private static JsonObject getRTCStats(JsonObject jsonObject, String stats, String mediaType) {
    JsonObject myObj = jsonObject.getJsonObject(stats);
    if (myObj != null) {
      for (String key : myObj.keySet()) {
        JsonObject o = myObj.getJsonObject(key);
        if (mediaType.equals(o.getString("mediaType"))) {
          return o;
        }
      }
    }
    return null;
  }
  
  /**
   * Execute and return the requested SDP message
   *
   * @param webDriver      used to execute command.
   * @param peerConnection the peer connection
   * @param type           offer or answer.
   *
   * @return SDP object.
   * @throws KiteTestException the kite test exception
   */
  public static Object getSDPMessage(WebDriver webDriver, String peerConnection, String type) throws KiteTestException {
    return ((JavascriptExecutor) webDriver).executeScript(getSDPMessageScript(peerConnection, type));
  }
  
  /**
   * Returns the test's getSDPMessageScript to retrieve the sdp message for either the offer or answer.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getSDPMessageScript as string.
   */
  private static String getSDPMessageScript(String peerConnection, String type) throws KiteTestException {
    switch (type) {
      case "offer":
        return "var SDP;"
          + "try {SDP = " + peerConnection + ".remoteDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      case "answer":
        return "var SDP;"
          + "try {SDP = " + peerConnection + ".localDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      default:
        throw new KiteTestException("Not a valid type for sdp message.", Status.BROKEN);
    }
  }
  
  /**
   * Gets the successful candidate pair (state = succeed)
   *
   * @param jsonObject of the successful candidate pair
   *
   * @return  successful candidate info from the stat object
   */
  private static JsonObject getSuccessfulCandidate(JsonObject jsonObject) {
    JsonObject candObj = jsonObject.getJsonObject("candidate-pair");
    if (candObj == null) {
      return null;
    }
    for (String key : candObj.keySet()) {
      JsonObject o = candObj.getJsonObject(key);
      if ("succeeded".equals(o.getString("state"))) {
        //sometimes there are multiple successful candidates but only one carries non zero stats
        if(!o.get("bytesReceived").equals("0") || !o.get("bytesSent").equals("0")) return o;
        else continue;
      }
    }
    for (String key : candObj.keySet()) {
      //sometimes there are no "succeeded" pair, but the "in-progress" with
      //a valid currentRoundTripTime value looks just fine.
      JsonObject o = candObj.getJsonObject(key);
      if ("in-progress".equals(o.getString("state")) &&
        !"NA".equals(o.getString("currentRoundTripTime"))) {
        return o;
      }
    }
    return null;
  }
  
  /**
   * Build a simple JsonObject of selected stats meant to test NW Instrumentation. * Stats
   * includes bitrate, packetLoss, Jitter and RTT
   *
   * @param senderStats the sender's PC stats
   * @param receiverStats the list of receiver PCs stats
   * @return  the stat object in json format
   */
  public static JsonObject extractStats(JsonObject senderStats, List<JsonObject> receiverStats) {
    JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
    mainBuilder.add("localPC", extractStats(senderStats, "out"));
    int i = 0;
    for (JsonObject recvStats : receiverStats) {
      mainBuilder.add("remotePC[" + i++ + "]", extractStats(recvStats, "in"));
    }
    return mainBuilder.build();
  }
  
  /**
   * Build a simple JsonObject of selected stats meant to test NW Instrumentation.
   * Stats includes bitrate, packetLoss, Jitter and RTT
   *
   * @param obj the object format
   * @param direction sent or receive
   * @return the inbound or outbound stat object in json format
   */
  public static JsonObjectBuilder extractStats(JsonObject obj, String direction) {
    JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
    JsonArray jsonArray = obj.getJsonArray("statsArray");
    int noStats = 0;
    if (jsonArray != null) {
      noStats = jsonArray.size();
      for (int i = 0; i < noStats; i++) {
        mainBuilder.add("candidate-pair_" + i, getStatsJsonBuilder(jsonArray.getJsonObject(i), candidatePairStats, "candidate-pair", ""));
        if ("both".equalsIgnoreCase(direction) || "in".equalsIgnoreCase(direction)) {
          mainBuilder.add(
            "inbound-audio_" + i,
            getStatsJsonBuilder(
              jsonArray.getJsonObject(i), inboundStats, "inbound-rtp", "audio"));
          mainBuilder.add(
            "inbound-video_" + i,
            getStatsJsonBuilder(
              jsonArray.getJsonObject(i), inboundStats, "inbound-rtp", "video"));
        }
        if ("both".equalsIgnoreCase(direction) || "out".equalsIgnoreCase(direction)) {
          mainBuilder.add(
            "outbound-audio_" + i,
            getStatsJsonBuilder(
              jsonArray.getJsonObject(i), outboundStats, "outbound-rtp", "audio"));
          mainBuilder.add(
            "outbound-video_" + i,
            getStatsJsonBuilder(
              jsonArray.getJsonObject(i), outboundStats, "outbound-rtp", "video"));
        }
      }
    } else {
      logger.error(
        "statsArray is null \r\n ---------------\r\n"
          + obj.toString()
          + "\r\n ---------------\r\n");
    }
    JsonObject result = mainBuilder.build();
    JsonObjectBuilder csvBuilder = Json.createObjectBuilder();
    csvBuilder.add("currentRoundTripTime (ms)", computeRoundTripTime(result, noStats, "current"));
//    csvBuilder.add("totalRoundTripTime (ms)", computeRoundTripTime(result, noStats, "total"));
    if ("both".equalsIgnoreCase(direction) || "in".equalsIgnoreCase(direction)) {
      csvBuilder.add("totalBytesReceived (Bytes)", totalBytes(result, noStats, "Received"));
      csvBuilder.add("avgReceivedBitrate (bps)", computeBitrate(result, noStats, "Received", "candidate-pair"));
      csvBuilder.add("inboundAudioBitrate (bps)", computeBitrate(result, noStats, "in", "audio"));
      csvBuilder.add("inboundVideoBitrate (bps)", computeBitrate(result, noStats, "in", "video"));
    }
    if ("both".equalsIgnoreCase(direction) || "out".equalsIgnoreCase(direction)) {
      csvBuilder.add("totalBytesSent (Bytes)", totalBytes(result, noStats, "Sent"));
      csvBuilder.add("avgSentBitrate (bps)", computeBitrate(result, noStats, "Sent", "candidate-pair"));
      csvBuilder.add("outboundAudioBitrate (bps)", computeBitrate(result, noStats, "out", "audio"));
      csvBuilder.add("outboundVideoBitrate (bps)", computeBitrate(result, noStats, "out", "video"));
    }
    if ("both".equalsIgnoreCase(direction) || "in".equalsIgnoreCase(direction)) {
      csvBuilder.add("audioJitter (ms)", computeAudioJitter(result, noStats));
      csvBuilder.add("audioPacketsLoss (%)", computePacketsLoss(result, noStats, "audio"));
      csvBuilder.add("videoPacketsLoss (%)", computePacketsLoss(result, noStats, "video"));
    }
    //uncomment the following line to add the detailed stats to the CSV
//    csvBuilder.add("stats", result);
    return csvBuilder;
  }




  /**
   * Build a simple LinkedHashMap of selected stats meant to test NW Instrumentation. * Stats
   * includes bitrate, packetLoss, Jitter and RTT
   *
   * @param statsSummary the stats Summary JsonObbject
   * @return  the stat object in LinkedHashMap format
   */
  public static LinkedHashMap<String, String> statsHashMap(JsonObject statsSummary, int noRemotePCs) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
//    {
//      "localPC": {
//      "currentRoundTripTime (ms)": "",
//        "totalRoundTripTime (ms)": "",
//        "totalBytesReceived (Bytes)": "6697",
//        "totalBytesSent (Bytes)": "970776",
//        "avgSentBitrate (bps)": "363609",
//        "avgReceivedBitrate (bps)": "690",
//        "outboundAudioBitrate (bps)": "40930",
//        "outboundVideoBitrate (bps)": "314619"
//    },
//      "remotePC[0]": {
//        "currentRoundTripTime (ms)": "",
//          "totalRoundTripTime (ms)": "",
//          "totalBytesReceived (Bytes)": "777674",
//          "totalBytesSent (Bytes)": "21441",
//          "avgSentBitrate (bps)": "9636",
//          "avgReceivedBitrate (bps)": "323129",
//          "inboundAudioBitrate (bps)": "34108",
//          "inboundVideoBitrate (bps)": "276660",
//          "audioJitter (ms)": "6.0",
//          "audioPacketsLoss (%)": "0.027",
//          "videoPacketsLoss (%)": "0.074"
//      }
//    }

    JsonObject localPC = statsSummary.getJsonObject("localPC");
    map.put("currentRoundTripTime (ms)", localPC.getString("currentRoundTripTime (ms)"));
//    map.put("totalRoundTripTime (ms)", localPC.getString("totalRoundTripTime (ms)"));
//    map.put("totalBytesReceived (Bytes)", localPC.getString("totalBytesReceived (Bytes)"));
    map.put("totalBytesSent (Bytes)", localPC.getString("totalBytesSent (Bytes)"));
    map.put("avgSentBitrate (bps)", localPC.getString("avgSentBitrate (bps)"));
    map.put("outboundAudioBitrate (bps)", localPC.getString("outboundAudioBitrate (bps)"));
    map.put("outboundVideoBitrate (bps)", localPC.getString("outboundVideoBitrate (bps)"));
    for (int i = 0; i < noRemotePCs; i++) {
      JsonObject remotePC = statsSummary.getJsonObject("remotePC[" + i + "]");
      map.put("currentRoundTripTime (ms) [" + i + "]", remotePC.getString("currentRoundTripTime (ms)"));
      map.put("totalBytesReceived (Bytes) [" + i + "]", remotePC.getString("totalBytesReceived (Bytes)"));
      map.put("avgReceivedBitrate (bps) [" + i + "]", remotePC.getString("avgReceivedBitrate (bps)"));
      map.put("inboundVideoBitrate (bps) [" + i + "]", remotePC.getString("inboundVideoBitrate (bps)"));
      map.put("inboundAudioBitrate (bps) [" + i + "]", remotePC.getString("inboundAudioBitrate (bps)"));
      map.put("audioJitter (ms) [" + i + "]", remotePC.getString("audioJitter (ms)"));
      map.put("audioPacketsLoss (%) [" + i + "]", remotePC.getString("audioPacketsLoss (%)"));
      map.put("videoPacketsLoss (%) [" + i + "]", remotePC.getString("videoPacketsLoss (%)"));
    }
    return map;
  }
  
  /**
   * Computes the average bitrate.
   *
   * @param jsonObject object containing the list getStats result.
   * @param noStats how many stats in jsonObject
   * @param direction "in" or "out" or "Sent" or "Received"
   * @param mediaType "audio", "video" or "candidate-pair"
   * @return totalNumberBytes sent or received
   */
  private static String computeBitrate(JsonObject jsonObject, int noStats, String direction, String mediaType) {
    long bytesStart = 0;
    long bytesEnd = 0;
    long tsStart = 0;
    long tsEnd = 0;
    long avgBitrate = 0;
    try {
      if (noStats < 2) {
        return "Error: less than 2 stats";
      }
      String jsonObjName = getJsonObjectName(direction, mediaType);
      String jsonKey = getJsonKey(direction);
      boolean debug = false;
      if (debug) {
        logger.info(" jsonObject:     " + jsonObject );
        logger.info("-----------------------------");
        logger.info(" jsonKey:     " + jsonKey );
      }
      for (int i = 0; i < noStats; i++) {
        if (debug) {
          logger.info("jsonObjName: " + jsonObjName + i);
        }
        String s = jsonObject.getJsonObject(jsonObjName + i).getString(jsonKey);
        if (s != null && !"NA".equals(s) && isLong(s)) {
          long b = Long.parseLong(s);
          bytesStart = (bytesStart == 0 || b < bytesStart) ? b : bytesStart;
          bytesEnd = (bytesEnd == 0 || b > bytesEnd) ? b : bytesEnd;
        }
        String ts = jsonObject.getJsonObject(jsonObjName + i).getString("timestamp");
        if (ts != null && !"NA".equals(ts) && isLong(ts)) {
          long b = Long.parseLong(ts);
          if (i == 0) {
            tsStart = b;
          }
          if (i == noStats - 1) {
            tsEnd = b;
          }
        }
        if (debug) {
          logger.info("jsonKey:     " + jsonKey);
          logger.info("bytesEnd:   " + bytesEnd);
          logger.info("bytesStart: " + bytesStart);
          logger.info("tsEnd:   " + tsEnd);
          logger.info("tsStart: " + tsStart);
        }
      }
      
      if (tsEnd != tsStart) {
        long timediff = (tsEnd - tsStart);
        avgBitrate = (8000 * (bytesEnd - bytesStart)) / timediff;
        avgBitrate = (avgBitrate < 0) ? avgBitrate * -1 : avgBitrate;
        if (debug) {
          logger.info(
            "computeBitrate()(8000 * ( " + bytesEnd + " - " + bytesStart + " )) /" + timediff);
        }
        return "" + (avgBitrate);
      } else {
        logger.error("computeBitrate() tsEnd == tsStart : " + tsEnd + " , " + tsStart);
      }
    } catch (NullPointerException npe) {
      logger.error("NullPointerException in computeBitrate");
      logger.error("" + ReportUtils.getStackTrace(npe));
    }
    return "";
  }
  
  /**
   *  Computes the average roundTripTime
   *
   * @param jsonObject object containing the list getStats result.
   * @param noStats how many stats in jsonObject
   * @return the average of valid (> 0) "totalRoundTripTime"
   */
  private static String computeRoundTripTime(JsonObject jsonObject, int noStats, String prefix) {
    double rtt = 0;
    int ct = 0;
    try {
      for (int i = 0; i < noStats; i++) {
        String s = jsonObject.getJsonObject("candidate-pair_" + i).getString(prefix + "RoundTripTime");
        if (s != null && !"NA".equals(s) && !"0".equals(s) && isDouble(s)) {
          rtt += 1000 * Double.parseDouble(s);
          ct++;
        }
      }
    } catch (NullPointerException npe) {
      logger.error("Unable to find " + prefix + "RoundTripTime in the stats. ");
      logger.error("" + ReportUtils.getStackTrace(npe));
    }
    if (ct > 0) {
      return "" + ((int)rtt/ct);
    }
    return "";
  }
  
  /**
   *  Computes the average audioJitter
   *
   * @param jsonObject object containing the list getStats result.
   * @param noStats how many stats in jsonObject
   * @return the average "audioJitter"
   */
  private static String computeAudioJitter(JsonObject jsonObject, int noStats) {
    double jitter = 0;
    int ct = 0;
    if (noStats < 2) return ""; //min two stats
    try {
      for (int i = 0; i < noStats; i++) {
        JsonObject myObject = jsonObject.getJsonObject("inbound-audio_" + i);
        if (myObject != null) {
          String s = myObject.getString("jitter");
          if (s != null && !"NA".equals(s) && isDouble(s)) {
            jitter += (1000 * Double.parseDouble(s));
            ct++;
          }
        }
      }
      if (ct > 0) {
        return "" + (jitter/ct);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Computes the packet losses as a % packetLost/total packets
   *
   * @param jsonObject object containing the list getStats result.
   * @param noStats how many stats in jsonObject
   * @param mediaType "audio" or "video"
   * @return the packet losses (% packetLost/total packets)
   */
  private static String computePacketsLoss(JsonObject jsonObject, int noStats, String mediaType) {
    if (noStats < 1) return ""; // min one stats
    try {
      JsonObject myObject = jsonObject.getJsonObject("inbound-" + mediaType + "_" + (noStats - 1));
      if (myObject != null) {
        String s = myObject.getString("packetsReceived");
        String l = myObject.getString("packetsLost");
        if (s != null && !"NA".equals(s) && isLong(s)
          && l != null && !"NA".equals(l) && isLong(l)) {
          long packetsLost = Long.parseLong(l);
          long totalPackets = Long.parseLong(s) + packetsLost;
          if (totalPackets > 0) {
            double packetLoss = (packetsLost * 1000) / totalPackets;
            return "" + (new DecimalFormat("#0.000").format(packetLoss/1000));
          }
        } else {
          logger.error(
            "computePacketsLoss  \r\n ---------------\r\n"
              + myObject.toString()
              + "\r\n ---------------\r\n");
        }
      } else {
        logger.error(
          "computePacketsLoss  my object is null " + ("inbound-" + mediaType + "_" + (noStats - 1)));
        
      }
    } catch (Exception e) {
      logger.error("" + ReportUtils.getStackTrace(e));
    }
    return "";
  }
  
  /**
   * Computes the total bytes sent or received by the candidate
   *
   * @param jsonObject object containing the list getStats result.
   * @param noStats how many stats in jsonObject
   * @param direction Sent or Received
   * @return totalNumberBytes sent or received
   */
  private static String totalBytes(JsonObject jsonObject, int noStats, String direction) {
    long bytes = 0;
    try {
      for (int i = 0; i < noStats; i++) {
        String s = jsonObject.getJsonObject("candidate-pair_" + i).getString("bytes" + direction);
        if (s != null && !"NA".equals(s) && isLong(s)) {
          long b = Long.parseLong(s);
          bytes = Math.max(b, bytes);
        }
      }
    } catch (NullPointerException npe) {
      logger.error("Unable to find \"bytes" + direction + "\" in the stats. ");
      logger.error("" + ReportUtils.getStackTrace(npe));
    }
    return "" + bytes;
  }
  
  private static JsonObjectBuilder getStatsJsonBuilder(JsonObject jsonObject, String[] stringArray, String stats, String mediaType) {
    JsonObjectBuilder subBuilder = Json.createObjectBuilder();
    if ("candidate-pair".equals(stats)) {
      JsonObject successfulCandidate = getSuccessfulCandidate(jsonObject);
      if (successfulCandidate != null) {
        for (int j = 0; j < stringArray.length; j++) {
          if (successfulCandidate.containsKey(stringArray[j])) {
            subBuilder.add(stringArray[j], successfulCandidate.getString(stringArray[j]));
          }
        }
      }
    } else {
      JsonObject myObj = getRTCStats(jsonObject, stats, mediaType);
      if (myObj != null) {
        for (int j = 0; j < stringArray.length; j++) {
          if (myObj.containsKey(stringArray[j])) {
            subBuilder.add(stringArray[j], myObj.getString(stringArray[j]));
          }
        }
      }
    }
    return subBuilder;
  }



  /**
   * stat JsonObjectBuilder to add to callback result.
   *
   * @param webDriver used to execute command.
   * @param duration during which the stats will be collected.
   * @param interval between each time getStats gets called.
   * @return JsonObjectBuilder of the stat object
   * @throws Exception
   */
  public static JsonObjectBuilder getStatOvertime(
    WebDriver webDriver, int duration, int interval, JsonArray selectedStats) throws KiteTestException {
    Map<String, Object> statMap = new HashMap<>();
    for (int timer = 0; timer < duration; timer += interval) {
      waitAround(interval);
      Object stats = getStatOnce(webDriver);
      if (timer == 0) {
        statMap.put("stats", new ArrayList<>());
        Object offer = executeJsScript(webDriver, getSDPMessageScript("appController.call_.pcClient_.pc_", "offer"));
        Object answer = executeJsScript(webDriver, getSDPMessageScript("appController.call_.pcClient_.pc_", "answer"));
        statMap.put("offer", offer);
        statMap.put("answer", answer);
      }
      ((List<Object>) statMap.get("stats")).add(stats);
    }
    return buildClientRTCStatObject(statMap, selectedStats);
  }


  /**
   * Stashes stats into a global variable and collects them 1s after
   *
   * @return String.
   * @throws InterruptedException
   */
  private static Object getStatOnce(WebDriver webDriver) throws KiteTestException {
    String stashStatsScript =
      "  appController.call_.pcClient_.pc_.getStats()"
        + "    .then(data => {"
        + "      window.KITEStats = [...data.values()];"
        + "    });";

    String getStashedStatsScript = "return window.KITEStats;";

    executeJsScript(webDriver, stashStatsScript);
    waitAround(ONE_SECOND_INTERVAL);
    return executeJsScript(webDriver, getStashedStatsScript);
  }
  
  public static LinkedHashMap<String, String> statsHashMap(JsonObject statsSummary) {
    LinkedHashMap<String, String> results = new LinkedHashMap<>();
//    {
//      "Received": {
//      "Total Bytes Received": 245702,
//        "Inbound Audio Bitrate": 71664,
//        "Inbound Video Bitrate": 1893952,
//        "Packet Loss (%)": 0.0,
//        "Audio Jitter": 0.0,
//        "Audio Level Received": 0.0,
//        "Frame Lost": 0
//      },
//        "Sent": {
//        "Total Bytes Sent": 263802,
//          "Outbound Audio Bitrate": 70312,
//          "Outbound Video Bitrate": 2040104,
//          "Audio Level Sent": 0.0
//      }
//    }
    JsonObject received = statsSummary.getJsonObject("Received");
    results.put("totalBytesReceived", "" + received.getInt("Total Bytes Received"));
    results.put("videoBitrateReceived", "" + received.getInt("Inbound Video Bitrate"));
    results.put("audioBitrateReceived", "" + received.getInt("Inbound Audio Bitrate"));
    results.put("packetLossReceived", "" + received.getJsonNumber("Packet Loss (%)").doubleValue());
    results.put("audioJitterReceived", "" + received.getJsonNumber("Audio Jitter").doubleValue());
    results.put("audioLevelReceived", "" + received.getJsonNumber("Audio Level Received").doubleValue());
    results.put("totalFramesLost", "" + received.getInt("Frame Lost"));
    JsonObject sent = statsSummary.getJsonObject("Sent");
    results.put("totalBytesSent", "" + sent.getInt("Total Bytes Sent"));
    results.put("videoBitrateSent", "" + sent.getInt("Outbound Video Bitrate"));
    results.put("audioBitrateSent", "" + sent.getInt("Outbound Audio Bitrate"));
    results.put("audioLevelSent", "" + sent.getJsonNumber("Audio Level Sent").doubleValue());
    return results;
  }

  public static JsonObject buildstatSummary(JsonObject rawData, JsonArray selectedStats) {
    JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonArray statsArr = rawData.getJsonArray("statsArray");
    long audioByteReceived = 0;
    long audioBytesSent = 0;
    long videoByteReceived = 0;
    long videoBytesSent = 0;
    long audioRcvdBitrate = 0;
    long audioSentBitrate = 0;
    long videoRcvdBitrate = 0;
    long videoSentBitrate = 0;
    double packetLoss = 0;
    double audioJitter = 0;
    double audioLevelRcvd = 0;
    double audioLevelSent = 0;
    double frameRcvdLost = 0;
    ArrayList<Long> timestampArr = new ArrayList<>();
    for (int i = 0; i < statsArr.size(); i++) {
      for (int j = 0; j < selectedStats.size(); j++) {
        if (statsArr.getJsonObject(i).getJsonObject(selectedStats.getString(j)) != null) {
          switch (selectedStats.getString(j)) {
            case "inbound-rtp":
              JsonObject inboundData = statsArr.getJsonObject(i).getJsonObject("inbound-rtp");
              List<String> keyArr = findKeys(inboundData);
              inboundData = formatData(inboundData, keyArr);
              timestampArr.add(
                Long.parseLong(
                  inboundData.getJsonObject(keyArr.get(0)).get("timestamp").toString()));
              for (String key : keyArr) {
                JsonObject data = inboundData.getJsonObject(key);
                long elapsedTime = timestampArr.get(timestampArr.size() - 1) - timestampArr.get(0);
                if (data.getString("mediaType").equals("audio")) {
                  audioByteReceived = data.getInt("bytesReceived");
                  if (elapsedTime != 0) {
                    audioRcvdBitrate = audioByteReceived * 8 / (elapsedTime / 1000);
                  }
                  packetLoss +=
                    (double) data.getInt("packetsLost") / data.getInt("packetsReceived") * 100;
                  audioJitter +=
                    data.get("jitter").toString().equals("0")
                      ? 0
                      : Double.parseDouble(data.getString("jitter"));
                } else {
                  videoByteReceived = data.getInt("bytesReceived");
                  if (elapsedTime != 0) {
                    videoRcvdBitrate = videoByteReceived * 8 / (elapsedTime / 1000);
                  }
                }
              }
              break;
            case "outbound-rtp":
              JsonObject outboundData = statsArr.getJsonObject(i).getJsonObject("outbound-rtp");
              keyArr = findKeys(outboundData);
              outboundData = formatData(outboundData, keyArr);
              for (String key : keyArr) {
                JsonObject data = outboundData.getJsonObject(key);
                long elapsedTime = timestampArr.get(timestampArr.size() - 1) - timestampArr.get(0);
                if (data.getString("mediaType").equals("audio")) {
                  audioBytesSent = data.getInt("bytesSent");
                  if (elapsedTime != 0) {
                    audioSentBitrate = audioBytesSent * 8 / (elapsedTime / 1000);
                  }
                } else {
                  videoBytesSent = data.getInt("bytesSent");
                  if (elapsedTime != 0) {
                    videoSentBitrate = videoBytesSent * 8 / (elapsedTime / 1000);
                  }
                }
              }
              break;
            case "track":
              JsonObject trackData = statsArr.getJsonObject(i).getJsonObject("track");
              keyArr = findKeys(trackData);
              for (String key : keyArr) {
                JsonObject data = trackData.getJsonObject(key);
                String audioLevel = data.getString("audioLevel");
                if (audioLevel.equals("0") || audioLevel.equals("NA") || audioLevel.length() > 9) {
                  continue;
                }
                if (key.contains("1")) {
                  if (key.contains("receiver")) {
                    audioLevelRcvd += Double.parseDouble(data.getString("audioLevel"));
                  } else {
                    audioLevelSent += Double.parseDouble(data.getString("audioLevel"));
                  }
                } else {
                  if (key.contains("receiver")) {
                    frameRcvdLost +=
                      (double)
                        (Integer.parseInt(data.getString("framesDropped"))
                          / Integer.parseInt(data.getString("framesReceived")))
                        * 100;
                  }
                }
              }
              break;
          }
        }
      }
    }
    builder.add("Total Bytes Received", audioByteReceived + videoByteReceived);
    builder.add("Inbound Audio Bitrate", audioRcvdBitrate);
    builder.add("Inbound Video Bitrate", videoRcvdBitrate);
    builder.add("Packet Loss (%)", packetLoss / statsArr.size());
    builder.add("Audio Jitter", audioJitter / statsArr.size());
    builder.add("Audio Level Received", audioLevelRcvd / statsArr.size());
    builder.add("Frame Lost", Math.round(frameRcvdLost / statsArr.size() * 1000) / 1000);
    mainBuilder.add("Received", builder.build());
    builder = Json.createObjectBuilder();
    builder.add("Total Bytes Sent", audioBytesSent + videoBytesSent);
    builder.add("Outbound Audio Bitrate", audioSentBitrate);
    builder.add("Outbound Video Bitrate", videoSentBitrate);
    builder.add("Audio Level Sent", audioLevelSent / statsArr.size());
    mainBuilder.add("Sent", builder.build());

    return mainBuilder.build();
  }

  private static JsonObject formatData(JsonObject json, List<String> keyArr) {
    JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
    for (String key : keyArr) {
      JsonObject data = json.getJsonObject(key);
      JsonObjectBuilder innerBuilder = Json.createObjectBuilder();
      for (String innerKey : data.keySet()) {
        String s = data.getString(innerKey);
        if (s.matches("-?\\d+")) {
          if (s.length() < 10) {
            innerBuilder.add(innerKey, Integer.parseInt(s));
          } else {
            innerBuilder.add(innerKey, Long.parseLong(s));
          }
        } else {
          innerBuilder.add(innerKey, s);
        }
      }
      mainBuilder.add(key, innerBuilder.build());
    }
    return mainBuilder.build();
  }

  private static List<String> findKeys(JsonObject json) {
    ArrayList<String> keyArr = new ArrayList<>();
    for (String key : json.keySet()) {
      if (key.contains("RTC")||key.contains("rtp")) {
        keyArr.add(key);
      }
    }
    return keyArr;
  }



  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object from data obtained via
   * tests.
   *
   * @param clientStats array of data sent back from test
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildClientRTCStatObject(
    Map<String, Object> clientStats, JsonArray selectedStats) {
    try {
      JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
      Map<String, Object> clientStatMap = clientStats;
      List<Object> clientStatArray = (ArrayList<Object>) clientStatMap.get("stats");
      JsonArrayBuilder jsonclientStatArray = Json.createArrayBuilder();
      for (Object stats : clientStatArray) {
        JsonObjectBuilder jsonRTCStatObjectBuilder = buildSingleRTCStatObject(stats, selectedStats);
        jsonclientStatArray.add(jsonRTCStatObjectBuilder);
      }

      JsonObjectBuilder sdpObjectBuilder = Json.createObjectBuilder();
      Map<Object, Object> sdpOffer = (Map<Object, Object>) clientStatMap.get("offer");
      Map<Object, Object> sdpAnswer = (Map<Object, Object>) clientStatMap.get("answer");
      sdpObjectBuilder
        .add("offer", new SDP(sdpOffer).getJsonObjectBuilder())
        .add("answer", new SDP(sdpAnswer).getJsonObjectBuilder());

      jsonObjectBuilder.add("sdp", sdpObjectBuilder).add("statsArray", jsonclientStatArray);

      return jsonObjectBuilder;
    } catch (ClassCastException e) {
      return Json.createObjectBuilder();
    }
  }

  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object from data obtained via
   * tests.
   *
   * @param statArray array of data sent back from test
   * @param statsSelection ArrayList<String> of the selected stats
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildSingleRTCStatObject(
    Object statArray, JsonArray statsSelection) {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    Map<String, List<RTCStatObject>> statObjectMap = new HashMap<>();
    if (statArray != null) {
      for (Object map : (ArrayList) statArray) {
        if (map != null) {
          Map<Object, Object> statMap = (Map<Object, Object>) map;
          String type = (String) statMap.get("type");
          if (statsSelection == null || statsSelection.toString().contains(type)) {
            RTCStatObject statObject = null;
            switch (type) {
              case "codec":
              {
                statObject = new RTCCodecStats(statMap);
                break;
              }
              case "track":
              {
                statObject = new RTCMediaStreamTrackStats(statMap);
                break;
              }
              case "stream":
              {
                statObject = new RTCMediaStreamStats(statMap);
                break;
              }
              case "inbound-rtp":
              {
                statObject = new RTCRTPStreamStats(statMap, true);
                break;
              }
              case "outbound-rtp":
              {
                statObject = new RTCRTPStreamStats(statMap, false);
                break;
              }
              case "peer-connection":
              {
                statObject = new RTCPeerConnectionStats(statMap);
                break;
              }
              case "transport":
              {
                statObject = new RTCTransportStats(statMap);
                break;
              }
              case "candidate-pair":
              {
                statObject = new RTCIceCandidatePairStats(statMap);
                break;
              }
              case "remote-candidate":
              {
                statObject = new RTCIceCandidateStats(statMap);
                break;
              }
              case "local-candidate":
              {
                statObject = new RTCIceCandidateStats(statMap);
                break;
              }
            }
            if (statObject != null) {
              if (statObjectMap.get(type) == null) {
                statObjectMap.put(type, new ArrayList<RTCStatObject>());
              }
              statObjectMap.get(type).add(statObject);
            }
          }
        }
      }
    }
    if (!statObjectMap.isEmpty()) {
      for (String type : statObjectMap.keySet()) {
        //        JsonArrayBuilder tmp = Json.createArrayBuilder();
        JsonObjectBuilder tmp = Json.createObjectBuilder();
        for (RTCStatObject stat : statObjectMap.get(type)) {
          tmp.add(stat.getId(), stat.getJsonObjectBuilder());
          //          tmp.add(/*stat.getId(),*/ stat.getJsonObjectBuilder());
        }
        jsonObjectBuilder.add(type, tmp);
      }
    }
    return jsonObjectBuilder;
  }
  
}

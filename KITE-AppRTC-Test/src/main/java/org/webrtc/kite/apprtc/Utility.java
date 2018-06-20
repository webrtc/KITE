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

package org.webrtc.kite.apprtc;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.KiteTest;
import org.webrtc.kite.apprtc.network.IceConnectionTest;
import org.webrtc.kite.apprtc.stats.*;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class holding various static methods.
 */
public class Utility {

  private static final Logger logger = Logger.getLogger(Utility.class.getName());

  /**
   * Returns the test JavaScript to retrieve appController.call_.pcClient_.pc_.iceConnectionState.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the JavaScript as string.
   */
  private static String getIceConnectionStateScript() {
    return "var retValue;"
      + "try {retValue = appController.call_.pcClient_.pc_.iceConnectionState;} catch (exception) {} "
      + "if (retValue) {return retValue;} else {return 'unknown';}";
  }

  /**
   * Returns the test's getSDPMessageScript to retrieve the sdp message for either the offer or answer.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getSDPMessageScript as string.
   */
  private static String getSDPMessageScript(String type) throws Exception {
    switch (type){
      case "offer":
        return "var SDP;"
          + "try {SDP = appController.call_.pcClient_.pc_.remoteDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      case "answer":
        return "var SDP;"
          + "try {SDP = appController.call_.pcClient_.pc_.localDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      default:
        throw new Exception("Not a valid type for sdp message.");
    }
  }

   /**
   * Returns the test's canvasCheck to check if the video is blank, and if it changes overtime.
   *
   * @return the canvasCheck as string.
   */
  private static String getFrameValueSum() {
    return "function getSum(total, num) {" + "    return total + num;" + "};"
      + "var canvas = document.createElement('canvas');" + "var ctx = canvas.getContext('2d');"
      + "ctx.drawImage(remoteVideo,0,0,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1);"
      + "var imageData = ctx.getImageData(0,0,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1).data;"
      + "var sum = imageData.reduce(getSum);"
      + "if (sum===255*(Math.pow(remoteVideo.videoHeight-1,(remoteVideo.videoWidth-1)*(remoteVideo.videoWidth-1))))"
      + "   return 0;" + "return sum;";
  }

  /**
   * Returns the test's GetResolutionScript to stash the result and stats of the test in a global variable to retrieve later.
   * @param webDriver used to execute command.
   * @param source local or remote track
   * @return JsonObject
   * @throws Exception
   */
  public  static JsonObject GetResolutionScript(WebDriver webDriver, String source) throws Exception {
    boolean remoteSource;
    switch (source){
      case "local":
        remoteSource = false;
        break;
      case "remote":
        remoteSource = true;
        break;
      default:
        throw new Exception("Invalid source for track resolution");
    }

    String stashResolutionScript =  "window.resolution = {width: -1, height: -1};" +
      "appController.call_.pcClient_.pc_.getStats().then(data => {" +
      "   [...data.values()].forEach(function(e){" +
      "       if (e.type.startsWith('track')){" +
      "           if ((e.remoteSource=="+remoteSource+") && (typeof e.audioLevel == 'undefined')) { " +
      "               window.resolution.width = e.frameWidth;" +
      "               window.resolution.height = e.frameHeight;" +
      "           }" +
      "       }" +
      "   });" +
      "});";

    String getStashedResolutionScript = "return JSON.stringify(window.resolution);";

    ((JavascriptExecutor) webDriver).executeScript(stashResolutionScript);
    Thread.sleep(1000);
    String resolution = (String) ((JavascriptExecutor) webDriver).executeScript(getStashedResolutionScript);
    InputStream stream = new ByteArrayInputStream(resolution.getBytes(StandardCharsets.UTF_8));
    JsonReader reader = Json.createReader(stream);
    return reader.readObject();
  }

  /**
   *
   * @param webDriver used to execute command.
   * @param TIMEOUT Max wait time for the test.
   * @param INTERVAL between each verification.
   * @return whether Ice connection has been established.
   * @throws InterruptedException
   */
  public static boolean checkIceConnectionState(WebDriver webDriver, int TIMEOUT, int INTERVAL) throws InterruptedException {
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      String res =
        (String) ((JavascriptExecutor) webDriver).executeScript(getIceConnectionStateScript());
      if (res.equalsIgnoreCase("failed")){
        return false;
      } else if (res.equalsIgnoreCase("completed") || res.equalsIgnoreCase("connected")){
        return true;
      } else {
        Thread.sleep(INTERVAL);
      }
    }
    return false;
  }

  /**
   * Computes the video's frame pixel sum to verify video playback
   *
   * @param webDriver used to execute command.
   * @return whether the video is actually showing
   * @throws InterruptedException
   */
  public static boolean checkVideoDisplay(WebDriver webDriver, int TIMEOUT, int INTERVAL) throws InterruptedException {
    long canvasData = 0;
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      canvasData = (Long) ((JavascriptExecutor) webDriver).executeScript(getFrameValueSum());
      if (canvasData == 0) {
        Thread.sleep(INTERVAL);
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Execute and return the requested SDP message
   * @param webDriver used to execute command.
   * @param type offer or answer.
   * @return SDP object.
   * @throws Exception
   */
  public static Object getSDPMessage(WebDriver webDriver, String type) throws Exception {
    return ((JavascriptExecutor) webDriver).executeScript(getSDPMessageScript(type));
  }

  public static Double getBitrate(WebDriver webDriver, String mediaType, boolean sending) throws Exception {
    String direction;
    if (sending){
      direction = "outbound-rtp";
    } else {
      direction = "inbound-rtp";
    }
    String stashBitrateScript = "appController.call_.pcClient_.pc_.getStats().then(data => {" +
      "   [...data.values()].forEach(function(e){" +
      "       if (e.type.startsWith('" + direction + "')){" +
      "           if (e.mediaType.startsWith('" + mediaType + "')){ " +
      "               if (typeof window.bitrate == 'undefined') {" +
      "                   window.bitrate = e.bytesSent;  " +
      "               } else {" +
      "                   window.bitrate = 8*(e.bytesSent - window.bitrate)/1000;" +
    "                 }" +
      "           }" +
      "       }" +
      "   });" +
      "});" +
      "return 0;";

    String getStashedBitrateScript = "return window.bitrate";
    ((JavascriptExecutor) webDriver).executeScript(stashBitrateScript);
    Thread.sleep(1000);
    ((JavascriptExecutor) webDriver).executeScript(stashBitrateScript);
    Thread.sleep(1000);
    return Double.parseDouble((((JavascriptExecutor) webDriver).executeScript(getStashedBitrateScript)).toString());
  }

    /**
     * Stashes stats into a global variable and collects them 1s after
     * @param webDriver used to execute command.
     * @return String.
     * @throws InterruptedException
     */
  public static Object getStatOnce(WebDriver webDriver) throws InterruptedException{
    String stashStatsScript =
      "  appController.call_.pcClient_.pc_.getStats()" +
      "    .then(data => {" +
      "      window.KITEStats = [...data.values()];" +
      "    });" ;

    String getStashedStatsScript = "return window.KITEStats;";

    ((JavascriptExecutor) webDriver).executeScript(stashStatsScript);
    Thread.sleep(1000);
    return ((JavascriptExecutor) webDriver).executeScript(getStashedStatsScript);
  }

  /**
   * stat JsonObjectBuilder to add to callback result.
   * @param webDriver used to execute command.
   * @param duration during which the stats will be collected.
   * @param interval between each time getStats gets called.
   * @return JsonObjectBuilder of the stat object
   * @throws Exception
   */
  public static JsonObjectBuilder getStatOvertime(WebDriver webDriver, int duration, int interval) throws Exception {
    Map<String, Object> statMap = new HashMap<String, Object>();
    for (int timer = 0; timer < duration; timer += interval) {
      Object stats = getStatOnce(webDriver);
      if (timer == 0) {
        statMap.put("stats", new ArrayList<>());

        Object offer = getSDPMessage(webDriver, "offer");
        Object answer = getSDPMessage(webDriver,"answer");
        statMap.put("offer", offer);
        statMap.put("answer", answer);
      }
      List<Object> tmp = (List) statMap.get("stats");
      tmp.add(stats);
    }
    return buildClientStatObject(statMap);
  }


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
  public static JsonObjectBuilder buildClientStatObject(Object clientStats) {
    try {
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
    } catch (ClassCastException e) {
      logger.error("ClassCastException while developing stat objects: ");
      logger.error("clientStats:" + clientStats);
      e.printStackTrace();
      return Json.createObjectBuilder();
    }
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
   * Retrieves browser console log if possible.
   * @return
   */
  public static JsonArrayBuilder getLog(WebDriver webDriver) {
    JsonArrayBuilder log = Json.createArrayBuilder();
    List<String> logEntries = KiteTest.analyzeLog(webDriver);
    for (String entry: logEntries){
      log.add(entry);
    }
    return log;
  }

  /**
   * Puts all result components together.
   *
   * @param result of the test.
   * @param stats from the test.
   * @param message if exists.
   * @param alertMsg if exists.
   * @return JsonObject
   */
  public static JsonObject developResult(String browser, String result, JsonObjectBuilder stats, String message, String alertMsg, JsonArrayBuilder log) {
    if (log != null){
      stats.add("log", log);
    } else {
      stats.add("log", "No log was provided.");
    }

    if (message != null){
      stats.add("message", message);
    } else {
      stats.add("message", "No message was provided.");
    }

    if (alertMsg != null) {
      stats.add("alert", alertMsg);
    } else {
      stats.add("alert", "No alert message was found.");
    }

    return Json.createObjectBuilder().add("browser", browser)
      .add("result", result)
      .add("stats", stats)
      .build();
  }

}

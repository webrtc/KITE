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
package org.webrtc.kite.apprtc.pages;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.webrtc.kite.stats.*;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.TestUtils.*;

public class AppRTCMeetingPage extends BasePage {
  
  @FindBy(id="mini-video")
  WebElement miniVideo;
  
  @FindBy(id="local-video")
  WebElement localVideo;
  
  @FindBy(id="remote-video")
  WebElement remoteVideo;
  
  @FindBy(id="hangup")
  WebElement hangUpButton;
  
  @FindBy(id="fullscreen")
  WebElement fullScreenButton;
  
  @FindBy(id="mute-audio")
  WebElement muteAudioButton;
  
  @FindBy(id="mute-video")
  WebElement muteVideoButton;

  
  public AppRTCMeetingPage(WebDriver webDriver) {
    super(webDriver);
  }
  
  public void muteAudio() throws KiteTestException {
    click(muteAudioButton);
  }
  
  public void muteVideo() throws KiteTestException {
    click(muteVideoButton);
  }
  
  public void hangup() throws KiteTestException {
    click(hangUpButton);
  }
  
  public void goFullScreen() throws KiteTestException {
    click(fullScreenButton);
  }
  
  public String getICEConnectionState() {
    return (String) executeJsScript(webDriver, getIceConnectionStateScript());
  }

  public long getRemoteVideoPixelSum() {
    return (Long) executeJsScript(webDriver, getRemoteVideoPixelSumScript());
  }

  public String remoteVideoCheck() {
    return videoCheck(webDriver, 1);
  }


  
  /**
   * Returns the test's GetResolutionScript to stash the result and stats of the test in a global variable to retrieve later.
   *
   * @param source    local or remote track
   *
   * @return JsonObject json object
   */
  public JsonObject getResolution(String source) {
    executeJsScript(webDriver, stashResolutionScript(source.equalsIgnoreCase("remote")));
    waitAround(ONE_SECOND_INTERVAL);
    String resolution = (String) executeJsScript(webDriver, getStashedResolutionScript());
    InputStream stream = new ByteArrayInputStream(resolution.getBytes(StandardCharsets.UTF_8));
    JsonReader reader = Json.createReader(stream);
    return reader.readObject();
  }
  
  /**
   * Return the bitrate of a media track
   * @param mediaType type of media (video/audio)
   * @param direction direction of the media stream (sending/receiving)
   * @return  the bytesSent or bytesReceived value from the stats
   */
  public long getBitrate(String mediaType, String direction) {
    executeJsScript(webDriver, stashBitrateScript(mediaType, direction));
    waitAround(ONE_SECOND_INTERVAL);
    return (Long) executeJsScript(webDriver, getStashedBitrateScript());
  }
  
  /**
   * stat JsonObjectBuilder to add to callback result.
   * @param webDriver used to execute command.
   * @param duration during which the stats will be collected.
   * @param interval between each time getStats gets called.
   * @return JsonObjectBuilder of the stat object
   * @throws Exception
   */
  public JsonObjectBuilder getStatOvertime(WebDriver webDriver, int duration, int interval, JsonArray selectedStats) {
    Map<String, Object> statMap = new HashMap<String, Object>();
    for (int timer = 0; timer < duration; timer += interval) {
      waitAround(interval);
      Object stats = getStatOnce();
      if (timer == 0) {
        statMap.put("stats", new ArrayList<>());
        Object offer = executeJsScript(webDriver, getSDPMessageScript("offer"));
        Object answer = executeJsScript(webDriver,getSDPMessageScript("answer"));
        statMap.put("offer", offer);
        statMap.put("answer", answer);
      }
      ((List<Object>) statMap.get("stats")).add(stats);
    }
    return buildClientRTCStatObject(statMap, selectedStats);
  }
  
  
  /**
   * Stashes stats into a global variable and collects them 1s after
   * @return String.
   * @throws InterruptedException
   */
  private Object getStatOnce() {
    String stashStatsScript =
      "  appController.call_.pcClient_.pc_.getStats()" +
        "    .then(data => {" +
        "      window.KITEStats = [...data.values()];" +
        "    });" ;
    
    String getStashedStatsScript = "return window.KITEStats;";
  
    executeJsScript(webDriver, stashStatsScript);
    waitAround(ONE_SECOND_INTERVAL);
    return executeJsScript(webDriver, getStashedStatsScript);
  }
  
  /**
   * Returns the test JavaScript to retrieve appController.call_.pcClient_.pc_.iceConnectionState.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getIceConnectionStateScript as string.
   */
  private String getIceConnectionStateScript() {
    return "var retValue;"
      + "try {retValue = appController.call_.pcClient_.pc_.iceConnectionState;} catch (exception) {} "
      + "if (retValue) {return retValue;} else {return 'unknown';}";
  }
  
  /**
   * Returns the test's canvasCheck to check if the video is blank, and if it changes overtime.
   *
   * @return the getRemoteVideoPixelSumScript as string.
   */
  private String getRemoteVideoPixelSumScript() {
    return "function getSum(total, num) {" + "    return total + num;" + "};"
      + "var canvas = document.createElement('canvas');" + "var ctx = canvas.getContext('2d');"
      + "ctx.drawImage(remoteVideo,1,1,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1);"
      + "var imageData = ctx.getImageData(1,1,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1).data;"
      + "var sum = imageData.reduce(getSum);"
      + "if (sum===255*(Math.pow(remoteVideo.videoHeight-1,(remoteVideo.videoWidth-1)*(remoteVideo.videoWidth-1))))"
      + "   return 0;" + "return sum;";
  }
  
  
  /**
   * Returns the test's getSDPMessageScript to retrieve the sdp message for either the offer or answer.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getSDPMessageScript as string.
   */
  private static String getSDPMessageScript(String type) {
    switch (type){
      case "offer":
        return "var SDP;"
          + "try {SDP = appController.call_.pcClient_.pc_.remoteDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
      case "answer":
        return "var SDP;"
          + "try {SDP = appController.call_.pcClient_.pc_.localDescription;} catch (exception) {} "
          + "if (SDP) {return SDP;} else {return 'unknown';}";
        
    }
    return null;
  }
  
  private String stashBitrateScript(String mediaType, String direction) {
    return "appController.call_.pcClient_.pc_.getStats().then(data => {" +
      "   [...data.values()].forEach(function(e){" +
      "       if (e.type.startsWith('" + (direction.equalsIgnoreCase("sending") ? "outbound-rtp" : "inbound-rtp") + "')){" +
      "           if (e.mediaType.startsWith('" + mediaType + "')){ " +
"                   window.bitrate = e."+ (direction.equalsIgnoreCase("sending") ? "bytesSent" : "bytesReceived") + ";  " +
      "           }" +
      "       }" +
      "   });" +
      "});" +
      "return 0;";
  }
  
  
  private String getStashedBitrateScript() {
    return "return window.bitrate";
  }
  
  private String stashResolutionScript(boolean remote) {
    return "window.resolution = {width: -1, height: -1};" +
      "appController.call_.pcClient_.pc_.getStats().then(data => {" +
      "   [...data.values()].forEach(function(e){" +
      "       if (e.type.startsWith('track')){" +
      "           if ((e.remoteSource==" + remote + ") && (typeof e.audioLevel == 'undefined')) { " +
      "               window.resolution.width = e.frameWidth;" +
      "               window.resolution.height = e.frameHeight;" +
      "           }" +
      "       }" +
      "   });" +
      "});";
  }
  
  private String getStashedResolutionScript() {
    return "return JSON.stringify(window.resolution);";
  }
  
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param clientStats array of data sent back from test
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildClientRTCStatObject(Map<String, Object> clientStats, JsonArray selectedStats) {
    try {
      JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
      Map<String, Object> clientStatMap = clientStats;
      
      List<Object> clientStatArray = (ArrayList) clientStatMap.get("stats");
      JsonArrayBuilder jsonclientStatArray = Json.createArrayBuilder();
      for (Object stats : clientStatArray) {
        JsonObjectBuilder jsonRTCStatObjectBuilder = buildSingleRTCStatObject(stats, selectedStats);
        jsonclientStatArray.add(jsonRTCStatObjectBuilder);
      }
      
      JsonObjectBuilder sdpObjectBuilder = Json.createObjectBuilder();
      Map<Object, Object> sdpOffer = (Map<Object, Object>) clientStatMap.get("offer");
      Map<Object, Object> sdpAnswer = (Map<Object, Object>) clientStatMap.get("answer");
      sdpObjectBuilder.add("offer", new SDP(sdpOffer).getJsonObjectBuilder())
        .add("answer", new SDP(sdpAnswer).getJsonObjectBuilder());
      
      jsonObjectBuilder.add("sdp", sdpObjectBuilder)
        .add("statsArray", jsonclientStatArray);
      
      return jsonObjectBuilder;
    } catch (ClassCastException e) {
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
  public static JsonObjectBuilder buildSingleRTCStatObject(Object statArray) {
    return buildSingleRTCStatObject(statArray, null);
  }
  
  
  
  /**
   * Create a JsonObjectBuilder Object to eventually build a Json object
   * from data obtained via tests.
   *
   * @param statArray array of data sent back from test
   * @param statsSelection ArrayList<String> of the selected stats
   * @return JsonObjectBuilder.
   */
  public static JsonObjectBuilder buildSingleRTCStatObject(Object statArray, JsonArray statsSelection) {
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

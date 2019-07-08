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
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL_IN_SECONDS;
import static io.cosmosoftware.kite.util.TestUtils.*;

public class AppRTCMeetingPage extends BasePage {
  
  @FindBy(id = "mini-video")
  WebElement miniVideo;
  
  @FindBy(id = "local-video")
  WebElement localVideo;
  
  @FindBy(id = "remote-video")
  WebElement remoteVideo;
  
  @FindBy(id = "hangup")
  WebElement hangUpButton;
  
  @FindBy(id = "fullscreen")
  WebElement fullScreenButton;
  
  @FindBy(id = "mute-audio")
  WebElement muteAudioButton;
  
  @FindBy(id = "mute-video")
  WebElement muteVideoButton;
  
  public AppRTCMeetingPage(Runner runner) {
    super(runner);
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
  
  public String getICEConnectionState() throws KiteTestException {
    return (String) executeJsScript(webDriver, getIceConnectionStateScript());
  }
  
  public long getRemoteVideoPixelSum() throws KiteTestException {
    return (Long) executeJsScript(webDriver, getRemoteVideoPixelSumScript());
  }
  
  public String remoteVideoCheck() throws KiteTestException {
    waitUntilVisibilityOf(remoteVideo, TEN_SECOND_INTERVAL_IN_SECONDS);
    return videoCheck(webDriver, 1);
  }
  
  /**
   * Returns the test's GetResolutionScript to stash the result and stats of the test in a global
   * variable to retrieve later.
   *
   * @param source local or remote track
   * @return JsonObject json object
   */
  public JsonObject getResolution(String source) throws KiteTestException {
    executeJsScript(webDriver, stashResolutionScript(source.equalsIgnoreCase("remote")));
    waitAround(ONE_SECOND_INTERVAL);
    String resolution = (String) executeJsScript(webDriver, getStashedResolutionScript());
    InputStream stream = new ByteArrayInputStream(resolution.getBytes(StandardCharsets.UTF_8));
    JsonReader reader = Json.createReader(stream);
    return reader.readObject();
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
    return "function getSum(total, num) {"
      + "    return total + num;"
      + "};"
      + "var canvas = document.createElement('canvas');"
      + "var ctx = canvas.getContext('2d');"
      + "ctx.drawImage(remoteVideo,1,1,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1);"
      + "var imageData = ctx.getImageData(1,1,remoteVideo.videoHeight-1,remoteVideo.videoWidth-1).data;"
      + "var sum = imageData.reduce(getSum);"
      + "if (sum===255*(Math.pow(remoteVideo.videoHeight-1,(remoteVideo.videoWidth-1)*(remoteVideo.videoWidth-1))))"
      + "   return 0;"
      + "return sum;";
  }
  
  private String stashResolutionScript(boolean remote) {
    return "window.resolution = {width: -1, height: -1};"
      + "appController.call_.pcClient_.pc_.getStats().then(data => {"
      + "   [...data.values()].forEach(function(e){"
      + "       if (e.type.startsWith('track')){"
      + "           if ((e.remoteSource=="
      + remote
      + ") && (typeof e.audioLevel == 'undefined')) { "
      + "               window.resolution.width = e.frameWidth;"
      + "               window.resolution.height = e.frameHeight;"
      + "           }"
      + "       }"
      + "   });"
      + "});";
  }
  
  private String getStashedResolutionScript() {
    return "return JSON.stringify(window.resolution);";
  }

}
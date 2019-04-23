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
package org.webrtc.kite.apprtc.checks;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestCheck;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.apprtc.pages.AppRTCMeetingPage;

import javax.json.JsonObject;

public class RemoteVideoHDCheck extends TestCheck {

  public RemoteVideoHDCheck(WebDriver webDriver) {
    super(webDriver);
  }
  
  @Override
  public String stepDescription() {
    return "Verify that the remote video resolution is HD (1280x720)";
  }
  
  @Override
  protected void step() throws KiteTestException {
    final AppRTCMeetingPage appRTCMeetingPage = new AppRTCMeetingPage(webDriver, logger);
    JsonObject resolution = appRTCMeetingPage.getResolution("remote");
    int width = resolution.getInt("width");
    int height = resolution.getInt("height");
    if ( width == -1 || height == -1) {
      throw new KiteTestException("Could not get the track information for the peer connection", Status.FAILED);
    }
    
    if (width != 1280|| height != 720) {
      throw new KiteTestException("The resolution was not HD, found "
        + width + "x" + height + " instead"
        , Status.FAILED);
    }
    
    Reporter.getInstance().textAttachment(this.report, "Remote video resolution:  width:", width + ", height:" + height, "plain");
  }
  
  
}

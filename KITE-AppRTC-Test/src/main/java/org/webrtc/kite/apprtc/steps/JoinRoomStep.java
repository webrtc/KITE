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
package org.webrtc.kite.apprtc.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.apprtc.pages.AppRTCJoinPage;
import org.webrtc.kite.apprtc.tests.AppRTCTest;

public class JoinRoomStep extends TestStep {

  protected String roomId;
  private String debugOption;
  private AppRTCJoinPage appRTCJoinPage = new AppRTCJoinPage(webDriver);
  
  
  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }
  
  public void setDebugOption(String debugOption) {
    this.debugOption = debugOption;
  }
  
  public JoinRoomStep(WebDriver webDriver) {
    super(webDriver);
  }
  
  @Override
  public String stepDescription() {
    return "Open apprtc web page and join a room";
  }
  
  @Override
  protected void step() throws KiteTestException {
    appRTCJoinPage.joinRoom(roomId + (debugOption != null ? debugOption : ""));
  }
}

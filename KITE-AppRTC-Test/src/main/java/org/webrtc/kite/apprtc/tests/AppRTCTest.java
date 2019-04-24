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
package org.webrtc.kite.apprtc.tests;

import org.webrtc.kite.tests.KiteBaseTest;

import javax.json.JsonArray;
import java.util.Random;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;

public abstract class AppRTCTest extends KiteBaseTest {

  final int DEFAULT_BITRATE = 8000;

  private final Random rand = new Random(System.currentTimeMillis());
  protected final String roomId = String.valueOf(Math.abs(rand.nextLong()));

  public static String apprtcURL = "https://appr.tc";
  protected String commandName;
  protected boolean getStats = true;
  protected int bitrate;
  protected int statsCollectionDuration = TEN_SECOND_INTERVAL;
  protected int statsCollectionInterval = ONE_SECOND_INTERVAL;
  protected JsonArray selectedStats = null;
  
  
  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }
  
  @Override
  protected void payloadHandling() {
    if (this.payload != null) {
      apprtcURL = payload.getString("url", apprtcURL); // this can be mod to throw exception
      bitrate = payload.getInt("bitrate", DEFAULT_BITRATE);
      getStats = payload.getBoolean("getStats", getStats); // getstats by default
      if (getStats) {
        statsCollectionDuration = payload.getInt("statsCollectionDuration", statsCollectionDuration);
        statsCollectionInterval = payload.getInt("statsCollectionInterval", statsCollectionInterval);
        selectedStats = payload.getJsonArray("selectedStats");
      }
    }
  }
  
  
  protected abstract String debugOption();

}

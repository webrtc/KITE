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

import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.apprtc.pages.AppRTCMeetingPage;

import javax.json.JsonArray;
import javax.json.JsonObject;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;

public class GetStatsStep extends TestStep {
  
  protected int statsCollectionDuration = TEN_SECOND_INTERVAL;
  protected int statsCollectionInterval = ONE_SECOND_INTERVAL;
  protected JsonArray selectedStats = null;

  protected AppRTCMeetingPage appRTCMeetingPage = new AppRTCMeetingPage(webDriver);
  
  public void setStatsCollectionDuration(int statsCollectionDuration) {
    this.statsCollectionDuration = statsCollectionDuration;
  }
  
  public void setStatsCollectionInterval(int statsCollectionInterval) {
    this.statsCollectionInterval = statsCollectionInterval;
  }
  
  public void setSelectedStats(JsonArray selectedStats) {
    this.selectedStats = selectedStats;
  }
  
  public GetStatsStep(WebDriver webDriver) {
    super(webDriver);
  }
  
  @Override
  public String stepDescription() {
    return "Get the peer connection's stats";
  }
  
  @Override
  protected void step() {
    JsonObject stats = appRTCMeetingPage.getStatOvertime(
      webDriver, statsCollectionDuration, statsCollectionInterval, selectedStats).build();
    Reporter.getInstance().textAttachment(this.report, "Peer connection's stats", stats.toString(), "json");
  }
}

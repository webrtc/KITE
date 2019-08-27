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
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import org.webrtc.kite.apprtc.pages.AppRTCMeetingPage;
import org.webrtc.kite.stats.RTCStatList;
import org.webrtc.kite.stats.RTCStatMap;

import javax.json.JsonObject;

import static org.webrtc.kite.Utils.getStackTrace;
import static org.webrtc.kite.stats.StatsUtils.*;

public class GetStatsStep extends TestStep {
  
  protected AppRTCMeetingPage appRTCMeetingPage = null;

  private final JsonObject getStatsConfig;

  public GetStatsStep(Runner runner, JsonObject getStatsConfig) {
    super(runner);
    this.getStatsConfig = getStatsConfig;
    appRTCMeetingPage = new AppRTCMeetingPage(runner);
  }

  @Override
  public String stepDescription() {
    return "GetStats";
  }

  @Override
  protected void step() throws KiteTestException {
    try {
      RTCStatMap statsOverTime =  getPCStatOvertime(webDriver, getStatsConfig);
      RTCStatList localPcStats = statsOverTime.getLocalPcStats();
      reporter.jsonAttachment(this.report, "Stats (Raw)", transformToJson(localPcStats));
      reporter.jsonAttachment(this.report, "Stats Summary", buildStatSummary(localPcStats));
    } catch (Exception e) {
      logger.error(getStackTrace(e));
      throw new KiteTestException("Failed to getStats", Status.BROKEN, e);
    }
  }
}

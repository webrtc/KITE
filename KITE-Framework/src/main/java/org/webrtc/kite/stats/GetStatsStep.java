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

import static org.webrtc.kite.Utils.getStackTrace;
import static org.webrtc.kite.stats.StatsUtils.buildStatSummary;
import static org.webrtc.kite.stats.StatsUtils.getPCStatOvertime;
import static org.webrtc.kite.stats.StatsUtils.transformToJson;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import javax.json.JsonObject;

public class GetStatsStep extends TestStep {
  private final JsonObject getStatsConfig;
  private final Runner runner;
  public GetStatsStep(Runner runner, JsonObject getStatsConfig) {
    super(runner);
    this.runner = runner;
    this.getStatsConfig = getStatsConfig;
  }

  @Override
  public String stepDescription() {
    return "GetStats";
  }

  @Override
  protected void step() throws KiteTestException {
    try {
      RTCStatMap statsOverTime =  getPCStatOvertime(webDriver, getStatsConfig);
      statsOverTime.setRegionId(this.runner.getClientRegion());
      RTCStatList localPcStats = statsOverTime.getLocalPcStats();
      JsonObject temp = transformToJson(localPcStats);
      if (!temp.isEmpty()) {
        for (String pc : statsOverTime.keySet()) {
          reporter.jsonAttachment(this.report, "Stats (Raw) - " + pc, transformToJson(statsOverTime.get(pc)));
          reporter.jsonAttachment(this.report, "Stats (Summary) - " + pc, buildStatSummary(statsOverTime.get(pc)));
        }
      }
    } catch (Exception e) {
      logger.error(getStackTrace(e));
      throw new KiteTestException("Failed to getStats", Status.BROKEN, e);
    }
  }
}

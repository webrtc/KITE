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
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestCheck;
import org.webrtc.kite.stats.rtc.RTCStatList;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static org.webrtc.kite.stats.StatsUtils.getPCStatOvertime;

public class BitrateCheck extends TestCheck {
  private int expectedBitrate = -1;
  private String mediaType;
  private String direction;
  
  public BitrateCheck(Runner runner) {
    super(runner);
  }
  
  @Override
  public String stepDescription() {
    return "Verify the bitrate of a media track";
  }
  
  private void updateReport() {
    this.report.setName("Verify that the "
      + (direction == null ? "" : direction) + " bitrate "
      + (mediaType == null ? "" : " for " +  mediaType)
      + (expectedBitrate == -1 ? "" : " is around "  +expectedBitrate  + " b/s"));
  }
  
  @Override
  protected void step() throws KiteTestException {
    // Get a stats array of the selected stat for 5 seconds
    int duration = 5;
    RTCStatList stats = getPCStatOvertime(
      webDriver,"", 5 * ONE_SECOND_INTERVAL, ONE_SECOND_INTERVAL, runner.getPlatform());
    double startingTotalByteCount = stats.get(0).getTotalBytesByMedia(direction, mediaType);
    double endingTotalByteCount = stats.get(duration - 1).getTotalBytesByMedia(direction, mediaType);
    double avgBitrate = (endingTotalByteCount - startingTotalByteCount)/duration;
    // Assuming that there's a 10% tolerance to the test result:
    reporter.textAttachment(report, "Bitrate check",
      "Expected : [" + 0.9*expectedBitrate + " -> " + 1.1*expectedBitrate + "], found " + avgBitrate  , "plain" );
    if (avgBitrate < 0.9*expectedBitrate || 1.1*expectedBitrate < avgBitrate) {
      throw new KiteTestException("Expected bitrate to be in [" + 0.9*expectedBitrate + "," + 1.1*expectedBitrate + "], found " + avgBitrate, Status.FAILED);
    }
  }

  public void setExpectedBitrate(int expectedBitrate) {
    this.expectedBitrate = expectedBitrate;
    updateReport();
  }
  
  public void setOption(String option) {
    this.mediaType = option.startsWith("a")? "audio" : "video";
    this.direction = option.endsWith("s")? "outbound" : "inbound";
    updateReport();
  }
  

}

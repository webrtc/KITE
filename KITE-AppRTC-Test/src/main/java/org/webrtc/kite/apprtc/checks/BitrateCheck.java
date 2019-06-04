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
import org.webrtc.kite.stats.StatsUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import static io.cosmosoftware.kite.entities.Timeouts.FIVE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;

public class BitrateCheck extends TestCheck {
  private int expectedBitrate = -1;
  private String mediaType;
  private String direction;
  
  public BitrateCheck(WebDriver webDriver) {
    super(webDriver);
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
    final AppRTCMeetingPage appRTCMeetingPage = new AppRTCMeetingPage(webDriver, logger);
    String stat = direction.equalsIgnoreCase("sending") ? "inbound-rtp" : "outbound-rtp";
    JsonArray selectedStat =
      Json.createArrayBuilder()
        .add(stat)
        .build();
    // Get a stats array of the selected stat for 5 seconds
    JsonObject stats = StatsUtils.getStatOvertime(
      webDriver, FIVE_SECOND_INTERVAL, ONE_SECOND_INTERVAL, selectedStat).build();
    double avgBitrate = computeBitrate(stats.getJsonArray("statsArray"), stat, mediaType);
    System.out.println("avgBitrate lah =>>>>>> " + avgBitrate);
    // Assuming that there's a 10% tolerance to the test result:
    Reporter.getInstance().textAttachment(report, "Bitrate check",
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
    this.direction = option.contains("s")? "sending" : "receiving";
    updateReport();
  }
  
  private double computeBitrate(JsonArray stats, String statName, String mediaType) throws KiteTestException {
    int totalBytes = 0;
    int totalDuration = 0;
    try {
      JsonObject firstMediaStat =
        getMediaObject(stats.getJsonObject(0).getJsonObject(statName), mediaType);
      JsonObject lastMediaStat =
        getMediaObject(stats.getJsonObject(stats.size() - 1).getJsonObject(statName), mediaType);
  
      String byteType = statName.contains("inbound") ? "bytesReceived" : "bytesSent";
  
      totalBytes = Integer.parseInt(lastMediaStat.getString(byteType))
        - Integer.parseInt(firstMediaStat.getString(byteType));
      System.out.println("totalBytes->" + totalBytes);
      totalDuration=  (int) (Long.parseLong(lastMediaStat.getString("timestamp"))
        - Long.parseLong(firstMediaStat.getString("timestamp"))) / ONE_SECOND_INTERVAL;
  
    } catch (Exception e) {
      throw new KiteTestException("Null point exception detected, please check if the stats are correct", Status.BROKEN, e.getCause());
    }
    if (totalDuration == 0) {
      throw new KiteTestException("Total duration seems to be zero, please check the algorithm", Status.BROKEN);
    }
    return totalBytes / totalDuration;
  }
  
  private JsonObject getMediaObject(JsonObject statObject, String mediaType) {
    for (String key: statObject.keySet()) {
      JsonObject mediaObject = statObject.getJsonObject(key);
      if (mediaObject.getString("mediaType").equalsIgnoreCase(mediaType)) {
        return mediaObject;
      }
    }
    return null;
  }
}

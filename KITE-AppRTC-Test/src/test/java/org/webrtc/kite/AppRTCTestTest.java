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

package org.webrtc.kite;

import junit.framework.TestCase;
import org.webrtc.kite.apprtc.tests.HDTest;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.EndPoint;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.TEN_SECOND_INTERVAL;
import static org.webrtc.kite.Utils.getSystemPlatform;

public class AppRTCTestTest extends TestCase {

  private static final String APPRTC_URL = "https://appr.tc";
  private static final String SELENIUM_SERVER_URL = "http://locahost:4444/wd/hub";

  private List<EndPoint> endPoints = new ArrayList<>();

  public void setUp() throws Exception {
    super.setUp();

    final Browser browser = new Browser(
            "chrome",
            "73",
      getSystemPlatform());
    browser.setRemoteAddress(SELENIUM_SERVER_URL);
    
    endPoints.add(browser);
    endPoints.add(browser);
  }
  
  private JsonObject getFakePayload () {
    return Json.createObjectBuilder()
      .add("url", APPRTC_URL)
      .add("statsCollectionDuration", TEN_SECOND_INTERVAL)
      .add("statsCollectionInterval", ONE_SECOND_INTERVAL)
      .add("printToCSV", false)
      .add("printToJson", false)
      .add("selectedStats", getFakeSelectedStat())
      .build();
  }
  
  private JsonArray getFakeSelectedStat() {
    return Json.createArrayBuilder()
      .add("candidate-pair")
      .add("inbound-rtp")
      .add("outbound-rtp")
      .add("track")
      .build();
  }

  public void testTestScript() {
    HDTest test = new HDTest();
    test.setEndPointList(endPoints);
    test.setPayload(getFakePayload());
    test.execute();
  }
}

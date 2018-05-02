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

package org.webrtc.kite.CodecTest;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.KiteTest;
import org.webrtc.kite.stat.Utility;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ReceivingVideoCodecTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * WebDriver:
 * <ul>
 * <li>1) Opens all the browsers with the url specified in APPRTC_URL.</li>
 * <li>2) Clicks 'confirm-join-button'.</li>
 * <li>3) Do the following every 1 second for 1 minute:</li>
 * <ul>
 * <li>a) Executes the JavaScript on the browser given via getIceConnectionScript() which returns
 * iceConnectionState.</li>
 * <li>b) Checks whether the browser has returned the expected result.</li>
 * <li>c) Executes the JavaScript on the given via stashStatsScript() which store stats
 * in a global variable to fetch later.</li>
 * stashed earlier.</li>
 * </ul>
 * <li>4) The test is considered as successful the default receiving video codec is where it should be
 * (i.e. the first payload in the video m line ).</li>
 * <li>5) A successful test returns a boolean 'true' while the unsuccessful test returns a boolean
 * 'false'.</li>
 * </ul>
 * </p>
 */
public class ReceivingVideoCodecTest extends KiteTest {

  private final static Logger logger = Logger.getLogger(ReceivingVideoCodecTest.class.getName());

  private final static String APPRTC_URL = "https://appr.tc/r/";
  private final static int TIMEOUT = 60000;
  private final static int INTERVAL = 1000;
  private final static String RESULT_TIMEOUT = "TIME OUT";
  private final static String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private final static String RESULT_FAILED = "FAILED";
  private static String alertMsg;
  private final String value = "VP8/90000";
  private final String option = "vsc=" + value;

  /**
   * Returns the test's getIceConnectionScript to retrieve appController.call_.pcClient_.pc_.iceConnectionState.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getIceConnectionScript as string.
   */
  private final static String getIceConnectionScript() {
    return "var retValue;"
      + "try {retValue = appController.call_.pcClient_.pc_.iceConnectionState;} catch (exception) {} "
      + "if (retValue) {return retValue;} else {return 'unknown';}";
  }

  /**
   * Returns the test's getSDPOfferScript to retrieve appController.call_.pcClient_.pc_.localDescription.sdp.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the getSDPOfferScript as string.
   */
  private final static String getSDPOfferScript() {
    return "var SDP;"
      + "try {SDP = appController.call_.pcClient_.pc_.localDescription.sdp;} catch (exception) {} "
      + "if (SDP) {return SDP;} else {return 'unknown';}";
  }

  /**
   * Returns the test's stashStatsScript to stash the stats of the test in a global variable to retrieve later.
   *
   * @return the stashStatsScript as string.
   */
  private final static String stashStatsScript() {
    return "const getStatsValues = () =>" +
      "  appController.call_.pcClient_.pc_.getStats()" +
      "    .then(data => {" +
      "      return [...data.values()];" +
      "    });" +
      "const stashStats = async () => {" +
      "  window.KITEStats = await getStatsValues();" +
      "  return 0;" +
      "};" +
      "stashStats();" +
      "return 0";
  }

  /**
   * Returns the test getResultScript to get the stashed stats.
   *
   * @return the getStatsScript as string.
   */
  private final static String getStatsScript() {
    return "return window.KITEStats;";
  }

  /**
   * Opens the APPRTC_URL and clicks 'confirm-join-button'.
   */
  private void takeAction() throws Exception {
    Random rand = new Random(System.currentTimeMillis());
    long channel = Math.abs(rand.nextLong());
    if (this.getWebDriverList().size() > 1)
      throw new Exception("This test is limited to 1 browser only");
    for (WebDriver webDriver : this.getWebDriverList()) {
      webDriver.get(APPRTC_URL + channel + "?" + option);
      try {
        Alert alert = webDriver.switchTo().alert();
        alertMsg = alert.getText();
        if (alertMsg != null) {
          alertMsg = ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName() + " alert: " +alertMsg;
          logger.warn(alertMsg);
          alert.accept();
        }
      } catch (NoAlertPresentException e) {
        alertMsg = null;
      }
      webDriver.findElement(By.id("confirm-join-button")).click();
    }
  }

  /**
   * Checks whether atleast one the result string matches 'failed'.
   *
   * @param result ice connection state for the browser as String
   * @return true if the result string matches 'failed'.
   */
  private boolean checkForFailed(String result) {
    return result.equalsIgnoreCase("failed");
  }

  /**
   * Checks whether the option argument has any effect.
   *
   * @param result local SDP as String
   * @return true if the effect is as expected.
   */
  private boolean validateResults(String result) {

    if (result.equalsIgnoreCase("unknown"))
      return false;
    else {
      String[] lines = result.split("\\n");
      String mLineVideo = null;
      String payload = null;
      for (String line : lines) {
        if (line.contains(value))
          payload = line.split(":")[1].split(" ")[0];
        if (line.startsWith("m=video"))
          mLineVideo = line;
      }
      if (payload == null || mLineVideo == null)
        return false;
      else {
        String defaultPayload = mLineVideo.split(" ")[3];
        if (defaultPayload.equalsIgnoreCase(payload))
          return true;
        return false;
      }
    }
  }


  @Override
  public Object testScript() throws Exception {
    this.takeAction();
/*    if (alertMsg != null) {
      return Json.createObjectBuilder().add("result", alertMsg).build().toString();
    }*/
    String result = RESULT_TIMEOUT;
    Map<String, Object> resultMap = new HashMap<String, Object>();
    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      String ICEConnectionState = "";
      for (WebDriver webDriver : this.getWebDriverList()) {
        ICEConnectionState =
          (String) ((JavascriptExecutor) webDriver).executeScript(getIceConnectionScript());
        if (logger.isInfoEnabled())
          logger.info(webDriver + ": " + ICEConnectionState);
      }
      if (this.checkForFailed(ICEConnectionState)) {
        result = "FAILED";
        break;
      } else {
        if (ICEConnectionState.equalsIgnoreCase("unknown")) {
          Thread.sleep(INTERVAL);
        } else { // The connection doesn't need to be connected, new is enough.
          String SDP;
          int count = 1;
          for (WebDriver webDriver : this.getWebDriverList()) {
            SDP = (String) ((JavascriptExecutor) webDriver).executeScript(getSDPOfferScript());
            if (validateResults(SDP))
              result = "SUCCESSFUL";
            else
              result = "FAILED";
            ((JavascriptExecutor) webDriver).executeScript(stashStatsScript());
            Thread.sleep(INTERVAL);
            Object stats = ((JavascriptExecutor) webDriver).executeScript(getStatsScript());
            resultMap.put("client_" + count, stats);
            count += 1;
          }
          break;
        }
      }
    }
    if (result.equalsIgnoreCase("ERROR"))
      return Json.createObjectBuilder().add("result", "No outbound stream was found by getStats()")
        .build().toString();    else {
      resultMap.put("result", result);
      JsonObjectBuilder tmp = Json.createObjectBuilder();
      for (int i = 1; i <= this.getWebDriverList().size(); i++) {
        String name = "client_" + i;
        if (resultMap.get(name) != null)
          tmp.add(name, Utility.buildSingleStatObject(resultMap.get(name)));
      }
      return Json.createObjectBuilder().add("result", (String) resultMap.get("result"))
        .add("stats", tmp).build().toString();
    }
  }
}

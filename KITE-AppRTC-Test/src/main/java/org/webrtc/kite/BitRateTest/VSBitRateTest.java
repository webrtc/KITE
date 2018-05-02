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

package org.webrtc.kite.BitRateTest;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.KiteTest;
import org.webrtc.kite.stat.Utility;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * VSBitRateTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * WebDriver:
 * <ul>
 * <li>1) Opens the browser with the url specified in APPRTC_URL with option argument.</li>
 * <li>2) Clicks 'confirm-join-button'.</li>
 * <li>3) Do the following every 1 second for 1 minute:</li>
 * <ul>
 * <li>a) Executes the JavaScript on the given via getIceConnectionScript() which returns
 * iceConnectionState.</li>
 * <li>b) Checks whether all the browsers have returned either 'completed' or 'connected'.</li>
 * <li>c) Executes the JavaScript on the given via stashResultScript() which store result and stats
 * in a global variable to fetch later.</li>
 * <li>d) Executes the JavaScript on the given via checkResultScript() which returns the result
 * stashed earlier.</li>
 * <li>e) Executes the JavaScript on the given via getStatsScript() which returns the stats
 * stashed earlier.</li>
 * </ul>
 * <li>4) The test is considered as successful if the bitrate of the sent video is equal to the one in the option</li>
 * <li>5) A successful test returns a boolean 'true' while the unsuccessful test returns a boolean
 * 'false'.</li>
 * </ul>
 * </p>
 */
public class VSBitRateTest extends KiteTest {

  private final static Logger logger = Logger.getLogger(VSBitRateTest.class.getName());

  private final static Map<String, String> expectedResultMap = new HashMap<String, String>();
  private final static String APPRTC_URL = "https://appr.tc/r/";
  private final static int TIMEOUT = 60000;
  private final static int INTERVAL = 1000;
  private final static String RESULT_TIMEOUT = "TIME OUT";
  private final static String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private final static String RESULT_FAILED = "FAILED";
  private static String alertMsg;

  static {
    expectedResultMap.put("completed", "completed");
    expectedResultMap.put("connected", "connected");
  }

  private final String value = "8000";
  private final String option = "arbr=" + value;
  private final double valueDouble = Double.parseDouble(value);

  /**
   * Returns the test's getIceConnectionScript to retrieve appController.call_.pcClient_.pc_.iceConnectionState.
   * If it doesn't exist then the method returns 'unknown'.
   * Initialize some value to use later.
   *
   * @return the getIceConnectionScript as string.
   */
  private final static String getIceConnectionScript() {
    return "var retValue;" +
      "window.result   = 0;" +
      "try {" +
      "   retValue = appController.call_.pcClient_.pc_.iceConnectionState;" +
      "} catch (exception) {} " +
      "if (retValue) {" +
      "   return retValue;" +
      "} else {" +
      "   return 'unknown';}";
  }

  /**
   * Returns the test's stashResultScript to stash the result and stats of the test in a global variable to retrieve later.
   *
   * @return the stashResultScript as string.
   */
  private final static String stashResultScript() {
    return "appController.call_.pcClient_.pc_.getStats().then(data => {" +
      "   window.KITEStats = [...data.values()];" +
      "   [...data.values()].forEach(function(e){" +
      "       if (e.type.startsWith('outbound-rtp')){" +
      "           if (e.mediaType.startsWith('video')){ " +
      "               console.log('e->'+JSON.stringify(e));" +
      "               if (window.result > 0)" +
      "                   window.result = 8*(e.bytesSent - window.result)/1000;" +
      "               else" +
      "                   window.result = e.bytesSent;  " +
      "           }" +
      "       }" +
      "   });" +
      "});" +
      "return 0"; // Just in case //
  }

  /**
   * Returns the test's getResultScript to get the stashed result.
   *
   * @return the getResultScript as string.
   */
  private final static String getResultScript() {
    return "return window.result;";
  }

  /**
   * Returns the test's getResultScript to get the stashed stats.
   *
   * @return the getStatsScript as string.
   */
  private final static String getStatsScript() {
    return "return window.KITEStats;";
  }

  /**
   * Opens the APPRTC_URL and clicks 'confirm-join-button'.
   */
  private void takeAction() {
    Random rand = new Random(System.currentTimeMillis());
    long channel = Math.abs(rand.nextLong());

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
   * Checks whether all of the result strings match at least one entry from the expectedResultMap.
   *
   * @param resultList ice connection states for the browsers as List<String>
   * @return true if all of the results strings match at least one entry from the expectedResultMap.
   */
  private boolean validateResults(List<String> resultList) {
    for (String result : resultList)
      if (expectedResultMap.get(result) == null)
        return false;
    return true;
  }


  /**
   * Checks whether at least one the result string matches 'failed'.
   *
   * @param resultList ice connection states for the browsers as List<String>
   * @return true if atleast one of the result string matches 'failed'.
   */
  private boolean checkForFailed(List<String> resultList) {
    for (String result : resultList)
      if (result.equalsIgnoreCase("failed"))
        return true;
    return false;
  }

  @Override
  public Object testScript() throws Exception {
    this.takeAction();

/*    if (alertMsg != null) {
      return Json.createObjectBuilder().add("result", alertMsg).build().toString();
    }*/
    Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("result", RESULT_TIMEOUT);

    for (int i = 0; i < TIMEOUT; i += INTERVAL) {
      List<String> resultList = new ArrayList<String>();
      List<Double> BRList = new ArrayList<Double>();
      for (WebDriver webDriver : this.getWebDriverList()) {
        String resultOfScript =
          (String) ((JavascriptExecutor) webDriver).executeScript(getIceConnectionScript());
        if (logger.isInfoEnabled())
          logger.info(webDriver + ": " + resultOfScript);
        resultList.add(resultOfScript);
      }

      if (this.checkForFailed(resultList)) {
        Thread.sleep(INTERVAL);
        break;
      } else if (this.validateResults(resultList)) {
        resultList.clear();
        int count = 1;
        for (WebDriver webDriver : this.getWebDriverList()) {
          double temp;
          ((JavascriptExecutor) webDriver).executeScript(stashResultScript());
          Thread.sleep(INTERVAL); // wait for 1s before getting the new value
          ((JavascriptExecutor) webDriver).executeScript(stashResultScript());
          Thread.sleep(INTERVAL);

          temp = Double.parseDouble((((JavascriptExecutor) webDriver).executeScript(getResultScript())).toString());
          BRList.add(temp);
          if (valueDouble / 1.050 < temp && temp < valueDouble / 0.95) // given a tolerance for 50ms variation
            resultList.add(RESULT_SUCCESSFUL);
          else
            resultList.add(RESULT_FAILED);

          Object stats = ((JavascriptExecutor) webDriver).executeScript(getStatsScript());
          resultMap.put("client_" + count, stats);
          count += 1;
        }
        if (resultList.contains(RESULT_FAILED)) {
          if (!resultList.contains(RESULT_SUCCESSFUL))
            resultMap.put("result", RESULT_FAILED);
          else {
            // Since for now apprtc can only support 2 browsers at a time
            if (resultList.indexOf("FAILED") == 0)
              resultMap.put("result", "FAILED for the caller (VSBR at " + BRList.get(0) + "kbps)");
            else
              resultMap.put("result", "FAILED for the callee (VSBR at " + BRList.get(1) + "kbps)");
          }
        } else
          resultMap.put("result", RESULT_SUCCESSFUL);
        break;
      } else {
        Thread.sleep(INTERVAL);
      }
    }

    JsonObjectBuilder tmp = Json.createObjectBuilder();
    for (int i = 1; i <= this.getWebDriverList().size(); i++) {
      String name = "client_" + i;
      if (resultMap.get(name) != null)
        tmp.add(name, Utility.buildSingleStatObject(resultMap.get(name)));
    }

    return Json.createObjectBuilder().add("result", (String) resultMap.get("result"))
      .build().toString();
  }
}

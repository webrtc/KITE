/*
 * Copyright 2017 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.webrtc.kite;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

/**
 * NoAdapterTest implementation of KiteTest.
 * <p>
 * The testScript() implementation does the following in sequential manner on the provided array of
 * WebDriver:
 * <ul>
 * <li>1) Opens all the browsers with the url specified in NOADAPTER_URL.</li>
 * <li>2) Clicks START_BUTTON.</li>
 * <li>3) Do the following every 1 second for 1 minute:</li>
 * <ul>
 * <li>a) Executes the JavaScript on all browsers given via testJavaScript() which returns
 * iceConnectionState.</li>
 * <li>b) Checks whether all the browsers have returned either 'completed' or 'connected'.</li>
 * </ul>
 * <li>4) The test is considered as successful if all the browsers either returns 'completed' or
 * 'connected' within 1 minute.</li>
 * <li>5) A successful test returns a boolean 'true' while the unsuccessful test returns a boolean
 * 'false'</li>
 * </ul>
 * </p>
 */
public class NoAdapterTest extends KiteTest {

  private final static Logger logger = Logger.getLogger(NoAdapterTest.class.getName());

  private final static Map<String, String> expectedResultMap = new HashMap<String, String>();

  static {
    expectedResultMap.put("completed", "completed");
    expectedResultMap.put("connected", "connected");
  }

  private final static String RESULT_TIMEOUT = "TIME OUT";
  private final static String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private final static String RESULT_FAILED = "FAILED";

  private final static String CHANNEL_INPUT = "channelId";
  private final static String START_BUTTON = "startButton";
  private static String url = null;
  private static String IP = "localhost";
  private static int port = 8083;

  private final static int TIMEOUT = 30000;
  private final static int INTERVAL = 1000;
  private static String alertMsg = null;

  private final static Random rand = new Random(System.currentTimeMillis());

  /**
   * Check whether a browser is ready to take calls
   *
   * @return true if browser is ready to take calls, false otherwise
   */
  private final static String TEST_READY_JS = "return (readyForCalls);";

  private boolean readyForCalls(final WebDriver webDriver) {
    final Object browserReturn = ((JavascriptExecutor) webDriver).executeScript(TEST_READY_JS);
    // undefined will return as null
    final Boolean isReady = (browserReturn == null) ? false : (Boolean) browserReturn;
    if (logger.isDebugEnabled())
      logger.debug(webDriver + " - testing readyForCalls: " + isReady);
    return isReady;
  }


  /**
   * Restructuring the test according to options given in payload object from config file.
   * This function will not be the same for every test.
   */
  private void payloadHandling() {
    if (this.getPayload() != null) {
      JsonValue jsonValue = this.getPayload();
      JsonObject payload = (JsonObject) jsonValue;
      url = payload.getString("url", null);
      if (url == null ) {
        IP = payload.getString("ip", "localhost");
        port = payload.getInt("port", 8083);
      }
    }
  }

  /**
   * Opens the NOADAPTER_URL, fills channelId input and clicks startButton
   *
   * @return true if all browsers are ready for calls, false otherwise
   */
  private boolean takeAction() {
    payloadHandling();
    final String channelId = Long.toString(Math.abs(rand.nextLong()));
    for (WebDriver webDriver : this.getWebDriverList()) {
      if (url == null ) {
        url = new StringBuilder("https://")
          .append(IP).append(":").append(port).append("/").toString();
      }
      webDriver.get(url);
      webDriver.findElement(By.id(CHANNEL_INPUT)).sendKeys(channelId);
      webDriver.findElement(By.id(START_BUTTON)).click();
      try {
        Alert alert = webDriver.switchTo().alert();
        alertMsg = alert.getText();
        if (alertMsg != null) {
          alertMsg = ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName() + " alert: " +alertMsg;
          alert.accept();
        }
      } catch (NoAlertPresentException e) {
        alertMsg = null;
      }
      // check that browser is ready for taking calls before initializing next one
      boolean isReady = false;
      for (int readinessTries = 0; readinessTries < TIMEOUT; readinessTries += INTERVAL) {
        isReady = readyForCalls(webDriver);
        if (isReady) {
          break;
        }
        try {
          Thread.sleep(INTERVAL);
        } catch (InterruptedException e) {
          logger.error("While testing for readiness - try number: " + readinessTries, e);
          return false;
        }
      }
      if (!isReady) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the test JavaScript to retrieve iceConnectionState.
   * If it doesn't exist then the method returns 'unknown'.
   *
   * @return the JavaScript as string.
   */
  private final static String JS_CHECK =
      "var retValue; try {retValue = peerConn.iceConnectionState;} catch (exception) {} "
          + "if (retValue) {return retValue;} else {return 'unknown';}";

  private String testJavaScript() {
    return JS_CHECK;
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
   * Checks whether atleast one the result string matches 'failed'.
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
    payloadHandling();
    final boolean actionResult = this.takeAction();
    if (!actionResult) {
/*      if (alertMsg != null) {
        return Json.createObjectBuilder().add("result", alertMsg).build().toString();
      }*/
      return Json.createObjectBuilder().add("result", RESULT_TIMEOUT).build().toString();
    }
/*    if (alertMsg != null) {
      return Json.createObjectBuilder().add("result", alertMsg).build().toString();
    }*/
    String result = RESULT_TIMEOUT;

    for (int i = 0; i < TIMEOUT; i += INTERVAL) {

      List<String> resultList = new ArrayList<String>();
      for (WebDriver webDriver : this.getWebDriverList()) {
        String resultOfScript =
            (String) ((JavascriptExecutor) webDriver).executeScript(this.testJavaScript());
        if (logger.isDebugEnabled())
          logger.debug(webDriver + ": " + resultOfScript);
        resultList.add(resultOfScript);
      }

      if (this.checkForFailed(resultList)) {
        result = RESULT_FAILED;
        break;
      } else if (this.validateResults(resultList)) {
        result = RESULT_SUCCESSFUL;
        break;
      } else {
        Thread.sleep(INTERVAL);
      }
    }

    return Json.createObjectBuilder().add("result", result).build().toString();
  }

}

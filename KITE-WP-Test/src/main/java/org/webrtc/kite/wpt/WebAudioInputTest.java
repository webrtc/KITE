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

package org.webrtc.kite.wpt;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.webrtc.kite.KiteTest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.webrtc.kite.wpt.Utility.*;

/**
 * WebAudioInputTest implementation of KiteTest.
 *
 * <p>The testScript() implementation does the following in sequential manner on the provided array
 * of WebDriver:
 *
 * <ul>
 *   <li>1) Opens all the browsers with the url specified in config file.
 *   <li>2) Generates audio stream with WebAudio.
 *   <li>3) Add generated audio stream to peer connection.
 *   <li>4) Send media stream to 2nd peer connection.
 *   <li>5) The test is considered as successful the ICE connection state changes to completed or connected.
 * </ul>
 */
public class WebAudioInputTest extends KiteTest {

  private static final Logger logger = Logger.getLogger(WebAudioInputTest.class.getName());

  private static final String RESULT_FAILED = "FAILED";
  private static final String RESULT_TIMEOUT = "TIME OUT";
  private static final String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private static final int TIMEOUT = 30000;

  private static final int INTERVAL = 1000;

  private static String url;
  private static String alertMsg = null;

  /**
   * Restructuring the test according to options given in payload object from config file. This
   * function will not be the same for every test.
   */
  private void payloadHandling() {
    if (this.getPayload() != null) {
      JsonValue jsonValue = this.getPayload();
      JsonObject payload = (JsonObject) jsonValue;
      url = payload.getString("url", null);
    }
  }

  /** Opens the MULTI_STREAM_URL, fills channelId input and clicks startButton */
  private void takeAction() throws Exception {
    payloadHandling();
    if (this.getWebDriverList().size() > 1) {
      throw new Exception("This test is limited to 1 browser only");
    }
    for (WebDriver webDriver : this.getWebDriverList()) {
      if (url == null) {
        throw new Exception("No URL was specified");
      }
      webDriver.get(url);
      try {
        Alert alert = webDriver.switchTo().alert();
        ////alertMsg = alert.getText();URL
        if (alertMsg != null) {
          alertMsg =
              ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName()
                  + " alert: "
                  + alertMsg;
          alert.accept();
        }
      } catch (NoAlertPresentException e) {
        alertMsg = null;
      }
    }
  }

  @Override
  public Object testScript() throws Exception {
    this.takeAction();
    JsonObjectBuilder res = Json.createObjectBuilder();
    JsonObjectBuilder stats = Json.createObjectBuilder();
    List<JsonObject> browserResults = new ArrayList<>();
    List<Boolean> resultList = new ArrayList<>();
    try {
      List<WebDriver> webDriverList = this.getWebDriverList();
      List<Tester> testerList = new ArrayList<>();
      for (WebDriver webDriver : webDriverList) {
        testerList.add(new Tester(webDriver));
      }
      ExecutorService executorService = Executors.newFixedThreadPool(webDriverList.size());
      List<Future<JsonObject>> futureList =
          executorService.invokeAll(testerList, 3, TimeUnit.MINUTES);
      executorService.shutdown();
      for (Future<JsonObject> future : futureList) {
        try {
          JsonObject jsonObject = future.get();
          browserResults.add(jsonObject);
        } catch (Exception e) {
          browserResults.add(null);
          logger.error(
              "Exception in Tester: "
                  + e.getLocalizedMessage()
                  + "\r\n"
                  + e.getStackTrace());
          logger.error(
              "Cause Exception in Tester: "
                  + e.getCause().getLocalizedMessage()
                  + "\r\n"
                  + e.getCause());
        }
      }
    } catch (TimeoutException e) {
      res.add("result", RESULT_TIMEOUT).add("stats", Json.createObjectBuilder());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for (int i = 0; i < browserResults.size(); i++) {
      JsonObject browserResult = browserResults.get(i);
      if (browserResult == null) {
        resultList.add(false);
      } else {
        String browser = browserResult.getString("browser", "client_" + (i + 1));
        browser = (i + 1) + "_" + browser;
        resultList.add(browserResult.getBoolean("result"));
        stats.add(browser, browserResult.getJsonObject("stats"));
      }
    }

    if (resultList.contains(false)) {
      res.add("result", RESULT_FAILED);
    } else {
      res.add("result", RESULT_SUCCESSFUL);
    }

    res.add("stats", stats);

    return res.build().toString();
  }

  private class Tester implements Callable<JsonObject> {
    private final WebDriver webDriver;
    private String browser;

    public Tester(WebDriver webDriver) {
      this.webDriver = webDriver;
      Capabilities capabilities = ((RemoteWebDriver) this.webDriver).getCapabilities();
      this.browser =
          capabilities.getBrowserName()
              + "_"
              + capabilities.getVersion()
              + "_"
              + capabilities.getPlatform();
    }

    @Override
    public JsonObject call() throws Exception {
      WebDriverWait wait = new WebDriverWait(webDriver, TIMEOUT);
      JsonObjectBuilder res = Json.createObjectBuilder();
      JsonObjectBuilder stats = Json.createObjectBuilder();
      res.add("stats", stats);
      try {
        res.add("browser", browser);
        boolean everythingOK;
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("getMediaButton")));
        WebElement button = this.webDriver.findElement(By.id("getMediaButton"));
        button.click();
        Thread.sleep(2000);
        button = this.webDriver.findElement(By.id("sendMediaButton"));
        button.click();
        everythingOK = checkPeerConnectionObject(webDriver, TIMEOUT, INTERVAL);


        if (!everythingOK) {
          logger.error("No peer connection was found.");
          res.add("result", false);
          stats.add("log", getLog(this.webDriver));
          res.add("stats", stats);
          return res.build();
        } else {
          everythingOK = checkPeerConnectionState(webDriver, TIMEOUT, INTERVAL);
        }


        if (!everythingOK) {
          logger.error("Ice connection was not established");
          res.add("result", false);
          stats.add("log", getLog(this.webDriver));
          res.add("stats", stats);
          return res.build();
        } else {
          res.add("result", true);
          stats.add("log", getLog(this.webDriver));
          res.add("stats", stats);
          return res.build();
        }
      } catch (TimeoutException e) {
        res.add("result", false);
        stats.add("log", "Couldn't find the buttons to click");
        res.add("stats", stats);
        return res.build();
      }
    }
  }
}

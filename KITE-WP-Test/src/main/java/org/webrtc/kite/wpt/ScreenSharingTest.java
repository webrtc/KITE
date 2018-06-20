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
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.webrtc.kite.wpt.Utility.*;

/**
 * ScreenSharingTest implementation of KiteTest.
 *
 * <p>The testScript() implementation does the following in sequential manner on the provided array
 * of WebDriver:
 *
 * <ul>
 *   <li>1) Opens all the browsers with the url specified in url.
 *   <li>2) Start publishing stream on publisher browser.
 *   <li>3) Waits for videos to be displayed on page and verifies if they are actually being
 *       displayed.
 *   <li>4) Connect other subscriber browsers to the same channel of the publishing browser.
 *   <li>5) Waits for videos to be displayed on page and verifies if they are actually being
 *       displayed.
 *   <li>6) The test is considered as successful if all the videos are displayed correctly.
 * </ul>
 */
public class ScreenSharingTest extends KiteTest {

  private static final Logger logger = Logger.getLogger(ScreenSharingTest.class.getName());

  private static final String RESULT_FAILED = "FAILED";
  private static final String RESULT_TIMEOUT = "TIME OUT";
  private static final String RESULT_SUCCESSFUL = "SUCCESSFUL";
  private static final String ROOM_DESC = "desc";
  private static final String ROOM_ID = "roomid";
  private static final String START_BUTTON = "start";
  private static final String CREATE_BUTTON = "create";
  private static final String JOIN_BUTTON = "join";
  private static final int TIMEOUT = 30000;

  private static final int INTERVAL = 1000;
  private static final Random rand = new Random(System.currentTimeMillis());

  private static String url = "https://janus.conf.meetecho.com/screensharingtest.html";
  private static String alertMsg = null;
  private static String roomId = null;

  /**
   * Restructuring the test according to options given in payload object from config file. This
   * function will not be the same for every test.
   */
  private void payloadHandling() {
    if (this.getPayload() != null) {
      JsonValue jsonValue = this.getPayload();
      JsonObject payload = (JsonObject) jsonValue;
      url = payload.getString("url", "https://janus.conf.meetecho.com/screensharingtest.html");
    }
  }

  /** Opens the MULTI_STREAM_URL, fills channelId input and clicks startButton */
  private void takeAction() throws Exception {
    payloadHandling();
    final String channelId = Long.toString(Math.abs(rand.nextLong()));
    logger.debug("Screen sharing Channel Id" +
            ": "+ channelId);
    if (this.getWebDriverList().size()<2){
      throw  new Exception("Not enough browsers for testing, need at least 2");
    }
    // publishing screen on the first webdriver
    WebDriver publisher = this.getWebDriverList().get(0);
    publisher.get(url);
    WebDriverWait publisherWait = new WebDriverWait(publisher, TIMEOUT);
    publisherWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id(START_BUTTON)));
    // click start button
    publisher.findElement(By.id(START_BUTTON)).click();
    publisherWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id(ROOM_DESC)));
    publisher.findElement(By.id(ROOM_DESC)).sendKeys(channelId);
    publisher.findElement(By.id(CREATE_BUTTON)).click();
    Robot robot = new Robot();
    String browserName = ((RemoteWebDriver)publisher).getCapabilities().getBrowserName();
    switch (browserName){
      case "chrome":
        robot.delay(2000);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(200);
        publisherWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("bootbox-body")));
        /*WebElement popUpMessage = publisher.findElements(By.className("bootbox-body")).get(0);
        roomId = popUpMessage.findElements(By.tagName("b")).get(0).getText();*/
        robot.delay(200);
        WebElement popUpFooter = publisher.findElements(By.className("modal-footer")).get(0);
        popUpFooter.findElements(By.tagName("button")).get(0).click();
        publisherWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("session")));
        roomId = publisher.findElement(By.id("session")).getText();
        logger.debug("room id is :" + roomId);
        break;
        default:
          return;
    }
    if (!checkVideoDisplay(publisher, 0, TIMEOUT, INTERVAL)) {
      roomId = null;
    }

  }

  @Override
  public Object testScript() throws Exception {
    this.takeAction();
    JsonObjectBuilder res = Json.createObjectBuilder();
    JsonObjectBuilder stats = Json.createObjectBuilder();
    List<JsonObject> browserResults = new ArrayList<>();
    List<Boolean> resultList = new ArrayList<>();
    if (roomId != null) {
      try {
        List<WebDriver> webDriverList = this.getWebDriverList();
        List<Tester> testerList = new ArrayList<>();
        for (int i = 1; i< this.getWebDriverList().size(); i++) {
          testerList.add(new Tester(this.getWebDriverList().get(i)));
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
            e.printStackTrace();
            logger.error(
                "Exception in Tester: " + e.getLocalizedMessage() + "\r\n" + e.getStackTrace());
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

    } else {
      res.add("result", RESULT_FAILED);
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
      int retry = 0;
      WebDriverWait subscriberWait = new WebDriverWait(this.webDriver, TIMEOUT);

      JsonObjectBuilder res = Json.createObjectBuilder();
      JsonObjectBuilder stats = Json.createObjectBuilder();
      res.add("stats", stats);
      res.add("browser", browser);
      while (retry < 2){
        try {
          boolean everythingOK;
          this.webDriver.get(url);
          subscriberWait.until(
              ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id(START_BUTTON)));
          // click start button
          this.webDriver.findElement(By.id(START_BUTTON)).click();
          subscriberWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id(ROOM_ID)));
          this.webDriver.findElement(By.id(ROOM_ID)).sendKeys(roomId);
          this.webDriver.findElement(By.id(JOIN_BUTTON)).click();
          //subscriberWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("screenvideo")));
          everythingOK = checkVideoDisplay(this.webDriver, 0, TIMEOUT, INTERVAL);
          System.out.println(everythingOK);
          if (!everythingOK) {
            if (retry < 2){
              System.out.println("Error displaying shared screen on first try");
              logger.error("Error displaying shared screen on first try");
              logger.error("Retrying");
              retry += 1;
            } else {
              logger.error("Error displaying shared screen");
              res.add("result", false);
              // stats.add("log", getLog(this.webDriver));
              res.add("stats", stats);
              return res.build();
            }
          } else {
            res.add("result", true);
            // stats.add("log", getLog(this.webDriver));
            res.add("stats", stats);
            return res.build();
          }
        } catch (WebDriverException e) {

          if (retry < 2){
            System.out.println("Error displaying shared screen on first try");
            logger.error("Error displaying shared screen on first try");
            logger.error("Retrying");
            retry += 1;
          } else {
            res.add("result", false);
            stats.add("log", "Timeout waiting for shared screen after" + (retry+1) +" tri(es)");
            res.add("stats", stats);
            return res.build();
          }
        }
      }
      res.add("result", false);
      stats.add("log", "Timeout waiting for shared screen after 2 tries");
      res.add("stats", stats);
      return res.build();
    }
  }
}


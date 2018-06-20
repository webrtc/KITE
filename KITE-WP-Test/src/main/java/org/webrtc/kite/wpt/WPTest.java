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

package org.webrtc.kite.wpt;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.webrtc.kite.KiteTest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.*;
import java.util.logging.Logger;

/**
 * WPTest implementation of KiteTest.
 */
public class WPTest extends KiteTest {

  private final static Logger logger = Logger.getLogger(WPTest.class.getName());
  private final static int TEST_TIME_OUT = 40;
  private static String IP = "localhost";
  private static int port = 8000;
  private static Map<String, List> testSuiteList = new HashMap<>();
  private boolean RUN_ALL;
  private Test root = null;

  /**
   * Restructuring the test according to options given in payload object from config file.
   * This function will not be the same for every test.
   */
  private void payloadHandling() {
    if (this.getPayload() != null) {
      JsonValue jsonValue = this.getPayload();
      JsonObject payload = (JsonObject) jsonValue;
      IP = payload.getString("ip", "localhost");
      port = payload.getInt("port", 8000);
      RUN_ALL = payload.getBoolean("all", false);
    }
  }


  /**
   * Opens the WPT_SERVER_URL and clicks collect wp tests.
   */
  private void takeAction() throws Exception {
    this.RUN_ALL = false;
    if (this.getWebDriverList().size() > 1) {
      throw new Exception("This test is limited to 1 browser only");
    }
    String ROOT_URL = new StringBuilder("http://")
        .append(IP).append(":").append(port).append("/").toString();
    root = new Test(ROOT_URL, null);
    retrieveTests(root.getTestLink(), root, RUN_ALL);


  }


  /**
   * Retrieve all automated web-platform tests.
   *
   * @param URL     base URL to find tests
   * @param root    parent test
   * @param RUN_ALL decides whether to run all wpt or just webrtc related ones
   */

  private void retrieveTests(String URL, Test root, boolean RUN_ALL) {

    for (WebDriver webDriver : this.getWebDriverList()) {
      webDriver.get(URL);
      List<WebElement> liElemList = webDriver.findElements(By.tagName("li"));
      List<String> fileList = this.fileFiltering(liElemList, URL);

      for (String file : fileList) {
        List<String> brokenDownURL = Arrays.asList(file.split("/"));
        String testName = brokenDownURL.get(brokenDownURL.size() - 1);
        Test temp = new Test(file, root);
        if (file.endsWith("html")) {
          temp.setTest(true);
          temp.setName(testName);
          root.addChild(temp);
        } else {
          if (RUN_ALL) {
            retrieveTests(file, temp, RUN_ALL);
            if (temp.getChildren().size() > 0)
              temp.setName(testName);
            root.addChild(temp);
          } else {
            if (root.getParent() == null) {
              // test suite filtering
              if (TestList.WEBRTC_TEST_LIST.contains(testName)) {
                temp.setName(testName);
                retrieveTests(file, temp, RUN_ALL);
                if (temp.getChildren().size() > 0)
                  root.addChild(temp);
              }
            }
          }
        }
      }
    }
  }

  /**
   * filtering out all non-automated web-platform tests.
   */
  private List<String> fileFiltering(List<WebElement> fileList, String parentAddress) {
    List<String> res = new ArrayList<>();
    for (WebElement file : fileList) {
      String fileName = file.getText();
      if (fileName.startsWith(".") ||
          fileName.startsWith("css") ||
          fileName.startsWith("testharness") ||
          fileName.equalsIgnoreCase("html") ||
          fileName.equalsIgnoreCase("wpt")) {
        // Skip these files
      } else {
        List<String> breakDown = Arrays.asList(fileName.split("\\."));
        if (breakDown.size() < 2) {
          if (file.getAttribute("class").equalsIgnoreCase("dir")) {
            res.add(parentAddress + "/" + fileName);
          }
        } else {
          String postFix = breakDown.get(breakDown.size() - 1);
          if (postFix.equalsIgnoreCase("html")) {
            res.add(parentAddress + "/" + fileName);
          }
        }
      }
    }
    return res;
  }

  private JsonObjectBuilder runTest(WebDriver webDriver, Test test) {
    Capabilities cap = ((RemoteWebDriver) webDriver).getCapabilities();
    boolean edge16 = false;
    if (cap.getBrowserName().equalsIgnoreCase("MicrosoftEdge") && cap.getVersion().contains("16299"))
      edge16 = true;
    JsonObjectBuilder result = Json.createObjectBuilder();
    if (test.getChildren().size() == 0) {
      System.out.println(cap.getBrowserName() + "_" + cap.getVersion() + "_" + cap.getPlatform() + " - running ->> " + test.getName());
      JsonObjectBuilder subTest = Json.createObjectBuilder();
      JsonObjectBuilder unitTests = Json.createObjectBuilder();

      if (test.getName().startsWith("RTCConfiguration-iceServers") && edge16) {
        //skip
        subTest.add("harness", "failed").add("isTest", true);
        subTest.add("passed", 0);
        subTest.add("total", 0);
        subTest.add("timeout", 0);
        subTest.add("failed", 0);
        subTest.add("tests", unitTests);
      } else {

        webDriver.get(test.getTestLink());
        WebDriverWait wait = new WebDriverWait(webDriver, TEST_TIME_OUT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));
        WebElement harness = webDriver.findElement(By.id("summary")).findElement(By.tagName("p"));
        String harnessStatus = harness.getText();
        subTest.add("harness", harnessStatus).add("isTest", true);

        WebElement resultTable = webDriver.findElement(By.id("results"));
        List<WebElement> pass = resultTable.findElements(By.className("pass"));
        List<WebElement> fail = resultTable.findElements(By.className("fail"));
        List<WebElement> timeout = resultTable.findElements(By.className("timeout"));
        int count = 0;
        int total = 0;
        for (WebElement element : pass) {
          unitTests.add(element.findElements(By.tagName("td")).get(1).getText(), "passed");
          count += 1;
        }
        subTest.add("passed", count);
        total += count;
        test.getParent().setPassed(test.getParent().getPassed() + count);
        count = 0;
        for (WebElement element : fail) {
          unitTests.add(element.findElements(By.tagName("td")).get(1).getText(), element.findElements(By.tagName("td")).get(2).getText());
          //subTest.add(element.findElements(By.tagName("td")).get(1).getText(), "failed");
          count += 1;
        }
        subTest.add("fail", count);
        total += count;
        count = 0;
        for (WebElement element : timeout) {
          unitTests.add(element.findElements(By.tagName("td")).get(1).getText(), "timeout");
          count += 1;
        }
        subTest.add("timeout", count);
        total += count;
        subTest.add("total", total);
        subTest.add("tests", unitTests);
        test.getParent().setTotal(test.getParent().getTotal() + total);
      }
      return subTest;
    } else {
      List<Test> testList = test.getChildren();
      for (Test subTest : testList) {
        result.add(subTest.getName(), runTest(webDriver, subTest));
      }
      if (test.getParent() != null)
        result.add("total", test.getTotal()).add("passed", test.getPassed()).add("isTest", false);
    }
    return result;
  }


  @Override
  public Object testScript() throws Exception {
    this.takeAction();
    JsonObjectBuilder result = Json.createObjectBuilder();
    long duration = System.currentTimeMillis();
    result.add("result", runTest(this.getWebDriverList().get((0)), root));
    result.add("all", this.RUN_ALL);
    duration = System.currentTimeMillis() - duration;
    duration = duration / 1000;
    System.out.println("total duration - >>>  " + duration / 60 + "minutes and " + duration % 60 + "s");
    return result.build().toString();
  }

}
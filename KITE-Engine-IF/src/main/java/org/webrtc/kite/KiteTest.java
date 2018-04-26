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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Parent class for a test case.
 * <p>
 * It provides a list of WebDriver objects to the child classes to execute the test algorithm.
 */
public abstract class KiteTest {

  private JsonValue payload;

  private List<WebDriver> webDriverList;

  protected JsonValue getPayload() {
    return this.payload;
  }

  /**
   * Method to set a payload.
   *
   * @param payload JsonValue
   */
  protected void setPayload(JsonValue payload) {
    this.payload = payload;
  }

  protected List<WebDriver> getWebDriverList() {
    return this.webDriverList;
  }

  /**
   * Method to set a web driver list.
   *
   * @param webDriverList Web Driver List
   */
  public void setWebDriverList(List<WebDriver> webDriverList) {
    this.webDriverList = webDriverList;
  }

  /**
   * Tests against List<WebDriver>
   *
   * @return Any object with a toString() implementation.
   * @throws Exception if an Exception occurs while method execution.
   */
  public abstract Object testScript() throws Exception;

  /**
   *
   * @param driver the subject web driver that we want to get console log
   * @return List of log entries.
   */
  public List<String> analyzeLog(WebDriver driver) {
    List<String> log = new ArrayList<>();
    Set<String> logTypes =  driver.manage().logs().getAvailableLogTypes();
    if (logTypes.contains(LogType.BROWSER)) {
      LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
      for (LogEntry entry : logEntries) {
        log.add(entry.getLevel() + " " + entry.getMessage());
      }
    } else {
      log.add("This browser does not support getting console log.");
    }
    return log;
  }

}

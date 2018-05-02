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

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.webrtc.kite.config.Browser;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Utility class serving WebDriver related operations.
 */
public class WebDriverUtility {

  private static final Logger logger = Logger.getLogger(WebDriverUtility.class.getName());

  /**
   * Gets web driver for browser.
   *
   * @param testName the test name
   * @param browser  the browser
   * @param id       an ID to identify the WebDriver
   * @return the web driver for browser
   * @throws MalformedURLException the malformed url exception
   * @throws WebDriverException    the web driver exception
   */
  public static WebDriver getWebDriverForBrowser(String testName, Browser browser, String id)
      throws MalformedURLException, WebDriverException {
    WebDriver webDriver = WebDriverFactory.createWebDriver(browser, testName, id);
    Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
    browser.setWebDriverVersion(capabilities.getVersion());
    browser.setWebDriverPlatform(capabilities.getPlatform().name());
    return webDriver;
  }

  /**
   * Gets web driver for browser.
   *
   * @param testName the test name
   * @param browser  the browser
   * @return the web driver for browser
   * @throws MalformedURLException the malformed url exception
   * @throws WebDriverException    the web driver exception
   */
  public static WebDriver getWebDriverForBrowser(String testName, Browser browser)
      throws MalformedURLException, WebDriverException {
    return WebDriverUtility.getWebDriverForBrowser(testName, browser, "");
  }

  /**
   * Populate info from navigator.
   *
   * @param webDriver the web driver
   * @param browser   the browser
   */
  public static void populateInfoFromNavigator(WebDriver webDriver, Browser browser) {
    if (!browser.shouldGetUserAgent() || !WebDriverUtility.isAlive(webDriver))
      return;

    webDriver.get("http://www.google.com");
    Object resultObject = ((JavascriptExecutor) webDriver).executeScript(userAgentScript());
    logger.info("Browser platform and userAgent for: " + browser.toString() + "->" + resultObject);

    if (resultObject instanceof String) {
      String resultOfScript = (String) resultObject;
      browser.setUserAgentVersionAndPlatfom(resultOfScript);
    }
  }

  /**
   * Close drivers.
   *
   * @param webDriverList the web driver list
   */
  public static void closeDrivers(List<WebDriver> webDriverList) {
    for (WebDriver webDriver : webDriverList)
      try {
        // Open about:config in case of fennec (Firefox for Android) and close.
        if (((RemoteWebDriver) webDriver).getCapabilities().getBrowserName()
            .equalsIgnoreCase("fennec")) {
          webDriver.get("about:config");
          webDriver.close();
        }
        webDriver.quit();
      } catch (Exception e) {
        logger.error("Exception while closing/quitting the WebDriver", e);
      }
  }

  private static Boolean isAlive(WebDriver webDriver) {
    try {
      webDriver.getCurrentUrl();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private final static String userAgentScript() {
    return "var nav = '';" + "try { var myNavigator = {};"
        + "for (var i in navigator) myNavigator[i] = navigator[i];"
        + "nav = JSON.stringify(myNavigator); } catch (exception) { nav = exception.message; }"
        + "return nav;";
  }

}

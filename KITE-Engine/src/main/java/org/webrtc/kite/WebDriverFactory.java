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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.Mobile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Factory object for creating a web driver.
 */
public class WebDriverFactory {

  /**
   * Creates a web driver based on the given Browser object.
   *
   * @param browser  Browser
   * @param testName the test name
   * @param id       an ID to identify the WebDriver
   * @return WebDriver web driver
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of the browser,
   *                               or an unknown protocol is found, or spec is null.
   */
  public static WebDriver createWebDriver(Browser browser, String testName, String id)
      throws MalformedURLException {
    return new RemoteWebDriver(new URL(browser.getRemoteAddress()),
        WebDriverFactory.createCapabilities(browser, testName, id));
  }

  /**
   * Creates a web driver based on the given Browser object.
   *
   * @param browser  Browser
   * @param testName the test name
   * @return WebDriver web driver
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of the browser,
   *                               or an unknown protocol is found, or spec is null.
   */
  public static WebDriver createWebDriver(Browser browser, String testName)
      throws MalformedURLException {
    return new RemoteWebDriver(new URL(browser.getRemoteAddress()),
        WebDriverFactory.createCapabilities(browser, testName, ""));
  }

  /**
   * Creates a Capabilities object based on the given Browser object.
   *
   * @param browser  Browser
   * @param testName name for individual test case
   * @return Capabilities
   */
  private static Capabilities createCapabilities(Browser browser, String testName, String id) {

    MutableCapabilities capabilities = new MutableCapabilities();

    if (browser.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION,browser.getVersion());
    }
    if (browser.getPlatform() != null) {
      capabilities.setCapability(CapabilityType.PLATFORM_NAME, browser.getPlatform());
    }
    // Remote test identifier
    capabilities.setCapability("name", testName);
    capabilities.setCapability("id", id);

    switch (browser.getBrowserName()) {
      case "chrome":
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("use-fake-ui-for-media-stream");
        chromeOptions.addArguments("use-fake-device-for-media-stream");
        if (browser.isHeadless()) {
          chromeOptions.addArguments("headless");
        }
        /*
         * chromeOptions.addArguments("no-sandbox"); chromeOptions.addArguments("disable-infobars");
         * chromeOptions.addArguments("test-type=browser");
         * chromeOptions.addArguments("disable-extensions");
         * chromeOptions.addArguments("--js-flags=--expose-gc");
         * chromeOptions.addArguments("--disable-default-apps");
         * chromeOptions.addArguments("--disable-popup-blocking");
         * chromeOptions.addArguments("--enable-precise-memory-info");
         */
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        break;
      case "firefox":
        FirefoxProfile firefoxProfile = null;
        String profile = System.getProperty("kite.firefox.profile");
        if (profile != null) {
          switch (browser.getPlatform().toUpperCase()) {
            case "WINDOWS":
              profile += "firefox-h264-profiles/h264-windows";
              break;
            case "MAC":
              profile += "firefox-h264-profiles/h264-mac";
              break;
            case "LINUX":
              profile += "firefox-h264-profiles/h264-linux";
              break;
          }
          firefoxProfile = new FirefoxProfile(new File(profile));
        } else {
          firefoxProfile = new FirefoxProfile();
        }
        firefoxProfile.setPreference("media.navigator.streams.fake", true);
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setProfile(firefoxProfile);
        if (browser.isHeadless()) {
          firefoxOptions.addArguments("-headless");
        }
        capabilities.merge(firefoxOptions);
        //  capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxProfile);
        break;
      case "MicrosoftEdge":
        // capabilities = DesiredCapabilities.edge();
        EdgeOptions MicrosoftEdgeOptions = new EdgeOptions();
        capabilities.setCapability("edgeOptions", MicrosoftEdgeOptions);
        capabilities.setCapability("avoidProxy", true);
        break;
      case "edge":
        // capabilities = DesiredCapabilities.edge();
        EdgeOptions edgeOptions = new EdgeOptions();
        capabilities.setCapability("edgeOptions", edgeOptions);
        capabilities.setCapability("avoidProxy", true);
        break;
      case "safari":
        SafariOptions options = new SafariOptions();
        if (browser.isTechnologyPreview()) {
          options.setUseTechnologyPreview(true);
        }
        capabilities.setCapability(SafariOptions.CAPABILITY, options);
        break;

    }

    // Capabilities for mobile browsers
    Mobile mobile = browser.getMobile();
    if (mobile != null) {
      capabilities.setCapability("deviceName", mobile.getDeviceName());
      capabilities.setCapability("platformName", mobile.getPlatformName());
      capabilities.setCapability("platformVersion", mobile.getPlatformVersion());
      if (mobile.getPlatformName().equalsIgnoreCase("iOS")) {
        capabilities.setCapability("automationName", "XCUITest");
      } else {
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability("noReset", true);
      }
    }

    // Add log preference to webdriver
    // TODO put log preference into config file
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

    return capabilities;

  }

}

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.webrtc.kite.config.Browser;
import java.net.MalformedURLException;
import java.net.URL;
import org.webrtc.kite.config.Mobile;

/**
 * Factory object for creating a web driver.
 */
public class WebDriverFactory {

  /**
   * Creates a web driver based on the given Browser object.
   *
   * @param browser Browser
   * @return WebDriver
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of the browser,
   *         or an unknown protocol is found, or spec is null.
   */
  public static WebDriver createWebDriver(Browser browser, String testName)
      throws MalformedURLException {
    return new RemoteWebDriver(new URL(browser.getRemoteAddress()),
        WebDriverFactory.createCapabilities(browser, testName));
  }

  /**
   * Creates a Capabilities object based on the given Browser object.
   *
   * @param browser Browser
   * @param testName name for individual test case
   * @return Capabilities
   */
  private static Capabilities createCapabilities(Browser browser, String testName) {

    DesiredCapabilities capabilities = new DesiredCapabilities();

    capabilities.setBrowserName(browser.getBrowserName());
    if (browser.getVersion() != null)
      capabilities.setVersion(browser.getVersion());
    if (browser.getPlatform() != null)
      capabilities.setCapability("platform", browser.getPlatform());
    // Remote test identifier
    capabilities.setCapability("name", testName);

    switch (browser.getBrowserName()) {
      case "chrome":
        // capabilities = DesiredCapabilities.chrome();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("use-fake-ui-for-media-stream");
        chromeOptions.addArguments("use-fake-device-for-media-stream");
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
        // capabilities = DesiredCapabilities.firefox();
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("media.navigator.streams.fake", true);
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setProfile(firefoxProfile);
        capabilities.merge(firefoxOptions.toCapabilities());
        /*
         * capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
         */
        break;
      case "MicrosoftEdge":
        // capabilities = DesiredCapabilities.edge();
        capabilities.setCapability("avoidProxy", true);
        break;
      case "safari":
        // capabilities = DesiredCapabilities.safari();
        SafariOptions options = new SafariOptions();
        options.setUseTechnologyPreview(true);
        capabilities.setCapability(SafariOptions.CAPABILITY, options);
        break;
    }

    // Capabilities for mobile browsers
    Mobile mobile = browser.getMobile();
    if (mobile != null) {
      capabilities.setCapability("deviceName", mobile.getDeviceName());
      capabilities.setCapability("platformName", mobile.getPlatformName());
      capabilities.setCapability("platformVersion", mobile.getPlatformVersion());
      if (mobile.getPlatformName().equalsIgnoreCase("iOS"))
        capabilities.setCapability("automationName", "XCUITest");
    }

    return capabilities;

  }

  /**
   * Checks whether a browser is alive.
   *
   * @param webDriver WebDriver
   * @return true if the given WebDriver instance is still alive.
   */
  public static Boolean isAlive(WebDriver webDriver) {
    try {
      webDriver.getCurrentUrl();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

}

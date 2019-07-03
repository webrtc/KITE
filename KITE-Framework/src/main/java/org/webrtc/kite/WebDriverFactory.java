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

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.TestUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.webrtc.kite.config.client.App;
import org.webrtc.kite.config.client.Browser;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.client.MobileSpecs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Factory object for creating a web driver.
 */
public class WebDriverFactory {

  private static final KiteLogger logger = KiteLogger.getLogger(WebDriverFactory.class.getName());

  /**
   * Build capabilities for app appium driver
   *
   * @param app the App client object
   * @return capabilities appium driver
   */
  private static MutableCapabilities buildAppCapabilities(App app) {
    MutableCapabilities capabilities = new MutableCapabilities();
    // The absolute local path or remote http URL to an .ipa or .apk file, or a .zip containing one of these.
    // Appium will attempt to install this app binary on the appropriate device first.

    capabilities.setCapability("app", app.getApp());
    capabilities.setCapability("deviceName", app.retrieveDeviceName());
    capabilities.setCapability("platformName", app.retrievePlatform());
    capabilities.setCapability("platformVersion", app.retrievePlatformVersion());
    if (app.retrievePlatform().name().equalsIgnoreCase("iOS")) {
      capabilities.setCapability("automationName", "XCUITest");
    } else {
      capabilities.setCapability("autoGrantPermissions", true);
      capabilities.setCapability("fullReset", app.isFullReset());
    }
    if (app.getAppPackage() == null || app.getAppActivity() == null) {
      logger.warn("Using [" + app.getApp() + "]: Some mobile applications may require appPackage and appActivity " +
          "to setStartTimestamp properly ..");
      if (app.getAppPackage() != null) {
        capabilities.setCapability("appPackage", app.getAppPackage());
      }
      if (app.getAppActivity() == null) {
        capabilities.setCapability("appActivity", app.getAppActivity());
      }
    } else {
      capabilities.setCapability("appPackage", app.getAppPackage());
      capabilities.setCapability("appActivity", app.getAppActivity());
    }
    return capabilities;
  }

  /**
   * Build capabilities for browser web driver
   *
   * @param browser the Browser client object
   * @return capabilities appium driver
   */
  private static MutableCapabilities buildBrowserCapabilities(Browser browser) {
    MutableCapabilities capabilities = new MutableCapabilities();
    if (browser.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION, browser.getVersion());
    }
    if (browser.retrievePlatform() != null) {
      capabilities.setCapability(CapabilityType.PLATFORM_NAME, browser.retrievePlatform());
    }

    if (browser.getGateway() != null) {
      capabilities.setCapability("gateway", browser.getGateway());
    }

    // Only consider next code block if this is a browser.
    switch (browser.getBrowserName()) {
      case "chrome":
        capabilities.setCapability(ChromeOptions.CAPABILITY, setCommonChromeOptions(browser));
        break;
      case "firefox":
        capabilities.merge(setCommonFirefoxOptions(browser));
        break;
      case "MicrosoftEdge":
        EdgeOptions MicrosoftEdgeOptions = new EdgeOptions();
        capabilities.setCapability("edgeOptions", MicrosoftEdgeOptions);
        capabilities.setCapability("avoidProxy", true);
        break;
      case "safari":
        SafariOptions options = new SafariOptions();
        options.setUseTechnologyPreview(browser.isTechnologyPreview());
        capabilities.setCapability(SafariOptions.CAPABILITY, options);
        break;
    }
    // Add log preference to webdriver
    // TODO put log preference into config file
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    // Capabilities for mobile browser/app
    MobileSpecs mobile = browser.getMobile();
    if (mobile != null) {
      // deviceName:
      // On iOS, this should be one of the valid devices returned by instruments with instruments -s devices.
      // On Android this capability is currently ignored, though it remains required.
      capabilities.setCapability("deviceName", mobile.getDeviceName());
      capabilities.setCapability("platformName", mobile.getPlatformName());
      capabilities.setCapability("platformVersion", mobile.getPlatformVersion());

      if (mobile.getPlatformName().name().equalsIgnoreCase("iOS")) {
        capabilities.setCapability("automationName", "XCUITest");
      } else {
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 300);
      }
      capabilities.setCapability("noReset", true);
    }
    return capabilities;
  }

  /**
   * /**
   * Creates a Capabilities object based on the given Client object.
   *
   * @param client   kite config object
   * @param testName name for individual test case
   * @return the capabilities for creating webdriver
   */
  private static Capabilities createCapabilities(Client client, String testName, String id) {

    MutableCapabilities capabilities;
    if (client instanceof Browser) {
      capabilities = buildBrowserCapabilities((Browser) client);

    } else {
      capabilities = buildAppCapabilities((App) client);
    }
    // Remote test identifier
    if (testName != null) {
      capabilities.setCapability("name", testName);
    }
    if (id != null) {
      capabilities.setCapability("id", id);
    }
    for (String capabilityName : client.getExtraCapabilities().keySet()) {
      logger.info("extraCapabilites : " + capabilityName + ": " + client.getExtraCapabilities().get(capabilityName));
      capabilities.setCapability(capabilityName, client.getExtraCapabilities().get(capabilityName));
    }
    return capabilities;
  }

  /**
   * Creates a web driver based on the given Browser object.
   *
   * @param client           Client
   * @param testName         the test name
   * @param id               an ID to identify the WebDriver
   * @param kiteServerGridId an ID to identify the WebDriver
   * @return WebDriver web driver
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of the client,
   *                               or an unknown protocol is found, or spec is null.
   * @throws WebDriverException    the web driver exception
   */
  public static WebDriver createWebDriver(Client client, String testName, String id, String... kiteServerGridId)
      throws MalformedURLException, WebDriverException {

    URL url = new URL(client.getPaas().getUrl());

    if (client instanceof Browser) {

      WebDriver webDriver = new RemoteWebDriver(url, WebDriverFactory.createCapabilities(client, testName, id));
      String hub = new URL(client.getPaas().getUrl()).getHost();
      String sessionId = ((RemoteWebDriver) webDriver).getSessionId().toString();
      String nodeIp = TestUtils.getPrivateIp(hub, sessionId, "4444");
      if (((Browser) client).getBrowserName().equalsIgnoreCase("firefox")) {
        if (((Browser) client).useFakeMedia()) {
          if (((Browser) client).getVideo() != null || ((Browser) client).getAudio() != null) {
            String gridId = kiteServerGridId.length > 0 ? kiteServerGridId[0] : "null";
            String command = writeCommand((Browser) client);
            Utils.makeCommand(gridId, nodeIp, command);
          }
        }
      }
      return webDriver;
    } else {
      if (client.retrievePlatform().name().equalsIgnoreCase("android")) {
        return new AndroidDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      } else if (client.retrievePlatform().name().equalsIgnoreCase("ios")) {
        return new IOSDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      } else {
        return new AppiumDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      }
    }
  }

  /**
   * Gets web driver for client.
   *
   * @param testName the test name
   * @param client   the client
   * @param id       an ID to identify the WebDriver
   * @return the web driver for client
   * @throws MalformedURLException the malformed url exception
   * @throws WebDriverException    the web driver exception
   */
  public static WebDriver getWebDriverForClient(String testName, Client client, String id)
      throws MalformedURLException, WebDriverException {
    return createWebDriver(client, testName, id);
  }

  /**
   * Gets web driver for client.
   *
   * @param testName the test name
   * @param client   the client
   * @return the web driver for client
   * @throws MalformedURLException the malformed url exception
   * @throws WebDriverException    the web driver exception
   */
  public static WebDriver getWebDriverForClient(String testName, Client client)
      throws MalformedURLException, WebDriverException {
    return createWebDriver(client, testName, "");
  }

  /**
   * Create common chrome option to create chrome web driver
   *
   * @param browser the Browser client object
   * @return the chrome option
   */
  private static ChromeOptions setCommonChromeOptions(Browser browser) {
    ChromeOptions chromeOptions = new ChromeOptions();
    if (browser.useFakeMedia()) {
      // We use fake media or mediafile as webcam/microphone
      chromeOptions.addArguments("use-fake-ui-for-media-stream");
      chromeOptions.addArguments("use-fake-device-for-media-stream");
      // If we use mediafile
      if (browser.getVideo() != null || browser.getAudio() != null) {
        chromeOptions.addArguments("allow-file-access-from-files");
        if (browser.getVideo() != null) {
          chromeOptions.addArguments("use-file-for-fake-video-capture="
              + browser.fetchMediaPath(browser.getVideo(), browser.getBrowserName()));
        }
        if (browser.getAudio() != null) {
          chromeOptions.addArguments("use-file-for-fake-audio-capture="
              + browser.fetchMediaPath(browser.getAudio(), browser.getBrowserName()));
        }
      }
    } else {
      // Create an Hashmap to edit user profile
      Map<String, Object> prefs = new HashMap<String, Object>();
      // Allow access to camera & micro
      prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
      prefs.put("profile.default_content_setting_values.media_stream_mic", 1);
      chromeOptions.setExperimentalOption("prefs", prefs);
    }
    chromeOptions.addArguments("auto-select-desktop-capture-source=Entire screen");
    if (! "electron".equals(browser.getVersion())) {
      // CHROME ONLY
      String extension = System.getProperty("kite.chrome.extension");
      if (extension != null) {
        chromeOptions.addExtensions(new File(extension));
      }
    }
    /*
     * if (browser.getVersion().toLowerCase().contains("electron")) {
     * chromeOptions.setBinary(browser.getSpecs().getPathToBinary()); }
     */
    if (browser.isHeadless()) {
      chromeOptions.addArguments("headless");
    }
    if (browser.getWindowSize() != null) {
      chromeOptions.addArguments("window-size=" + browser.getWindowSize());
    }
    for (String flag : browser.getFlags()) {
      chromeOptions.addArguments(flag);
      // Examples:
      /*
       * chromeOptions.addArguments("--disable-gpu");
       * chromeOptions.addArguments("no-sandbox");
       */
    }
    logger.debug("ChromeOptions: " + chromeOptions.toJson());
    return chromeOptions;
  }

  /**
   * Create common firefox option to create firefox web driver
   *
   * @param browser the Browser client object
   * @return the firefox option
   */
  private static FirefoxOptions setCommonFirefoxOptions(Browser browser) {
    FirefoxProfile firefoxProfile = null;
    String profile = System.getProperty("kite.firefox.profile");
    if (profile != null) {
      switch (browser.retrievePlatform().name().toUpperCase()) {
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
      logger.warn("FIREFOX: Some tests require specific profile for firefox to work properly.");
      firefoxProfile = new FirefoxProfile();
    }
    if (browser.useFakeMedia()) {
      if (browser.getAudio() == null && browser.getVideo() == null) {
        firefoxProfile.setPreference("media.navigator.streams.fake", browser.useFakeMedia());
      }
    }
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    firefoxOptions.setProfile(firefoxProfile);
    if (browser.isHeadless()) {
      firefoxOptions.addArguments("-headless");
    }
    if (browser.getWindowSize() != null) {
      firefoxOptions.addArguments("-window-size " + browser.getWindowSize());
    }
    for (String flag : browser.getFlags()) {
      firefoxOptions.addArguments(flag);
    }
    return firefoxOptions;
  }

  private static String writeCommand(Browser browser) {
    String command = "play ";
    if (browser.getVideo() != null && browser.getAudio() != null) {
      command += browser.fetchMediaPath(browser.getVideo(), browser.getBrowserName())
          + " " + browser.fetchMediaPath(browser.getAudio(), browser.getBrowserName());
    } else if (browser.getVideo() != null && browser.getAudio() == null) {
      command += browser.fetchMediaPath(browser.getVideo(), browser.getBrowserName());
    } else if (browser.getVideo() == null && browser.getAudio() != null) {
      command += browser.fetchMediaPath(browser.getAudio(), browser.getBrowserName());
    } else {
      logger.info("No file provided.");
    }
    return command;
  }
}

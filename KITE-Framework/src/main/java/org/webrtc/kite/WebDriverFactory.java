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
import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.TestUtils;
import io.cosmosoftware.kite.util.WebDriverUtils;
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
import org.webrtc.kite.config.client.Client;

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
  
  //this will be updated by Tomcat during context init according to the actual server settings.
  public static String kiteServerUrl = "http://localhost:8080/KITEServer";
  
  /**
   * Build capabilities for app appium driver
   *
   * @param app the Client object
   * @return capabilities appium driver
   */
  private static MutableCapabilities buildAppCapabilities(Client app) {
    MutableCapabilities capabilities = new MutableCapabilities();
    // The absolute local path or remote http URL to an .ipa or .apk file, or a .zip containing one of these.
    // Appium will attempt to install this app binary on the appropriate device first.

    if (app.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION, app.getVersion());
    }
    capabilities.setCapability("browserName", "app");
    capabilities.setCapability("app", app.getAppName());
    capabilities.setCapability("deviceName", app.getDeviceName());
    capabilities.setCapability("platformName", app.getPlatform());
    if (app.getPlatformVersion() != null) {
      capabilities.setCapability("platformVersion", app.getPlatformVersion());
    }
    if (app.getPlatform().name().toLowerCase().equalsIgnoreCase("ios")) {
      capabilities.setCapability("automationName", "XCUITest");
    }
    if (app.getPlatform().name().toLowerCase().equalsIgnoreCase("android")) {
      capabilities.setCapability("autoGrantPermissions", true);
      capabilities.setCapability("fullReset", app.isFullReset());
    }
    if (app.getApp().getAppWorkingDir() != null) {
      capabilities.setCapability("appWorkingDir", app.getApp().getAppWorkingDir());
    }
    if (app.getAppPackage() == null || app.getAppActivity() == null) {
      logger.warn("Using [" + app.getAppName() + "]: Some mobile applications may require appPackage and appActivity " +
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
   * Build capabilities for client web driver
   *
   * @param client the Client object
   * @return capabilities appium driver
   */
  private static MutableCapabilities buildBrowserCapabilities(Client client) {
    MutableCapabilities capabilities = new MutableCapabilities();
    if (client.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION, client.getVersion());
    }
    if (client.getPlatform() != null) {
      capabilities.setCapability(CapabilityType.PLATFORM_NAME, client.getPlatform());
    }

    if (client.getGateway() != null && !"none".equalsIgnoreCase(client.getGateway())) {
      capabilities.setCapability("gateway", client.getGateway());
    }

    // Only consider next code block if this is a client.
    switch (client.getBrowserName()) {
      case "chrome":
        capabilities.setCapability(ChromeOptions.CAPABILITY, setCommonChromeOptions(client));
        break;
      case "firefox":
        capabilities.merge(setCommonFirefoxOptions(client));
        break;
      case "MicrosoftEdge":
        EdgeOptions MicrosoftEdgeOptions = new EdgeOptions();
        capabilities.setCapability("edgeOptions", MicrosoftEdgeOptions);
        capabilities.setCapability("avoidProxy", true);
        break;
      case "safari":
        SafariOptions options = new SafariOptions();
        options.setUseTechnologyPreview(client.isTechnologyPreview());
        capabilities.setCapability(SafariOptions.CAPABILITY, options);
        break;
      default:
        capabilities.setCapability(CapabilityType.BROWSER_NAME, client.getBrowserName());
        break;
    }
    // Add log preference to webdriver
    // TODO put log preference into config file
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
    // Capabilities for mobile client/app
    if (client.getPlatform().toString().equalsIgnoreCase("android")
      ||client.getPlatform().toString().equalsIgnoreCase("ios")) {
      // deviceName:
      // On iOS, this should be one of the valid devices returned by instruments with instruments -s devices.
      // On Android this capability is currently ignored, though it remains required.
      capabilities.setCapability("deviceName", client.getDeviceName());
      capabilities.setCapability("platformName", client.getPlatform());
      capabilities.setCapability("platformVersion", client.getPlatformVersion());
      if (client.getPlatform().name().equalsIgnoreCase("ios")) {
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("autoAcceptAlerts", true);
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
    if (!client.isApp()) {
      capabilities = buildBrowserCapabilities(client);
    } else {
      capabilities = buildAppCapabilities(client);
    }
    // Remote test identifier
    if (testName != null && !testName.isEmpty()) {
      capabilities.setCapability("name", testName);
    }
    if (id != null && !id.isEmpty()) {
      capabilities.setCapability("id", id);
    }
    for (String capabilityName : client.getExtraCapabilities().keySet()) {
      logger.debug("extraCapabilites : " + capabilityName + ": " + client.getExtraCapabilities().get(capabilityName));
      capabilities.setCapability(capabilityName, client.getExtraCapabilities().get(capabilityName));
    }
    logger.debug("Capabilites for " + client.toString() + " = \r\n" + capabilities);
    return capabilities;
  }

  /**
   * Creates a web driver based on the given Client object.
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
          throws MalformedURLException, WebDriverException, KiteTestException {
    String urlStr = client.getPaas().getUrl();
    logger.debug("createWebDriver on " + urlStr + " for " + client);
    URL url = new URL(urlStr);
    if (!client.isApp()) {
      RemoteWebDriver webDriver = new RemoteWebDriver(url, WebDriverFactory.createCapabilities(client, testName, id));
      if (client.getBrowserName().equalsIgnoreCase("firefox")) {
        if (client.useFakeMedia()) {
          if (client.getVideo() != null || client.getAudio() != null) {
            String nodePublicIp = new URL(TestUtils.getNode(urlStr, webDriver.getSessionId().toString())).getHost();
            String gridId = kiteServerGridId.length > 0 ? kiteServerGridId[0] : "null";
            String command = writeCommand(client);
            String uri = "/command?id=" + gridId + "&ip=" + nodePublicIp + "&cmd=" + command;
            logger.debug("kiteServerCommand response:\r\n" + TestUtils.kiteServerCommand(kiteServerUrl, uri));
          }
        }
      }
      return webDriver;
    } else {
      if (client.getPlatform().name().equalsIgnoreCase("android")) {
        return new AndroidDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      } else if (client.getPlatform().name().equalsIgnoreCase("ios")) {
        return new IOSDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      } else {
        return new RemoteWebDriver(url, WebDriverFactory.createCapabilities(client, testName, id));
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
          throws MalformedURLException, WebDriverException, KiteTestException {
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
          throws MalformedURLException, WebDriverException, KiteTestException {
    return createWebDriver(client, testName, "");
  }

  /**
   * Create common chrome option to create chrome web driver
   *
   * @param client the Client object
   * @return the chrome option
   */
  private static ChromeOptions setCommonChromeOptions(Client client) {
    ChromeOptions chromeOptions = new ChromeOptions();
    if (client.useFakeMedia()) {
      // We use fake media or mediafile as webcam/microphone
      chromeOptions.addArguments("use-fake-ui-for-media-stream");
      chromeOptions.addArguments("use-fake-device-for-media-stream");
      // If we use mediafile (only if not on android)
      if (!client.getPlatform().equals(Platform.ANDROID)) {
        if (client.getVideo() != null || client.getAudio() != null) {
          chromeOptions.addArguments("allow-file-access-from-files");
          if (client.getVideo() != null) {
            chromeOptions.addArguments("use-file-for-fake-video-capture="
                + client.fetchMediaPath(client.getVideo(), client.getBrowserName()));
          }
          if (client.getAudio() != null) {
            chromeOptions.addArguments("use-file-for-fake-audio-capture="
                + client.fetchMediaPath(client.getAudio(), client.getBrowserName()));
          }
        }
      } else {
        chromeOptions.setExperimentalOption("w3c", false);
      }
    } else {
      // Create an Hashmap to edit user profile
      Map<String, Object> prefs = new HashMap<String, Object>();
      // Allow access to camera & micro
      prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
      prefs.put("profile.default_content_setting_values.media_stream_mic", 1);
      chromeOptions.setExperimentalOption("prefs", prefs);
    }

    if (client.getPlatform().equals(Platform.ANDROID)) {
      return chromeOptions;
    }

    chromeOptions.addArguments("auto-select-desktop-capture-source=Entire screen");
    if (! "electron".equals(client.getVersion())) {
      // CHROME ONLY
      String extension = System.getProperty("kite.chrome.extension");
      extension = extension == null ? client.getExtension() : extension;
      if (extension != null && !extension.isEmpty()) {
        chromeOptions.addExtensions(new File(extension));
      }
    }
    /*
     * if (client.getVersion().toLowerCase().contains("electron")) {
     * chromeOptions.setBinary(client.getSpecs().getPathToBinary()); }
     */
    if (client.isHeadless()) {
      chromeOptions.addArguments("headless");
    }
    if (client.getWindowSize() != null && client.getWindowSize().trim().length() > 0) {
      chromeOptions.addArguments("window-size=" + client.getWindowSize());
    }
    for (String flag : client.getFlags()) {
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
   * @param client the Client object
   * @return the firefox option
   */
  private static FirefoxOptions setCommonFirefoxOptions(Client client) {
    FirefoxProfile firefoxProfile = null;
    String profile = System.getProperty("kite.firefox.profile")  ;
    profile = profile == null ? client.getProfile() : profile;
    if (profile != null && !profile.isEmpty()) {
      switch (client.getPlatform().name().toUpperCase()) {
        case "WINDOWS":
          profile += "windows";
          break;
        case "MAC":
          profile += "mac";
          break;
        case "LINUX":
          profile += "linux";
          break;
      }
      firefoxProfile = new FirefoxProfile(new File(profile));
    } else {
      logger.warn("FIREFOX: Some tests require specific profile for firefox to work properly.");
      firefoxProfile = new FirefoxProfile();
    }
    firefoxProfile.setPreference("media.navigator.permission.disabled", true);
    if (client.useFakeMedia()) {
      if (client.getAudio() == null && client.getVideo() == null) {
        firefoxProfile.setPreference("media.navigator.streams.fake", client.useFakeMedia());
      }
    }
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    firefoxOptions.setProfile(firefoxProfile);
    if (client.isHeadless()) {
      firefoxOptions.addArguments("-headless");
    }
    if (client.getWindowSize() != null) {
      firefoxOptions.addArguments("-window-size " + client.getWindowSize());
    }
    for (String flag : client.getFlags()) {
      firefoxOptions.addArguments(flag);
    }
    return firefoxOptions;
  }

  private static String writeCommand(Client client) {
    String command = "play%20";
    if (client.getVideo() != null && client.getAudio() != null) {
      command += client.fetchMediaPath(client.getVideo(), client.getBrowserName())
          + "%20" + client.fetchMediaPath(client.getAudio(), client.getBrowserName());
    } else if (client.getVideo() != null && client.getAudio() == null) {
      command += client.fetchMediaPath(client.getVideo(), client.getBrowserName());
    } else if (client.getVideo() == null && client.getAudio() != null) {
      command += client.fetchMediaPath(client.getAudio(), client.getBrowserName());
    } else {
      logger.info("No file provided.");
    }
    return command + "%20&";
  }
}

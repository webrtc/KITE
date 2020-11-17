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

import static org.webrtc.kite.Utils.fetchMediaPath;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.cosmosoftware.kite.exception.KiteTestException;
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
import org.webrtc.kite.config.client.BrowserSpecs;
import org.webrtc.kite.config.client.Capability;
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
    BrowserSpecs specs = app.getBrowserSpecs();
    if (specs.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION, specs.getVersion());
    }
    capabilities.setCapability("browserName", "app");
    capabilities.setCapability("app", app.getAppName());
    capabilities.setCapability("deviceName", specs.getDeviceName());
    capabilities.setCapability("platformName", specs.getPlatform());
    if (specs.getPlatformVersion() != null) {
      capabilities.setCapability("platformVersion", specs.getPlatformVersion());
    }
    if (specs.getPlatform().name().toLowerCase().equalsIgnoreCase("ios")) {
      capabilities.setCapability("automationName", "XCUITest");
    }
    if (specs.getPlatform().name().toLowerCase().equalsIgnoreCase("android")) {
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
    BrowserSpecs specs = client.getBrowserSpecs();
    if (specs.getVersion() != null) {
      capabilities.setCapability(CapabilityType.VERSION, specs.getVersion());
    }
    if (specs.getPlatform() != null) {
      capabilities.setCapability(CapabilityType.PLATFORM_NAME, specs.getPlatform());
    }

//    if (client.getCapability().getGateway().toString() != null && !"none".equalsIgnoreCase(client.getCapability().getGateway().toString())) {
//      capabilities.setCapability("gateway", client.getCapability().getGateway().toString());
//    }

    // Only consider next code block if this is a client.
    switch (specs.getBrowserName()) {
      case "chrome":
        capabilities.setCapability(ChromeOptions.CAPABILITY, setCommonChromeOptions(client.getCapability(), specs));
        break;
      case "firefox":
        capabilities.merge(setCommonFirefoxOptions(client.getCapability(), specs));
        break;
      case "MicrosoftEdge":
        EdgeOptions MicrosoftEdgeOptions = new EdgeOptions();
        capabilities.setCapability("edgeOptions", MicrosoftEdgeOptions);
        capabilities.setCapability("avoidProxy", true);
        break;
      case "safari":
        SafariOptions options = new SafariOptions();
        capabilities.setCapability(SafariOptions.CAPABILITY, options);
        break;
      case "Safari Technology Preview":
        SafariOptions TPoptions = new SafariOptions();
        TPoptions.setCapability(CapabilityType.BROWSER_NAME, specs.getBrowserName());
        TPoptions.setUseTechnologyPreview(true);
        capabilities.setCapability(SafariOptions.CAPABILITY, TPoptions);
        break;
      default:
        capabilities.setCapability(CapabilityType.BROWSER_NAME, specs.getBrowserName());
        break;
    }
    // Add log preference to webdriver
    // TODO put log preference into config file
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//    capabilities.setCapability("goog:loggingPrefs", logPrefs);
    // Capabilities for mobile client/app
    if (specs.getPlatform().toString().equalsIgnoreCase("android")
      ||specs.getPlatform().toString().equalsIgnoreCase("ios")) {
      // deviceName:
      // On iOS, this should be one of the valid devices returned by instruments with instruments -s devices.
      // On Android this capability is currently ignored, though it remains required.
      capabilities.setCapability("deviceName", specs.getDeviceName());
      capabilities.setCapability("platformName", specs.getPlatform());
      capabilities.setCapability("platformVersion", specs.getPlatformVersion());
      if (specs.getPlatform().name().equalsIgnoreCase("ios")) {
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
    for (String capabilityName : client.getCapability().getExtraCapabilities().keySet()) {
      logger.debug("extraCapabilites : " + capabilityName + ": " + client.getCapability().getExtraCapabilities().get(capabilityName));
      capabilities.setCapability(capabilityName, client.getCapability().getExtraCapabilities().get(capabilityName));
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
    Capability capability = client.getCapability();
    URL url = new URL(urlStr);
    if (!client.isApp()) {
      RemoteWebDriver webDriver = new RemoteWebDriver(url, WebDriverFactory.createCapabilities(client, testName, id));
      if (client.getBrowserSpecs().getBrowserName().equalsIgnoreCase("firefox")) {
        if (capability.useFakeMedia()) {
          if (capability.getVideo() != null || capability.getAudio() != null) {
            String nodePublicIp = new URL(TestUtils.getNode(urlStr, webDriver.getSessionId().toString())).getHost();
            String gridId = kiteServerGridId.length > 0 ? kiteServerGridId[0] : "null";
            String command = writeCommand(capability, client.getBrowserSpecs());
            String uri = "/command?id=" + gridId + "&ip=" + nodePublicIp + "&cmd=" + command;
            logger.debug("kiteServerCommand response:\r\n" + TestUtils.kiteServerCommand(kiteServerUrl, uri));
          }
        }
      }
      return webDriver;
    } else {
      if (client.getBrowserSpecs().getPlatform().name().equalsIgnoreCase("android")) {
        return new AndroidDriver<>(url, WebDriverFactory.createCapabilities(client, testName, id));
      } else if (client.getBrowserSpecs().getPlatform().name().equalsIgnoreCase("ios")) {
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
   * @param capability the Client capability
   * @param specs the Client browser specs
   * @return the chrome option
   */
  private static ChromeOptions setCommonChromeOptions(Capability capability, BrowserSpecs specs) {
    ChromeOptions chromeOptions = new ChromeOptions();
    if (capability.useFakeMedia()) {
      // We use fake media or mediafile as webcam/microphone
      chromeOptions.addArguments("use-fake-ui-for-media-stream");
      chromeOptions.addArguments("use-fake-device-for-media-stream");
      chromeOptions.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
      chromeOptions.addArguments("no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
      chromeOptions.addArguments("disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
      chromeOptions.addArguments("disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
      chromeOptions.addArguments("disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
      chromeOptions.addArguments("disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc      // If we use mediafile (only if not on android)
      chromeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
      if (!specs.getPlatform().equals(Platform.ANDROID)) {
        if (capability.getVideo() != null || capability.getAudio() != null) {
          chromeOptions.addArguments("allow-file-access-from-files");
          if (capability.getVideo() != null) {
            chromeOptions.addArguments("use-file-for-fake-video-capture="
                + fetchMediaPath(capability.getVideo(), specs.getBrowserName()));
          }
          if (capability.getAudio() != null) {
            chromeOptions.addArguments("use-file-for-fake-audio-capture="
                + fetchMediaPath(capability.getAudio(), specs.getBrowserName()));
          }
        }
      } else {
        chromeOptions.setExperimentalOption("w3c", false);
      }
    }

    if (specs.getPlatform().equals(Platform.ANDROID)) {
      return chromeOptions;
    }
    // Create an Hashmap to edit user profile
    Map<String, Object> prefs = new HashMap<String, Object>();
    // Allow access to camera & micro
    // This does not affect fake media options above
    // but still logged the permission in profile, somes sfu require these
    prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
    prefs.put("profile.default_content_setting_values.media_stream_mic", 1);
    chromeOptions.setExperimentalOption("prefs", prefs);



    chromeOptions.addArguments("auto-select-desktop-capture-source=Entire screen");
    if (! "electron".equals(specs.getVersion())) {
      // CHROME ONLY
      String extension = System.getProperty("kite.chrome.extension");
      extension = extension == null ? specs.getExtension() : extension;
      if (extension != null && !extension.isEmpty()) {
        chromeOptions.addExtensions(new File(extension));
      }
    }
    /*
     * if (client.getVersion().toLowerCase().contains("electron")) {
     * chromeOptions.setBinary(client.getBrowserSpecs().getPathToBinary()); }
     */
    if (capability.isHeadless()) {
      chromeOptions.addArguments("headless");
    }
    if (capability.getWindowSize() != null && capability.getWindowSize().trim().length() > 0) {
      chromeOptions.addArguments("window-size=" + capability.getWindowSize());
    }
    for (String flag : capability.getFlags()) {
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
   * @param capability the Client capability
   * @param specs the Client browser specs
   * @return the firefox option
   */
  private static FirefoxOptions setCommonFirefoxOptions(Capability capability, BrowserSpecs specs) {
    FirefoxProfile firefoxProfile = null;
    String profile = System.getProperty("kite.firefox.profile")  ;
    profile = profile == null ? specs.getProfile() : profile;
    if (profile != null && !profile.isEmpty()) {
      switch (specs.getPlatform().name().toUpperCase()) {
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
    firefoxProfile.setPreference("media.autoplay.enabled.user-gestures-needed", false);
    if (capability.useFakeMedia()) {
      if (capability.getAudio() == null && capability.getVideo() == null) {
        firefoxProfile.setPreference("media.navigator.streams.fake", capability.useFakeMedia());
      }
    }
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    firefoxOptions.setProfile(firefoxProfile);
    if (capability.isHeadless()) {
      firefoxOptions.addArguments("-headless");
    }
    if (capability.getWindowSize() != null) {
      firefoxOptions.addArguments("-window-size " + capability.getWindowSize());
    }
    for (String flag : capability.getFlags()) {
      firefoxOptions.addArguments(flag);
    }
    return firefoxOptions;
  }

  private static String writeCommand(Capability capability, BrowserSpecs specs) {
    String command = "play%20";
    if (capability.getVideo() != null && capability.getAudio() != null) {
      command += fetchMediaPath(capability.getVideo(), specs.getBrowserName())
          + "%20" + fetchMediaPath(capability.getAudio(), specs.getBrowserName());
    } else if (capability.getVideo() != null && capability.getAudio() == null) {
      command += fetchMediaPath(capability.getVideo(), specs.getBrowserName());
    } else if (capability.getVideo() == null && capability.getAudio() != null) {
      command += fetchMediaPath(capability.getAudio(), specs.getBrowserName());
    } else {
      logger.info("No file provided.");
    }
    return command + "%20&";
  }
}

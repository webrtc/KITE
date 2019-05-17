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

package org.webrtc.kite.config;

import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import is.tagomor.woothee.Classifier;
import org.webrtc.kite.Utils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of a browser object in the config file.
 * <p>
 * { "browserName": "chrome", "version": "48.0", "platform": "LINUX" }
 * <p>
 * See README for possible values of platform.
 */
public class Browser extends EndPoint {

  private final String DEFAULT_WINDOW_SIZE = "1920,1200";
  // Mandatory
  private final String browserName;
  private String version;
  
  // Optional
  private boolean headless = false;
  private boolean useFakeMedia = true;
  private List<String> flags = new ArrayList<>();
  private boolean technologyPreview = false;
  private String pathToBinary;
  private String fakeMediaFile;
  private String fakeMediaAudio;
  private String windowSize = DEFAULT_WINDOW_SIZE;
  
  // Info from web driver
  private String webDriverVersion;
  private String webDriverPlatform;
  
  // Info from user agent
  private String userAgentVersion;
  private String userAgentPlatform;
  
  private Mobile mobile;
  
  /**
   * Constructs a new Browser for the given browser name, without specifying version and platform.
   *
   * @param browserName name of the browser as accepted in Selenium
   */
  public Browser(String browserName) {
    this.browserName = browserName;
  }
  
  /**
   * Constructs a new Browser with the given browser name, version and platform.
   *
   * @param browserName name of the browser as accepted in Selenium
   * @param version     the version
   * @param platform    the platform
   */
  public Browser(String browserName, String version, String platform) {
    this.browserName = browserName;
    this.version = version;
    this.platformName = platform;
  }
  
  /**
   * Constructs a new Browser with the given remote address and JsonObject.
   *
   * @param remoteAddress a string representation of the Selenium hub url
   * @param jsonObject    JsonObject
   */
  public Browser(String remoteAddress, JsonObject jsonObject) {
    super(remoteAddress, jsonObject);
    String nullValue = "browserName";
    try {
      this.browserName = jsonObject.getString("browserName");
      nullValue = "version";
      this.version = jsonObject.getString("version");
      nullValue = "platform";
      this.platformName = jsonObject.getString("platform");
      this.headless = jsonObject.getBoolean("headless", headless);
      this.useFakeMedia = jsonObject.getBoolean("useFakeMedia", useFakeMedia);
      this.technologyPreview = jsonObject.getBoolean("technologyPreview", technologyPreview);
      this.pathToBinary = jsonObject.getString("pathToBinary", "");
      this.fakeMediaFile = jsonObject.getString("fakeMediaFile", null);
      JsonValue jsonValue = jsonObject.getOrDefault("flags", null);
      this.fakeMediaAudio = jsonObject.getString("fakeMediaAudio", null);
      this.gateway = jsonObject.getString("gateway", null);
      this.windowSize = jsonObject.getString("windowSize", windowSize);
      if (jsonValue != null) {
        JsonArray flagArray = (JsonArray) jsonValue;
        for (int i = 0; i < flagArray.size(); i++) {
          this.flags.add(flagArray.getString(i));
        }
      }
    } catch (NullPointerException e) {
      logger.error("Missing mandatory config \"" + nullValue + "\" for Browser object.");
      throw e;
    }
  }
  
  /**
   * Constructs a new Browser with the a Browser.
   *
   * @param browser Browser
   */
  public Browser(Browser browser) {
    super(browser);
    this.browserName = browser.getBrowserName();
    this.version = browser.getVersion();
    this.headless = browser.isHeadless();
    this.useFakeMedia = browser.useFakeMedia();
    this.technologyPreview = browser.isTechnologyPreview();
    this.pathToBinary = browser.getPathToBinary();
    this.fakeMediaFile = browser.getFakeMediaFile();
    this.fakeMediaAudio = browser.getFakeMediaAudio();
    this.windowSize = this.getWindowSize();
    this.flags = browser.getFlags();
    this.extraCapabilities = browser.getExtraCapabilities();
  }

  /**
   * Gets the window size
   *
   * @return the window size
   */
  public String getWindowSize() {
    return windowSize;
  }

  /**
   * Gets browser name.
   *
   * @return the browser name
   */
  public String getBrowserName() {
    return browserName;
  }
  
  /**
   * Gets version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }
  
  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
  }
  
  /**
   * Gets path binary.
   *
   * @return the path binary
   */
  public String getPathToBinary() {
    return pathToBinary;
  }
  
  /**
   * Sets browser name.
   *
   * @param pathToBinary the path binary
   */
  public void setPathToBinary(String pathToBinary) {
    this.pathToBinary = pathToBinary;
  }
  
  /**
   * Is headless boolean.
   *
   * @return the boolean
   */
  public boolean isHeadless() {
    return headless;
  }
  
  /**
   * Sets headless.
   *
   * @param headless the headless
   */
  public void setHeadless(boolean headless) {
    this.headless = headless;
  }
  
  /**
   * Is technologyPreview boolean.
   *
   * @return the boolean
   */
  public boolean isTechnologyPreview() {
    return technologyPreview;
  }
  
  /**
   * Sets headless.
   *
   * @param technologyPreview the technologyPreview
   */
  public void setTechnologyPreview(boolean technologyPreview) {
    this.technologyPreview = technologyPreview;
  }
  
  /**
   * Get browser's flags.
   *
   * @return list of arguments to launch browser with.
   */
  public List<String> getFlags() {
    return flags;
  }
  
  /**
   * Adds one flag to the flag list.
   *
   * @param flag to add.
   */
  public void addFlag(String flag) {
    this.flags.add(flag);
  }
  
  /**
   * Gets web driver version.
   *
   * @return the web driver version
   */
  public String getWebDriverVersion() {
    return webDriverVersion;
  }
  
  /**
   * Sets web driver version.
   *
   * @param webDriverVersion the web driver version
   */
  public void setWebDriverVersion(String webDriverVersion) {
    this.webDriverVersion = webDriverVersion;
  }
  
  /**
   * Gets web driver platform.
   *
   * @return the web driver platform
   */
  public String getWebDriverPlatform() {
    return webDriverPlatform;
  }
  
  /**
   * Sets web driver platform.
   *
   * @param webDriverPlatform the web driver platform
   */
  public void setWebDriverPlatform(String webDriverPlatform) {
    this.webDriverPlatform = webDriverPlatform;
  }
  
  /**
   * Gets user agent version.
   *
   * @return the user agent version
   */
  public String getUserAgentVersion() {
    return userAgentVersion;
  }
  
  /**
   * Sets user agent version.
   *
   * @param userAgentVersion the user agent version
   */
  public void setUserAgentVersion(String userAgentVersion) {
    this.userAgentVersion = userAgentVersion;
  }
  
  /**
   * Gets user agent platform.
   *
   * @return the user agent platform
   */
  public String getUserAgentPlatform() {
    return userAgentPlatform;
  }
  
  /**
   * Sets user agent platform.
   *
   * @param userAgentPlatform the user agent platform
   */
  public void setUserAgentPlatform(String userAgentPlatform) {
    this.userAgentPlatform = userAgentPlatform;
  }
  
  /**
   * Gets mobile.
   *
   * @return the mobile
   */
  public Mobile getMobile() {
    return mobile;
  }
  
  /**
   * Sets mobile.
   *
   * @param mobile the mobile
   */
  public void setMobile(Mobile mobile) {
    this.mobile = mobile;
  }
  
  /**
   * Checks whether it is required to get navigator.userAgent from the browser.
   *
   * @return true if either of the user agent version and user agent platform is null.
   */
  public boolean shouldGetUserAgent() {
    return this.userAgentVersion == null || this.userAgentPlatform == null;
  }
  
  /**
   * Parses the user agent string using user-agent-RCutils and retrieve the browser version and the
   * platform details. If the operating system is 'mac' then further parses using Woothie to get the
   * operating system version.
   *
   * @param userAgentString navigator.userAgent
   */
  public void setUserAgentVersionAndPlatform(String userAgentString) {
    UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
    Version version = userAgent.getBrowserVersion();
    if (version != null) {
      this.userAgentVersion = version.getVersion();
    }
    OperatingSystem operatingSystem = userAgent.getOperatingSystem();
    if (operatingSystem != null) {
      this.userAgentPlatform = operatingSystem.getName();
    }
    if (this.userAgentPlatform != null && this.userAgentPlatform.toLowerCase().startsWith("mac")) {
      Map<String, String> map = Classifier.parse(userAgentString);
      this.userAgentPlatform = this.userAgentPlatform + " " + map.get("os_version");
    }
  }
  
  /**
   * Checks whether the given platform equals the receiver's platform.
   *
   * @param platform Platform
   * @return true if the provided platform is equal to the receiver's platform.
   */
  private boolean isEqualToPlatform(String platform) {
    if (Utils.areBothNull(this.platformName, platform)) {
      return true;
    } else if (Utils.areBothNotNull(this.platformName, platform)) {
      return this.platformName.equalsIgnoreCase(platform);
    } else {
      return false;
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Browser browser = (Browser) o;
    return browserName.equals(browser.browserName) &&
            version.equals(browser.version) && platformName.equals(browser.platformName) && Objects.equals(gateway, browser.gateway) &&
            Objects.equals(mobile, browser.mobile);
  }
  
  @Override
  public int hashCode() {
    int hashCode = this.browserName.hashCode();
    if (this.version != null) {
      hashCode += this.version.hashCode();
    }
    if (this.platformName != null) {
      hashCode += this.platformName.hashCode();
    }
    if (this.mobile != null) {
      hashCode += this.mobile.hashCode();
    }
    if (this.gateway != null) {
      hashCode += this.gateway.hashCode();
    }
    return hashCode;
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    
    JsonObjectBuilder jsonObjectBuilder =
      super.getJsonObjectBuilder().add("browserName", this.getBrowserName());
    
    if (this.version != null) {
      jsonObjectBuilder.add("version", this.version);
    }
    if (this.platformName != null) {
      jsonObjectBuilder.add("platform", this.platformName);
    }
    if (this.pathToBinary != null) {
      jsonObjectBuilder.add("pathToBinary", this.pathToBinary);
    }
    if (this.mobile != null) {
      jsonObjectBuilder.add("mobile", this.mobile.getJsonObjectBuilder());
    }
    jsonObjectBuilder.add("focus", this.isFocus());
    
    return jsonObjectBuilder;
  }
  
  /**
   * Use fake media boolean.
   *
   * @return true if to use the fake media from the browser
   */
  public boolean useFakeMedia() {
    return useFakeMedia;
  }
  
  /**
   * For Chrome only.
   *
   * @return path to the video file to be used by Chrome as fakemedia, or null if no file has been specified
   */
  public String getFakeMediaFile() {
    return fakeMediaFile;
  }
  
  /**
   * For Chrome only.
   *
   * @return path to the audio file to be used by Chrome as fakemedia, or null if no file has been specified
   */
  public String getFakeMediaAudio() {
    return fakeMediaAudio;
  }
  
  
  /**
   * Checks whether the given mobile object equals the receiver's mobile object.
   *
   * @param mobile Mobile
   *
   * @return true if the provided mobile object is equal to the receiver's mobile object.
   */
  protected boolean isEqualToMobile(Mobile mobile) {
    if (Utils.areBothNull(this.mobile, mobile)) {
      return true;
    } else if (Utils.areBothNotNull(this.mobile, mobile)) {
      return this.mobile.equals(mobile);
    } else {
      return false;
    }
  }
}

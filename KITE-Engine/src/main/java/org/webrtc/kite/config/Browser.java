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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.log4j.Logger;
import org.webrtc.kite.Utility;

import java.util.Map;

/**
 * Representation of a browser object in the config file.
 * <p>
 * { "browserName": "chrome", "version": "48.0", "platform": "LINUX" }
 * <p>
 * See README for possible values of platform.
 */
public class Browser extends KiteConfigObject {

  private static final Logger logger = Logger.getLogger(Browser.class.getName());

  // Mandatory
  private String browserName;
  private String version;
  private String platform;

  // Optional
  private Mobile mobile;
  private String remoteAddress;
  private boolean headless = false;
  private boolean technologyPreview = false;

  // Info from web driver
  private String webDriverVersion;
  private String webDriverPlatform;

  // Info from user agent
  private String userAgentVersion;
  private String userAgentPlatform;

  // Private
  private int maxInstances;


  /**
   * Constructs a new Browser with the given browser name.
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
    this.platform = platform;
  }

  /**
   * Constructs a new Browser with the given remote address and JsonObject.
   *
   * @param remoteAddress a string representation of the Selenium hub url
   * @param jsonObject    JsonObject
   */
  public Browser(String remoteAddress, JsonObject jsonObject) {
    this.browserName = jsonObject.getString("browserName");
    this.version = jsonObject.getString("version");
    this.platform = jsonObject.getString("platform");
    this.headless = jsonObject.getBoolean("headless", false);
    this.technologyPreview = jsonObject.getBoolean("technologyPreview", false);
    this.remoteAddress = jsonObject.getString("remoteAddress", remoteAddress);
    JsonValue jsonValue = jsonObject.getOrDefault("mobile", null);
    if (jsonValue != null)
      this.mobile = new Mobile((JsonObject) jsonValue);
  }

  /**
   * Constructs a new Browser with the given Browser.
   *
   * @param browser Browser
   */
  public Browser(Browser browser) {
    this.browserName = browser.getBrowserName();
    this.version = browser.getVersion();
    this.platform = browser.getPlatform();
    this.mobile = browser.getMobile();
    this.remoteAddress = browser.getRemoteAddress();
    this.headless = browser.isHeadless();
    this.technologyPreview = browser.isTechnologyPreview();
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
   * Sets browser name.
   *
   * @param browserName the browser name
   */
  public void setBrowserName(String browserName) {
    this.browserName = browserName;
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
   * Gets platform.
   *
   * @return the platform
   */
  public String getPlatform() {
    return platform;
  }

  /**
   * Sets platform.
   *
   * @param platform the platform
   */
  public void setPlatform(String platform) {
    this.platform = platform;
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
   * Gets remote address.
   *
   * @return the remote address
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * Sets remote address.
   *
   * @param remoteAddress the remote address
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
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
   * @param technologyPreview the technologyPreview
   */
  public void setTechnologyPreview(boolean technologyPreview) {
    this.technologyPreview = technologyPreview;
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
   * @param headless the headless
   */
  public void setHeadless(boolean headless) {
    this.headless = headless;
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
   * Gets max instances.
   *
   * @return the max instances
   */
  public int getMaxInstances() {
    return maxInstances;
  }

  /**
   * Sets max instances.
   *
   * @param maxInstances the max instances
   */
  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
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
   * Parses the user agent string using user-agent-utils and retrieve the browser version and the
   * platform details. If the operating system is 'mac' then further parses using Woothie to get the
   * operating system version.
   *
   * @param userAgentString navigator.userAgent
   */
  public void setUserAgentVersionAndPlatfom(String userAgentString) {
    UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
    Version version = userAgent.getBrowserVersion();
    if (version != null)
      this.userAgentVersion = version.getVersion();
    OperatingSystem operatingSystem = userAgent.getOperatingSystem();
    if (operatingSystem != null)
      this.userAgentPlatform = operatingSystem.getName();
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
    if (Utility.areBothNull(this.platform, platform)) {
      return true;
    } else if (Utility.areBothNotNull(this.platform, platform)) {
      return this.platform.equalsIgnoreCase(platform);
    } else {
      return false;
    }
  }

  /**
   * Checks whether the given mobile object equals the receiver's mobile object.
   *
   * @param mobile Mobile
   * @return true if the provided mobile object is equal to the receiver's mobile object.
   */
  private boolean isEqualToMobile(Mobile mobile) {
    if (Utility.areBothNull(this.mobile, mobile)) {
      return true;
    } else if (Utility.areBothNotNull(this.mobile, mobile)) {
      return this.mobile.equals(mobile);
    } else {
      return false;
    }
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Browser temp = (Browser) obj;
    if (this.browserName.equalsIgnoreCase(temp.getBrowserName())) {
      if (Utility.areBothNull(this.version, temp.getVersion())) {
        if (this.isEqualToPlatform(temp.getPlatform())) {
          return this.isEqualToMobile(temp.getMobile());
        }
      } else if (Utility.areBothNotNull(this.version, temp.getVersion())) {
        if (this.version.equalsIgnoreCase(temp.getVersion())) {
          if (this.isEqualToPlatform(temp.getPlatform())) {
            return this.isEqualToMobile(temp.getMobile());
          }
        }
      }
    }

    return false;
  }

  @Override public int hashCode() {
    long hashCode = this.browserName.hashCode();
    if (this.version != null)
      hashCode += this.version.hashCode();
    if (this.platform != null)
      hashCode += this.platform.hashCode();
    if (this.mobile != null)
      hashCode += this.mobile.hashCode();
    return (int) hashCode;
  }

  @Override public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
        Json.createObjectBuilder().add("browserName", this.getBrowserName());

    if (this.version != null)
      jsonObjectBuilder.add("version", this.version);
    if (this.platform != null)
      jsonObjectBuilder.add("platform", this.platform);
    if (this.mobile != null)
      jsonObjectBuilder.add("mobile", this.mobile.getJsonObjectBuilder());

    return jsonObjectBuilder;
  }

  @Override public JsonObjectBuilder getJsonObjectBuilderForResult() {
    JsonObjectBuilder jsonObjectBuilder =
        Json.createObjectBuilder().add("browserName", this.getBrowserName());

    // Select the version
    String myVersion = null;
    if (Utility.isNotNullAndNotEmpty(this.userAgentVersion))
      myVersion = this.userAgentVersion;
    else if (Utility.isNotNullAndNotEmpty(this.webDriverVersion))
      myVersion = this.webDriverVersion;
    else if (Utility.isNotNullAndNotEmpty(this.version))
      myVersion = this.version;
    if (myVersion != null)
      jsonObjectBuilder.add("version", myVersion);

    // Select the platform
    String myPlatform = null;
    if (Utility.isNotNullAndNotEmpty(this.userAgentPlatform)) {
      myPlatform = this.userAgentPlatform;
    } else if (Utility.isNotNullAndNotEmpty(this.webDriverPlatform)) {
      myPlatform = this.webDriverPlatform;
      if (myPlatform.equalsIgnoreCase("ANY") && Utility.isNotNullAndNotEmpty(this.platform))
        myPlatform = this.platform;
    } else if (Utility.isNotNullAndNotEmpty(this.platform)) {
      myPlatform = this.platform;
    }
    if (myPlatform != null)
      jsonObjectBuilder.add("platform", myPlatform);

    if (this.mobile != null)
      jsonObjectBuilder.add("mobile", this.mobile.getJsonObjectBuilder());

    return jsonObjectBuilder;
  }

  /**
   * Gets json object builder for result.
   *
   * @param osName    the os name
   * @param osVersion the os version
   * @return the json object builder for result
   */
  public JsonObjectBuilder getJsonObjectBuilderForResult(String osName, String osVersion) {
    JsonObjectBuilder jsonObjectBuilder =
        Json.createObjectBuilder().add("browserName", this.getBrowserName());

    // Select the version
    String myVersion = null;
    if (Utility.isNotNullAndNotEmpty(this.userAgentVersion))
      myVersion = this.userAgentVersion;
    else if (Utility.isNotNullAndNotEmpty(this.webDriverVersion))
      myVersion = this.webDriverVersion;
    else if (Utility.isNotNullAndNotEmpty(this.version))
      myVersion = this.version;
    if (myVersion != null)
      jsonObjectBuilder.add("version", myVersion);

    // Select the platform
    String myPlatform = null;
    if (Utility.isNotNullAndNotEmpty(this.userAgentPlatform)) {
      myPlatform = this.userAgentPlatform;
    } else if (Utility.isNotNullAndNotEmpty(this.webDriverPlatform)) {
      myPlatform = this.webDriverPlatform;
      if (myPlatform.equalsIgnoreCase("ANY") && Utility.isNotNullAndNotEmpty(this.platform))
        myPlatform = this.platform;
    } else if (Utility.isNotNullAndNotEmpty(this.platform)) {
      myPlatform = this.platform;
    }

    if (osName != null && osVersion != null) {
      if (!myPlatform.toLowerCase().contains("linux") && !myPlatform.matches("[0-9]")) {
        if (myPlatform.toLowerCase().startsWith("win") && osName.toLowerCase().startsWith("win")
            || myPlatform.toLowerCase().startsWith("mac") && osName.toLowerCase()
            .startsWith("mac")) {
          myPlatform = osName + osVersion;
          if (logger.isDebugEnabled())
            logger.debug(
                "Replacing platform info from exception info - replacing: " + myPlatform + " with: "
                    + osName + osVersion);
        }
      }
    }

    if (myPlatform != null)
      jsonObjectBuilder.add("platform", myPlatform);

    return jsonObjectBuilder;
  }

}

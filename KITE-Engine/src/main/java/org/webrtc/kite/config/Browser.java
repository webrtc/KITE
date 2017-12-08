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

  private String browserName;
  private String version;
  private String platform;
  private String remoteAddress;
  private Mobile mobile;

  private String webDriverVersion;
  private String webDriverPlatform;

  private String userAgentVersion;
  private String userAgentPlatform;

  /**
   * Constructs a new Browser with the given browser name.
   *
   * @param browserName name of the browser as accepted in Selenium.
   */
  public Browser(String browserName) {
    this.browserName = browserName;
  }

  /**
   * Constructs a new Browser with the given remote address and JsonObject.
   *
   * @param remoteAddress a string representation of the Selenium hub url.
   * @param jsonObject JsonObject
   */
  public Browser(String remoteAddress, JsonObject jsonObject) {
    this.browserName = jsonObject.getString("browserName");

    this.version = jsonObject.getString("version", null);
    this.platform = jsonObject.getString("platform", null);
    this.remoteAddress = jsonObject.getString("remoteAddress", null);
    if (this.remoteAddress == null)
      this.remoteAddress = remoteAddress;
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
    this.remoteAddress = browser.getRemoteAddress();
    this.mobile = browser.getMobile();
  }

  public String getBrowserName() {
    return browserName;
  }

  public void setBrowserName(String browserName) {
    this.browserName = browserName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public Mobile getMobile() { return mobile; }

  public void setMobile(Mobile mobile) { this.mobile = mobile; }

  public String getWebDriverVersion() {
    return webDriverVersion;
  }

  public void setWebDriverVersion(String webDriverVersion) {
    this.webDriverVersion = webDriverVersion;
  }

  public String getWebDriverPlatform() {
    return webDriverPlatform;
  }

  public void setWebDriverPlatform(String webDriverPlatform) {
    this.webDriverPlatform = webDriverPlatform;
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

  @Override
  public boolean equals(Object obj) {
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

  @Override
  public int hashCode() {
    long hashCode = this.browserName.hashCode();
    if (this.version != null)
      hashCode += this.version.hashCode();
    if (this.platform != null)
      hashCode += this.platform.hashCode();
    if (this.mobile != null)
      hashCode += this.mobile.hashCode();
    return (int) hashCode;
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
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

  @Override
  public JsonObjectBuilder getJsonObjectBuilderForResult() {
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

}

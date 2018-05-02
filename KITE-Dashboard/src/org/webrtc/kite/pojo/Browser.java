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

package org.webrtc.kite.pojo;

import org.webrtc.kite.Mapping;
import org.webrtc.kite.Utility;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Browser object containing the information as name, version and platform.
 */
public class Browser {
  private int id;
  private String name;
  private String version;
  private String platform;

  /**
   * Constructs a new Browser from given information as name, version and platform.
   *
   * @param name     name of browser.
   * @param version  version of browser.
   * @param platform platform on which the browser runs.
   */
  public Browser(String name, String version, String platform) {
    this.name = name;
    this.version = processVersion(version);
    this.platform = processPlatform(platform);
  }

  /**
   * Constructs a new Browser from a Json object containing all the needed information
   * and 'translate' to appropriate name of platform.
   *
   * @param jsonObject Json object obtained from the configuration file.
   */
  public Browser(JsonObject jsonObject) {
    String browserName = jsonObject.getString("browserName");
    String browserVersion = "?";
    String browserPlatform = "?";
    if (jsonObject.get("version") != null) {
      browserVersion = processVersion(jsonObject.getString("version").toLowerCase());
      browserPlatform = jsonObject.getString("version").toLowerCase();
    }
    if (browserPlatform.contains("android") || browserPlatform.contains("ios") || browserPlatform.contains("fennec")) {
      if (browserPlatform.contains("fennec"))
        browserPlatform = "android";
      else
        browserPlatform = browserPlatform.split(" ")[0];
    } else {
      if (browserName.equalsIgnoreCase("MicrosoftEdge"))
        browserPlatform = "Windows 10";
      else
        browserPlatform = processPlatform(jsonObject.getString("platform"));
    }
    this.name = browserName;
    this.version = browserVersion;
    this.platform = browserPlatform;
  }

  /**
   * Returns browser's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns browser's platform.
   */
  public String getPlatform() {
    return platform;
  }

  /**
   * Returns browser's version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns true or false on whether a browser is equal to another one.
   */
  public boolean isEqualTo(Browser browser) {
    if (!this.name.equals(browser.getName()))
      return false;
    if (!this.version.equals(browser.getVersion()))
      return false;
    if (!this.platform.equals(browser.getPlatform()))
      return false;
    return true;
  }


  /**
   * Preprocesses browser platform.
   */
  public String processPlatform(String platform) {
    String browserPlatform;
    List<String> bPlatform = Arrays.asList(platform.toLowerCase().split(Pattern.quote(" ")));
    browserPlatform = bPlatform.get(0);
    switch (browserPlatform) {
      case "mac": {
            /*bPlatform = Arrays.asList(bPlatform.get(3).split(Pattern.quote(".")));
            browserPlatform = "OS X " + bPlatform.get(0) + "." + bPlatform.get(1);*/
        browserPlatform = "OS X 10.13";
        break;
      }
      case "windows": {
        /*if (jsonObject.getString("platform").equalsIgnoreCase("Windows"))*/
        browserPlatform = "Windows 10";
            /*else {
              switch (bPlatform.get(1)) {
                case "XP":
                  browserPlatform = "XP";
                  break;
                case "7":
                  browserPlatform = "Vista";
                  break;
                case "8.1":
                  browserPlatform = "Windows 8.1";
                  break;
                case "10":
                  browserPlatform = "Windows 10";
                  break;
              }
            }*/
        break;
      }
      case "xp":
        browserPlatform = "Windows 10";
        break;
      case "ubuntu":
        browserPlatform = "Linux";
        break;
      case "linux":
        browserPlatform = "Linux";
        break;
      default:
        browserPlatform = platform;
    }
    return browserPlatform.toUpperCase();
  }

  /**
   * Preprocesses browser version.
   */
  public String processVersion(String version) {
    String browserVersion = version;
    if (browserVersion.contains("android") || browserVersion.contains("ios") || browserVersion.contains("fennec"))
      browserVersion = browserVersion.split(" ")[1];
    if (!browserVersion.contains("."))
      browserVersion += ".0";
    return browserVersion;
  }

  /**
   * Returns Json string of the browser
   */
  public String toSunburstJson() {
    String res = "";
    switch (this.name) {
      case "safari":
        res += "Sa";
        break;
      case "chrome":
        res += "Cr";
        break;
      case "firefox":
        res += "FF";
        break;
      case "MicrosoftEdge":
        res += "E";
        break;
    }
    res += this.version;
    res += this.platform.replaceAll(" ", "");
    return "\"name\":\"" + res + "\",\"children\":[{";
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Browser temp = (Browser) obj;
    if (this.name.equalsIgnoreCase(temp.getName())) {
      if (Utility.areBothNull(this.version, temp.getVersion())) {
        return this.isEqualToPlatform(temp.getPlatform());
      } else if (Utility.areBothNotNull(this.version, temp.getVersion())) {
        if (this.version.equalsIgnoreCase(temp.getVersion())) {
          return this.isEqualToPlatform(temp.getPlatform());
        }
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    long hashCode = this.name.hashCode();
    if (this.version != null)
      hashCode += this.version.hashCode();
    if (this.platform != null)
      hashCode += this.platform.hashCode();
    return (int) hashCode;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}

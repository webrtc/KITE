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

import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Browser object containing the information as name, version and platform. */
public class Browser {
  private int id;
  private String name;
  private String version;
  private String longVersion;
  private String platform;
  private boolean focus;

  /**
   * Constructs a new Browser from given information as name, version and platform.
   *
   * @param name name of browser.
   * @param version version of browser.
   * @param platform platform on which the browser runs.
   */
  public Browser(String name, String version, String platform) {
    this.id = -1;
    this.name = name;
    this.version = processVersion(version);
    if (this.platform == null){
      this.platform = processPlatform(platform);
    }
    this.focus = true;
  }

  /**
   * Constructs a new Browser from a Json object containing all the needed information and
   * 'translate' to appropriate name of platform.
   *
   * @param jsonObject Json object obtained from the configuration file.
   */
  public Browser(JsonObject jsonObject) {
    this.id = -1;
    String name = jsonObject.getString("browserName");
    String version = jsonObject.getString("version", null).toLowerCase();
    String platform = "?";
    if (version != null) {
      if (version.contains("android")
          || version.contains("ios")
          || version.contains("fennec")) {
        if (version.contains("fennec")) {
          platform = "ANDROID";
        } else {
          platform = version.split(" ")[0];
        }
      } else {
        version = processVersion(version);
        platform = processPlatform(jsonObject.getString("platform", null).toLowerCase());
      }
    }

    this.name = name;
    this.version = processVersion(version);
    this.platform = platform;
    this.focus = jsonObject.getBoolean("focus", true);
  }

  /** Returns browser's name. */
  public String getName() {
    return name;
  }

  /** Returns browser's platform. */
  public String getPlatform() {
    return platform;
  }

  /** Returns browser's version. */
  public String getVersion() {
    return version;
  }

  /** Returns browser's version. */
  public String getLongVersion() {
    return longVersion;
  }

  /** Sets browser's version. */
  public void setLongVersion(String longVersion) {
    this.longVersion = longVersion;
  }

  public String getDetailedName(){
    return name+"_"+version+"_"+platform;
  }
  /** Returns true or false on whether a browser is equal to another one. */
  public boolean isEqualTo(Browser browser) {
    if (!this.name.equals(browser.getName())) {
      return false;
    }
    if (!this.version.equals(browser.getVersion())) {
      return false;
    }
    if (!this.platform.equals(browser.getPlatform())) {
      return false;
    }
    return true;
  }

  public boolean isFocus() {
    return focus;
  }

  /** Preprocesses browser platform. */
  public String processPlatform(String platform) {
    String browserPlatform;
    List<String> bPlatform = Arrays.asList(platform.toLowerCase().split(Pattern.quote(" ")));
    browserPlatform = bPlatform.get(0);
    switch (browserPlatform) {
      case "mac":
        {
          /*bPlatform = Arrays.asList(bPlatform.get(3).split(Pattern.quote(".")));
          browserPlatform = "OS X " + bPlatform.get(0) + "." + bPlatform.get(1);*/
          browserPlatform = "OS X 10.13";
          break;
        }
      case "windows":
        {
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

  /** Preprocesses browser version. */
  public String processVersion(String version) {
    String browserVersion = version;
    if (version != null) {
      if (version.contains("android") || version.contains("ios") || version.contains("fennec")) {
        if (version.contains("fennec")) {
          platform = "android";
        } else {
          platform = version.split(" ")[0];
        }
        browserVersion = version.split(" ")[1];
      }
      if (!browserVersion.contains(".")) {
        browserVersion += ".0";
      }
    }
    setLongVersion(browserVersion);
    browserVersion = browserVersion.split("\\.")[0] + "." + browserVersion.split("\\.")[1];
    return browserVersion;
  }

  /** Returns Json string of the browser */
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
  /** Returns JsonObjectBuilder of the browser */
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
    jsonObjectBuilder
            .add("id", this.id)
            .add("name", this.name)
            .add("version", this.version)
            //.add("longVersion", this.longVersion)
            .add("platform", this.platform);
    return jsonObjectBuilder;
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
    if (this.version != null) {
      hashCode += this.version.hashCode();
    }
    if (this.platform != null) {
      hashCode += this.platform.hashCode();
    }
    return (int) hashCode;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}

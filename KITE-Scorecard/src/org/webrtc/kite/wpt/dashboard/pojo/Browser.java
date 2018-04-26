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

package org.webrtc.kite.wpt.dashboard.pojo;


import org.webrtc.kite.wpt.dashboard.Utility;

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
   * Preprocesses browser platform.
   */
  public String processPlatform(String platform) {
    String browserPlatform;
    List<String> bPlatform = Arrays.asList(platform.toLowerCase().split(Pattern.quote(" ")));
    browserPlatform = bPlatform.get(0);
    switch (browserPlatform) {
      case "mac": {
        browserPlatform = "OS X 10.13";
        break;
      }
      case "windows": {
        browserPlatform = "Windows 10";
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
}

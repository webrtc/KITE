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

import javax.json.JsonObject;
import org.webrtc.kite.Mapping;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Browser object containing the information as name, version and platform.
 */
public class Browser {
  private static List<String> boringOSList = Arrays.asList("MAC", "WINDOWS", "UNKNOWN");

  private String name;
  private String version;
  private String platform;

  /**
   * Constructs a new Browser from given information as name, version and platform.
   *
   * @param name name of browser.
   * @param version version of browser.
   * @param platform platform on which the browser runs.
   */
  public Browser(String name, String version, String platform) {
    this.name = name;
    this.version = version;
    this.platform = platform;
  }

  /**
   * Constructs a new Browser from a Json object containing all the needed information and
   * 'translate' to appropriate name of platform.
   *
   * @param jsonObject Json object obtained from the configuration file.
   */
  public Browser(JsonObject jsonObject) {
    String browserName = jsonObject.getString("browserName");
    String browserVersion = "?";
    String browserPlatform = "?";
    if (jsonObject.get("version") != null) {
      browserVersion = jsonObject.getString("version").split(Pattern.quote("."))[0];
      if (browserVersion.equals("ANY"))
        browserVersion = "?";
    }

    if (jsonObject.get("platform") != null) {
      List<String> bPlatform =
          Arrays.asList(jsonObject.getString("platform").split(Pattern.quote(" ")));
      browserPlatform = bPlatform.get(0);
      switch (browserPlatform) {
        case "Mac": {
          bPlatform = Arrays.asList(bPlatform.get(3).split(Pattern.quote(".")));
          browserPlatform = "OS X " + bPlatform.get(0) + "." + bPlatform.get(1);
          break;
        }
        case "Windows": {
          if (jsonObject.getString("platform").equals("Windows"))
            browserPlatform = "Windows 10";
          else {
            switch (bPlatform.get(1)) {
              case "XP":
                browserPlatform = "XP";
                break;
              case "7":
                browserPlatform = "Vista";
                break;
              case "8":
                browserPlatform = "Windows 8";
              case "8.1":
                browserPlatform = "Windows 8.1";
                break;
              case "10":
                browserPlatform = "Windows 10";
                break;
            }
            break;
          }
        }
        case "Linux":
          browserPlatform = "Linux";
          break;
        case "Ubuntu":
          browserPlatform = "Linux";
          break;
        default:
          if (Mapping.OS.containsKey(jsonObject.getString("platform"))) {
            browserPlatform = Mapping.OS.get(jsonObject.getString("platform"));
          } else
            browserPlatform = "?";
      }
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
   * Returns true or false on whether a browser is relevant in term of version to be in the
   * overview. The list of interesting browsers and their versions can be found in the class
   * mapping.
   *
   */
  public boolean shouldBeInOverView() {
    if (version.equals("UNKNOWN"))
      return false;
    else {
      switch (name) {
        case "firefox":
          if (!Mapping.FirefoxVersionList.contains(version))
            return false;
          break;
        case "chrome":
          if (!Mapping.ChromeVersionList.contains(version))
            return false;
          break;
        case "MicrosoftEdge":
          if (!Mapping.EdgeVersionList.contains(version))
            return false;
          break;
      }
    }
    if (boringOSList.contains(platform))
      return false;

    return true;
  }

  /**
   * Returns true or false on whether a browser has a certain name and version (to use in overview
   * template only).
   *
   */
  public boolean hasNameAndVersion(List<String> browser) {
    boolean res = true;
    if (!name.equals(browser.get(0)))
      return false;
    if (!version.equals(browser.get(1)))
      return false;
    return res;
  }
  
  /**
   * Returns String representatoin for this Browser object.
   *
   */
  @Override
  public String toString() {
    return new StringBuilder("Browser-name:").append(name).append("-version:")
        .append(version).append("-platform:").append(platform).toString();
  }

}

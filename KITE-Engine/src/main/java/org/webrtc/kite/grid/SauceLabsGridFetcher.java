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

package org.webrtc.kite.grid;

import javax.json.JsonObject;

import org.webrtc.kite.config.Browser;

import java.io.IOException;
import java.util.List;

/**
 * saucelabs implementation of RemoteGridFetcher.
 */
public class SauceLabsGridFetcher extends RemoteGridFetcher {

  /**
   * Constructs a new SauceLabsGridFetcher with the given pathToDB, remoteAddress and restApiUrl.
   *
   * @param pathToDB      path to db.
   * @param remoteAddress string representation of the Selenium hub url.
   * @param restApiUrl    string representation of the rest API url for fetching the supported
   *                      browsers.
   */
  public SauceLabsGridFetcher(String pathToDB, String remoteAddress, String restApiUrl) {
    super(pathToDB, "SAUCE_LABS", remoteAddress, restApiUrl);
  }

  @Override public void fetchConfig() throws IOException {

    List<JsonObject> availableConfigList
        = this.getAvailableConfigList(null, null);

    /* might be not necessary, depending on data format it DB */
    for (JsonObject jsonObject : availableConfigList) {
      String name = jsonObject.getString("api_name", "").trim().toLowerCase();
      if (name.equalsIgnoreCase("internet explorer"))
        name = "iexplore";

      Browser browser = new Browser(name);
      browser.setVersion(jsonObject.getString("short_version", ""));

      String platform = jsonObject.getString("os", "");
      switch (platform) {
        case "Windows 2003":
          platform = "XP";
          break;
        case "Windows 2008":
          platform = "VISTA";
          break;
        case "Windows 2012":
          platform = "WIN8";
          break;
        case "Windows 2012 R2":
          platform = "WIN8_1";
          break;
        case "Windows 10":
          platform = "WIN10";
          break;
        case "Mac 10.8":
          platform = "MOUNTAIN_LION";
          break;
        case "Mac 10.9":
          platform = "MAVERICKS";
          break;
        case "Mac 10.10":
          platform = "YOSEMITE";
          break;
        case "Mac 10.11":
          platform = "EL_CAPITAN";
          break;
        case "Mac 10.12":
          platform = "SIERRA";
          break;
      }
      browser.setPlatform(platform.toUpperCase());

      this.browserList.add(browser);
    }
  }

}

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

/**
 * Enumeration of supported remotes.
 */
public enum SupportedRemote {

  /**
   * Local supported remote.
   */
  local(null, null), /**
   * Saucelabs supported remote.
   */
  saucelabs("https://%s:%s@ondemand.saucelabs.com:443/wd/hub",
          "https://saucelabs.com/rest/v1/info/browsers/webdriver"), /**
   * Browserstack supported remote.
   */
  browserstack("http://%s:%s@hub.browserstack.com/wd/hub",
          "https://browserstack.com/automate/browsers.json"), /**
   * Testingbot supported remote.
   */
  testingbot("http://%s:%s@hub.testingbot.com:4444/wd/hub",
          "https://api.testingbot.com/v1/browsers");

  private final String remoteAddress;
  private final String restApiUrl;

  /**
   * Constructs a new SupportedRemote with the given remote address and the rest API url string
   * representation.
   *
   * @param remoteAddress a string representation of the Selenium hub url
   * @param restApiUrl    a string representation of the rest API url for fetching the supported
   *                      browsers
   */
  private SupportedRemote(String remoteAddress, String restApiUrl) {
    this.remoteAddress = remoteAddress;
    this.restApiUrl = restApiUrl;
  }

  /**
   * Rest api url string.
   *
   * @return the string
   */
  public String restApiUrl() {
    return restApiUrl;
  }

  /**
   * Remote address string.
   *
   * @return the string
   */
  public String remoteAddress() {
    return remoteAddress;
  }

  /**
   * Remote address string.
   *
   * @param username  the username
   * @param accesskey the accesskey
   * @return the string
   */
  public String remoteAddress(String username, String accesskey) {
    return String.format(this.remoteAddress, username, accesskey);
  }

}

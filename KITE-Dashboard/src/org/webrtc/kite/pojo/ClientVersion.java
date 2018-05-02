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

/**
 * ClientVersion object containing the information as name, version, last version and update time.
 */
public class ClientVersion {
  private String name;
  private String version;
  private String lastVersion;
  private String lastUpdate;

  /**
   * Constructs a new Browser from given information as name, version and platform.
   *
   * @param name        name of browser.
   * @param version     current version of browser.
   * @param lastVersion last time updated version of browser.
   * @param lastUpdate  time stamp of the update.
   */
  public ClientVersion(String name, String version, String lastVersion, String lastUpdate) {
    this.name = name;
    this.version = version;
    this.lastVersion = lastVersion;
    this.lastUpdate = lastUpdate;
  }

  public String getLastUpdate() {
    return lastUpdate;
  }

  public String getLastVersion() {
    return lastVersion;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }
}

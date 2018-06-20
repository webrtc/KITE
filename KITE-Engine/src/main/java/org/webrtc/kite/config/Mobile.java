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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Representation of a mobile extended part of the browser object in config file. <p> {
 * "deviceName": "iPhone", "platformName": "iOS", "platformVersion": "11" } <p>
 */
public class Mobile extends KiteConfigObject {

  // Mandatory
  private String deviceName;
  private String platformName;
  private String platformVersion;

  /**
   * Instantiates a new Mobile.
   */
  public Mobile() {

  }

  /**
   * Constructs a new Mobile with the jsonObject.
   *
   * @param jsonObject JsonObject
   */
  public Mobile(JsonObject jsonObject) {
    this.deviceName = jsonObject.getString("deviceName");
    this.platformName = jsonObject.getString("platformName");
    this.platformVersion = jsonObject.getString("platformVersion");
  }

  /**
   * Gets device name.
   *
   * @return the device name
   */
  public String getDeviceName() {
    return deviceName;
  }

  /**
   * Sets device name.
   *
   * @param deviceName the device name
   */
  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  /**
   * Gets platform name.
   *
   * @return the platform name
   */
  public String getPlatformName() {
    return platformName;
  }

  /**
   * Sets platform name.
   *
   * @param platformName the platform name
   */
  public void setPlatformName(String platformName) {
    this.platformName = platformName;
  }

  /**
   * Gets platform version.
   *
   * @return the platform version
   */
  public String getPlatformVersion() {
    return platformVersion;
  }

  /**
   * Sets platform version.
   *
   * @param platformVersion the platform version
   */
  public void setPlatformVersion(String platformVersion) {
    this.platformVersion = platformVersion;
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Mobile mobile = (Mobile) obj;

    if (!deviceName.equals(mobile.deviceName)) {
      return false;
    }
    if (!platformName.equals(mobile.platformName)) {
      return false;
    }
    return platformVersion.equals(mobile.platformVersion);
  }

  @Override public int hashCode() {
    return this.deviceName.hashCode() + this.platformName.hashCode() + platformVersion.hashCode();
  }

  @Override public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder().add("deviceName", this.getDeviceName())
        .add("platformName", this.getPlatformName())
        .add("platformVersion", this.getPlatformVersion());
  }

  @Override public JsonObjectBuilder getJsonObjectBuilderForResult() {
    return this.getJsonObjectBuilder();
  }

}

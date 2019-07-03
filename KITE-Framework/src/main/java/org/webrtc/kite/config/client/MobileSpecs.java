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

package org.webrtc.kite.config.client;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.openqa.selenium.Platform;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;

/**
 * Representation of a mobile extended part of the browser object in config file. <p> {
 * "deviceName": "iPhone", "platformName": "iOS", "platformVersion": "11" } <p>
 */
@Entity (name = MobileSpecs.TABLE_NAME)
public class MobileSpecs extends KiteEntity implements JsonBuilder {

  /**
   * The constant DEFAULT_ORIENTATION.
   */
  final static String DEFAULT_ORIENTATION = "portrait";
  /**
   * The constant DEFAULT_PLATFORM_VERSION.
   */
  final static String DEFAULT_PLATFORM_VERSION = "6";
  /**
   * The Constant TABLE_NAME.
   */
  final static String TABLE_NAME = "mobiles";
  private String deviceName;
  // Mandatory
  private String id;
  /**
   * The orientation.
   */
  private Orientation orientation;
  private String platformName;
  private String platformVersion;
  /**
   * The real device.
   */
  private boolean realDevice;

  /**
   * Instantiates a new mobile.
   */
  public MobileSpecs() {
    super();
  }

  /**
   * Instantiates a new mobile.
   *
   * @param jsonObject the json object
   */
  public MobileSpecs(JsonObject jsonObject) {
    this();
    if (jsonObject.get("mobile") != null) {
      jsonObject = jsonObject.getJsonObject("mobile");
    } else {
      if (jsonObject.get("browserName") != null) {
        return;
      }
    }
    // Mandatory
    this.deviceName = jsonObject.getString("deviceName");
    this.platformVersion = jsonObject.getString("platformVersion", DEFAULT_PLATFORM_VERSION);
    this.platformName = jsonObject.getString("platformName");

    // Optional
    this.orientation = Orientation.valueOf(jsonObject.getString("orientation", DEFAULT_ORIENTATION));
    this.realDevice = jsonObject.getBoolean("realDevice", true);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.cosmo.kite.dao.JsonBuilder#buildJsonObjectBuilder()
   */
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    return Json.createObjectBuilder()
        .add("deviceName", this.deviceName == null ? "N/C" : this.deviceName)
        .add("platformVersion", this.platformVersion == null ? "N/C" : this.platformVersion)
        .add("platformName", this.platformName == null ? "N/C" : this.platformName);
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
   * Gets the id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue (generator = MobileSpecs.TABLE_NAME)
  @GenericGenerator (name = MobileSpecs.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
      @Parameter (name = "prefix", value = "MCFG")
  })
  public String getId() {
    return this.id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the orientation.
   *
   * @return the orientation
   */
  @Enumerated (EnumType.STRING)
  public Orientation getOrientation() {
    return this.orientation;
  }

  /**
   * Sets orientation.
   *
   * @param orientation the orientation
   */
  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  /**
   * Gets the platform name.
   *
   * @return the platform name
   */
  @Enumerated (EnumType.STRING)
  public Platform getPlatformName() {
    return Platform.valueOf(this.platformName);
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

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
    result = prime * result + ((platformVersion == null) ? 0 : platformVersion.hashCode());
    result = prime * result + ((platformName == null) ? 0 : platformName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    MobileSpecs mobile = (MobileSpecs) obj;

    if (!deviceName.equals(mobile.deviceName)) {
      return false;
    }
    if (!platformName.equals(mobile.platformName)) {
      return false;
    }
    return platformVersion.equals(mobile.platformVersion);
  }

  /**
   * Is real device boolean.
   *
   * @return the boolean
   */
  public boolean isRealDevice() {
    return realDevice;
  }

  /**
   * Sets real device.
   *
   * @param realDevice the real device
   */
  public void setRealDevice(boolean realDevice) {
    this.realDevice = realDevice;
  }
}

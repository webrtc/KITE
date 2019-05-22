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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Representation of a App object in the config file.
 * <p>
 * { "app": "PATH_TO_APP_PACKAGE",
 * "mobile": {"deviceName": "iPhone","platformName": "iOS","platformVersion": "11"}
 * }
 * <p>
 */
public class App extends EndPoint {

  // Mandatory
  private String app;
  private String appPackage;
  private String appActivity;
  private final String deviceName;
  private final boolean fullReset;
  
  /**
   * Constructs a new App with the given app and Mobile Object.
   *
   * @param app          path to app package
   * @param deviceName   device's name
   * @param platformName app's platform
   */
  public App(String app, String deviceName, String platformName) {
    this.app = app;
    this.deviceName = deviceName;
    this.platformName = platformName;
    this.appPackage = null;
    this.appActivity = null;
    this.fullReset = true;
  }
  
  /**
   * Constructs a new App with the given remote address and JsonObject.
   *
   * @param remoteAddress a string representation of the Selenium hub url
   * @param jsonObject    JsonObject
   */
  public App(String remoteAddress, JsonObject jsonObject) {
    super(remoteAddress, jsonObject);
    this.app = jsonObject.getString("app");
    this.appPackage = jsonObject.getString("appPackage", null);
    this.appActivity = jsonObject.getString("appActivity", null);
    this.deviceName = jsonObject.getString("deviceName");
    this.platformName = jsonObject.getString("platformName");
    this.fullReset = jsonObject.getString("reset", "fullReset")
      .equalsIgnoreCase("fullReset");
    this.gateway = jsonObject.getString("gateway", null);
  }
  
  /**
   * Constructs a new App with a given App.
   *
   * @param app given App object
   */
  public App(App app) {
    super(app);
    this.app = app.getApp();
    this.appPackage = app.getAppPackage();
    this.appActivity = app.getAppActivity();
    this.deviceName = app.getDeviceName();
    this.fullReset = app.isFullReset();
  }

  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    JsonObjectBuilder jsonObjectBuilder =
      super.getJsonObjectBuilder()
        .add("app", this.app)
        .add("appPackage", this.appPackage)
        .add("appActivity", this.appActivity)
        .add("fullReset", this.appActivity)
      ;

    if (this.deviceName != null) {
      jsonObjectBuilder.add("deviceName", this.deviceName);
    }

    return jsonObjectBuilder;

  }
  
  /**
   * Sets app.
   *
   * @param app the app
   */
  public void setApp(String app) {
    this.app = app;
  }
  
  /**
   * returns app's path to app package
   *
   * @return String app path
   */
  public String getApp() {
    return app;
  }
  
  /**
   * returns app's DeviceName
   *
   * @return String device name
   */
  public String getDeviceName() {
    return deviceName;
  }
  
  /**
   * returns app's starting Activity
   *
   * @return String app activity
   */
  public String getAppActivity() {
    return appActivity;
  }
  
  /**
   * Sets app's starting Activity
   *
   * @param appActivity the app activity
   */
  public void setAppActivity(String appActivity) {
    this.appActivity = appActivity;
  }
  
  /**
   * returns app's package
   *
   * @return String app package
   */
  public String getAppPackage() {
    return appPackage;
  }
  
  /**
   * Sets app's package
   *
   * @param appPackage the app package
   */
  public void setAppPackage(String appPackage) {
    this.appPackage = appPackage;
  }
  
  /**
   * returns app's reset value
   *
   * @return Boolean reset
   */
  public boolean getReset() {
    return fullReset;
  }
  
  /**
   * @return whether the Appium fullReset option should be set to true
   */
  public boolean isFullReset() {
    return fullReset;
  }
}

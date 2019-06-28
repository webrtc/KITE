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

import io.cosmosoftware.kite.interfaces.SampleData;
import org.openqa.selenium.Platform;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;

/**
 * Representation of a App object in the config file.
 * <p>
 * { "app": "PATH_TO_APP_PACKAGE",
 * "mobile": {"deviceName": "iPhone","platformName": "iOS","platformVersion": "11"}
 * }
 * <p>
 */
@Entity (name = Client.TABLE_NAME)
public class App extends Client {
  
  // Mandatory
  private String app;
  private String appActivity;
  private String appPackage;
  private boolean fullReset;
  
  /**
   * Instantiates a new App.
   */
  public App() {
    super();
  }
  
  /**
   * Constructs a new App with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  public App(JsonObject jsonObject) {
    super(jsonObject);
    this.app = jsonObject.getString("app");
    this.appPackage = jsonObject.getString("appPackage", null);
    this.appActivity = jsonObject.getString("appActivity", null);
    this.fullReset = jsonObject.getString("reset", "fullReset")
      .equalsIgnoreCase("fullReset");
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
    this.fullReset = app.isFullReset();
  }
  
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() {
    JsonObjectBuilder builder = super.buildJsonObjectBuilder()
      .add("app", app)
      .add("appPackage", appPackage)
      .add("appActivity", appActivity)
      .add("fullReset", fullReset);
    return builder;
  }
  
  @Override
  public Platform retrievePlatform() {
    return this.mobile.getPlatformName();
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
   * Sets app.
   *
   * @param app the app
   */
  public void setApp(String app) {
    this.app = app;
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
    return appPackage == null ? "" : appPackage;
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
   * Is full reset boolean.
   *
   * @return whether the Appium fullReset option should be set to true
   */
  public boolean isFullReset() {
    return fullReset;
  }
  
  /**
   * Sets full reset.
   *
   * @param fullReset the full reset
   */
  public void setFullReset(boolean fullReset) {
    this.fullReset = fullReset;
  }
  
  @Override
  public SampleData makeSampleData() {
    // todo
    return null;
  }
  
  /**
   * returns app's DeviceName
   *
   * @return String device name
   */
  public String retrieveDeviceName() {
    return mobile.getDeviceName();
  }
  
  /**
   * Retrieve platform version string.
   *
   * @return the string
   */
  public String retrievePlatformVersion() {
    return this.mobile.getPlatformVersion();
  }
  
}

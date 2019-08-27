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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/** Representation of a App object in the config file. */
@Entity(name = App.TABLE_NAME)
public class App extends KiteEntity implements JsonBuilder {

  /** The constant TABLE_NAME. */
  static final String TABLE_NAME = "apps";

  /**
   * The id.
   */
  private String id;

  // Mandatory
  private String appName;

  // Optional
  private String file = "";
  private Boolean fullReset = false;
  private String automationName;
  private String appActivity = "";
  private String appPackage = "";
  private String appWorkingDir = "";

  /** Instantiates a new App. */
  public App() {
    super();
  }

  /**
   * Constructs a new App with the given remote address and JsonObject.
   *
   * @param jsonObject JsonObject
   */
  public App(JsonObject jsonObject) {
    this.appName = jsonObject.getString("appName");
    this.file = jsonObject.getString("file", null);
    this.appPackage = jsonObject.getString("appPackage",null);
    this.appActivity = jsonObject.getString("appActivity", null);
    this.fullReset = jsonObject.getBoolean("fullReset", fullReset);
    this.automationName = jsonObject.getString("automationName", null);
    this.appWorkingDir = jsonObject.getString("appWorkingDir", null);
  }

  /**
   * Constructs a new App with a given App.
   *
   * @param app given App object
   */
  public App(App app) {
    this.file = app.getFile();
    this.appName = app.getAppName();
    this.appPackage = app.getAppPackage();
    this.appActivity = app.getAppActivity();
    this.fullReset = app.isFullReset();
    this.automationName = app.getAutomationName();
    this.appWorkingDir = app.getAppWorkingDir();
  }

  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder
        .add("file", file == null ? "NC" : file)
        .add("name", appName == null ? "NC" : appName)
        .add("appPackage", appPackage == null ? "NC" : appPackage)
        .add("appActivity", appActivity == null ? "NC" : appActivity)
        .add("automationName", automationName == null ? "NC" : automationName)
        .add("appWorkingDir", appWorkingDir == null ? "NC" : appWorkingDir)
        .add("fullReset", fullReset == null ? false : false);
    return builder;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue(generator = App.TABLE_NAME)
  @GenericGenerator(name = App.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
    @org.hibernate.annotations.Parameter(name = "prefix", value = "APPS")
  })
  public String getId() {
    return this.id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getAppName() {
    return appName;
  }

  /**
   * Sets name.
   *
   * @param appName the name
   */
  public void setAppName(String appName) {
    this.appName = appName;
  }

  /**
   * returns either the path to the file or the app name if the app is already installed
   *
   * @return String  the path to the file or the app name
   */
  @Transient
  public String getAppFileOrName() {
    return file != null ? file : appName;
  }
  

  /**
   * returns file to app package (apk for Android, ipa on iOS)
   *
   * @return String file path
   */
  public String getFile() {
    return file;
  }

  /**
   * Sets file.
   *
   * @param file String the file path
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * returns app's automationName
   *
   * @return String automationName
   */
  public String getAutomationName() {
    return automationName;
  }

  /**
   * Sets app's automationName
   *
   * @param automationName the app automationName
   */
  public void setAutomationName(String automationName) {
    this.automationName = automationName;
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
   * returns app's working directory
   *
   * @return String app's working directory
   */
  public String getAppWorkingDir() {
    return appWorkingDir;
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
  public Boolean isFullReset() {
    return fullReset;
  }

  /**
   * Sets full reset.
   *
   * @param fullReset the full reset
   */
  public void setFullReset(Boolean fullReset) {
    this.fullReset = fullReset;
  }

  /**
   * Sets app's working directory
   * @param appWorkingDir app's working directory
   */
  public void setAppWorkingDir(String appWorkingDir) {
    this.appWorkingDir = appWorkingDir;
  }
}

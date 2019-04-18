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
import javax.json.JsonValue;

/**
 * The type Test.
 */
public class Test extends KiteConfigObject {
  
  private final String DEFAULT_DESC = "No description was provided fot this test.";
  
  // Mandatory
  protected final String name;
  protected final String testImpl;
  
  // Optional
  private String description;
  private JsonValue payload;
  private String callbackURL;
  private boolean permute;
  
  /**
   * Constructs a new TestConf with the given callback url and JsonObject.
   *
   * @param callbackURL a string representation of callback url.
   * @param jsonObject  JsonObject
   */
  public Test(boolean permute, String callbackURL, JsonObject jsonObject) {
    this.name = jsonObject.getString("name");
    this.testImpl = jsonObject.getString("testImpl");
    
    this.description =
      jsonObject.getString("description", DEFAULT_DESC);
    this.payload = jsonObject.getOrDefault("payload", null);
    
    // Override the global value with the local value
    this.callbackURL = jsonObject.getString("callback", null);
    
    if (this.callbackURL == null) {
      this.callbackURL = callbackURL;
    }
    
    this.permute = jsonObject.getBoolean("permute", permute);
  }
  
  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
  

  /**
   * Gets test.
   *
   * @return the test
   */
  public String getTestImpl() {
    return testImpl;
  }
  
  
  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Gets payload.
   *
   * @return the payload
   */
  public JsonValue getPayload() {
    return payload;
  }
  
  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(JsonValue payload) {
    this.payload = payload;
  }
  
  /**
   * Gets callback url.
   *
   * @return the callback url
   */
  public String getCallbackURL() {
    return callbackURL;
  }
  
  /**
   * Sets callback url.
   *
   * @param callbackURL the callback url
   */
  public void setCallbackURL(String callbackURL) {
    this.callbackURL = callbackURL;
  }
  
  /**
   * Is permute boolean.
   *
   * @return the boolean
   */
  public boolean isPermute() {
    return permute;
  }
  
  /**
   * Sets permute.
   *
   * @param permute the permute
   */
  public void setPermute(boolean permute) {
    this.permute = permute;
  }
  
  @Override public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder().add("name", this.name).add("testImpl", this.testImpl);
  }
}

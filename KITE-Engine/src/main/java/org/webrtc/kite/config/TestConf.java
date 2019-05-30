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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.webrtc.kite.exception.KiteInsufficientValueException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static org.webrtc.kite.Utils.getIntFromJsonObject;

/**
 * The type TestConf.
 */
public class TestConf extends KiteConfigObject {
  
  
  
  // Mandatory
  private final Logger logger;
  protected final String testImpl;
  protected String name;
  private final String DEFAULT_DESC = "No description was provided fot this test.";
  private String callbackURL;
  private int tupleSize;
  
  // Optional
  private String commandName;
  private String description;
  private int maxRetryCount;
  private int increment;
  private int interval;
  private boolean loadTest;
  private boolean permute;
  private boolean regression;
  private JsonObject testJsonConfig;
  private JsonObject payload;
  private int noOfThreads;
  
  
  
  /**
   * Constructs a new TestConf with the given callback url and JsonObject.
   *
   * Representation of a test object in the config file.
   * <p>
   * {
   * "name": "IceConnectionTest",
   * "description": "Some Description",
   * "tupleSize": 2,
   * "testImpl": "org.webrtc.kite.IceConnectionTest",
   * "payload": "A custom json object",
   * "noOfThreads": 10,
   * "maxRetryCount": 2,
   * "callback": "http://test.com/resulthandler"
   * }
   *
   * @param permute     the permute
   * @param callbackURL a string representation of callback url.
   * @param jsonObject  JsonObject
   */
  public TestConf(boolean permute, String callbackURL, JsonObject jsonObject) throws KiteInsufficientValueException, IOException {
    this.callbackURL = this.callbackURL == null ? callbackURL : this.callbackURL;
    this.testJsonConfig = jsonObject;
    this.payload = jsonObject.getJsonObject("payload");
    this.name = jsonObject.getString("name");
    this.name = name.contains("%ts") ? name.replaceAll("%ts", "") + " (" + timestamp() + ")" : name;
    this.testImpl = jsonObject.getString("testImpl")
      + (System.getProperty("testName") == null ? "" : System.getProperty("testName"));
    this.description = jsonObject.getString("description", DEFAULT_DESC);
    
    // Override the global value with the local value
    this.callbackURL = jsonObject.getString("callback", null);
    this.permute = jsonObject.getBoolean("permute", permute);
    this.regression = jsonObject.getBoolean("regression", false);
    this.tupleSize = getIntFromJsonObject(jsonObject, "tupleSize", -1);
    this.maxRetryCount = getIntFromJsonObject(jsonObject, "maxRetryCount", 0);
    this.loadTest = jsonObject.getBoolean("loadTest", false);
    this.increment = getIntFromJsonObject(jsonObject, "increment", 1);
    this.interval = getIntFromJsonObject(jsonObject, "interval", 5);
    this.noOfThreads = getIntFromJsonObject(jsonObject, "noOfThreads", 1);
    this.logger = createTestLogger();
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
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return Json.createObjectBuilder().add("name", this.name).add("testImpl", this.testImpl);
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
   * Gets payload.
   *
   * @return the payload
   */
  public JsonValue getPayload() {
    return this.payload;
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
   * Is permute boolean.
   *
   * @return the boolean
   */
  public boolean isPermute() {
    return permute;
  }
  
  /**
   * Is regression boolean.
   *
   * @return true if this is a regression test
   */
  public boolean isRegression() {
    return regression;
  }
  
  /**
   * Gets the test config in json object format
   *
   * @return the test config in json object format
   */
  public JsonObject getTestJsonConfig() {
    return testJsonConfig;
  }
  
  /**
   * Gets max retry count.
   *
   * @return the max retry count
   */
  public int getMaxRetryCount() {
    return maxRetryCount;
  }
  
  /**
   * Gets no of threads.
   *
   * @return the no of threads
   */
  public int getNoOfThreads() {
    return noOfThreads;
  }
  
  /**
   * Sets no of threads.
   *
   */
  public void setNoOfThreads(int noOfThreads) {
    this.noOfThreads = noOfThreads;
  }
  
  /**
   * Gets test class name.
   *
   * @return the test class name
   */
  public String getTestClassName() {
    if (isJavascript()) {
      return this.testImpl.substring(0, this.testImpl.indexOf("."));
    } else {
      return this.testImpl.substring(this.testImpl.lastIndexOf(".") + 1);
    }
  }
  
  /**
   * Gets tuple size.
   *
   * @return the tuple size
   */
  public int getTupleSize() {
    return tupleSize;
  }
  
  /**
   * Get logger
   * @return the logger that will be pass down to test runners.
   */
  public Logger getLogger() {
    return logger;
  }
  
  /**
   * Is javascript boolean.
   *
   * @return the boolean
   */
  public boolean isJavascript() {
    return testImpl.endsWith("js");
  }
  
  /**
   * Sets command name.
   *
   * @param commandName the command name
   */
  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }
  
  /**
   * Sets payload.
   *
   * @param payload the payload
   */
  public void setPayload(JsonObject payload) {
    this.payload = payload;
  }
  
  /**
   * Create a common test logger for all test cases of a given test
   *
   * @return the logger for tests
   * @throws IOException if the FileAppender fails
   */
  private Logger createTestLogger() throws IOException {
    Logger testLogger = Logger.getLogger(new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
    FileAppender fileAppender = new FileAppender(new PatternLayout("%d %-5p - %m%n"), "logs/" + getTestClassName() + "/test_" + testLogger.getName() + ".log", false);
    fileAppender.setThreshold(Level.INFO);
    testLogger.addAppender(fileAppender);
    return testLogger;
  }
  
  public void setTupleSize(int tupleSize) {
    this.tupleSize = tupleSize;
  }
  
  public int getIncrement() {
    return increment;
  }
  
  public boolean isLoadTest() {
    return loadTest;
  }
  
}

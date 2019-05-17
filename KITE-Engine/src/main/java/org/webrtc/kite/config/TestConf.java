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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.webrtc.kite.Utils.getIntFromJsonObject;

/**
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
 */
public class TestConf extends Test {
  
  private String commandName;
  private final Logger logger;
  private int maxRetryCount;
  // Mandatory
  private int tupleSize;
  // Optional
  private int noOfThreads;
  
  /**
   * Constructs a new TestConf with the given callback url and JsonObject.
   *
   * @param permute     the permute
   * @param callbackURL a string representation of callback url.
   * @param jsonObject  JsonObject
   *
   * @throws KiteInsufficientValueException the kite insufficient value exception
   */
  public TestConf(boolean permute, String callbackURL, JsonObject jsonObject)
    throws KiteInsufficientValueException, IOException {
    super(permute, callbackURL, jsonObject);
    this.tupleSize = getIntFromJsonObject(jsonObject, "tupleSize", -1);
    this.noOfThreads = getIntFromJsonObject(jsonObject, "noOfThreads", 1);
    this.maxRetryCount = getIntFromJsonObject(jsonObject, "maxRetryCount", 0);
    this.logger = createTestLogger();
  }
  
  @Override
  public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder().add("tupleSize", this.tupleSize);
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
  
}

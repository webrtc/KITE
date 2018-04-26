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

import org.webrtc.kite.exception.KiteInsufficientValueException;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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

  // Mandatory
  private int tupleSize;

  // Optional
  private int noOfThreads;
  private int maxRetryCount;

  /**
   * Constructs a new TestConf with the given callback url and JsonObject.
   *
   * @param callbackURL a string representation of callback url.
   * @param jsonObject  JsonObject
   * @throws KiteInsufficientValueException the kite insufficient value exception
   */
  public TestConf(String callbackURL, JsonObject jsonObject) throws KiteInsufficientValueException {
    super(callbackURL, jsonObject);

    this.tupleSize = jsonObject.getInt("tupleSize");

    this.noOfThreads = jsonObject.getInt("noOfThreads", 1);
    if (this.noOfThreads < 1)
      throw new KiteInsufficientValueException(
          "noOfThreads for " + this.name + " is less than one.");

    this.maxRetryCount = jsonObject.getInt("maxRetryCount", 1);
    if (this.maxRetryCount < 0)
      throw new KiteInsufficientValueException(
          "maxRetryCount for " + this.name + " is a negative value.");
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
   * Sets tuple size.
   *
   * @param tupleSize the tuple size
   */
  public void setTupleSize(int tupleSize) {
    this.tupleSize = tupleSize;
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
   * @param noOfThreads the no of threads
   */
  public void setNoOfThreads(int noOfThreads) {
    this.noOfThreads = noOfThreads;
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
   * Sets max retry count.
   *
   * @param maxRetryCount the max retry count
   */
  public void setMaxRetryCount(int maxRetryCount) {
    this.maxRetryCount = maxRetryCount;
  }

  /**
   * Returns an identifier for the TestConf in the following format:
   * name + "_" + last four digits of the Configurator's timestamp + "_" + index.
   *
   * @param index Index of the testcase in the array
   * @return Remote test identifier
   */
  public String getRemoteTestIdentifier(int index) {
    String identifier = "" + Configurator.getInstance().getTimeStamp();
    identifier = identifier.substring(identifier.length() - 4);
    return name + "_" + identifier + "_" + index;
  }

  @Override public JsonObjectBuilder getJsonObjectBuilder() {
    return super.getJsonObjectBuilder().add("tupleSize", this.tupleSize);
  }

  @Override public JsonObjectBuilder getJsonObjectBuilderForResult() {
    return super.getJsonObjectBuilderForResult().add("tupleSize", this.tupleSize);
  }

}

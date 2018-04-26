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

package org.webrtc.kite;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.webrtc.kite.config.Browser;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.exception.KiteGridException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A thread to execute an implementation of KiteTest.
 * <p>
 * The algorithm of the thread is as follows: 1) Instantiate the WebDriver objects. 2) Instantiate
 * KiteTest implementation. 3) Set the WebDriver objects to the implementation. 4) Execute the test.
 * 5) Retrieve, parse and populate from userAgent. 6) Get the stack trace of an exception if it
 * occurs during the execution. 7) Quit all WebDrivers. 8) Develop result json. 9) Post the result
 * to the callback url synchronously for the first and last test and asynchronously for the rest of
 * the tests.
 */
public class TestManager implements Callable<Object> {

  private static final Logger logger = Logger.getLogger(TestManager.class.getName());

  private static final boolean ENABLE_CALLBACK = true;

  private TestConf testConf;
  private List<Browser> browserList;
  private String testName;

  private int retryCount;

  private int totalTests = 0;
  private boolean isLastTest = false;

  private long timeTaken;
  /**
   * The Web driver list.
   */
  protected List<WebDriver> webDriverList;

  /**
   * Constructs a new TestManager with the given TestConf and List<Browser>.
   *
   * @param testConf    TestConf
   * @param browserList List<Browser>
   * @param testName    the test name
   */
  public TestManager(TestConf testConf, List<Browser> browserList, String testName) {
    this.testConf = testConf;
    this.browserList = browserList;
    this.testName = testName;
  }

  /**
   * Sets total tests.
   *
   * @param totalTests the total tests
   */
  public void setTotalTests(int totalTests) {
    this.totalTests = totalTests;
  }

  /**
   * Sets is last test.
   *
   * @param isLastTest the is last test
   */
  public void setIsLastTest(boolean isLastTest) {
    this.isLastTest = isLastTest;
  }

  /**
   * Checks whether it is the first test of the batch.
   *
   * @return true if totalTests > 0.
   */
  private boolean isFirstTest() {
    return this.totalTests > 0;
  }

  /**
   * Constructs a list of web drivers against the number of provided browsers.
   *
   * @throws MalformedURLException if no protocol is specified in the remoteAddress of a browser, or
   *                               an unknown protocol is found, or spec is null.
   */
  private void populateDrivers(String testName) throws MalformedURLException {
    this.webDriverList = new ArrayList<WebDriver>();
    for (Browser browser : this.browserList)
      this.webDriverList.add(WebDriverUtility.getWebDriverForBrowser(testName, browser));
  }

  /**
   * Retrieves the navigator.userAgent from all of the browser and passes it to the the respective
   * Browser object for processing.
   */
  private void populateInfoFromNavigator() {
    int tupleSize = this.testConf.getTupleSize();
    if (tupleSize != this.webDriverList.size())
      return;

    for (int i = 0; i < tupleSize; i++)
      WebDriverUtility
          .populateInfoFromNavigator(this.webDriverList.get(i), this.browserList.get(i));
  }

  /**
   * Develops the final json object to be posted.
   * <p>
   * Sample output:
   * <p>
   * { "test": { "timeStamp": 1492401875950, "configName": "sample.config", "testName":
   * "IceConnectionTest", "tupleSize": 2, "testImpl": "org.webrtc.kite.IceConnectionTest" },
   * "target": [ { "browserName": "chrome", "version": "56.0", "platform": "Linux" },
   * { "browserName": "firefox", "version": "48.0", "platform": "Mac OS X 10.9" } ],
   * "destination": [ { "browserName": "chrome", "version": "56.0", "platform": "Linux" },
   * { "browserName": "firefox", "version": "48.0", "platform": "Mac OS X 10.9" } ],
   * "payload": { "timeTaken": 9953, "result": "SUCCESSFUL" }, "meta": { "totalTests": 1,
   * "lastTest": true } }
   *
   * @param object of type Exception or String
   * @return
   * @throws KiteGridException if the test has failed
   */
  private JsonObject developResult(Object object) throws KiteGridException {

    Object payload = null;
    if (object instanceof Exception) {
      Exception e = (Exception) object;
      String message = e.getLocalizedMessage();
      message = "{\"result\":\"" + message + "\"}";
      payload = Json.createObjectBuilder()
          .add("result", message == null ? "Message not found in the exception" : message);
    } else if (object instanceof String) {
      payload = Json.createObjectBuilder()
          .add("result", object == null ? "Null result" : (String) object);
    } else {
      throw new IllegalArgumentException("Unexpected class in result " + object.getClass());
    }

    String osName = null;
    String osVersion = null;
    if (object instanceof WebDriverException) {
      final Exception e = (WebDriverException) object;
      final String message = e.getLocalizedMessage();
      if (message != null) {
        final String[] lines = message.split("\\n");
        for (final String l : lines) {
          if (l.startsWith("System info:")) {
            for (final String f : l.split(",")) {
              if (f.startsWith(" os.name")) {
                osName = f.split(":")[1].trim().replace("'", "");
              } else if (f.startsWith(" os.version")) {
                osVersion = f.split(":")[1].trim().replace("'", "");
              }
            }
          }
        }
      } else {
        throw new KiteGridException("The node has failed to execute the test script, "
            + "most likely because of the session had ended unexpectedly for unknown error");
      }
    }

    JsonArrayBuilder targetArrayBuilder = Json.createArrayBuilder();
    for (Browser browser : this.browserList) {
      targetArrayBuilder.add(browser.getJsonObjectBuilder());
    }

    JsonArrayBuilder destinationArrayBuilder = Json.createArrayBuilder();
    for (Browser browser : this.browserList) {
      final JsonObjectBuilder destinationBrowserB =
          browser.getJsonObjectBuilderForResult(osName, osVersion);
      destinationArrayBuilder.add(destinationBrowserB);
    }

    JsonObjectBuilder jsonObjectBuilder =
        Json.createObjectBuilder().add("test", this.testConf.getJsonObjectBuilderForResult())
            .add("target", targetArrayBuilder).add("destination", destinationArrayBuilder)
            .add("result", this.getResult(payload));

    JsonObjectBuilder meta = this.getMeta(true);
    if (meta != null)
      jsonObjectBuilder.add("meta", meta);

    return jsonObjectBuilder.build();

  }

  /**
   * Develops result json object. See developResult() above for details.
   *
   * @param payload payload as String or JsonObjectBuilder.
   * @return JsonObjectBuilder
   */
  private JsonObjectBuilder getResult(Object payload) {
    JsonObjectBuilder jsonObjectBuilder =
        Json.createObjectBuilder().add("timeTaken", this.timeTaken);
    if (payload instanceof JsonObjectBuilder)
      jsonObjectBuilder.add("payload", (JsonObjectBuilder) payload);
    else
      jsonObjectBuilder.add("payload", (String) payload);
    return jsonObjectBuilder;
  }

  /**
   * Develops meta json object. See developResult() above for details.
   * <p>
   * Sample output: {"totalTests": 1, "lastTest":true}} where; totalTests would be part of the first
   * result while lastTest would be part of the last result from the batch.
   *
   * @return JsonObjectBuilder
   */
  private JsonObjectBuilder getMeta(boolean withBrowserList) {
    JsonObjectBuilder jsonObjectBuilder = null;

    if (this.isFirstTest()) {
      if (jsonObjectBuilder == null)
        jsonObjectBuilder = Json.createObjectBuilder();
      jsonObjectBuilder.add("totalTests", this.totalTests);
      if (withBrowserList)
        jsonObjectBuilder.add("browsers", Configurator.getInstance().getBrowserListJsonArray())
            .add("description", this.testConf.getDescription());
    }

    if (this.isLastTest) {
      if (jsonObjectBuilder == null)
        jsonObjectBuilder = Json.createObjectBuilder();
      jsonObjectBuilder.add("lastTest", this.isLastTest);
    }

    return jsonObjectBuilder;
  }

  @Override public Object call() throws Exception {

    boolean performTest = true;
    Object object = null;
    long startTime = System.currentTimeMillis();

    // Populate WebDrivers
    try {
      this.populateDrivers(this.testName);
    } catch (Exception e) {
      logger.error("Exception while populating web drivers", e);
      WebDriverUtility.closeDrivers(this.webDriverList);
      if (this.retryCount < this.testConf.getMaxRetryCount()) {
        this.retryCount++;
        return this;
      } else {
        object = new KiteGridException(e.getLocalizedMessage());
        performTest = false;
      }
    }

    // Perform the test if the driver population was successful
    if (performTest) {
      try {
        KiteTest test = (KiteTest) Class.forName(this.testConf.getTestImpl()).newInstance();

        test.setWebDriverList(this.webDriverList);
        test.setPayload(this.testConf.getPayload());

        logger.info(this.testConf.getName() + " :: Testing against :: " + this.browserList);

        startTime = System.currentTimeMillis();
        object = test.testScript();
        this.timeTaken = System.currentTimeMillis() - startTime;
      } catch (Exception e) {
        logger.warn("Exception while running the test", e);
        this.timeTaken = System.currentTimeMillis() - startTime;
        object = e;
      } finally {
        this.populateInfoFromNavigator();
        WebDriverUtility.closeDrivers(this.webDriverList);
      }
    }

    // Develop result
    JsonObject jsonObject = null;
    try {
      jsonObject = this.developResult(object);
    } catch (KiteGridException e) {
      logger.error("Exception while developing result", e);
      return e;
    }

    // Callback
    if (ENABLE_CALLBACK)
      if (this.testConf.getCallbackURL() == null) {
        logger.warn("No callback specified for " + this.testConf);
      } else {
        CallbackThread callbackThread =
            new CallbackThread(this.testConf.getCallbackURL(), jsonObject);
        // if no "meta", post result in other thread; if "meta", post result in same thread
        // "meta" is included for the first and last tests, that are executed synchronously
        if (jsonObject.getString("meta", null) == null)
          callbackThread.start();
        else
          callbackThread.postResult();
      }

    return jsonObject;

  }

}

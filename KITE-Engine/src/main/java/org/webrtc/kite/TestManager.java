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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
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

  private final static boolean ENABLE_CALLBACK = true;

  private final static Logger logger = Logger.getLogger(TestManager.class.getName());

  private TestConf testConf;
  private List<Browser> browserList;
  private String testName;

  private int retryCount;

  private int totalTests = 0;
  private boolean isLastTest = false;

  private long timeTaken;
  protected List<WebDriver> webDriverList;

  /**
   * Constructs a new TestManager with the given TestConf and List<Browser>.
   *
   * @param testConf TestConf
   * @param browserList List<Browser>
   */
  public TestManager(TestConf testConf, List<Browser> browserList, String testName) {
    this.testConf = testConf;
    this.browserList = browserList;
    this.testName = testName;
  }

  public void setTotalTests(int totalTests) {
    this.totalTests = totalTests;
  }

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
   *         an unknown protocol is found, or spec is null.
   */
  private void populateDrivers(String testName) throws MalformedURLException {
    this.webDriverList = new ArrayList<WebDriver>();
    for (Browser browser : this.browserList) {
      WebDriver webDriver = WebDriverFactory.createWebDriver(browser, testName);
      Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
      browser.setWebDriverVersion(capabilities.getVersion());
      browser.setWebDriverPlatform(capabilities.getPlatform().name());
      this.webDriverList.add(webDriver);
    }
  }

  /**
   * Retrieves the navigator.userAgent from all of the browser and passes it to the the respective
   * Browser object for processing.
   */
  private void populateInfoFromNavigator() {
    int tupleSize = this.testConf.getTupleSize();
    if (tupleSize != this.webDriverList.size())
      return;
    for (int i = 0; i < tupleSize; i++) {
      try {
        Browser browser = this.browserList.get(i);
        if (browser.shouldGetUserAgent()) {
          WebDriver webDriver = this.webDriverList.get(i);
          if (!WebDriverFactory.isAlive(webDriver))
            webDriver.get("http://www.google.com");
          Object resultObject =
              ((JavascriptExecutor) webDriver).executeScript(userAgentScript());
          if (resultObject instanceof String) {
            String resultOfScript = (String) resultObject;
            logger.info("Browser platform and userAgent->" + resultOfScript);
            browser.setUserAgentVersionAndPlatfom(resultOfScript);
          } else {
            logger.warn("Bad result object for->" + browser.toString() + "::" + resultObject);
          }
        }
      } catch (Exception e) {
        logger.error(e);
      }
    }
  }

  /**
   * Returns the JavaScript to get navigator.userAgent.
   *
   * @return the JavaScript as string.
   */
  private final static String userAgentScript() {
    return "var nav = '';" + "try { var myNavigator = {};"
        + "for (var i in navigator) myNavigator[i] = navigator[i];"
        + "nav = JSON.stringify(myNavigator); } catch (exception) { nav = exception.message; }"
        + "return nav;";
  }

  /**
   * Quits all of the web drivers in the list.
   */
  private void closeDrivers() {
    for (WebDriver webDriver : this.webDriverList)
      try {
        // Open about:config in case of fennec (Firefox for Android) and close.
        if (((RemoteWebDriver) webDriver).getCapabilities().getBrowserName().equalsIgnoreCase("fennec")) {
          webDriver.get("about:config");
          webDriver.close();
        }
          webDriver.quit();
      } catch (Exception e) {
        logger.error("closing driver:", e);
      }
  }

  /**
   * Develops the final json object to be posted.
   * <p>
   * Sample output:
   * 
   * { "test": { "timeStamp": 1492401875950, "configName": "sample.config", "testName":
   * "IceConnectionTest", "tupleSize": 2, "testImpl": "org.webrtc.kite.IceConnectionTest" },
   * "browsers": [ { "browserName": "chrome", "version": "56.0", "platform": "Linux" }, {
   * "browserName": "firefox", "version": "48.0", "platform": "Mac OS X 10.9" } ], "result": {
   * "timeTaken": 9953, "payload": "SUCCESSFUL" }, "meta": { "totalTests": 1, "lastTest": true } }
   *
   * @param object an instance of Exception or any Object with toString() implementation.
   * @return JsonObject
   */
  private JsonObject developResult(Object object) throws KiteGridException {
    JsonArrayBuilder targetArrayBuilder = Json.createArrayBuilder();
    JsonArrayBuilder destinationArrayBuilder = Json.createArrayBuilder();
    for (Browser browser : this.browserList) {
      targetArrayBuilder.add(browser.getJsonObjectBuilder());
    }

    Object payload = null;
    if (object instanceof Exception) {
      Exception e = (Exception) object;
      String message = e.getLocalizedMessage();
/*      payload = Json.createObjectBuilder().add("type", e.getClass().getName()).add("message",
          message == null ? "Message not found in the exception" : message);*/
      payload = Json.createObjectBuilder().add("result",
              message == null ? "Message not found in the exception" : message);
    }
    else if (object instanceof String) {
      payload = Json.createObjectBuilder().add("result",
          object == null ? "Null result" : (String) object);      
    }
    else {
      throw new IllegalArgumentException("Unexpected class in result " + object.getClass());
    }
    
    String osName = null;
    String osVersion = null;
    if (object instanceof WebDriverException) {
      final Exception e = (WebDriverException) object;
      final String message = e.getLocalizedMessage();
      if (message!=null) {
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
        throw new KiteGridException("The node has failed to execute the test script, " +
                "most likely because of the session had ended unexpectedly for unknown error");
      }
    }

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
                .add("description",this.testConf.getDescription());
    }

    if (this.isLastTest) {
      if (jsonObjectBuilder == null)
        jsonObjectBuilder = Json.createObjectBuilder();
      jsonObjectBuilder.add("lastTest", this.isLastTest);
    }

    return jsonObjectBuilder;
  }

  @Override
  public Object call() throws Exception {

    try {

      boolean performTest = true;
      Object object = null;
      long startTime = System.currentTimeMillis();

      try {
        this.populateDrivers(this.testName);
      } catch (Exception e) {
        logger.warn("Exception while populating web drivers", e);
        this.closeDrivers();
        if (this.retryCount < this.testConf.getMaxRetryCount()) {
          this.retryCount++;
          return this;
        } else {
          object = new KiteGridException(e.getLocalizedMessage());
          performTest = false;
        }
      }

      if (performTest)
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
          this.closeDrivers();
        }

      try {
        JsonObject jsonObject = this.developResult(object);
        if (ENABLE_CALLBACK) {
          if (this.testConf.getCallbackURL() == null)
            logger.warn("No callback specified for " + this.testConf);
          else {
            CallbackThread callbackThread =
                new CallbackThread(this.testConf.getCallbackURL(), jsonObject);
            // if no "meta", post result in other thread; if "meta", post result in same thread
            // "meta" is included for the first and last tests, that are executed synchronously
            if (jsonObject.getString("meta", null) == null)
              callbackThread.start();
            else
              callbackThread.postResult();
          }
        }

        return jsonObject;
      } catch (Exception e) {
        logger.error("Developing and posting result:", e);
        return e;
      }

    } catch (Exception e) {
      logger.error("Running test:", e);
      return e;

    }

  }

}

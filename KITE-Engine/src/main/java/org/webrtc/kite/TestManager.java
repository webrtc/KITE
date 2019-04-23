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

import io.cosmosoftware.kite.report.Container;
import org.apache.log4j.Logger;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.KiteJsTest;

import javax.json.JsonObject;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A thread to step an implementation of KiteTest.
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
  private final TestConf testConf;
  private final List<EndPoint> endPointList;
  private final int retry;
  private Container testSuite;
  private final Logger testLogger;
  /**
   * Constructs a new TestManager with the given TestConf and List<EndPoint>.
   *
   * @param testConf             TestConf
   * @param endPointList List<EndPoint>
   */
  public TestManager(TestConf testConf, List<EndPoint> endPointList, int id, Logger testLogger) {
    this.testConf = testConf;
    this.endPointList = endPointList;
    this.retry = id;
    this.testLogger = testLogger;
  }
  
  @Override
  public Object call() throws Exception {
    String testImpl = this.testConf.getTestImpl();
    KiteBaseTest test;
    if (this.testConf.isJavascript()) {
      test = new KiteJsTest(testImpl);
    } else {
      test = (KiteBaseTest) Class.forName(this.testConf.getTestImpl()).getConstructor().newInstance();
    }
    test.setLogger(testLogger);
    testSuite.addChild(test.getReport().getUuid());
    test.setDescription(testConf.getDescription());
    test.setParentSuite(Configurator.getInstance().getName());
    test.setSuite(testSuite.getName());
    test.setPayload(this.testConf.getPayload());
    test.setEndPointList(endPointList);
    JsonObject testResult = test.execute();
    
    // todo: need some retry mechanism here
    
    if (ENABLE_CALLBACK) {
      if (this.testConf.getCallbackURL() == null) {
        logger.warn("No callback specified for " + this.testConf);
      } else {
        CallbackThread callbackThread =
          new CallbackThread(this.testConf.getCallbackURL(), testResult);
        // if no "meta", post result in other thread; if "meta", post result in same thread
        // "meta" is included for the first and last tests, that are executed synchronously
        if (testResult.getString("meta", null) == null) {
          callbackThread.start();
        } else {
          callbackThread.postResult();
        }
      }
    }
    
    // Update allure report:
    
    return testResult;
  }
  
  
  public void setTestSuite(Container testSuite) {
    this.testSuite = testSuite;
  }
}

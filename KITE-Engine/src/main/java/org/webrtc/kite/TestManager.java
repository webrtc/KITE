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
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.KiteJsTest;

import javax.json.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A thread to step an implementation of KiteTest.
 * <p>
 * The algorithm of the thread is as follows:
 * 1) Instantiate the WebDriver objects.
 * 2) Instantiate KiteBaseTest implementation.
 * 3) Set the endpoint list to the implementation.
 * 4) Execute the test.
 * 5) Retrieve, parse and populate from userAgent.
 * 6) Get the stack trace of an exception if it occurs during the execution.
 * 7) Quit all WebDrivers.
 * 8) Develop result json.
 * 9) Post the result to the callback url
 * </p>
 */
public class TestManager implements Callable<Object> {
  
  private final int retry;
  private final boolean ENABLE_CALLBACK = true;
  private final TestConf testConf;
  private final List<EndPoint> endPointList;
  private Container testSuite;
  
  /**
   * Constructs a new TestManager with the given TestConf and List<EndPoint>.
   *
   * @param testConf     test configuration object
   * @param endPointList tuple of endpoint for this test
   */
  public TestManager(TestConf testConf, List<EndPoint> endPointList) {
    this.testConf = testConf;
    this.endPointList = endPointList;
    this.retry = testConf.getMaxRetryCount();
  }
  
  private KiteBaseTest buildTest() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    KiteBaseTest test = testConf.isJavascript()
      ? new KiteJsTest(testConf.getTestImpl())
      : (KiteBaseTest) Class.forName(this.testConf.getTestImpl()).getConstructor().newInstance();
    
    test.setSuite(testSuite.getName());

    test.setLogger(testConf.getLogger());
    test.setDescription(testConf.getDescription());
    test.setPayload(testConf.getPayload());
  
    test.setConfigFilePath(Configurator.getInstance().getConfigFilePath());
    test.setInstrumentation(Configurator.getInstance().getInstrumentation());
    test.setParentSuite(Configurator.getInstance().getName());
    
    test.setEndPointList(endPointList);
    
    return test;
  }
  
  @Override
  public Object call() throws Exception {
    KiteBaseTest test = buildTest();
    testSuite.addChild(test.getReport().getUuid());
    
    JsonObject testResult = test.execute();
    
    // todo: need some retry mechanism here
    
    if (ENABLE_CALLBACK) {
      if (this.testConf.getCallbackURL() != null) {
        CallbackThread callbackThread =
          new CallbackThread(this.testConf.getCallbackURL(), testResult);
        if (testResult.getString("meta", null) == null) {
          callbackThread.start();
        } else {
          callbackThread.postResult();
        }
      }
    }
    
    // todo: Update allure report for on-going status
    
    return testResult;
  }
  
  /**
   * Set the container for the test's parent suite, for report purpose
   *
   * @param testSuite test's parent suite
   */
  public void setTestSuite(Container testSuite) {
    this.testSuite = testSuite;
  }
  
}

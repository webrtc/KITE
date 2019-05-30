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
import io.cosmosoftware.kite.steps.StepPhase;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.config.Tuple;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.KiteJsTest;

import javax.json.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
  private final Tuple tuple;
  private String id;
  private Container testSuite;
  
  /**
   * Constructs a new TestManager with the given TestConf and List<EndPoint>.
   *
   * @param testConf     test configuration object
   * @param tuple       tuple of endpoint for this test
   */
  public TestManager(TestConf testConf, Tuple tuple) {
    this.testConf = testConf;
    this.tuple = tuple;
    this.retry = testConf.getMaxRetryCount();
  }
  
  private KiteBaseTest buildTest() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    KiteBaseTest test = testConf.isJavascript()
      ? new KiteJsTest(testConf.getTestImpl())
      : (KiteBaseTest) Class.forName(this.testConf.getTestImpl()).getConstructor().newInstance();
    
    test.setSuite(testSuite.getName());
  
    test.setConfigFilePath(Configurator.getInstance().getConfigFilePath());
    test.setInstrumentation(Configurator.getInstance().getInstrumentation());
    test.setParentSuite(Configurator.getInstance().getName());
    
    test.setLogger(testConf.getLogger());
    test.setDescription(testConf.getDescription());
    test.setPayload(testConf.getPayload());

    test.setEndPointList(tuple);
    
    if (testConf.isLoadTest()) {
      test.setLoadTest(true);
      String simpleHubId = "";
      try {
        simpleHubId = new URL(tuple.get(0).getRemoteAddress()).getHost();
      } catch (Exception e) {
        simpleHubId = tuple.get(0).getRemoteAddress();
      }
      test.setName(simpleHubId + (id == null ? "" : " " + id));
  
    }
    
    return test;
  }
  
  @Override
  public Object call() throws Exception {
    
    KiteBaseTest test = buildTest();
    
    testSuite.addChild(test.getReport().getUuid());

    JsonObject testResultRampup = test.execute(StepPhase.RAMPUP);
    JsonObject testResultLoadReached = test.execute(StepPhase.LOADREACHED);
    
    // todo: need some retry mechanism here
    
    if (ENABLE_CALLBACK) {
      if (this.testConf.getCallbackURL() != null) {
        CallbackThread callbackThread =
          new CallbackThread(this.testConf.getCallbackURL(), testResultRampup);
        if (testResultRampup.getString("meta", null) == null) {
          callbackThread.start();
        } else {
          callbackThread.postResult();
        }
      }
    }
    
    // todo: Update allure report for on-going status
    
    return testResultRampup;
  }
  
  /**
   * Set the container for the test's parent suite, for report purpose
   *
   * @param testSuite test's parent suite
   */
  public void setTestSuite(Container testSuite) {
    this.testSuite = testSuite;
  }
  
  
  /**
   * Set the id, or test case id, for report purpose
   *
   * @param id case id
   */
  public void setId(String id) {
    this.id = id;
  }
}

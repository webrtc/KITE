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
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.KiteJsTest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static io.cosmosoftware.kite.report.CSVHelper.jsonToString;
import static io.cosmosoftware.kite.util.TestUtils.getDir;

/**
 * A thread to step an implementation of KiteTest.
 * <p>
 * The algorithm of the thread is as follows:
 * 1) Instantiate the WebDriver objects.
 * 2) Instantiate KiteBaseTest implementation.
 * 3) Set the client list to the implementation.
 * 4) Execute the test.
 * 5) Retrieve, parse and populate from userAgent.
 * 6) Get the stack trace of an exception if it occurs during the execution.
 * 7) Quit all WebDrivers.
 * 8) Develop result json.
 * 9) Post the result to the callback url
 * </p>
 */
public class TestManager implements Callable<Object> {
  final String JAR_DOWNLOAD_PATH = getDir("java.io.tmpdir");
  private final boolean ENABLE_CALLBACK = true;
  private final int retry;
  private long startTimestamp = System.currentTimeMillis();
  private long endTimestamp = 0;
  private final TestConfig testConfig;
  private final Tuple tuple;
  private String id;
  private List<StepPhase> phases;
  private Container testSuite;

  /**
   * Constructs a new TestManager with the given TestConfig and List<Client>.
   *
   * @param testConfig test configuration object
   * @param tuple      tuple of client for this test
   */
  public TestManager(TestConfig testConfig, Tuple tuple) {
    this.testConfig = testConfig;
    this.tuple = tuple;
    this.retry = testConfig.getMaxRetryCount();

  }

  private KiteBaseTest buildTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
    String testJar = testConfig.getImplJar() == null ? null : JAR_DOWNLOAD_PATH + testConfig.getName() + ".jar";
    KiteBaseTest test = testConfig.isJavascript()
        ? new KiteJsTest(testConfig.getTestImpl(), testConfig.getImplJar())
        : (KiteBaseTest) instantiate(testConfig.getTestImpl(), testJar);

    test.setTestJar(testJar);
    test.setLogger(testConfig.getLogger());
    test.setReporter(this.testConfig.getReporter());
    test.setTuple(tuple);
    test.setCloseWebDrivers(testConfig.getCloseBrowsers());
    phases = getPhases(testConfig.isLoadTest());
    test.setPhases(phases);
    test.setNetworkInstrumentation(testConfig.getNetworkInstrumentation());
    test.setParentSuite(testSuite.getParentSuite());
    test.setSuite(testSuite);
    test.setDescription(testConfig.getDescription());
    test.setRoomManager(testConfig.getRoomManager());
    if (testConfig.getPayload() != null) {
      JsonObject payload = (JsonObject) Json.createReader(new ByteArrayInputStream(testConfig.getPayload().getBytes())).read();
      test.setPayload(payload);
    }

    if (testConfig.isLoadTest()) {
      test.setLoadTest(true);
      String simpleHubId = "";
      try {
        simpleHubId = new URL(tuple.get(0).getPaas().getUrl()).getHost();
      } catch (Exception e) {
        simpleHubId = tuple.get(0).getPaas().getUrl();
      }
      test.setName(simpleHubId + (id == null ? "" : " " + id));
    }

    return test;
  }

  @Override
  public Object call() throws Exception {
    startTimestamp = System.currentTimeMillis();
    KiteBaseTest test = buildTest();
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (StepPhase phase : phases) {
      testSuite.addChild(test.getReport(phase).getUuid());
      Object phaseResult = test.execute(phase);
      builder.add(phase.getName(), (JsonObject) phaseResult);
    }
    JsonObject testResult = builder.build();
    // todo: need some retry mechanism here

    JsonObject jsonTestResult = developResult(testResult);

    if (ENABLE_CALLBACK) {
      if (this.testConfig.getCallbackUrl() != null) {
        CallbackThread callbackThread =
            new CallbackThread(this.testConfig.getCallbackUrl(), jsonTestResult);
        // todo: to be advised
//        if (testResult.getString("meta", null) == null) {
//          callbackThread.start();
//        } else {
//          callbackThread.postResult();
//        }
      }
    }

    // todo: Update allure report for on-going status

    endTimestamp = System.currentTimeMillis();
    if (testConfig.getEmailSender() != null) {
      String emailText = "\r\n";
      emailText += jsonToString(jsonTestResult);
      testConfig.getEmailSender().send(emailText);
    }
    return jsonTestResult;
  }

  private JsonObject developResult(Object object) {
    if (endTimestamp == 0) {
      endTimestamp = System.currentTimeMillis();
    }
    return Json.createObjectBuilder()
        .add("resultId", tuple.getResultId() == null
            ? UUID.randomUUID().toString()
            : tuple.getResultId())
        .add("startTimestamp", "" + startTimestamp)
        .add("endTimestamp", "" +
            (endTimestamp == 0 ? System.currentTimeMillis() : endTimestamp))
        .add("result", object instanceof Exception
            ? ((Exception) object).getMessage()
            : object.toString()).build();
  }

  private ArrayList<StepPhase> getPhases(boolean isLoad) {
    ArrayList<StepPhase> phases = new ArrayList<>();
    if (isLoad) {
      phases.add(StepPhase.RAMPUP);
      phases.add(StepPhase.LOADREACHED);
    } else {
      phases.add(StepPhase.DEFAULT);
    }
    return phases;
  }

  /**
   * Instantiate.
   *
   * @param className the class name
   * @param jarFile   the jar file
   * @return the object
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws ClassNotFoundException the class not found exception
   * @throws MalformedURLException  the malformed URL exception
   */
  private Object instantiate(String className, String jarFile)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
    Object object = null;
    if (jarFile == null) {
      object = Class.forName(className).newInstance();
    } else {
      URLClassLoader classLoader =  URLClassLoader
          .newInstance(new URL[]{new File(jarFile).toURI().toURL()}, TestManager.class.getClassLoader());
      Thread.currentThread().setContextClassLoader(classLoader);
      object = classLoader.loadClass(className).newInstance();     
    }
    return object;
  }

  /**
   * Set the id, or test case id, for report purpose
   *
   * @param id case id
   */
  public void setId(String id) {
    this.id = id;
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

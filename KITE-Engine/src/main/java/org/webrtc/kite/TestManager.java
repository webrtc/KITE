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

import static io.cosmosoftware.kite.report.CSVHelper.jsonToString;
import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.TestUtils.getDir;

import io.cosmosoftware.kite.report.Container;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.usrmgmt.EmailSender;
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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.tests.KiteBaseTest;
import org.webrtc.kite.tests.KiteJsTest;
import org.webrtc.kite.tests.TestRunner;

/**
 * A thread to step an implementation of KiteTest.
 * <p>
 * The algorithm of the thread is as follows: 1) Instantiate the WebDriver objects. 2) Instantiate
 * KiteBaseTest implementation. 3) Set the client list to the implementation. 4) Execute the test.
 * 5) Retrieve, parse and populate from userAgent. 6) Get the stack trace of an exception if it
 * occurs during the execution. 7) Quit all WebDrivers. 8) Develop result json. 9) Post the result
 * to the callback url
 * </p>
 */
public class TestManager implements Callable<Object> {

  private final KiteLogger logger = KiteLogger.getLogger(TestManager.class.getName());

  /** The jar download path. */
  final String JAR_DOWNLOAD_PATH = getDir("java.io.tmpdir");

  /** The enable callback. */
  private final boolean ENABLE_CALLBACK = true;

  /** The retry. */
  private final int retry;

  /** The start timestamp. */
  private long startTime = System.currentTimeMillis();

  /** The end timestamp. */
  private long endTimestamp = 0;

  /** The test config. */
  private final TestConfig testConfig;

  /** The tuple. */
  private final Tuple tuple;

  /** The id. */
  private int id;

  private int total = 0;

  /** The phases. */
  private List<StepPhase> phases;

  /** The test suite. */
  private Container testSuite;

  /** The test. */
  private KiteBaseTest test;

  private StepPhase currentPhase;

  private boolean finished = false;
  /**
   * Constructs a new TestManager with the given TestConfig and List<Client>.
   *
   * @param testConfig test configuration object
   * @param tuple tuple of client for this test
   */
  public TestManager(TestConfig testConfig, Tuple tuple) {
    this.testConfig = testConfig;
    this.tuple = tuple;
    this.retry = testConfig.getMaxRetryCount();
    this.phases = getPhases(testConfig.isLoadTest());
  }

  /**
   * Builds the test.
   *
   * @return the kite base test
   * @throws ClassNotFoundException the class not found exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InstantiationException the instantiation exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private KiteBaseTest buildTest()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
    String testJar =
        testConfig.getImplJar() == null ? null : JAR_DOWNLOAD_PATH + testConfig.getName() + ".jar";
    KiteBaseTest test = testConfig.isJavascript()
        ? new KiteJsTest(testConfig.getTestImpl(), testConfig.getImplJar())
        : (KiteBaseTest) instantiate(testConfig.getTestImpl(), testJar);

    test.setTestJar(testJar);
    
    test.setTestConfig(testConfig);
    
    test.setLogger(testConfig.getLogger());
    
    test.setReporter(this.testConfig.getReporter());
    
    test.setNetworkInstrumentation(testConfig.getNetworkInstrumentation());
    test.setDescription(testConfig.getDescription());
    test.setRoomManager(testConfig.getRoomManager());    
    
    test.setTuple(tuple);
    test.setCurrentIteration(this.id);
    test.setDelayForClosing(testConfig.getDelayForClosing());

    test.setPhases(this.phases);

    test.setParentSuite(testSuite.getParentSuite());
    test.setSuite(testSuite);

    if (testConfig.getPayload() != null) {
      JsonObject payload = (JsonObject) Json
          .createReader(new ByteArrayInputStream(testConfig.getPayload().getBytes())).read();
      test.setPayload(payload);
    }

    // todo : to be advised
    if (testConfig.isLoadTest()) {
      test.setLoadTest(true);
      String simpleHubId = "";
      try {
        simpleHubId = new URL(tuple.get(0).getPaas().getUrl()).getHost();
      } catch (Exception e) {
        simpleHubId = tuple.get(0).getPaas().getUrl();
      }
      test.setName(simpleHubId + (id == 0 ? "" : " " + id));
    }

    return test;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Object call() throws Exception {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    if (!currentPhase.equals(StepPhase.LOADREACHED)) {
      startTime = System.currentTimeMillis();
      if (this.id == 1) { // not zero ?
        this.testConfig.getReporter().setStartTime(startTime);
      }
      test = buildTest();
    }
    Object phaseResult;
    if (!testConfig.isLoadTest()) {
      String ETA = "Estimating..";
      long elapsedSecond = (int)(System.currentTimeMillis() - testConfig.getReporter().getStartTime())/1000;
      int currentTestCount = testConfig.getReporter().numberOfRegisteredTest();
      long avgTimeForOneTest = currentTestCount == 1 ? 0 : elapsedSecond/(currentTestCount - 1);
      if (avgTimeForOneTest != 0) {
        long ETASeconds = avgTimeForOneTest*(total - id + 1);
        ETA = ETASeconds/60 + "m" + ETASeconds%60 + "s";
      }
      logger.info("\n"
          + "           |--------------------------------------------------\n"
          + "           | Number of registered tests: " + currentTestCount + "\n"
          + "           | Elapsed time: " + elapsedSecond/60 + "m" + (elapsedSecond%60)  + "s (avg:"
                        + avgTimeForOneTest/60 + "m" + avgTimeForOneTest%60 + "s" + ")\n"
          + "           | Running test case with ID: " + this.id + "/" + (total == 0 ? "N/A" : total) + "\n"
          + "           | ETA (for " + this.testConfig.getName() + "): " + ETA + "\n"
          + "           |--------------------------------------------------");
      // if there will be more phase to interop test in the future, we'll deal with it in the future
      phaseResult = test.execute(currentPhase);
    } else {
      if (currentPhase.equals(StepPhase.RAMPUP)) {
        phaseResult = test.execute(currentPhase);
        logger.info("RAM UP phase finished, return test manager object for LOAD REACHED execution..");
        if (phaseResult != null) {
          builder.add(currentPhase.getName(), (JsonObject) phaseResult);
        }
        this.currentPhase = StepPhase.LOADREACHED;
        return this;
      } else {
        phaseResult = test.execute(currentPhase);
        logger.info("LOAD REACHED phase finished, finishing the test ..");
      }
    }

    if (phaseResult != null) {
      builder.add(currentPhase.getName(), (JsonObject) phaseResult); 
    }
    JsonObject jsonTestResult = developResult(builder.build());

    if (ENABLE_CALLBACK) {
      if (this.testConfig.getCallbackUrl() != null) {
        CallbackThread callbackThread =
            new CallbackThread(this.testConfig.getCallbackUrl(), jsonTestResult);
        // todo
      }
    }

    sendEmail(jsonTestResult);
    test = null;
    finished = true;
    return jsonTestResult;
  }

  public boolean isFinished() {
    return finished;
  }

  public StepPhase getCurrentPhase() {
    return currentPhase;
  }

  public TestConfig getTestConfig() {
    return testConfig;
  }

  private void sendEmail(JsonObject jsonTestResult) {
    try {
      // check if the test passed or failed
      boolean testFailed = false;
      for (StepPhase phase : phases) {
        if (test.isLoadTest()) {
          for (TestRunner runner : test) {
            Status status = runner.getReport(phase).getActualStatus();
            if (status.equals(Status.FAILED) || status.equals(Status.BROKEN)) {
              testFailed = true;
              break;
            }
          }
        } else {
          Status status = test.getReport(phase).getActualStatus();
          if (status.equals(Status.FAILED) || status.equals(Status.BROKEN)) {
            testFailed = true;
            break;
          }
        }
      }
      endTimestamp = System.currentTimeMillis();
      EmailSender emailSender = testConfig.getEmailSender();
      logger.info("Test " + (testFailed ? "FAILED" : "PASSED"));
      if (emailSender != null
        && ((emailSender.sendOnlyOnFailure() && testFailed) || !emailSender.sendOnlyOnFailure())) {
        if (emailSender.sendJsonResults()) {
          String emailText = "\r\n";
          emailText += jsonToString(jsonTestResult);
          emailSender.send(emailText);
        } else {
          emailSender.send();
        }
      }
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    }
  }
  
  /**
   * Develop result.
   *
   * @param object the object
   * @return the json object
   */
  private JsonObject developResult(Object object) {
    if (endTimestamp == 0) {
      endTimestamp = System.currentTimeMillis();
    }
    return Json.createObjectBuilder()
        .add("resultId",
            tuple.getResultId() == null ? UUID.randomUUID().toString() : tuple.getResultId())
        .add("startTime", "" + startTime)
        .add("endTimestamp", "" + (endTimestamp == 0 ? System.currentTimeMillis() : endTimestamp))
        .add("result",
            object instanceof Exception ? ((Exception) object).getMessage() : object.toString())
        .build();
  }

  /**
   * Gets the phases.
   *
   * @param isLoad the is load
   * @return the phases
   */
  private ArrayList<StepPhase> getPhases(boolean isLoad) {
    ArrayList<StepPhase> phases = new ArrayList<>();
    if (isLoad) {
      phases.add(StepPhase.RAMPUP);
      phases.add(StepPhase.LOADREACHED);
    } else {
      phases.add(StepPhase.DEFAULT);
    }
    this.currentPhase = phases.get(0);
    logger.info("current phase in test manager has been set to" + this.currentPhase);
    return phases;
  }

  /**
   * Instantiate.
   *
   * @param className the class name
   * @param jarFile the jar file
   * @return the object
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws ClassNotFoundException the class not found exception
   * @throws MalformedURLException the malformed URL exception
   */
  private Object instantiate(String className, String jarFile) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException, MalformedURLException {
    Object object = null;
    if (jarFile == null) {
      object = Class.forName(className).newInstance();
    } else {
      URLClassLoader classLoader = URLClassLoader.newInstance(
          new URL[] {new File(jarFile).toURI().toURL()}, TestManager.class.getClassLoader());
      Thread.currentThread().setContextClassLoader(classLoader);
      object = classLoader.loadClass(className).newInstance();
    }
    return object;
  }

  /**
   * Set the id, or test case id, for report purpose.
   *
   * @param id case id
   */
  public void setId(int id) {
    this.id = id;
  }
  /**
   * Set the container for the test's parent suite, for report purpose.
   *
   * @param testSuite test's parent suite
   */
  public void setSuite(Container testSuite) {
    this.testSuite = testSuite;
    if (this.test != null) {
      test.setSuite(testSuite);
    }
  }

  public void moveToNextPhase() {
    this.currentPhase = phases.get((phases.indexOf(currentPhase) + 1 ));
  }


  public Tuple getTuple() {
    return tuple;
  }

  /**
   * Terminate.
   */
  public void terminate() {
    if (this.test != null) {
      for (StepPhase stepPhase : this.phases) {
        this.test.terminate(stepPhase);
      }
    }
  }

  public void setTotal(int total) {
    this.total = total;
  }
}

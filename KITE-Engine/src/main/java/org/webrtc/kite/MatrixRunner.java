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

import static io.cosmosoftware.kite.entities.Timeouts.FIVE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.entities.Timeouts.ONE_SECOND_INTERVAL;
import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;
import static io.cosmosoftware.kite.util.TestUtils.waitAround;

import io.cosmosoftware.kite.report.Container;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.steps.StepPhase;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.webrtc.kite.config.client.BrowserSpecs;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;

// TODO: Auto-generated Javadoc
/**
 * A class to manage the asynchronous execution of TestManager objects.
 */
public class MatrixRunner {

  /** The logger. */
  private final KiteLogger logger = KiteLogger.getLogger(MatrixRunner.class.getName());

  /** The test config. */
  private final TestConfig testConfig;

  /** The interrupted. */
  private boolean interrupted;

  /** The multi executor service. */
  private ExecutorService multiExecutorService;

  /** The tuple list. */
  private List<Tuple> tupleList = new ArrayList<>();

  /** The test suite. */
  private Container testSuite;

  /** The test manager list. */
  private List<TestManager> testManagerList = new ArrayList<>();

  private StepPhase currentPhase;

  private int currentIteration = 0;

  private boolean lastThread = false;

  /**
   * Constructs a new MatrixRunner with the given TestConfig and List<Tuple>.
   *
   * @param testConfig TestConfig
   * @param listOfTuples a list of tuples (containing 1 or multiples kite config objects).
   * @param parentSuiteName the parent suite name
   */
  public MatrixRunner(TestConfig testConfig, List<Tuple> listOfTuples, String parentSuiteName) {
    this.testConfig = testConfig;
    this.tupleList.addAll(listOfTuples);
    for (Tuple tuple : this.tupleList) {
      for (Client client : tuple.getClients()) {
        BrowserSpecs specs = client.getBrowserSpecs();
        if (specs.getBrowserName().equals("firefox")) {
          if (specs.getProfile() != null && !specs.getProfile().isEmpty()) {
            specs.setProfile(testConfig.getFirefoxProfile());
          }
        }
        if (specs.getBrowserName().equals("chrome") && specs.getVersion() != null && !specs.getVersion().contains("electron")) {
          if (specs.getExtension() != null && !specs.getExtension().isEmpty()) {
            specs.setExtension(testConfig.getChromeExtension());
          }
        }
      }
    }
    if (testConfig.isLoadTest()) {
      this.testSuite = new Container("1st phase(RU) | " + getHostUrlWithTs(this.tupleList.get(0).getClients().get(0)));
    } else {
      this.testSuite = new Container("" + testConfig.getNameWithTS());
    }
    this.testSuite.setParentSuite(parentSuiteName);
    this.testSuite.setReporter(testConfig.getReporter());
  }

  public MatrixRunner(List<TestManager> testManagers, String parentSuiteName) {
    this.testConfig = testManagers.get(0).getTestConfig();
    this.testManagerList = testManagers;
    waitAround(new Random().nextInt(1000)); // avoid duplicated container
    this.testSuite = new Container("2nd phase(LR) | " + getHostUrl(this.testManagerList.get(0).getTuple().getClients().get(0)) + testConfig.getReporter().getTimestamp());
    logger.info("Created container " + this.testSuite.getName());
    this.testSuite.setParentSuite(parentSuiteName);
    this.testSuite.setReporter(testConfig.getReporter());
    for (TestManager manager : testManagers) {
      manager.setSuite(this.testSuite);
    }
  }

  private String getHostUrl(Client client) {
    String hubUrl = client.getPaas().getUrl();
    try {
      String temp = new URL(hubUrl).getHost();
      hubUrl = temp;
    } catch (Exception e) {
      // ignore
    }
    return hubUrl;
  }

  private String getHostUrlWithTs(Client client) {
    return getHostUrl(client) + " (" + timestamp() + ")";
  }

  /**
   * Interrupt.
   */
  public void interrupt() {
    this.interrupted = true;
    for (TestManager manager : this.testManagerList) {
      manager.terminate();
    }
    this.shutdownExecutors();
  }

  /**
   * Executes the test contained inside the TestManager for the provided matrix.
   *
   * @return List<Future < Object>>
   */
  public List<Future<Object>> run() {
    List<Future<Object>> futureList = new ArrayList<>();
    this.multiExecutorService = Executors.newFixedThreadPool(this.testConfig.getNoOfThreads());

    if (this.testManagerList.isEmpty()) {
      int totalTestCases = this.tupleList.size();
      if (totalTestCases < 1) {
        return null;
      }
      logger.info("Executing " + this.testConfig + " for " + totalTestCases
          + " browser tuples with size :" + tupleList.get(0).size());
      testConfig.setLogger(createTestLogger(testConfig.getKiteRequestId(), testConfig.getTestClassName()));
      if (testConfig.isLoadTest()) {
        Tuple neo = new Tuple();
        for (Tuple tuple: this.tupleList) {
          neo.mergeWith(tuple);
        }
        this.tupleList.clear();
        this.tupleList.add(neo);
      }

      for (int index = 0; index < this.tupleList.size(); index++) {
        TestManager manager = new TestManager(this.testConfig, this.tupleList.get(index));
        manager.setSuite(this.testSuite);
        if (this.testConfig.isLoadTest()) {
          manager.setId((currentIteration + index * this.tupleList.get(index).size() + 1));
        } else {
          manager.setId(index);
        }
        manager.setTotal(this.tupleList.size());
        manager.setDelay(index*ONE_SECOND_INTERVAL);
        this.testManagerList.add(manager);
      }
    }

    try {
      this.currentPhase = testManagerList.get(0).getCurrentPhase(); // if null -> firs time executing
      //Runtime.getRuntime().addShutdownHook(new Thread(() -> terminate()));
      futureList.addAll(multiExecutorService.invokeAll(testManagerList));
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    } finally {
      if (this.currentPhase.isLastPhase()) {
        if (this.currentPhase.equals(StepPhase.LOADREACHED)) {
          // todo: wait for last iteration
          if (this.isLastThread()) {
            logger.info("Last thread has finished!");
            this.testConfig.setDone(true);
          }
          logger.info("Waiting for all threads to finish");
          while (!this.testConfig.isDone()) {
            waitAround(FIVE_SECOND_INTERVAL);
          }
        } else {
          logger.info("Matrix runner at last phase, ending. ");
        }
        terminate();
      }
      this.shutdownExecutors();
    }

    return futureList;
  }

  private void terminate() {
    for (TestManager manager : this.testManagerList) {
      if (!manager.isFinished()) {
        manager.terminate();
      }
    }
    testSuite.setStopTimestamp();
    if (currentPhase == null || !currentPhase.equals(StepPhase.RAMPUP)) {
      if (testConfig.generateReport()) {
        testConfig.getReporter().generateReportFiles();
      }
    }
  }

  /**
   * Shutdown executors.
   */
  synchronized private void shutdownExecutors() {
    if (this.multiExecutorService != null && !this.multiExecutorService.isShutdown()) {
      if (testConfig.isLoadTest() && (this.currentPhase == null || this.currentPhase.equals(StepPhase.RAMPUP))) {
        // todo: maybe some action is needed at this point
      } else {
        for (TestManager manager : this.testManagerList) {
          manager.terminate();
        }
      }
      this.multiExecutorService.shutdownNow();
      this.multiExecutorService = null;
    }
    logger.info("shutdownExecutors() done.");
  }


  /**
   * Create a common test logger for all test cases of a given test
   *
   * @return the logger for tests
   */
  private KiteLogger createTestLogger(String kiteRequestId, String testName) {
    KiteLogger testLogger = KiteLogger.getLogger(new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
    String logFileName = ((kiteRequestId == null || kiteRequestId.equals("null")) ? 
      "" : (kiteRequestId + "_")) + testName + "/test_" + testLogger.getName() + ".log";
    String logFilePath = "logs/" + logFileName;
    if (System.getProperty("catalina.base") != null) {
      logFilePath = System.getProperty("catalina.base") + "/" + logFilePath;
    }
    try {
      FileAppender fileAppender = new FileAppender(
        new PatternLayout("%d %-5p - %m%n"), logFilePath, false);
      fileAppender.setThreshold(Level.INFO);
      testLogger.addAppender(fileAppender);
    } catch (IOException e) {
      logger.error(getStackTrace(e));
    }
    return testLogger;
  }

  public void setCurrentIteration(int currentIteration) {
    this.currentIteration = currentIteration;
  }


  public void setLastThread(boolean lastThread) {
    this.lastThread = lastThread;
  }

  public boolean isLastThread() {
    return lastThread;
  }
}

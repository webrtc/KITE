/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.webrtc.kite;

import io.cosmosoftware.kite.report.KiteLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;

/**
 * The Class TestRunThread.
 */
public class TestRunThread implements Callable<List<Future<Object>>> {

  /** The logger. */
  private final KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());

  /** The test config. */
  private final TestConfig testConfig;

  /** The tuple list. */
  private final List<Tuple> tupleList;

  /** The name. */
  private String name;

  private int currentIteration = 0;

  /** The matrix runner. */
  private MatrixRunner matrixRunner;

  /**
   * Instantiates a new test run thread.
   *
   * @param testConfig the test config
   * @param tupleList the tuple list
   */
  public TestRunThread(TestConfig testConfig, List<Tuple> tupleList) {
    this.testConfig = testConfig;
    this.tupleList = tupleList;
  }

  public TestRunThread (String name, List<TestManager> testManagers) {
    this.testConfig = testManagers.get(0).getTestConfig();
    this.tupleList = null;
    this.name = name;
    this.matrixRunner = new MatrixRunner(testManagers, this.name);
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public List<Future<Object>> call() {
    List<Future<Object>> listOfResults = null;
    try {
      if (this.matrixRunner == null) {
        logger.info("Running " + testConfig + " ...");
        this.matrixRunner = new MatrixRunner(testConfig, tupleList, this.name);
        this.matrixRunner.setCurrentIteration(this.currentIteration);
      } else {
        logger.info("Running LOADREACHED phase for " + testConfig);
      }
      listOfResults = this.matrixRunner.run();
    } catch (Exception e) {
      logger.fatal("Error [Interruption]: The execution has been interrupted with the "
          + "following error: " + e.getLocalizedMessage(), e);
    }
    StringBuilder testResults =
        new StringBuilder("The following are results for " + testConfig + ":\n");

    if (listOfResults != null) {
      List<Future<Object>> temp = new ArrayList<>(listOfResults);
      for (Future<Object> future : temp) {
        try {
          if (future.get() instanceof  TestManager) {
            logger.info("End of first phase (RAMUP), waiting for next phase");
          } else {
            testResults.append("\r\n").append(future.get().toString());
          }
        } catch (Exception e) {
          logger.error("Exception during test execution:\n" + getStackTrace(e));
        }
      }
      testResults.append("\r\nEND OF RESULTS\r\n");
      logger.debug(testResults.toString());
    } else {
      logger.warn("No test case was found.");
    }
    return listOfResults;
  }

  /**
   * Interrupt.
   */
  public void interrupt() {
    this.matrixRunner.interrupt();
  }

  public void setCurrentIteration(int currentIteration) {
    this.currentIteration = currentIteration;
  }
}

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

import io.cosmosoftware.kite.manager.RoomManager;
import io.cosmosoftware.kite.report.Container;
import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.report.Reporter;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;

/**
 * A class to manage the asynchronous execution of TestManager objects.
 */
public class MatrixRunner {
  
  private final KiteLogger logger = KiteLogger.getLogger(MatrixRunner.class.getName());
  private final TestConfig testConfig;
  private boolean interrupted;
  private ExecutorService multiExecutorService;
  private List<Tuple> tupleList = new ArrayList<>();
  
  /**
   * Constructs a new MatrixRunner with the given TestConfig and List<Tuple>.
   *
   * @param testConfig   TestConfig
   * @param listOfTuples a list of tuples (containing 1 or multiples kite config objects).
   */
  public MatrixRunner(TestConfig testConfig, List<Tuple> listOfTuples) {
    this.testConfig = testConfig;
    this.tupleList.addAll(listOfTuples);
  }
  
  /**
   * Returns a sublist of the given futureList exclusive of the type of objects specified by the
   * objectClass.
   *
   * @param futureList  List of Future<Object>
   * @param objectClass The class for the undesired required object.
   *
   * @return A sublist of the given futureList exclusive of the type of objects specified by the
   * objectClass.
   */
  private List<Future<Object>> getExclusiveSubList(List<Future<Object>> futureList,
                                                   Class<?> objectClass) {
    List<Future<Object>> listOfFutureObjects = new ArrayList<Future<Object>>();
    for (Future<Object> future : futureList) {
      try {
        Object object = future.get();
        if (!objectClass.isInstance(object)) {
          listOfFutureObjects.add(future);
        }
      } catch (InterruptedException | ExecutionException e) {
        logger.error(getStackTrace(e));
      }
    }
    return listOfFutureObjects;
  }
  
  /**
   * Returns a sublist from the given list of the type of objects specified by the objectClass.
   *
   * @param futureList  List of Future<Object>
   * @param objectClass The class for the desired required object list.
   *
   * @return A sublist from the given list of the type of objects specified by the objectClass.
   */
  private List<?> getSubList(List<Future<Object>> futureList, Class<?> objectClass) {
    List<Object> listOfObject = new ArrayList<Object>();
    for (Future<Object> future : futureList) {
      try {
        Object object = future.get();
        if (objectClass.isInstance(object)) {
          listOfObject.add(object);
        }
      } catch (InterruptedException | ExecutionException e) {
        logger.error(getStackTrace(e));
      }
    }
    return listOfObject;
  }
  
  /**
   * Interrupt.
   */
  public void interrupt() {
    this.interrupted = true;
    this.shutdownExecutors();
  }
  
  /**
   * Executes the test contained inside the TestManager for the provided matrix.
   *
   * @return List<Future < Object>>
   */
  public List<Future<Object>> run() {
    Container testSuite = new Container(testConfig.getName());
    
    int totalTestCases = this.tupleList.size();
    if (totalTestCases < 1) {
      return null;
    }
    
    List<TestManager> testManagerList = new ArrayList<>();
    List<Future<Object>> futureList = new ArrayList<>();
    RoomManager.init();
    this.multiExecutorService =
      Executors.newFixedThreadPool(this.testConfig.getNoOfThreads());
    
    logger.info("Executing " + this.testConfig + " for " + totalTestCases + " browser tuples with size :" + tupleList.get(0).size());
    try {
      for (int index = 0; index < this.tupleList.size(); index++) {
        TestManager manager = new TestManager(this.testConfig, this.tupleList.get(index));
        manager.setTestSuite(testSuite);
        if (testConfig.isLoadTest()) {
          manager.setId("iteration " + (index + 1));
        }
        testManagerList.add(manager);
      }
      
      List<Future<Object>> tempFutureList;
      while (testManagerList.size() > 0) {
        tempFutureList = multiExecutorService.invokeAll(testManagerList);
        testManagerList = (List<TestManager>) this.getSubList(tempFutureList, TestManager.class);
        futureList.addAll(this.getExclusiveSubList(tempFutureList, TestManager.class));
      }
      
      testManagerList.clear();
      
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    } finally {
      testSuite.setStopTimestamp();
      Reporter.getInstance().generateReportFiles();
      this.shutdownExecutors();
    }
    
    return futureList;
  }
  
  /**
   * Shutdown executors.
   */
  synchronized private void shutdownExecutors() {
    if (this.multiExecutorService != null && !this.multiExecutorService.isShutdown()) {
      this.multiExecutorService.shutdownNow();
      this.multiExecutorService = null;
    }
  }
  
}

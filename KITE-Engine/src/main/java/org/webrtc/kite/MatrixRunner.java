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
import io.cosmosoftware.kite.report.Reporter;
import org.apache.log4j.Logger;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.config.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static io.cosmosoftware.kite.util.ReportUtils.timestamp;

/**
 * A class to manage the asynchronous execution of TestManager objects.
 */
public class MatrixRunner {
  
  private final Logger logger = Logger.getLogger(MatrixRunner.class.getName());
  private final TestConf testConf;
  private List<Tuple> tupleList = new ArrayList<>();
  
  /**
   * Constructs a new MatrixRunner with the given TestConf and List<Tuple>.
   *
   * @param testConf     TestConf
   * @param listOfTuples a list of tuples (containing 1 or multiples kite config objects).
   */
  public MatrixRunner(TestConf testConf, List<Tuple> listOfTuples) {
    this.testConf = testConf;
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
   * Executes the test contained inside the TestManager for the provided matrix.
   *
   * @return List<Future < Object>>
   */
  public List<Future<Object>> run() {
    Container testSuite = new Container(testConf.getName());
    
    int totalTestCases = this.tupleList.size();
    if (totalTestCases < 1) {
      return null;
    }
    
    List<TestManager> testManagerList = new ArrayList<>();
    List<Future<Object>> futureList = new ArrayList<>();
    
    ExecutorService multiExecutorService =
      Executors.newFixedThreadPool(this.testConf.getNoOfThreads());
    
    logger.info("Executing " + this.testConf + " for " + totalTestCases + " browser tuples with size :" + tupleList.get(0).size());
    try {
      for (int index = 0; index < this.tupleList.size(); index++) {
        TestManager manager = new TestManager(this.testConf, this.tupleList.get(index));
        manager.setTestSuite(testSuite);
        if (testConf.isLoadTest()) {
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
      multiExecutorService.shutdown();
    }
    return futureList;
  }
  
  
}

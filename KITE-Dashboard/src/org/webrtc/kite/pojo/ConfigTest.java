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

package org.webrtc.kite.pojo;

import java.util.List;

/**
 * ConfigExecution object containing the information of an executed test.
 */
public class ConfigTest {

  private int testId;
  private String testName;
  private long startTime;
  private long endTime;
  private String impl;
  private int tupleSize;
  private String resultTable;
  private int configId;
  private int totalTests;
  private int doneTests;
  private List<Integer> stats;
  private boolean status;
  private String description;

  /**
   * Returns test's start time.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Sets test's start time.
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  /**
   * Returns test's finish time.
   */
  public long getEndTime() {
    return endTime;
  }

  /**
   * Sets test's finish time.
   */
  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  /**
   * Returns test's id.
   */
  public int getTestId() {
    return testId;
  }

  /**
   * Sets test's id.
   */
  public void setTestId(int testId) {
    this.testId = testId;
  }

  /**
   * Returns test's name.
   */
  public String getTestName() {
    return testName;
  }

  /**
   * Sets test's name.
   */
  public void setTestName(String testName) {
    this.testName = testName;
  }

  /**
   * Returns test's implementation.
   */
  public String getImpl() {
    return impl;
  }

  /**
   * Sets test's implementation.
   */
  public void setImpl(String impl) {
    this.impl = impl;
  }

  /**
   * Returns test's number of involved browsers.
   */
  public int getTupleSize() {
    return tupleSize;
  }

  /**
   * Sets test's number of involved browsers.
   */
  public void setTupleSize(int tupleSize) {
    this.tupleSize = tupleSize;
  }

  /**
   * Returns the name of test's resulttable.
   */
  public String getResultTable() {
    return resultTable;
  }

  /**
   * Sets the name of test's resulttable.
   */
  public void setResultTable(String resultTable) {
    this.resultTable = resultTable;
  }

  /**
   * Returns the id of the configuration that contain the test.
   */
  public int getConfigId() {
    return configId;
  }

  /**
   * Sets the id of the configuration that contain the test.
   */
  public void setConfigId(int configId) {
    this.configId = configId;
  }

  /**
   * Returns test's number of test cases.
   */
  public int getTotalTests() {
    return totalTests;
  }

  /**
   * Sets test's number of test cases.
   */
  public void setTotalTests(int totalTests) {
    this.totalTests = totalTests;
  }

  /**
   * Returns test's status.
   */
  public boolean getStatus() {
    return this.status;
  }

  /**
   * Sets test's status.
   */
  public void setStatus(boolean status) {
    this.status = status;
  }

  /**
   * Returns test's number of finished test cases.
   */
  public int getDoneTests() {
    return doneTests;
  }

  /**
   * Sets test's number of finished test cases.
   */
  public void setDoneTests(int doneTests) {
    this.doneTests = doneTests;
  }

  public List<Integer> getStats() {
    return stats;
  }

  public void setStats(List<Integer> stats) {
    this.stats = stats;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the Json String containing all properties of this
   */
  public String getJsonData() {
    String res = "{";
    res += "\"name\": \"" + testName + "\",";
    res += "\"id\": " + testId + ",";
    res += "\"tupleSize\": " + tupleSize + ",";
    res += "\"configId\": " + configId + ",";
    res += "\"total\": " + totalTests + ",";
    res += "\"done\": " + doneTests + ",";
    res += "\"start\": " + startTime + ",";
    res += "\"status\": " + status + ",";
    res += "\"stats\": [" + stats.get(0) + "," + stats.get(1) + "," + stats.get(2) + "," + stats.get(3) + "]}";
    return res;
  }
}

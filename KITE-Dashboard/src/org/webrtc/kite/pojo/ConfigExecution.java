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
 * ConfigExecution object containing the information of an executed configuration.
 */
public class ConfigExecution {

  List<ConfigTest> testList;
  private int configId;
  private String configName;
  private long startTime;
  private long endTime;
  private int testCount;
  private Boolean isDone;

  /**
   * Constructs a new ConfigExecution object from given information.
   *
   * @param configId   id of the configuration.
   * @param configName name of the configuration.
   * @param startTime  time stamp at the beginning of the run.
   * @param testCount  number of test executed in the configuration.
   */
  public ConfigExecution(int configId, String configName, long startTime, int testCount) {
    super();
    this.configId = configId;
    this.configName = configName;
    this.startTime = startTime;
    this.endTime = 0;
    this.testCount = testCount;
    this.isDone = false;
  }


  public int getConfigId() {
    return configId;
  }


  public void setConfigId(int configId) {
    this.configId = configId;
  }


  public String getConfigName() {
    return configName;
  }


  public void setConfigName(String configName) {
    this.configName = configName;
  }


  public long getStartTime() {
    return startTime;
  }


  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }


  public void setDone(Boolean done) {
    isDone = done;
  }


  public int getTestCount() {
    return testCount;
  }

  public void setTestCount(int testCount) {
    this.testCount = testCount;
  }

  public Boolean getIsDone() {
    return isDone;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public List<ConfigTest> getTestList() {
    return testList;
  }

  public void setTestList(List<ConfigTest> testList) {
    this.testList = testList;
  }
}

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
 * ConfigExecution object containing the overall information of an executed configuration.
 */
public class ConfigurationOverall {

  private String configName;
  private List<Integer> idList;
  private List<Long> startTimeList;
  private int numberOfRuns;
  private int numberOfTested;
  private int numberOfSuccess;
  private int numberOfFailed;
  private int numberOfError;
  private int numberOfPending;
  private long oldestRun;
  private long latestRun;

  /**
   * Constructs a new ConfigurationOverall object from given information.
   *
   * @param configName name of the configuration.
   */
  public ConfigurationOverall(String configName, long oldestRun, long latestRun) {
    this.configName = configName;
    this.numberOfRuns = 0;
    this.numberOfTested = 0;
    this.numberOfSuccess = 0;
    this.numberOfFailed = 0;
    this.numberOfError = 0;
    this.numberOfPending = 0;
    this.oldestRun = oldestRun;
    this.latestRun = latestRun;

  }

  public void setIdList(List<Integer> idList) {
    this.idList = idList;
  }

  public int getNumberOfError() {
    return numberOfError;
  }

  public void setNumberOfError(int numberOfError) {
    this.numberOfError = numberOfError;
  }

  public int getNumberOfFailed() {
    return numberOfFailed;
  }

  public void setNumberOfFailed(int numberOfFailed) {
    this.numberOfFailed = numberOfFailed;
  }

  public int getNumberOfPending() {
    return numberOfPending;
  }

  public void setNumberOfPending(int numberOfPending) {
    this.numberOfPending = numberOfPending;
  }

  public int getNumberOfRuns() {
    return numberOfRuns;
  }

  public void setNumberOfRuns(int numberOfRuns) {
    this.numberOfRuns = numberOfRuns;
  }

  public int getNumberOfSuccess() {
    return numberOfSuccess;
  }

  public void setNumberOfSuccess(int numberOfSuccess) {
    this.numberOfSuccess = numberOfSuccess;
  }

  public int getNumberOfTested() {
    return numberOfTested;
  }

  public void setNumberOfTested(int numberOfTested) {
    this.numberOfTested = numberOfTested;
  }

  public long getLatestRun() {
    return latestRun;
  }

  public void setLatestRun(long latestRun) {
    this.latestRun = latestRun;
  }

  public long getOldestRun() {
    return oldestRun;
  }

  public void setOldestRun(long oldestRun) {
    this.oldestRun = oldestRun;
  }

  public String getConfigName() {
    return configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

  public int getLatestId() {
    return idList.get(0);
  }

  public int getId(long startTime) {
    return idList.get(startTimeList.indexOf(startTime));
  }

  public List<Long> getStartTimeList() {
    return startTimeList;
  }

  public void setStartTimeList(List<Long> startTimeList) {
    this.startTimeList = startTimeList;
  }
}

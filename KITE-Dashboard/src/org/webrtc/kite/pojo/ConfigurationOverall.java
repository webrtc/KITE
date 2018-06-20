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

/** Execution object containing the overall information of an executed configuration. */
public class ConfigurationOverall {

  private String configName;
  private List<Integer> idList;
  private int numberOfRuns;
  private long oldestRun;
  private long latestRun;

  /**
   * Constructs a new ConfigurationOverall object from given information.
   *
   * @param configName name of the configuration.
   */
  public ConfigurationOverall(String configName) {
    this.configName = configName;
    this.numberOfRuns = 0;
  }

  public void setIdList(List<Integer> idList) {
    this.idList = idList;
  }

  public int getNumberOfRuns() {
    return numberOfRuns;
  }

  public void setNumberOfRuns(int numberOfRuns) {
    this.numberOfRuns = numberOfRuns;
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

}

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

import java.util.ArrayList;
import java.util.List;

/**
 * ConfigExecution object containing the information of a result of a test case.
 */
public class ResultTable {

  private String tableName;
  private String result;
  private long duration;
  private boolean stats;
  private long startTime;
  private List<Browser> browserList = new ArrayList<>();

  /**
   * Constructs a new ResultTable object from given information.
   *
   * @param result   a string representing actual result of the test case.
   * @param duration duration of the test case.
   */
  public ResultTable(String result, long duration, boolean stats) {
    super();
    this.result = result;
    this.duration = duration;
    this.stats = stats;
  }

  /**
   * Returns true if the browserList contains a specific browser
   */
  public boolean hasBrowser(Browser aBrowser) {
    for (Browser browser : browserList) {
      if (browser.equals(aBrowser))
        return true;
    }
    return false;
  }

  /**
   * Returns result string.
   */
  public String getResult() {
    return result;
  }

  /**
   * Sets result string.
   */
  public void setResult(String result) {
    this.result = result;
  }

  /**
   * Returns test case's duration.
   */
  public long getDuration() {
    return duration;
  }

  /**
   * Sets test case's duration.
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  /**
   * Add a browser to the list of participating browser of the test case.
   */
  public void addBrowser(Browser browser) {
    this.browserList.add(browser);
  }

  /**
   * Returns test case's list of participating browser.
   */
  public List<Browser> getBrowserList() {
    return browserList;
  }

  /**
   * Sets test case's result table's name.
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Returns test case's start time.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Sets test case's start time.
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public boolean getStats() {
    return stats;
  }
}

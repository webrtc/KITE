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

package org.webrtc.kite.wpt.dashboard.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Score object containing the information as test name, map of browser list and simplified results.
 */
public class WPTScore {

  private Browser browser;
  private String tableName;
  private String webRTCReadyTableName;
  private long timeStamp;
  private List<WPTTest> testList;
  private List<WebRTCGroupScore> testGroupList;


  /**
   * Constructs a new Score from given information as test name, browser list and simplified results.
   */
  public WPTScore(Browser browser, String tableName, long timeStamp) {
    this.browser = browser;
    this.tableName = tableName;
    this.timeStamp = timeStamp;
    this.testList = new ArrayList<>();
    this.testGroupList = new ArrayList<>();
  }


  public String getTableName() {
    return tableName;
  }

  public void setTestList(List<WPTTest> testList) {
    this.testList = testList;
  }

  public String getJson(int layer) {
    String res = "";
    res += "\"" + this.browser.getName() + "_" + this.browser.getVersion() + "_" + this.browser.getPlatform() + "\":{";
    res += "\"time_stamp\":" + this.timeStamp + ",";
    res += "\"results\":{";
    for (int i = 0; i < this.testList.size(); i++) {
      res += this.testList.get(i).getJson(layer);
      if (i < this.testList.size() - 1)
        res += ",";
    }
    res += "}}";
    return res;
  }

  public String getGroupJson() {
    String res = "";
    res += "\"" + this.browser.getName() + "_" + this.browser.getVersion() + "_" + this.browser.getPlatform() + "\":{";
    res += "\"time_stamp\":" + this.timeStamp + ",";
    res += "\"results\":{";
    for (int i = 0; i < this.testGroupList.size(); i++) {
      res += this.testGroupList.get(i).getJson();
      if (i < this.testGroupList.size() - 1)
        res += ",";
    }
    res += "}}";
    return res;
  }

  public String getWebRTCReadyTableName() {
    return webRTCReadyTableName;
  }

  public void setWebRTCReadyTableName(String webRTCReadyTableName) {
    this.webRTCReadyTableName = webRTCReadyTableName;
  }

  public List<WebRTCGroupScore> getTestGroupList() {
    return testGroupList;
  }

  public void setTestGroupList(List<WebRTCGroupScore> testGroupList) {
    this.testGroupList = testGroupList;
  }

  public void addTestGroup(WebRTCGroupScore groupScore) {
    this.getTestGroupList().add(groupScore);
  }

}

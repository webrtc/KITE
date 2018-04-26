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

/**
 * Score object containing the information as test name, map of browser list and simplified results.
 */
public class WPTTest {

  private String name;
  private String group;
  private int total, pass;
  private String result;
  private long lastUpdate;


  /**
   * Constructs a new Score from given information as test name, browser list and simplified results.
   */
  public WPTTest(String name, int total, int pass, long lastUpdate, String result) {
    this.name = name;
    this.total = total;
    this.pass = pass;
    this.result = result;
    this.lastUpdate = lastUpdate;
    this.group = "N/A";
  }

  public String getJson(int layer) {
    String res = "\"" + this.name + "\":{";
    res += "\"total\":" + this.total + ",";
    res += "\"passed\":" + this.pass + ",";
    if (!this.group.equalsIgnoreCase("N/A"))
      res += "\"group\":\"" + this.group + "\",";
    if (layer > 1)
      res += "\"result\":" + this.result + "}";
    else
      res += "\"result\":{}}";
    //res += "\"result\":\"placebo\"}";
    return res;
  }


  public void setGroup(String group) {
    this.group = group;
  }
}

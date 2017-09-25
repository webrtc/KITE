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
 * A class containing information on the date and frequency of the runs in database.
 */
public class TimeChart {
  private List<String> dateList;
  private List<Integer> dateFreqList;

  /**
   * Constructs a new TimeChart object from given information.
   *
   * @param dateList list of dates on which there were tests.
   * @param dateFreqList frequency of these dates' apparition in the database.
   */
  public TimeChart(List<String> dateList, List<Integer> dateFreqList) {
    this.dateList = dateList;
    this.dateFreqList = dateFreqList;
  }

  /**
   * Returns the list of dates.
   */
  public List<String> getDateList() {
    return dateList;
  }

  /**
   * Returns the list of dates' frequency.
   */
  public List<Integer> getDateFreqList() {
    return dateFreqList;
  }
}

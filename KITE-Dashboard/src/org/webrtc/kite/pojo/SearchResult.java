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
 * A class representing the result of a user's search.
 */
public class SearchResult {

  private List<ConfigExecution> listOfConfigResult;
  private List<ConfigTest> listOfConfigTestResult;
  private List<ResultTable> listOfResult;
  private String searchKey;

  public SearchResult() {
  }

  /**
   * Returns key word for the search.
   */
  public String getSearchKey() {
    return searchKey;
  }

  /**
   * Sets key word for the search.
   */
  public void setSearchKey(String searchKey) {
    this.searchKey = searchKey;
  }

  /**
   * Returns the list of found configurations.
   */
  public List<ConfigExecution> getListOfConfigResult() {
    return listOfConfigResult;
  }

  /**
   * Sets the list of found configurations.
   */
  public void setListOfConfigResult(List<ConfigExecution> listOfConfigResult) {
    this.listOfConfigResult = listOfConfigResult;
  }

  /**
   * Returns the list of found tests.
   */
  public List<ConfigTest> getListOfConfigTestResult() {
    return listOfConfigTestResult;
  }

  /**
   * Sets the list of found tests.
   */
  public void setListOfConfigTestResult(List<ConfigTest> listOfConfigTestResult) {
    this.listOfConfigTestResult = listOfConfigTestResult;
  }

  /**
   * Returns the list of found results.
   */
  public List<ResultTable> getListOfResult() {
    return listOfResult;
  }

  public void setListOfResult(List<ResultTable> listOfResult) {
    this.listOfResult = listOfResult;
  }
}

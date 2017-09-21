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

import java.text.SimpleDateFormat;
import java.util.*;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.ResultTable;
import org.webrtc.kite.pojo.TimeChart;

/**
 * A class containing the information for the display of the overview.
 */
public class OverviewResult {

  private List<ResultTable> listOfResultTable;
  private List<String> OSList;

  /**
   * Constructs a new OverviewResult object from given information.
   *
   * @param listOfResultTable list of test case's result in the OVERVIEW table in the database.
   * @param filtered determine whether the list of results should be filtered.
   */
  public OverviewResult(List<ResultTable> listOfResultTable, boolean filtered) {
    if (filtered)
      this.listOfResultTable = versionFilter(listOfResultTable);
    else
      this.listOfResultTable = listOfResultTable;
    OSList = Mapping.OVERVIEW;
    for (ResultTable result : listOfResultTable) {
      String platform = result.getBrowserList().get(0).getPlatform();
      if (!OSList.contains(platform)) {
        OSList.add(platform);
      }
    }
  }

  /**
   * Filters out the non-interesting results.
   *
   * @param listOfResultTable list of test case's result in the OVERVIEW table in the database.
   */
  private List<ResultTable> versionFilter(List<ResultTable> listOfResultTable) {
    List<ResultTable> res = new ArrayList<>();
    for (ResultTable result : listOfResultTable) {
      if (checkBrowserVersion(result.getBrowserList().get(0))
          && checkBrowserVersion(result.getBrowserList().get(1)))
        res.add(result);
    }
    return res;
  }

  /**
   * Verifies whether the version of a certain browser is relevant in the overview.
   *
   * @param browser browser object in question.
   */
  private boolean checkBrowserVersion(Browser browser) {
    String version = browser.getVersion();
    switch (browser.getName()) {
      case "MicrosoftEdge":
        return Mapping.EdgeVersionList.contains(version);
      case "chrome":
        return Mapping.ChromeVersionList.contains(version);
      case "firefox":
        return Mapping.FirefoxVersionList.contains(version);
      default:
        return false;
    }
  }

  /**
   * Designed for getting browserName and version for displaying purpose. Gets the list of browsers
   * at a designated position.
   *
   * @param index position of browser in a test case.
   */
  public List<List<String>> getBrowserListAtCertainPosition(int index) {
    List<List<String>> listOfFirstBrowser = new ArrayList<>();
    for (ResultTable result : listOfResultTable) {
      List<String> browser = Arrays.asList(result.getBrowserList().get(index - 1).getName(),
          result.getBrowserList().get(index - 1).getVersion());
      listOfFirstBrowser.add(browser);
    }
    listOfFirstBrowser = new ArrayList<>(new LinkedHashSet<List<String>>(listOfFirstBrowser));

    return listOfFirstBrowser;
  }

  /**
   * Returns the information of a test case with 2 browsers.
   *
   * @param browser1 first browser (caller).
   * @param browser2 second browser (callee).
   */
  public long getInfo(List<String> browser1, List<String> browser2) {
    List<List<String>> listOfBrowser = new ArrayList<>();
    listOfBrowser.add(browser1);
    listOfBrowser.add(browser2);
    for (ResultTable result : this.listOfResultTable)
      if (result.getBrowserList().equals(listOfBrowser))
        return result.getDuration();
    return 0;
  }

  /**
   * Returns the number of result to display.
   */
  public int getSize() {
    return this.listOfResultTable.size();
  }

  /**
   * Returns a map with browsers' OS pair as keys and result as value. These browsers are not
   * Browser Object, but list of strings representing name, version and platform.
   *
   * @param browser1 first browser.
   * @param browser2 second browser.
   */
  private HashMap<List<String>, ResultTable> getResultMapByOSPair(List<String> browser1,
      List<String> browser2) {
    HashMap<List<String>, ResultTable> res = new HashMap<>();
    for (ResultTable result : listOfResultTable) {
      List<Browser> browserList = result.getBrowserList();
      if (browserList.get(0).hasNameAndVersion(browser1)
          && browserList.get(1).hasNameAndVersion(browser2)) {
        res.put(Arrays.asList(browserList.get(0).getPlatform(), browserList.get(1).getPlatform()),
            result);
      }
    }
    return res;
  }

  /**
   * Returns a 2D matrix of result with OSes as axes, columns are callers, rows are callees. 'NA'
   * represents the untested cases. 'NP' represents the untestable cases.
   *
   * @param browser1 first browser.
   * @param browser2 second browser.
   */
  public List<List<ResultTable>> getResultTableListInOSGrid(List<String> browser1,
      List<String> browser2) {
    List<List<ResultTable>> res = new ArrayList<>();
    HashMap<List<String>, ResultTable> resultMap = getResultMapByOSPair(browser1, browser2);
    for (String OS1 : this.OSList) {
      List<ResultTable> row = new ArrayList<>();
      for (String OS2 : this.OSList) {
        List<String> OSPair = Arrays.asList(OS1, OS2);

        if (resultMap.containsKey(OSPair)) {
          row.add(resultMap.get(OSPair));
        } else {
          if (isTestable(browser1.get(0), OS1)) {
            if (isTestable(browser2.get(0), OS2))
              row.add(new ResultTable("NA", 0));
            else
              row.add(new ResultTable("NP", 0));
          } else
            row.add(new ResultTable("NP", 0));
        }
      }
      res.add(row);
    }
    return res;
  }

  /**
   * Verifies whether a browser is testable on a platform.
   *
   * @param browserName browser's name.
   * @param OS platform on which we intent to test this browser.
   * @return true if testable, false otherwise.
   */
  private boolean isTestable(String browserName, String OS) {
    switch (browserName) {
      case "MicrosoftEdge":
        return OS.equals("Windows 10");
      default:
        return true;
    }
  }

  /**
   * Returns a TimeChart object containing information to display the time chart in overview.
   */
  public TimeChart getTimeChartInfo() {
    List<String> fullDateList = new ArrayList<>();
    for (ResultTable result : listOfResultTable) {
      Date tmp = new Date(result.getStartTime());
      SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
      String formatted = df2.format(tmp);
      fullDateList.add(formatted);
    }
    Collections.sort(fullDateList);
    List<String> distinctDateList = new ArrayList<String>(new HashSet<String>(fullDateList));
    List<Integer> freqList = new ArrayList<>();
    for (String aDate : distinctDateList) {
      freqList.add(Collections.frequency(fullDateList, aDate));
    }
    return new TimeChart(distinctDateList, freqList);
  }

  /**
   * Return a TimeChart object containing the information to display a time chart in a grid view.
   *
   * @param browser1 first browser.
   * @param browser2 second browser.
   */
  public TimeChart getGridTimeChartInfo(List<String> browser1, List<String> browser2) {
    List<List<ResultTable>> gridResultList = getResultTableListInOSGrid(browser1, browser2);
    List<String> fullDateList = new ArrayList<>();
    for (List<ResultTable> listOfResult : gridResultList) {
      for (ResultTable result : listOfResult) {
        Date tmp = new Date(result.getStartTime());
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        String formatted = df2.format(tmp);
        fullDateList.add(formatted);
      }
    }
    Collections.sort(fullDateList);
    List<String> distinctDateList = new ArrayList<String>(new HashSet<String>(fullDateList));
    List<Integer> freqList = new ArrayList<>();
    for (String aDate : distinctDateList) {
      freqList.add(Collections.frequency(fullDateList, aDate));
    }
    return new TimeChart(distinctDateList, freqList);
  }

  /**
   * Returns the list of test cases' result.
   */
  public List<ResultTable> getListOfResultTable() {
    return listOfResultTable;
  }

  public List<String> getOSList() {
    return OSList;
  }
}

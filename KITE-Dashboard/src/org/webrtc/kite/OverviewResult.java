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

import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.ResultTable;

import java.util.ArrayList;
import java.util.List;

/**
 * A class containing the information for the display of the overview.
 */
public class OverviewResult {

  private List<ResultTable> listOfResultTable;
  private int tupleSize;

  /**
   * Constructs a new OverviewResult object from given information.
   *
   * @param listOfResultTable list of test case's result in the OVERVIEW table in the database.
   */
  public OverviewResult(List<ResultTable> listOfResultTable) {
    this.listOfResultTable = listOfResultTable;
  }

  /**
   * Constructs a new OverviewResult object from given information.
   *
   * @param listOfResultTable     list of test case's result in the OVERVIEW table in the database.
   * @param listOfOverviewBrowser filter out the relevant result.
   */
  public OverviewResult(List<ResultTable> listOfResultTable, List<Browser> listOfOverviewBrowser) {
    this.listOfResultTable = versionFilter(listOfResultTable, listOfOverviewBrowser);
    if (!this.listOfResultTable.isEmpty())
      this.tupleSize = this.listOfResultTable.get(0).getBrowserList().size();
  }

  /**
   * Filters out the non-interesting results.
   *
   * @param listOfResultTable list of test case's result in the OVERVIEW table in the database.
   */
  private List<ResultTable> versionFilter(List<ResultTable> listOfResultTable, List<Browser> listOfOverviewBrowsers) {
    List<ResultTable> res = new ArrayList<>();
    for (ResultTable result : listOfResultTable) {
      boolean add = true;
      for (Browser browser : result.getBrowserList())
        //if (!browser.shouldBeInOverView())
        if (!listOfOverviewBrowsers.contains(browser))
          add = false;
      if (add)
        res.add(result);
    }
    return res;
  }

  /**
   * Filters out the non-interesting results.
   *
   * @param filter the browser we want to filter with.
   */
  public void browserFilter(Browser filter) {
    List<ResultTable> res = new ArrayList<>();
    for (ResultTable result : listOfResultTable) {
      boolean add = false;
      for (Browser browser : result.getBrowserList())
        if (browser.equals(filter))
          add = true;
      if (add)
        res.add(result);
    }
    this.setListOfResultTable(res);
  }

  /**
   * Filters out the non-stable results.
   */
  public void stableFilter() {
    List<ResultTable> tmp = new ArrayList<>();
    for (ResultTable result : this.getListOfResultTable()) {
      boolean ok = true;
      List<Boolean> checkList = new ArrayList<>();
      for (Browser browser : result.getBrowserList()) {
        boolean stable = false;
        for (String version : Mapping.StableList) {
          if (browser.getVersion().startsWith(version))
            stable = true;
        }
        checkList.add(stable);
      }
      if (!checkList.contains(false))
        tmp.add(result);
    }
    setListOfResultTable(tmp);
  }

  /**
   * Returns the number of result to display.
   */
  public int getSize() {
    return this.listOfResultTable.size();
  }

  /**
   * Returns the tupleSize.
   */
  public int getTupleSize() {
    return this.tupleSize;
  }

  /**
   * Returns the list of test cases' result.
   */
  public List<ResultTable> getListOfResultTable() {
    return listOfResultTable;
  }

  public void setListOfResultTable(List<ResultTable> listOfResultTable) {
    this.listOfResultTable = listOfResultTable;
  }

  /**
   * Returns the list of browsers.
   */
  public List<Browser> getDistincBrowserList() {
    List<Browser> res = new ArrayList<Browser>();
    for (ResultTable result : this.getListOfResultTable()) {
      for (Browser browser : result.getBrowserList()) {
        if (!res.contains(browser))
          res.add(browser);
      }
    }
    return res;
  }

  /**
   * Resort the result list by browser for better sunburst (1st level only).
   */
  private void sort() {
    List<ResultTable> temp = new ArrayList<>(this.listOfResultTable);
    List<ResultTable> res = new ArrayList<ResultTable>();
    Browser anchor = null;
    while (this.listOfResultTable.size() > 0) {
      for (ResultTable resultTable : temp) {
        Browser firstBrowser = resultTable.getBrowserList().get(0);
        if (anchor == null) {
          anchor = firstBrowser;
          res.add(resultTable);
          this.listOfResultTable.remove(resultTable);
        } else {
          if (anchor.isEqualTo(firstBrowser)) {
            res.add(resultTable);
            this.listOfResultTable.remove(resultTable);
          }
        }
      }
      if (this.listOfResultTable.size() == 0)
        break;
      temp.clear();
      temp = new ArrayList<>(this.listOfResultTable);
      anchor = null;
    }
    this.listOfResultTable = res;
  }

  /**
   * Returns the String of this under Json form to create sunburst Chart.
   */
  public String getSunburstJsonData() {
    int tupleSize;
    String res = "";
    String result = ",\"results\": [";
    String sunburst = "{\"sunburst\": {";
    sunburst += "\"name\":\"result\",";
    sunburst += "\"children\": [{";
    int ok = 0, failed = 0, error = 0, pending = 0;
    if (this.listOfResultTable.size() > 0) {
      tupleSize = this.listOfResultTable.get(0).getBrowserList().size();
      List<Browser> anchorList = new ArrayList<>();
      int index = 0;
      for (int l = 0; l < this.listOfResultTable.size(); l++) {
        ResultTable resultTable = this.listOfResultTable.get(l);
        result += "{";
        result += "\"result\": \"" + resultTable.getResult().replaceAll("\n", "").replaceAll("\\\\", "") + "\",";
        result += "\"stats\":" + resultTable.getStats() + ",";
        result += "\"duration\": \"" + resultTable.getDuration() + "\",";
        //result += "\"stats\":"+resultTable.getStats()+",";
        result += "\"browsers\": [";
        List<Browser> bList = resultTable.getBrowserList();
        for (int j = 0; j < bList.size(); j++) {
          Browser browser = bList.get(j);
          result += "{";
          result += "\"id\":" + browser.getId() + ",";
          result += "\"name\":" + "\"" + browser.getName() + "\",";
          result += "\"version\":" + "\"" + browser.getVersion() + "\",";
          result += "\"platform\":" + "\"" + browser.getPlatform() + "\"";
          result += "}";
          if (j < bList.size() - 1)
            result += ",";
          else
            result += "]";
        }
        result += "}";
        if (l < this.listOfResultTable.size() - 1)
          result += ",";
      }
      sort();
      for (int l = 0; l < this.listOfResultTable.size(); l++) {
        ResultTable resultTable = this.listOfResultTable.get(l);
        List<Browser> bList = resultTable.getBrowserList();
        if (anchorList.isEmpty()) {
          for (Browser browser : bList) {
            anchorList.add(browser);
            sunburst += browser.toSunburstJson();
          }
          if (resultTable.getResult().equals("SUCCESSFUL")) {
            sunburst += "\"name\":\"OK\",";
            ok += 1;
          } else {
            if (resultTable.getResult().equals("FAILED") || resultTable.getResult().equals("TIME OUT")) {
              sunburst += "\"name\":\"FAILED\",";
              failed += 1;
            } else {
              if (resultTable.getResult().equals("SCHEDULED")) {
                sunburst += "\"name\":\"PENDING\",";
                pending += 1;
              } else {
                sunburst += "\"name\":\"ERROR\",";
                error += 1;
              }
            }
          }
          sunburst += "\"size\": 1}]}";
        } else {
          for (int i = 0; i < tupleSize; i++) {
            if (!anchorList.get(i).equals(bList.get(i))) {
              anchorList = bList;
              index = i;
              for (int j = tupleSize - 1; j > i; j--) {
                sunburst += "]}";
              }
              sunburst += ",{";
              for (int k = i; k < tupleSize; k++) {
                sunburst += bList.get(k).toSunburstJson();
              }
              if (resultTable.getResult().equals("SUCCESSFUL")) {
                sunburst += "\"name\":\"OK\",";
                ok += 1;
              } else {
                if (resultTable.getResult().equals("FAILED") || resultTable.getResult().equals("TIME OUT")) {
                  sunburst += "\"name\":\"FAILED\",";
                  failed += 1;
                } else {
                  if (resultTable.getResult().equals("SCHEDULED")) {
                    sunburst += "\"name\":\"PENDING\",";
                    pending += 1;
                  } else {
                    sunburst += "\"name\":\"ERROR\",";
                    error += 1;
                  }
                }
              }
              sunburst += "\"size\": 1}]}";
              break;
            }
          }

        }
      }
      for (int x = 0; x < tupleSize; x++) {
        sunburst += "]}";
      }
    } else {
      sunburst += "}]}";
    }
    result += "]";
    res += sunburst;
    res += result;
    res += ",\"overall\": [" + ok + "," + failed + "," + error + "," + pending + "]";
    res += ",\"total\":" + this.listOfResultTable.size() + "}";
    return res;
  }

  /**
   * Returns the list of stats concerning the result.
   */
  public List<Long> getStat() {
    List<Long> stats = new ArrayList<>();
    long total = 0, success = 0, failed = 0, error = 0, pending = 0;
    for (ResultTable resultTable : this.listOfResultTable) {
      total += 1;
      switch (resultTable.getResult()) {
        case "SUCCESSFUL":
          success += 1;
          break;
        case "FAILED":
          failed += 1;
          break;
        case "TIME OUT":
          failed += 1;
          break;
        case "SCHEDULED":
          pending += 1;
          break;
        default:
          error += 1;
      }
    }
    stats.add(total);
    stats.add(success);
    stats.add(failed);
    stats.add(error);
    stats.add(pending);
    if (total > 0) {
      stats.add(100 * success / total);
      stats.add(100 * failed / total);
      stats.add(100 * error / total);
      stats.add(100 * pending / total);
    } else {
      stats.add((long) 0);
      stats.add((long) 0);
      stats.add((long) 0);
      stats.add((long) 0);
    }
    return stats;
  }
}
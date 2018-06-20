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
import org.webrtc.kite.pojo.Result;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Created by hussainsajid on 3/23/17. */
public class Utility {

  /**
   * Closes the given jdbc resources if they are not null.
   *
   * @param s Statement
   * @param rs ResultSet
   */
  public static void closeDBResources(Statement s, ResultSet rs) {
    if (s == null && rs == null) {
      // Both are null, don't do anything.
    } else if (s == null && rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
      }
    } else if (s != null && rs == null) {
      try {
        s.close();
      } catch (SQLException e) {
      }
    } else {
      try {
        rs.close();
      } catch (SQLException e) {
      }
      try {
        s.close();
      } catch (SQLException e) {
      }
    }
  }

  /**
   * Gets the DBConnection object inside the ServletContext.
   *
   * @param servletContext ServletContext
   * @return Connection
   */
  public static Connection getDBConnection(ServletContext servletContext) {
    return (Connection) servletContext.getAttribute("DBConnection");
  }

  /**
   * Gets the CompDBConnection object inside the ServletContext.
   *
   * @param servletContext ServletContext
   * @return Connection
   */
  public static Connection getCompDBConnection(ServletContext servletContext) {
    return (Connection) servletContext.getAttribute("CompDBConnection");
  }

  /**
   * Replaces all the unwanted characters that might cause problems to the DB
   *
   * @param payload raw payload received from KITE
   * @return String with (all) special characters replaced
   */
  public static String escapeSpecialCharacter(String payload) {
    return payload
        .replaceAll("\\n", "")
        .replaceAll("\\\\", "")
        .replaceAll("\"", "")
        .replaceAll("\'", "");
  }

  /**
   * Checks whether both the given objects are null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are null.
   */
  public static boolean areBothNull(Object object1, Object object2) {
    return object1 == null && object2 == null;
  }

  /**
   * Checks whether both the given objects are not null.
   *
   * @param object1 Object
   * @param object2 Object
   * @return true if both the provided objects are not null.
   */
  public static boolean areBothNotNull(Object object1, Object object2) {
    return object1 != null && object2 != null;
  }

  /**
   * Creates a matrix of browser tuples.
   *
   * @param tupleSize tuple size
   * @return a matrix of browser tuples as List<List<Browser>>
   */
  public static List<List<Browser>> buildTuples(List<Browser> browserList, int tupleSize) {


    List<Browser> focusedList = new ArrayList<>();
    for (Browser browser: browserList){
      if (browser.isFocus()){
        focusedList.add(browser);
      }
    }

    List<List<Browser>> listOfBrowserList = new ArrayList<List<Browser>>();

    double totalTuples = Math.pow(browserList.size(), tupleSize);

    for (int i = 0; i < totalTuples; i++) listOfBrowserList.add(new ArrayList<Browser>());

    for (int i = 0; i < tupleSize; i++) {
      double marge = totalTuples / Math.pow(browserList.size(), i + 1);
      double rep = Math.pow(browserList.size(), i);
      for (int x = 0; x < rep; x++)
        for (int j = 0; j < browserList.size(); j++)
          for (int k = 0; k < marge; k++) {
            (listOfBrowserList.get((int) (x * totalTuples / rep + j * marge + k)))
                .add(i, browserList.get(j));
          }
    }
    if (tupleSize > 1) {
      for (Browser browser : browserList) {
        if (browser.getPlatform().equalsIgnoreCase("android")
            || browser.getPlatform().equalsIgnoreCase("ios")) {
          List<Browser> tmp = new ArrayList<>();
          for (int i = 0; i < tupleSize; i++) {
            tmp.add(browser);
          }
          listOfBrowserList.remove(tmp);
        }
      }
    }
    List<List<Browser>> tempListOfTuples = new ArrayList<>(listOfBrowserList);
    for (List<Browser> tuple: tempListOfTuples){
      if (Collections.disjoint(tuple,focusedList)){
        listOfBrowserList.remove(tuple);
      }
    }
    return listOfBrowserList;
  }

  /**
   * Creates a matrix of browser tuples.
   *
   * @return a matrix of browser tuples as List<List<Browser>>
   */
  public static List<List<Browser>> build2DTuples(List<Browser> browserList) {


    List<List<Browser>> listOfBrowserList = new ArrayList<List<Browser>>();

    double totalTuples = Math.pow(browserList.size(), 2);

    for (int i = 0; i < totalTuples; i++) listOfBrowserList.add(new ArrayList<Browser>());

    for (int i = 0; i < 2; i++) {
      double marge = totalTuples / Math.pow(browserList.size(), i + 1);
      double rep = Math.pow(browserList.size(), i);
      for (int x = 0; x < rep; x++)
        for (int j = 0; j < browserList.size(); j++)
          for (int k = 0; k < marge; k++) {
            (listOfBrowserList.get((int) (x * totalTuples / rep + j * marge + k)))
                .add(i, browserList.get(j));
          }
    }

    return listOfBrowserList;
  }

  /** Resort the result list by browser for better sunburst (1st level only). */
  private static List<Result> sortByBrowser(List<Result> listOfResult) {
    List<Result> temp = new ArrayList<>(listOfResult);
    List<Result> res = new ArrayList<Result>();
    Browser anchor = null;
    while (listOfResult.size() > 0) {
      for (Result result : temp) {
        Browser firstBrowser = result.getBrowserList().get(0);
        if (anchor == null) {
          anchor = firstBrowser;
          res.add(result);
          listOfResult.remove(result);
        } else {
          if (anchor.isEqualTo(firstBrowser)) {
            res.add(result);
            listOfResult.remove(result);
          }
        }
      }
      if (listOfResult.size() == 0) break;
      temp.clear();
      temp = new ArrayList<>(listOfResult);
      anchor = null;
    }
    return res;
  }

  public static List<Browser> sortByOs(List<Browser> browserList) {
    String os = null;
    List<Browser> res = new ArrayList<>();
    List<Browser> temp = new ArrayList<>(browserList);
    while (temp.size()>0) {
      for (Browser browser : temp) {
        if (os == null) {
          os = browser.getPlatform();
        }
        if (browser.getPlatform().equalsIgnoreCase(os)) {
          res.add(browser);
          browserList.remove(browser);
        }
      }
      temp = new ArrayList<>(browserList);
      os = null;
    }
    return res;
  }

  public static String getSunburstJsonData(List<Result> listOfResult){
    int tupleSize;
    String res = "";
    String sunburst = "{\"sunburst\": {";
    sunburst += "\"name\":\"result\",";
    sunburst += "\"children\": [{";
    if (listOfResult.size() > 0) {
      tupleSize = listOfResult.get(0).getBrowserList().size();
      List<Browser> anchorList = new ArrayList<>();
      listOfResult = sortByBrowser(listOfResult);
      for (int l = 0; l < listOfResult.size(); l++) {
        Result resultTable = listOfResult.get(l);
        List<Browser> bList = resultTable.getBrowserList();
        if (anchorList.isEmpty()) {
          for (Browser browser : bList) {
            anchorList.add(browser);
            sunburst += browser.toSunburstJson();
          }
          sunburst += "\"size\": 1}]}";
        } else {
          for (int i = 0; i < tupleSize; i++) {
            if (!anchorList.get(i).equals(bList.get(i))) {
              anchorList = bList;
              for (int j = tupleSize - 1; j > i; j--) {
                sunburst += "]}";
              }
              sunburst += ",{";
              for (int k = i; k < tupleSize; k++) {
                sunburst += bList.get(k).toSunburstJson();
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
    res += sunburst;
    res += ",\"total\":" + listOfResult.size() + "}";
    System.out.println(res);
    return res;
  }
}

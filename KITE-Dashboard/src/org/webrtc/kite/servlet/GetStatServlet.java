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

package org.webrtc.kite.servlet;

import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Stats.Stats;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/getstat")
public class GetStatServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetStatServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   * response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String overtime = request.getParameter("overtime");
    if (overtime.equalsIgnoreCase("no")) {
      String tableName = request.getParameter("name");
      String statArrayString = "";
      if (tableName == null)
        throw new KiteNoKeyException("table name");
      String idStr = request.getParameter("id");
      if (idStr == null)
        throw new KiteNoKeyException("id");
      List<String> idStrList = Arrays.asList(idStr.split("_"));
      List<Integer> idList = new ArrayList<Integer>();
      List<Stats> statsList = new ArrayList<>();
      String statJsonResponse = "{";
      for (String id : idStrList)
        idList.add(Integer.parseInt(id));
      try {
        statArrayString = new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getStatById(tableName, idList);
        if (statArrayString.equalsIgnoreCase("{\"stats\":\"NA\"}") || statArrayString.equalsIgnoreCase("{}")) {

        } else {
          JsonReader jsonReader = Json.createReader(new StringReader(statArrayString));
          JsonObject statArrayJson = jsonReader.readObject();
          jsonReader.close();


          Set<String> browserList = statArrayJson.keySet();
          for (String browser : browserList) {
            Stats browserStat = new Stats(browser, statArrayJson.getJsonObject(browser));
            statsList.add(browserStat);
          }

          for (Stats browser : statsList) {
            if (browser.isCaller())
              statJsonResponse += "\"caller\":" + browser.getJsonData() + ",";
            else
              statJsonResponse += "\"callee\":" + browser.getJsonData();
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new KiteSQLException(e.getLocalizedMessage());
      }
      statJsonResponse += "}";
      response.getWriter().print(statJsonResponse);
    } else {
      String testName = request.getParameter("test").trim();
      String caller = request.getParameter("caller").trim();
      String callee = request.getParameter("callee").trim();
      System.out.println("Requesting: " + testName + " for " + caller + " & " + callee);
      List<String> statStringList = new ArrayList<>();
      List<Stats> statsList = new ArrayList<>();
      int callerInt = -1, calleeInt = -1;
      String statJsonResponse = "{";
      String dateLabels = "\"run_dates\": [";
      String callerData = "\"caller\": [";
      String calleeData = "\"callee\": [";
      try {
        callerInt = getBrowserID(caller);
        calleeInt = getBrowserID(callee);
        List<Integer> idList = new ArrayList<>();
        idList.add(callerInt);
        idList.add(calleeInt);
        if (idList.contains(0) || idList.contains(-1)) {
          // throw some exception I guess
        } else {
          List<String> resultTableList = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getResultTableList(testName);
          for (String resultTable : resultTableList) {
            List<String> temp = new ArrayList<>(Arrays.asList(resultTable.split("_")));
            dateLabels += temp.get(temp.size() - 1) + ",";
            String stat = "";
            stat = new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getStatById(resultTable, idList);
            if (stat.equalsIgnoreCase("{}") || stat.equalsIgnoreCase("{\"stats\":\"NA\"}") || stat.equalsIgnoreCase(""))
              statStringList.add(null);
            else
              statStringList.add(stat);
          }
          for (String statStr : statStringList) {
            if (statStr != null) {
              JsonReader jsonReader = Json.createReader(new StringReader(statStr));
              JsonObject statArrayJson = jsonReader.readObject();
              jsonReader.close();

              Set<String> browserList = statArrayJson.keySet();
              for (String browser : browserList) {
                Stats browserStat = new Stats(browser, statArrayJson.getJsonObject(browser));
                statsList.add(browserStat);
              }

              for (Stats browser : statsList) {
                if (browser.isCaller()) {
                  callerData += browser.getJsonData();
                  callerData += ",";
                } else {
                  calleeData += browser.getJsonData();
                  calleeData += ",";
                }
              }
            } else {
              callerData += "{},";
              calleeData += "{},";
            }
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      if (callerData.substring(callerData.length() - 1).equalsIgnoreCase(","))
        callerData = callerData.substring(0, callerData.length() - 1);
      if (calleeData.substring(calleeData.length() - 1).equalsIgnoreCase(","))
        calleeData = calleeData.substring(0, calleeData.length() - 1);
      if (dateLabels.substring(dateLabels.length() - 1).equalsIgnoreCase(","))
        dateLabels = dateLabels.substring(0, dateLabels.length() - 1);
      dateLabels += "]";
      callerData += "]";
      calleeData += "]";
      statJsonResponse += dateLabels + "," + callerData + "," + calleeData;
      statJsonResponse += "}";
      response.getWriter().print(statJsonResponse);
    }
  }

  private int getBrowserID(String browser) throws SQLException {
    int res;
    String name = browser.split("_")[0].trim();
    String version = browser.split("_")[1].trim();
    String platform = browser.split("_")[2].trim();
    Browser tmp = new Browser(name, version, platform);
    res = new BrowserDao(Utility.getDBConnection(this.getServletContext())).getBrowserId(tmp);
    return res;
  }

  private String getStatString(String tableName, int browser1, int browser2) {
    String res = null;

    return res;
  }
}

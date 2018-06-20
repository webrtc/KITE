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
import org.webrtc.kite.dao.StatsDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Stats.Stats;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Servlet implementation class TestServlet */
@WebServlet("/getstat")
public class GetStatServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /** @see HttpServlet#HttpServlet() */
  public GetStatServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws  IOException {
    String overtime = request.getParameter("overtime");
    if (overtime.equalsIgnoreCase("no")) {
      String tableName = request.getParameter("name");
      JsonObject statJson ;
      if (tableName == null) {
        throw new KiteNoKeyException("table name");
      }
      String idStr = request.getParameter("id");
      if (idStr == null) {
        throw new KiteNoKeyException("id");
      }
      List<Stats> statsList = new ArrayList<>();
      String statJsonResponse = "{";
      try {
        statJson =
            new StatsDao(Utility.getDBConnection(this.getServletContext()))
                .getStatById(Integer.parseInt(idStr));
        if (statJson == null) {

        } else {
          Set<String> browserList = statJson.keySet();
          for (String browser : browserList) {
            Stats browserStat = new Stats(browser, statJson.getJsonObject(browser));
            statsList.add(browserStat);
          }

          for (Stats stat : statsList) {
            if (stat.isCaller()) statJsonResponse += "\"caller\":" + stat.getJsonData() + ",";
            else statJsonResponse += "\"callee\":" + stat.getJsonData();
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
      List<Stats> statsList = new ArrayList<>();
      String statJsonResponse = "{";
      String dateLabels = "\"run_dates\": [";
      String callerData = "\"caller\": [";
      String calleeData = "\"callee\": [";
      try {
        String browsers = Json.createArrayBuilder()
            .add(getBrowserID(caller))
            .add(getBrowserID(callee)).build().toString();

        List<String> statStringList =
            new StatsDao(Utility.getDBConnection(this.getServletContext()))
                .getStatByBrowsers(browsers);

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
    res = new BrowserDao(Utility.getDBConnection(this.getServletContext())).getId(tmp);
    return res;
  }

  private String getStatString(String tableName, int browser1, int browser2) {
    String res = null;

    return res;
  }
}

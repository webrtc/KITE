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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.BrowserMapping;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.ResultDao;
import org.webrtc.kite.dao.TestDao;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Result;
import org.webrtc.kite.pojo.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Servlet implementation class DashboardServlet */
@WebServlet("/public")
public class PublicOverviewServlet extends HttpServlet {

  private static final long serialVersionUID = 3456562049892798394L;
  private static final Log log = LogFactory.getLog(PublicOverviewServlet.class);

  /** @see HttpServlet#HttpServlet() */
  public PublicOverviewServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String testName = request.getParameter("testname");
    Test test;
    if (testName == null || testName == "") {
      testName = "IceConnectionTest";
    }
    List<Test> listOfDistinctTest;

    try {
      listOfDistinctTest =
          new TestDao(Utility.getDBConnection(this.getServletContext())).getDistinctTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      test = new TestDao(Utility.getDBConnection(this.getServletContext())).getTestListByTestName(testName).get(0);
      request.setAttribute("test", test);
      List<Browser> browserList = new ArrayList<>(BrowserMapping.BrowserList);
      browserList = Utility.sortByOs(browserList);
      List<Browser> stableBrowserList = new ArrayList<>();
      JsonArrayBuilder browserListJson = Json.createArrayBuilder();
      for (Browser browser : browserList) {
        if (BrowserMapping.StableList.contains(browser.getVersion())) {
          int browserId =
                  new BrowserDao(Utility.getDBConnection(this.getServletContext()))
                          .getId(browser);
          browser.setId(browserId);
          stableBrowserList.add(browser);
          browserListJson.add(browser.getJsonObjectBuilder());
        }
      }
      request.setAttribute("jsonBrowserList", browserListJson.build().toString());
      if (test.getTupleSize() == 2) {
        JsonArrayBuilder resultJson = Json.createArrayBuilder();
        for (List<Browser> browsers :
                Utility.build2DTuples(stableBrowserList)) {
          JsonObjectBuilder singleResult = Json.createObjectBuilder();
          JsonArrayBuilder browserJson = Json.createArrayBuilder();
          JsonArrayBuilder browserIdJson = Json.createArrayBuilder();
          for (Browser browser : browsers) {
            browserJson.add(browser.getJsonObjectBuilder());
            browserIdJson.add(browser.getId());
          }
          String result = new ResultDao(Utility.getDBConnection(this.getServletContext()))
                  .getLatestResultByBrowser(testName, browserIdJson.build().toString());
          if (result == null) {
            result = "N/A";
          }

          singleResult.add("browsers", browserJson);
          singleResult.add("result", result);
          resultJson.add(singleResult);
        }
        request.setAttribute("resultJson", resultJson.build().toString());
      }
      else {
        List<Result> resultList = new ArrayList<>();
        for (List<Browser> browsers :
                Utility.buildTuples(stableBrowserList, test.getTupleSize())) {
          JsonArrayBuilder browserJson = Json.createArrayBuilder();
          JsonArrayBuilder browserIdJson = Json.createArrayBuilder();
          for (Browser browser : browsers) {
            browserJson.add(browser.getJsonObjectBuilder());
            browserIdJson.add(browser.getId());
          }
          String resultStr = new ResultDao(Utility.getDBConnection(this.getServletContext()))
                  .getLatestResultByBrowser(testName, browserIdJson.build().toString());
          if (resultStr == null) {
            resultStr = "N/A";
          }
          Result result = new Result(resultStr);
          result.setBrowserList(browsers);
          resultList.add(result);
        }
        request.setAttribute("resultJson", Utility.getSunburstJsonData(resultList));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }
    // get UI
    if (log.isDebugEnabled()) log.debug("Displaying: public_overview.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("public_overview.vm");
    requestDispatcher.forward(request, response);
  }
}

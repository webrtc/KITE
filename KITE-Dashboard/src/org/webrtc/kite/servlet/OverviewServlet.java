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
import org.webrtc.kite.Mapping;
import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.*;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.ConfigTest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/overview")
public class OverviewServlet extends HttpServlet {

  private static final long serialVersionUID = 3456562049892798394L;
  private static final Log log = LogFactory.getLog(OverviewServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public OverviewServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Boolean duo = Boolean.valueOf(request.getParameter("1v1"));
    String testName = request.getParameter("testname");
    if (testName == null) {
      throw new KiteNoKeyException("test name");
    }
    String startTime = request.getParameter("time");
    String description = "";
    List<ConfigTest> listOfDistinctTest;

    if (duo) {
      OverviewResult listOfResult;
      List<Long> listOfRuns;

      try {
        listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
        request.setAttribute("listOfTest", listOfDistinctTest);
        listOfRuns = new PublicOverviewDao(Utility.getDBConnection(this.getServletContext())).getRunList(testName);
        description = new PublicOverviewDao(Utility.getDBConnection(this.getServletContext())).getDescription(testName);
        request.setAttribute("listOfRuns", listOfRuns);
        request.setAttribute("testname", testName);
        request.setAttribute("description", description);
        request.setAttribute("stableList", Mapping.StableList);
        if (startTime == null && !listOfRuns.isEmpty())
          startTime = listOfRuns.get(0).toString();
        if (startTime != null) {
          request.setAttribute("startTime", Long.parseLong(startTime));
          String tmp = "TN" + testName + "_" + startTime;
          listOfResult = new OverviewResult(
              new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getResultList(tmp, 2));
          listOfResult.stableFilter();
          request.setAttribute("jsonData", listOfResult.getSunburstJsonData());
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new KiteSQLException(e.getLocalizedMessage());
      }
      // get UI
      if (log.isDebugEnabled())
        log.debug("Displaying: overview_2D.vm");
      RequestDispatcher requestDispatcher = request.getRequestDispatcher("overview_2D.vm");
      requestDispatcher.forward(request, response);

    } else {


      OverviewResult listOfResult;
      List<ConfigTest> testList;

      try {
        listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
        request.setAttribute("listOfTest", listOfDistinctTest);
        if (testName != null) {
          testList = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getConfigTestList(testName);
          int tupleSize = testList.get(0).getTupleSize();
          listOfResult = new OverviewResult(new OverviewDao(Utility.getDBConnection(this.getServletContext())).getOverviewResultList(testName, tupleSize),
              new BrowserDao(Utility.getDBConnection(this.getServletContext())).getOverviewBrowserList());
          request.setAttribute("tupleSize", tupleSize);
          request.setAttribute("lastRun", testList.get(testList.size() - 1).getStartTime());
          request.setAttribute("description", testList.get(0).getDescription());
          request.setAttribute("testname", testName);
          request.setAttribute("listOfVersion", Mapping.VersionList);
          request.setAttribute("listOfTests", testList);
          request.setAttribute("listOfResult", listOfResult);
          request.setAttribute("stats", listOfResult.getStat());
          request.setAttribute("jsonData", listOfResult.getSunburstJsonData());
          request.setAttribute("browserList", listOfResult.getDistincBrowserList());
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new KiteSQLException(e.getLocalizedMessage());
      }
      // get UI
      if (log.isDebugEnabled())
        log.debug("Displaying: overview_circle.vm");
      RequestDispatcher requestDispatcher = request.getRequestDispatcher("overview_circle.vm");
      requestDispatcher.forward(request, response);

    }

  }

}

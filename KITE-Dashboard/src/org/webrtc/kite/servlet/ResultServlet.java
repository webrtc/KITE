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
import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.dao.ResultTableDao;
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
 * Servlet implementation class ResultServlet
 */
@WebServlet("/results")
public class ResultServlet extends HttpServlet {

  private static final long serialVersionUID = -5351796663598211601L;
  private static final Log log = LogFactory.getLog(ResultServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ResultServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String testID = request.getParameter("test");
    if (testID == null)
      throw new KiteNoKeyException("testID");
    if (log.isDebugEnabled())
      log.debug("in->test ID: " + testID);


    OverviewResult listOfResult;
    List<ConfigTest> listOfDistinctTest;
    ConfigTest test;
    int testIDInt = Integer.parseInt(testID);
    try {
      listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      test = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestById(testIDInt);
      request.setAttribute("test", test);
      listOfResult = new OverviewResult(
          new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getResultList(test.getResultTable(), test.getTupleSize()));
      request.setAttribute("listOfResult", listOfResult);
      request.setAttribute("total", listOfResult.getListOfResultTable().size());
      request.setAttribute("jsonData", listOfResult.getSunburstJsonData());
      request.setAttribute("testJsonData", test.getJsonData());
      request.setAttribute("browserList", listOfResult.getDistincBrowserList());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: results.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("results.vm");
    requestDispatcher.forward(request, response);
  }

}

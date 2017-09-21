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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import java.io.IOException;
import java.sql.SQLException;

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
    // TODO Auto-generated method stub
    // response.getWriter().append("Served at:
    // ").append(request.getContextPath());

    String tableName = request.getParameter("name");
    if (tableName == null)
      throw new KiteNoKeyException("name");
    if (log.isDebugEnabled())
      log.debug("in->name: " + tableName);

    String testName = request.getParameter("testname");
    if (testName == null)
      throw new KiteNoKeyException("testname");
    if (log.isDebugEnabled())
      log.debug("in->testname: " + testName);

    OverviewResult listOfResult;
    try {
      listOfResult =
          new OverviewResult(new ResultTableDao(Utility.getDBConnection(this.getServletContext()))
              .getResultTestList(tableName), false);
      if (log.isDebugEnabled())
        log.debug("out->listOfResult: " + listOfResult);
      request.setAttribute("listOfResult", listOfResult);
      request.setAttribute("listOfFirstBrowser", listOfResult.getBrowserListAtCertainPosition(1));
      request.setAttribute("listOfSecondBrowser", listOfResult.getBrowserListAtCertainPosition(2));

      request.setAttribute("listOfOS", listOfResult.getOSList());
      request.setAttribute("numberOfOS", listOfResult.getOSList().size() - 1);
      // request.setAttribute("resultColorMap", Mapping.resultColorMap);
      if (log.isDebugEnabled())
        log.debug("out->testName: " + testName);
      request.setAttribute("testName", testName);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
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

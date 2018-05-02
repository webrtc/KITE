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
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.ConfigExecutionDao;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.ConfigTest;
import org.webrtc.kite.pojo.ConfigurationOverall;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

  private static final long serialVersionUID = -5016256996275105856L;
  private static final Log log = LogFactory.getLog(DashboardServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public DashboardServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<ConfigurationOverall> listOfConfig;
    List<Browser> listOfBrowser;
    List<ConfigTest> listOfDistinctTest;
    try {
      listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      listOfConfig = new ConfigExecutionDao(Utility.getDBConnection(this.getServletContext()))
          .getDistinctConfigExecutionList();
      listOfBrowser = new BrowserDao(Utility.getDBConnection(this.getServletContext()))
          .getBrowserList();
      listOfBrowser = new ArrayList<Browser>(new LinkedHashSet<Browser>(listOfBrowser));
      if (log.isDebugEnabled())
        log.debug("out->listOfConfigName: " + listOfConfig);
      request.setAttribute("listOfConfig", listOfConfig);
      request.setAttribute("listOfBrowser", listOfBrowser);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: dashboard.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("dashboard.vm");
    requestDispatcher.forward(request, response);
  }

}

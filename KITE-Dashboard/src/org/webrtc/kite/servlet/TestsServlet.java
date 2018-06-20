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
import org.webrtc.kite.dao.ConfigTestDao;
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
 * Servlet implementation class TestServlet
 */
@WebServlet("/tests")
public class TestsServlet extends HttpServlet {

  private static final long serialVersionUID = 2934364558720526033L;
  private static final Log log = LogFactory.getLog(TestsServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public TestsServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String strConfigId = request.getParameter("id");
    if (strConfigId == null)
      throw new KiteNoKeyException("id");
    if (log.isDebugEnabled())
      log.debug("in->id: " + strConfigId);
    int configId = Integer.parseInt(strConfigId);

    String configName = request.getParameter("configname");
    if (configName == null)
      throw new KiteNoKeyException("configname");
    if (log.isDebugEnabled())
      log.debug("in->configname: " + strConfigId);

    List<ConfigTest> listOfTest;
    List<ConfigTest> listOfDistinctTest;
    try {
      listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
      request.setAttribute("listOfTestOverview", listOfDistinctTest);
      listOfTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext()))
          .getConfigTestList(configId);
      if (log.isDebugEnabled())
        log.debug("out->listOfTest: " + listOfTest);
      request.setAttribute("listOfTest", listOfTest);
      if (log.isDebugEnabled())
        log.debug("out->configName: " + configName);
      request.setAttribute("configName", configName);
      request.setAttribute("configID", strConfigId);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: tests.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("tests.vm");
    requestDispatcher.forward(request, response);
  }

}

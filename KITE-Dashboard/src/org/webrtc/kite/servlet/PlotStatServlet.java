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
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/stat")
public class PlotStatServlet extends HttpServlet {
  private static final long serialVersionUID = 348927983L;
  private static final Log log = LogFactory.getLog(PublicOverviewServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public PlotStatServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   * response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String testName = request.getParameter("name");
    if (testName == null)
      throw new KiteNoKeyException("table name");
    String idStr = request.getParameter("id");
    if (idStr == null)
      throw new KiteNoKeyException("id");

    String requestString = "getstat?name=" + testName + "&id=" + idStr + "&overtime=no";
    request.setAttribute("statRequest", requestString);
    List<String> idStrList = Arrays.asList(idStr.split("_"));
    List<Browser> browserList = new ArrayList<>();
    try {
      for (String id : idStrList) {
        Browser browser = new BrowserDao(Utility.getDBConnection(this.getServletContext()))
            .getBrowserById(Integer.parseInt(id));
        browserList.add(browser);
      }
      request.setAttribute("browserList", browserList);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: stats.vm");

    RequestDispatcher requestDispatcher = request.getRequestDispatcher("stats.vm");
    requestDispatcher.forward(request, response);
  }
}

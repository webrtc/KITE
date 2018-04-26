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

package org.webrtc.kite.wpt.dashboard.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.wpt.dashboard.Utility;
import org.webrtc.kite.wpt.dashboard.dao.WPTDao;
import org.webrtc.kite.wpt.dashboard.exception.KiteSQLException;
import org.webrtc.kite.wpt.dashboard.pojo.WPTScore;

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
@WebServlet("/score")
public class ScoreBoardServlet extends HttpServlet {

  private static final long serialVersionUID = 3456562049892798394L;
  private static final Log log = LogFactory.getLog(ScoreBoardServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ScoreBoardServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String testSuite = request.getParameter("testsuite");
    String test = request.getParameter("test");

    List<WPTScore> WPTScoreList;
    String scoreJson = "{";
    try {
      WPTScoreList = new WPTDao(Utility.getDBConnection(this.getServletContext())).getScoreList();
      for (int i = 0; i < WPTScoreList.size(); i++) {
        WPTScore score = WPTScoreList.get(i);
        String tableName = score.getTableName();
        score.setTestList(new WPTDao(Utility.getDBConnection(this.getServletContext())).getTestListFromTable(tableName, false));
        if (testSuite == null)
          scoreJson += score.getJson(1);
        else
          scoreJson += score.getJson(2);
        if (i < WPTScoreList.size() - 1)
          scoreJson += ",";
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    scoreJson += "}";
    request.setAttribute("scoreJson", scoreJson);

    if (testSuite == null)
      request.setAttribute("layer", 1);
    else {
      if (test == null) {
        request.setAttribute("layer", 2);
        request.setAttribute("testSuite", testSuite);
      } else {
        request.setAttribute("layer", 3);
        request.setAttribute("testSuite", testSuite);
        request.setAttribute("test", test);
      }
    }


    if (log.isDebugEnabled())
      log.debug("Displaying: score.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("score.vm");
    requestDispatcher.forward(request, response);
  }

}

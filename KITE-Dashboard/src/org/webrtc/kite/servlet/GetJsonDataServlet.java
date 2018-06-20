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
import org.webrtc.kite.dao.ResultDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Servlet implementation class TestServlet */
@WebServlet("/getjson")
public class GetJsonDataServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /** @see HttpServlet#HttpServlet() */
  public GetJsonDataServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String result;

    String resultTableName = request.getParameter("resultTableName");
    String testId = request.getParameter("testId");
    if (resultTableName == null) {
      throw new KiteNoKeyException("resultTableName");
    }
    String filter = request.getParameter("type");
    try {
      result =
          new ResultDao(Utility.getDBConnection(this.getServletContext()))
              .getRequestedResultList(resultTableName, filter, Integer.parseInt(testId));
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }
    response.getWriter().print(result);
  }
}

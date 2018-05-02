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
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;

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
@WebServlet("/getresult")
public class GetResultServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetResultServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   * response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String tableName = request.getParameter("name");
    String resultString = "";
    if (tableName == null)
      throw new KiteNoKeyException("table name");
    String idStr = request.getParameter("id");
    if (idStr == null)
      throw new KiteNoKeyException("id");
    List<String> idStrList = Arrays.asList(idStr.split("_"));
    List<Integer> idList = new ArrayList<Integer>();
    for (String id : idStrList)
      idList.add(Integer.parseInt(id));
    try {
      resultString = new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getResultById(tableName, idList);

    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }
    response.getWriter().print(resultString);
  }
}

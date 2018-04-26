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

import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.OverviewDao;
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/getjson")
public class GetJsonDataServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetJsonDataServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   * response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    String location = request.getParameter("location");
    if (location == null)
      throw new KiteNoKeyException("location");
    OverviewResult listOfResult = null;

    String testName = request.getParameter("testName");
    if (testName == null)
      throw new KiteNoKeyException("testName");
    String strSize = request.getParameter("size");
    if (strSize == null)
      throw new KiteNoKeyException("tuple size");
    int size = Integer.parseInt(strSize);
    String strval = request.getParameter("val");
    if (strval == null)
      throw new KiteNoKeyException("value");
    try {
      if (location.equalsIgnoreCase("result"))
        listOfResult = new OverviewResult(new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getRequestedResultList(testName, size, strval));
      else
        listOfResult = new OverviewResult(new OverviewDao(Utility.getDBConnection(this.getServletContext())).getRequestedOverviewResultList(testName, size, strval),
            new BrowserDao(Utility.getDBConnection(this.getServletContext())).getOverviewBrowserList());
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    String name = request.getParameter("name");
    if (name != null) {
      String version = request.getParameter("version");
      if (version == null)
        throw new KiteNoKeyException("tuple size");
      String platform = request.getParameter("platform");
      if (platform == null)
        throw new KiteNoKeyException("platform");
      Browser browser = new Browser(name, version, platform);
      listOfResult.browserFilter(browser);
    }

    if (listOfResult.getListOfResultTable().size() > 0)
      response.getWriter().print(listOfResult.getSunburstJsonData());
    else {
      String res = "{";
      res += "\"total\":0,";
      res += "\"overall\":[],";
      res += "\"result\":[],";
      res += "\"sunburst\":{";
      res += "\"name\":\"result\",";
      res += "\"children\": []}";
      res += "}";
      response.getWriter().print(res);
    }

  }
}

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
import org.webrtc.kite.pojo.ConfigTest;

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
@WebServlet("/getprogress")
public class GetConfigProgressServlet extends HttpServlet {

  private static final long serialVersionUID = 8510755982033949344L;
  private static final Log log = LogFactory.getLog(GetConfigProgressServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetConfigProgressServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {


    String strTestID = request.getParameter("id");
    if (strTestID == null)
      throw new KiteNoKeyException("id");
    if (log.isDebugEnabled())
      log.debug("in->id: " + strTestID);
    int testID = Integer.parseInt(strTestID);
    String result = request.getParameter("result");
    if (result == null)
      throw new KiteNoKeyException("result");
    boolean resultBool = Boolean.parseBoolean(result);
    ConfigTest test;
    OverviewResult listOfResult;
    try {
      test = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestById(testID);
      if (log.isDebugEnabled())
        log.debug("testJson : " + test.getJsonData());
      else {
        if (resultBool) {
          listOfResult = new OverviewResult(
              new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getResultList(test.getResultTable(), test.getTupleSize()));
          if (log.isDebugEnabled())
            log.debug("listOfResultJson : " + listOfResult.getSunburstJsonData());
          if (test.getStatus())
            response.getWriter().print("done");
          else
            response.getWriter().print(listOfResult.getSunburstJsonData());
        } else {
          response.getWriter().print(test.getJsonData());
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}

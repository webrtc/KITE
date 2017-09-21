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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
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
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub


    String strTestID = request.getParameter("id");
    if (strTestID == null)
      throw new KiteNoKeyException("id");
    if (log.isDebugEnabled())
      log.debug("in->id: " + strTestID);
    int testID = Integer.parseInt(strTestID);
    String strType = request.getParameter("type");
    if (strType == null)
      throw new KiteNoKeyException("type");
    int type = Integer.parseInt(strType);
    switch (type) {
      case 1:
        try {
          int percentage = new ConfigTestDao(Utility.getDBConnection(this.getServletContext()))
              .getPercentage(testID);
          response.getWriter().print(percentage);
        } catch (SQLException e) {
          e.printStackTrace();
          throw new KiteSQLException(e.getLocalizedMessage());
        }
        break;
      case 2:
        try {
          long ETA =
              new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getETA(testID);
          ETA = ETA / 1000;
          int hour = (int) ETA / 3600;
          int minute = (int) (ETA - hour * 3600) / 60;
          int second = (int) ETA - hour * 3600 - minute * 60;
          String res = hour + "h" + minute + "m" + second + "s";
          response.getWriter().print(res);
        } catch (SQLException e) {
          e.printStackTrace();
          throw new KiteSQLException(e.getLocalizedMessage());
        }
        break;
      case 3:
        try {
          int finishedTestCase =
              new ConfigTestDao(Utility.getDBConnection(this.getServletContext()))
                  .getNumberOfFinishedTestCase(testID);
          response.getWriter().print(finishedTestCase);
        } catch (SQLException e) {
          e.printStackTrace();
          throw new KiteSQLException(e.getLocalizedMessage());
        }
        break;
    }
  }
}

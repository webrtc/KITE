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
import org.webrtc.kite.dao.StatsDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

/** Servlet implementation class TestServlet */
@WebServlet("/getlog")
public class getLogServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /** @see HttpServlet#HttpServlet() */
  public getLogServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String tableName = request.getParameter("name");
    if (tableName == null) {
      throw new KiteNoKeyException("table name");
    }
    String idStr = request.getParameter("id");
    if (tableName == null) {
      throw new KiteNoKeyException("id");
    }
    JsonObject logObject = null;
    try {
      int statId =
          new ResultDao(Utility.getDBConnection(this.getServletContext()))
              .getStatIdById(tableName, Integer.parseInt(idStr));
      System.out.println("stat id is: "+statId);
      if (statId != 0) {
        logObject = new StatsDao(Utility.getDBConnection(this.getServletContext()))
                .getLogById(statId);
        if (logObject == null) {
          JsonObjectBuilder logObjectBuilder = Json.createObjectBuilder();
          List<Browser> browsers = new ResultDao(Utility.getDBConnection(this.getServletContext()))
                  .getBrowsersById(tableName, Integer.parseInt(idStr));
          for (Browser browser: browsers){
            logObjectBuilder.add(browser.getDetailedName(), "No log was provided");
          }
          logObject = logObjectBuilder.build();
        }
      } else {
        JsonObjectBuilder logObjectBuilder = Json.createObjectBuilder();
        List<Browser> browsers = new ResultDao(Utility.getDBConnection(this.getServletContext()))
                .getBrowsersById(tableName, Integer.parseInt(idStr));
        for (Browser browser: browsers){
          logObjectBuilder.add(browser.getDetailedName(), "No log was provided");
        }
        logObject = logObjectBuilder.build();
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    if (logObject == null) {
      logObject = Json.createObjectBuilder().build();
    }
    response.getWriter().print(logObject.toString());

  }
}

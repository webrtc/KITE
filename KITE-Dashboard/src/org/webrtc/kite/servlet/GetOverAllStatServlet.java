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
import org.webrtc.kite.dao.ExecutionDao;
import org.webrtc.kite.dao.ResultDao;
import org.webrtc.kite.dao.TestDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.pojo.Result;
import org.webrtc.kite.pojo.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Servlet implementation class TestServlet */
@WebServlet("/getoverall")
public class GetOverAllStatServlet extends HttpServlet {

  private static final long serialVersionUID = 8510755982033949344L;
  private static final Log log = LogFactory.getLog(GetOverAllStatServlet.class);

  /** @see HttpServlet#HttpServlet() */
  public GetOverAllStatServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String testName = request.getParameter("testName");
    if (testName == null) {
      throw new KiteNoKeyException("testName");
    }
    if (log.isDebugEnabled()){
      log.debug("in->testName: " + testName);
    }

    String testId = request.getParameter("testId");

    if (testId != null) {
      int success = 0;
      int failed = 0;
      int error = 0;
      int pending = 0;
      try {
        List<Result> results =
            new ResultDao(Utility.getDBConnection((this.getServletContext())))
                .getresultByTestId(testName, Integer.parseInt(testId));
        for (Result result : results) {
          switch (result.getResult()) {
            case "SUCCESSFUL":
              success++;
              break;
            case "TIME OUT":
              failed++;
              break;
            case "FAILED":
              failed++;
              break;
            case "SCHEDULED":
              pending++;
              break;
            default:
              error++;
              break;
          }
        }
        response.getWriter().print(Json.createArrayBuilder()
            .add(success)
            .add(failed)
            .add(error)
            .add(pending)
            .build().toString());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      int success = 0;
      int failed = 0;
      int error = 0;
      try {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        List<Test> tests =
            new TestDao(Utility.getDBConnection(this.getServletContext()))
                .getTestListByTestName(testName);
        if (tests.size() > 20) {
          tests = tests.subList(0, 20);
        }
        for (Test test : tests) {
          success = 0;
          failed = 0;
          error = 0;
          JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
          List<Result> results =
              new ResultDao(Utility.getDBConnection((this.getServletContext())))
                  .getresultByTestId(test.getTestName(), test.getTestId());
          for (Result result : results) {
            switch (result.getResult()) {
              case "SUCCESSFUL":
                success++;
                break;
              case "TIME OUT":
                failed++;
                break;
              case "FAILED":
                failed++;
                break;
              default:
                error++;
                break;
            }
          }
          Date startTime = new Date(test.getStartTime());
          DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
          jsonObjectBuilder
              .add("startTime", sdf.format(startTime))
              .add("stats", Json.createArrayBuilder().add(success).add(failed).add(error));
          jsonArrayBuilder.add(jsonObjectBuilder);
        }
        response.getWriter().print(jsonArrayBuilder.build().toString());

      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}

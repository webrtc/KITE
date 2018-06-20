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
import org.webrtc.kite.BrowserMapping;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.ExecutionDao;
import org.webrtc.kite.dao.ResultDao;
import org.webrtc.kite.dao.TestDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Execution;
import org.webrtc.kite.pojo.Result;
import org.webrtc.kite.pojo.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Servlet implementation class ResultServlet */
@WebServlet("/results")
public class ResultServlet extends HttpServlet {

  private static final long serialVersionUID = -5351796663598211601L;
  private static final Log log = LogFactory.getLog(ResultServlet.class);

  /** @see HttpServlet#HttpServlet() */
  public ResultServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String testID = request.getParameter("test");
    String browserId = request.getParameter("browser");
    String configName = request.getParameter("configName");
    if (testID == null) {
      throw new KiteNoKeyException("testID");
    }
    if (log.isDebugEnabled()) {
      log.debug("in->test ID: " + testID);
    }

    List<Test> listOfDistinctTest;
    List<Result> resultList;
    Test test;
    int testIDInt = Integer.parseInt(testID);
    try {
      listOfDistinctTest =
          new TestDao(Utility.getDBConnection(this.getServletContext())).getDistinctTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      test = new TestDao(Utility.getDBConnection(this.getServletContext())).getTestById(testIDInt);
      request.setAttribute("test", test);
      request.setAttribute("testJsonData", test.getJsonData());
      JsonArrayBuilder resultJson = Json.createArrayBuilder();
      List<Browser> browserList = new ArrayList<>();
      if (browserId==null) {
        resultList = new ResultDao(Utility.getDBConnection(this.getServletContext()))
                .getresultByTestId(test.getTestName(), test.getTestId());
      } else {
        resultList = new ArrayList<>();

        Browser browser = new BrowserDao(Utility.getDBConnection(this.getServletContext()))
                .getBrowserById(Integer.parseInt(browserId));
        for (List<Browser> browsers :
                Utility.buildTuples(BrowserMapping.BrowserList, test.getTupleSize())) {
        }
      }
      for (Result result : resultList){
        JsonObjectBuilder resultObj = Json.createObjectBuilder();
        JsonArrayBuilder browsers = Json.createArrayBuilder();
        for (Browser browser: result.getBrowserList()){
          browserList.add(browser);
          browsers.add(browser.getJsonObjectBuilder());
        }
        resultObj.add("browsers", browsers)
                .add("id", result.getId())
                .add("result", result.getResult())
                .add("duration", result.getDuration())
                .add("startTime", result.getStartTime())
                .add("stats", result.getStats());
        resultJson.add(resultObj);
      }
      request.setAttribute("jsonResultList", resultJson.build().toString());
      JsonArrayBuilder browserListJson = Json.createArrayBuilder();
      Set<Browser> distinctBrowserList = new HashSet<>(browserList);
      for (Browser browser: distinctBrowserList){
        browserListJson.add(browser.getJsonObjectBuilder());
      }

      request.setAttribute("jsonBrowserList", browserListJson.build().toString());
      if (configName == null) {
        Execution config = new ExecutionDao(Utility.getDBConnection(this.getServletContext()))
                .getExecutionById(test.getConfigId());
        if (config!=null){
          configName = config.getConfigName();
        }
      }
      request.setAttribute("configName", configName);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled()) log.debug("Displaying: results.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("results.vm");
    requestDispatcher.forward(request, response);
  }
}

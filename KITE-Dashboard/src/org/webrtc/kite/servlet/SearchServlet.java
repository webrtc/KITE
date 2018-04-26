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
import org.webrtc.kite.dao.ConfigExecutionDao;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.ConfigExecution;
import org.webrtc.kite.pojo.ConfigTest;
import org.webrtc.kite.pojo.SearchResult;

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
 * Created by nam on 10/4/17.
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

  private static final long serialVersionUID = -7891194352023162488L;
  private static final Log log = LogFactory.getLog(SearchServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public SearchServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<Object> listOfResult = null;

    String searchKey = request.getParameter("key");
    List<ConfigExecution> configExecutionList = null;
    List<ConfigTest> configTestList = null;
    List<ConfigTest> listOfDistinctTest;
    SearchResult result = new SearchResult();
    result.setSearchKey(searchKey);
    if (searchKey == null) {
      throw new KiteNoKeyException("searchKey");
    } else {
      try {
        listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
        request.setAttribute("listOfTest", listOfDistinctTest);
        configExecutionList =
            new ConfigExecutionDao(Utility.getDBConnection(this.getServletContext()))
                .searchConfigExecutionList(searchKey);
        if (log.isDebugEnabled())
          log.debug("out->listOfExecution: " + configExecutionList);
        result.setListOfConfigResult(configExecutionList);
      } catch (SQLException e) {
        e.printStackTrace();
        throw new KiteSQLException(e.getLocalizedMessage());
      }

      try {
        configTestList = new ConfigTestDao(Utility.getDBConnection(this.getServletContext()))
            .getConfigTestList(searchKey);
        if (log.isDebugEnabled())
          log.debug("out->listOfTest: " + configTestList);
        request.setAttribute("listOfTest", configTestList);
        result.setListOfConfigTestResult(configTestList);
      } catch (SQLException e) {
        e.printStackTrace();
        throw new KiteSQLException(e.getLocalizedMessage());
      }
      request.setAttribute("result", result);
    }

    request.setAttribute("listOfResult", listOfResult);

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: search.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("search.vm");
    requestDispatcher.forward(request, response);
  }

}

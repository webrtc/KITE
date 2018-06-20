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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/execution")
public class ExecutionServlet extends HttpServlet {

  private static final long serialVersionUID = -1650907688090780137L;
  private static final Log log = LogFactory.getLog(ExecutionServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ExecutionServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String configName = request.getParameter("name");
    if (configName == null)
      throw new KiteNoKeyException("name");
    if (log.isDebugEnabled())
      log.debug("in->name: " + configName);

    List<ConfigExecution> listOfExecution;
    List<ConfigTest> listOfDistinctTest;
    try {
      listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      listOfExecution = new ConfigExecutionDao(Utility.getDBConnection(this.getServletContext()))
          .getConfigExecutionList(configName);
      if (log.isDebugEnabled())
        log.debug("out->listOfExecution: " + listOfExecution);
      request.setAttribute("listOfExecution", listOfExecution);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: execution.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("execution.vm");
    requestDispatcher.forward(request, response);
  }

}

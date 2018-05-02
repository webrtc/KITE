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

package org.webrtc.kite.wpt.dashboard.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.wpt.dashboard.exception.KiteNoKeyException;
import org.webrtc.kite.wpt.dashboard.exception.KiteSQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/apperror")
public class AppExceptionHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = 5643806873850566969L;
  private static final Log log = LogFactory.getLog(AppExceptionHandlerServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public AppExceptionHandlerServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String targetVM = "general-error.html";
    Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
    throwable.printStackTrace();
    if (throwable instanceof KiteNoKeyException) {
      request.setAttribute("key", ((KiteNoKeyException) throwable).getKey());
      targetVM = "nokeyerror.vm";
    } else if (throwable instanceof KiteSQLException) {
      request.setAttribute("message", ((KiteSQLException) throwable).getLocalizedMessage());
      targetVM = "sqlerror.vm";
    }

    // get UI
    if (log.isDebugEnabled())
      log.debug("Displaying: " + targetVM);
    RequestDispatcher requestDispatcher = request.getRequestDispatcher(targetVM);
    requestDispatcher.forward(request, response);
  }

}

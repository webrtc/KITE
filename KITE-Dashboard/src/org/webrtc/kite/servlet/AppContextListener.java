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

import org.webrtc.kite.DataCenterQueueManager;
import org.webrtc.kite.dao.DBConnectionManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Application Lifecycle Listener implementation class AppContextListener
 */
@WebListener
public class AppContextListener implements ServletContextListener {

  /**
   * Default constructor.
   */
  public AppContextListener() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @see ServletContextListener#contextInitialized(ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext ctx = servletContextEvent.getServletContext();

    // initialize DB Connection
    String dbURL = ctx.getRealPath(ctx.getInitParameter("pathToDB"));
    String dbCompURL = ctx.getRealPath(("COMPABILITIES.db"));

    try {
      DBConnectionManager connectionManager = new DBConnectionManager(dbURL);
      DBConnectionManager connectionManagerComp = new DBConnectionManager(dbCompURL);
      ctx.setAttribute("DBConnection", connectionManager.getConnection());
      ctx.setAttribute("CompDBConnection", connectionManagerComp.getConnection());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return;
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    DataCenterQueueManager.getInstance().initResultHandler(dbURL);
    DataCenterQueueManager.getInstance().startManager();
  }

  /**
   * @see ServletContextListener#contextDestroyed(ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    //FIXME: release tomcat JDBC driver resources
    DataCenterQueueManager.getInstance().stopManager();

    Connection mainCon =
        (Connection) servletContextEvent.getServletContext().getAttribute("DBConnection");
    try {
      mainCon.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Connection subCon =
        (Connection) servletContextEvent.getServletContext().getAttribute("CompDBConnection");
    try {
      subCon.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
